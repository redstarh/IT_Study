package com.sktelecom.tos.stream.base.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.log4j.Logger

import scala.collection.mutable

/**
  * Kafka Producer 클래스
  */
object StreamKafkaProducer {

  import scala.collection.JavaConverters._

  private val Producers = mutable.Map[Map[String, Object], KafkaProducer[String, Array[Byte]]]()

  /**
    * kafka producer 를 생성한다.
    *
    * @param config kafka producer properties
    * @return kafka producer
    */
  def getOrCreateProducer(config: Map[String, Object]): KafkaProducer[String, Array[Byte]] = {

    Producers.getOrElseUpdate(
      config, {
        val producer = new KafkaProducer[String, Array[Byte]](config.asJava)
        sys.addShutdownHook {
          producer.close()
        }
        producer
      })
  }
}