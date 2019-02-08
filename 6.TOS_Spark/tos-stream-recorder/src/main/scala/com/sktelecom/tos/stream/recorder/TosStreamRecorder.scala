package com.sktelecom.tos.stream.recorder

import java.io._
import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer._
import org.apache.log4j.Logger


trait Logging {

  final val logger = Logger.getLogger(getClass)

  def logTrace(msg: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg)
  }

  def logInfo(msg: => String): Unit = {
    if (logger.isInfoEnabled) logger.info(msg)
  }

}

object TosStreamRecorder extends Logging {

  def createContext(bootstrapServers: String, topic: String, path: String, consumerGroup: String, offset: String, batchDuration: Long, charset: String): Unit = {
    logInfo("creating a streaming context")

    //      "auto.offset.reset" -> "latest",
    //      "enable.auto.commit" -> (true: java.lang.Boolean)
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("group.id", consumerGroup)
    props.put("auto.offset.reset", offset)
//    props.put("enable.auto.commit", true)
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")

    val consumer = new KafkaConsumer[String, Array[Byte]](props)

    consumer.subscribe(util.Arrays.asList(topic))

    val w = new PrintWriter(new FileOutputStream(path, true))

    var count = 0
    var round = 1

    try {
      while (true) {
        val prevCount = count

        val records = consumer.poll(batchDuration)
        logTrace(s"round: $round")

        val i = records.iterator()

        while (i.hasNext) {
          count += 1
          val r = i.next()
          logTrace(s"p=${r.partition()} o=${r.offset()} k=${r.key()} v=${new String(r.value(), charset)}")
          w.println(new String(r.value(), charset))
        }
        if (count > prevCount) {
          w.flush()
          logTrace(s"recorded: $count")
        } else {
          Thread.sleep(batchDuration * 1000)
        }
        round += 1
      }
    } finally {
      w.close()
    }
  }

}

class HelpException extends Exception

/**
  * master 를 local[2] 로 지정해서 스트리밍을 시작
  * 단위 테스트 용
  *
  * JMX 를 활성화하려면 JVM 옵션에 추가 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
  */
object StreamAppLocalMain extends Logging {
  final val defaultConsumerGroup = "tos_stream_recorder"
  final val defaultBatchDuration = 60L
  final val defaultCharset = "utf-8"
  final val defaultOffsetReset = "none"

  /**
    * Kafka 에서 Oracle 로 저장하는 앱
    *
    * @param args 실행 파라미터
    */
  def main(args: Array[String]) {

    val usage =
      """
       Usage: TosStreamRecorder --bs <bootstrap servers> --topic <topic> --path <path> [--consumer-group <consumer-group>] [--offset <offset>] [--batch-duration <duration>] [--charset <charset>]
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
        case "--consumer-group" :: value :: tail if !isSwitch(value) =>
          logTrace("consumer-group")
          nextOption(map ++ Map('consumerGroup -> value), tail)
        case "--offset" :: value :: tail if !isSwitch(value) =>
          logTrace("offset")
          nextOption(map ++ Map('offsetReset -> value), tail)
        case "--batch-duration" :: value :: tail if !isSwitch(value) =>
          logTrace("batch-duration")
          nextOption(map ++ Map('batchDuration -> value.toLong), tail)
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
          throw new Exception
      }

      val topic = options.get('topic) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception
      }

      val path = options.get('path) match {
        case Some(x) => x.asInstanceOf[String]
        case None =>
          throw new Exception
      }

      val consumerGroup = options.get('consumerGroup) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultConsumerGroup
      }

      val offsetReset = options.get('offsetReset) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultOffsetReset
      }

      val batchDuration = options.get('batchDuration) match {
        case Some(x) => x.asInstanceOf[Long]
        case None => defaultBatchDuration
      }

      val charset = options.get('charset) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultCharset
      }

      TosStreamRecorder.createContext(bootstrapServers, topic, path, consumerGroup, offsetReset, batchDuration, charset)
    } catch {
      case _: HelpException =>
        println(usage)
      case e: Throwable =>
        println(usage)
        e.printStackTrace()
    }
  }
}
