package com.sktelecom.tos.stream.logger

import java.io.{File}

import akka.actor._
import akka.contrib.throttle.Throttler._
import akka.contrib.throttle._
import akka.util._
import akka.pattern.ask
import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.concurrent._

import ExecutionContext.Implicits.global
import scala.io.Source

/**
  * R2K 로깅을 시뮬레이션하는 프로그램입니다.
  * 기본적인 목적은 R2K 데이터 수집 시스템의 성능을 측정하는 것입니다.
  */
object LogGenerator extends Logging {

  def generate(samples: List[Sample], numberOfCalls: Int, duration: FiniteDuration, rounds: Int): Unit = {
    implicit val timeout = Timeout(1 minute)

    val system = ActorSystem("system")
    val throttler = system.actorOf(Props(new TimerBasedThrottler(new Rate(numberOfCalls, duration))))
    val worker = system.actorOf(Props(classOf[ExternalApiActor]))
    throttler ! SetTarget(Option(worker))

    for (i <- 0 until rounds) {
      val listOfFutures: Seq[Future[Response]] = samples.map { sample =>
        ask(throttler, Request(sample.line, sample.target)).mapTo[Response]
      }
      val futureList: Future[Seq[Response]] = Future.sequence(listOfFutures)
      val results: Seq[Response] = Await.result(futureList, 1 minute)
      logTrace(s"round: $i")
//      println(results)
    }

    val countFuture = ask(throttler, CountRequest()).mapTo[CountResponse]
    val countResult = Await.result(countFuture, 10 seconds)

    println(s"Total message count: ${countResult.count}")

    val f = system.terminate()
    Await.result(f, 1 minute)
  }

  def createDir(logPath: String): String = {
    val f = new File(logPath)

    if (!f.exists()) {
      println(s"making dir: $logPath")
      f.mkdirs()
    }

    f.getAbsolutePath
  }

  def process(samplePath: String, numOfCalls: Int, duration: Int, logPath: String) {
    println("starting 시작")

    val logDir = createDir(logPath)

    val sampleSource = Source.fromFile(samplePath)
    val samples = sampleSource.getLines().toList.map(line => Sample(logDir, line))

    logTrace(s"prepared samples: ${samples.size}")

    println(s"running at a rate of $numOfCalls calls per second")

    val seconds = samples.size / numOfCalls

    val rounds = duration / seconds

    println(s"rounds: $rounds")

    generate(samples, numOfCalls, Duration(1, TimeUnit.SECONDS), rounds)
  }

  class HelpException extends Exception

  def main(args: Array[String]): Unit = {
    // 속도: 10 건/초
    var numOfCalls = 10
    // 지속시간: 10 초
    var duration = 10

    var logPath = "."

    var samplePath = "sample.txt"

    val usage =
      """
       Usage: LogGenerator --sample-path <sample path> --calls <calls> --duration <duration> [--log-path <log root path>]
      """.stripMargin

    val argList = args.toList

    type OptionMap = Map[Symbol, Any]

    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      def isSwitch(s: String) = s(0) == '-'

      list match {
        case Nil =>
          logTrace("end")
          map
        case "-h" :: tail =>
          logTrace("help")
          throw new HelpException
        case "--help" :: tail =>
          logTrace("help")
          throw new HelpException
        case "--calls" :: value :: tail if !isSwitch(value) =>
          logTrace("calls")
          nextOption(map ++ Map('Calls -> value.toInt), tail)
        case "--duration" :: value :: tail if !isSwitch(value) =>
          logTrace("duration")
          nextOption(map ++ Map('Duration -> value.toInt), tail)
        case "--log-path" :: value :: tail if !isSwitch(value) =>
          logTrace("log-path")
          nextOption(map ++ Map('LogPath -> value), tail)
        case "--sample-path" :: value :: tail if !isSwitch(value) =>
          logTrace("sample-path")
          nextOption(map ++ Map('SamplePath -> value), tail)
        case option :: tail =>
          throw new Exception(s"Unknown option: $option")
      }
    }

    try {
      val options = nextOption(Map.empty, argList)
      //    println(options)

      // "SKT-CLIPHDP02:6667"
      numOfCalls = options.get('Calls) match {
        case Some(x) => x.asInstanceOf[Int]
        case None =>
          throw new Exception("calls 옵션은 필수입니다.")
      }

      duration = options.get('Duration) match {
        case Some(x) => x.asInstanceOf[Int]
        case None =>
          throw new Exception("duration 옵션은 필수입니다.")
      }

      logPath = options.get('LogPath) match {
        case Some(x) => x.asInstanceOf[String]
        case None => "."
      }

      samplePath = options.get('SamplePath) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception("sample-path 옵션은 필수입니다.")
      }

      process(samplePath, numOfCalls, duration, logPath)
    } catch {
      case _: HelpException =>
        println(usage)
      case e: Throwable =>
        println(usage)
        println(e.getLocalizedMessage)
    }
  }
}

class ExternalApiActor extends Actor {
  def receive = {
    case req: Request => sender ! Response(Van.syncCallToApi(Message(req.target, req.line)))
    case _: CountRequest =>
      println("processing count request")
      sender ! CountResponse(Van.number)
    case _ => println("unknown request")
  }
}

case class Sample(target: String, line: String)

case class Request(line: String, target: String)

case class Response(id: Int)

case class CountRequest()
case class CountResponse(count: Int)