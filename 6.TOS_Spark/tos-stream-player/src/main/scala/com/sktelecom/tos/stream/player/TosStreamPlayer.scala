package com.sktelecom.tos.stream.player

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.Logger

import scala.io.Source

trait Logging {

  final val logger = Logger.getLogger(getClass)

  def logTrace(msg: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg)
  }

  def logInfo(msg: => String): Unit = {
    if (logger.isInfoEnabled) logger.info(msg)
  }

}

class HelpException extends Exception

object TosStreamPlayer extends Logging {
  final val defaultCharset = "utf-8"
  final val defaultRepeat = 1

  /**
    *
    * @param path
    * @param bootstrapServers
    * @param topic
    * @param charset
    * @param repeat
    */
  def process(path: String, bootstrapServers: String, topic: String, charset: String, repeat: Int): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

    val producer = new KafkaProducer[String, Array[Byte]](props)

    var count = 0

    var repeat_ = 1

    while (repeat_ <= repeat) {
      logInfo(s"round: $repeat_" )

      val textFile = Source.fromFile(path, "utf-8")

      val lines = textFile.getLines()

      lines.foreach { p =>
        count += 1
        logTrace(p)
        producer.send(new ProducerRecord(topic, p.getBytes(charset)))
      }

      repeat_ += 1
    }

    logInfo(s"played ${count} messages from $path to $topic of $bootstrapServers")
  }

  def main(args: Array[String]): Unit = {

    val usage =
      """
       Usage: TosStreamPlayer --bs <bootstrap servers> --topic <topic> --path <path> [--repeat <repeat>] [--charset <charset>]
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
        case "--bs" :: value :: tail if !isSwitch(value) =>
          logTrace("bs")
          nextOption(map ++ Map('bootstrapServers -> value), tail)
        case "--topic" :: value :: tail if !isSwitch(value) =>
          logTrace("topic")
          nextOption(map ++ Map('topic -> value), tail)
        case "--path" :: value :: tail if !isSwitch(value) =>
          logTrace("path")
          nextOption(map ++ Map('path -> value), tail)
        case "--repeat" :: value :: tail if !isSwitch(value) =>
          logTrace("repeat")
          nextOption(map ++ Map('repeat -> value.toInt), tail)
        case "--charset" :: value :: tail if !isSwitch(value) =>
          logTrace("charset")
          nextOption(map ++ Map('charset -> value), tail)
        case option :: tail =>
          println(s"Unknown option: $option")
          throw new Exception(s"Unknown option: $option")
      }
    }

    try {
      val options = nextOption(Map.empty, argList)
      //    println(options)

      // "SKT-CLIPHDP02:6667"
      val bootstrapServers = options.get('bootstrapServers) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception("bootstrap-servers 옵션은 필수입니다.")
      }

      val topic = options.get('topic) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception("topic 옵션은 필수입니다.")
      }

      val path = options.get('path) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception("path 옵션은 필수입니다.")
      }

      val charset = options.get('charset) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultCharset
      }

      val repeat = options.get('repeat) match {
        case Some(x) => x.asInstanceOf[Int]
        case None => defaultRepeat
      }

      process(path, bootstrapServers, topic, charset, repeat)
    } catch {
      case _: HelpException =>
        println(usage)
      case e: Throwable =>
        println(usage)
        e.printStackTrace()
    }
  }
}
