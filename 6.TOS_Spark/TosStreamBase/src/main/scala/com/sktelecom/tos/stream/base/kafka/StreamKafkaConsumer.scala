package com.sktelecom.tos.stream.base.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.Logger
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

/**
  * Kafka Consumer 역할의 클래스
  *
  * @param config kafka consumer properties
  */
class StreamKafkaConsumer(config: Map[String, String]) {

  /**
    * Kafka 와 연동하여 Input DStream을 생성한다.
    *
    * @param ssc    StreamingContext
    * @param topics kafka 토픽 ',' 로 구분하여 다중 토픽을 구독 가능
    * @return
    */
  def createDirectStream(ssc: StreamingContext, topics: String): InputDStream[ConsumerRecord[String, Array[Byte]]] = {
    val kafkaParams = config
    val topicsSet = topics.split(",").toSet

    KafkaUtils.createDirectStream[String, Array[Byte]](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, Array[Byte]](topicsSet, kafkaParams))
  }

}

/**
  * companion object
  */
object StreamKafkaConsumer {
  def apply(config: Map[String, String]): StreamKafkaConsumer = new StreamKafkaConsumer(config)
}

/**
  * kafka 전송 데이터
  *
  * @param topic kafka topic
  * @param key   kafka key
  * @param value kafka value
  */
case class StreamKafkaRecord(topic: String, key: String, value: String) extends Serializable