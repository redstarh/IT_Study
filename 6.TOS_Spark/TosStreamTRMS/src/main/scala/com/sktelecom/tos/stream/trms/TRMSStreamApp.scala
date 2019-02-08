package com.sktelecom.tos.stream.trms

import com.sktelecom.tos.stream.base.ConfigManager
import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.kafka.{ StreamKafkaConsumer, StreamKafkaProducer, StreamKafkaRecord }
import com.sktelecom.tos.stream.base.meta.info.MetaInfo
import com.sktelecom.tos.stream.base.meta.{ CEPFormatter, EventMapper, MetaInfoLoader }
import com.sktelecom.tos.stream.base.spark.TosStreamApp
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.spark.streaming.dstream.DStream

/**
 * TRMS log 를 수신하여 정제 및 매핑하여
 * CEP Topic 및 Save Topic에 전송한다
 *
 * @param configManager tos.stream의 Config를 관리하는 클래스
 */
class TRMSStreamApp(configManager: ConfigManager) extends TosStreamApp with Serializable {

  override def sparkConfig: Map[String, String] = configManager.getMap("sparkConfig")

  override def batchDuration: Int = configManager.getInt("batchDuration")

  override def checkpointDir: String = configManager.getString("checkpointDir")

  /**
   * TRMSStreamApp 을 기동한다.
   */
  def start(): Unit = {

    // biz logic
    streamContext { (ss, ssc) =>

      // 기준정보 로딩
      val metaInfoConfig = configManager.getConfig("metaInfo")

      val metaInfo: MetaInfo = MetaInfoLoader(ss, metaInfoConfig).start()
      val eventMapper = new EventMapper(metaInfo)
      val cepFormatter = new CEPFormatter(metaInfo)

      // kafka consumer
      val kafkaConsumerProp = configManager.getMap("kafkaConsumer")
      val kafkaConsumerTopics = configManager.getString("input_topic.topics")
      val kafkaConsumerCharset = configManager.getString("input_topic.charset")

      // kafka producer
      val kafkaProducerProp = configManager.getMap("kafkaProducer")
      val kafkaProducerCepTopic = configManager.getString("output_topic.cepTopic")
      val kafkaProducerSaveTopic = configManager.getString("output_topic.saveTopic")

      // Direct Kafka Consumer 생성
      val kafkaDStream = StreamKafkaConsumer(kafkaConsumerProp)
        .createDirectStream(ssc, kafkaConsumerTopics)
        .map(record => StreamKafkaRecord(record.topic, record.key, new String(record.value, kafkaConsumerCharset)))

      kafkaDStream.map(_.value)
        .foreachRDD(rdd => {
          rdd.foreachPartition(records => {
            records.foreach(println)
          })
        })

      //      val cleaningDStream = dataCleaning(kafkaDStream)

      //      val mappingDStream = dataMapping(cleaningDStream, eventMapper)

      //      sendToKafka(mappingDStream, cepFormatter, kafkaProducerProp, kafkaProducerCepTopic, kafkaProducerSaveTopic)

    }

  }

  /**
   * 수집된 Streaming Data에 대해 정제 작업을 진행한다.
   *
   * @param dstream kafka 로 받은 dstream
   * @return 정제 완료된 dstream
   */
  def dataCleaning(dstream: DStream[StreamKafkaRecord]): DStream[String] = {
    dstream.map(_.value)
      .filter(record => {
        val splitRecord = record.split("\\|", -1)
        if (splitRecord.length > 0) {
          true
        } else {
          Log.warn(s"warn record : $splitRecord")
          false
        }
      })
  }

  /**
   * 수집된 Streaming Data에 대해 event mapping 작업을 진행한다.
   *
   * @param dstream 정제 완료된 dstream
   * @return 매핑 완료된 dstream
   */
  def dataMapping(dstream: DStream[String], eventMapper: EventMapper): DStream[StreamData] = {
    dstream
      .map(TRMSData(_))
      .map(eventMapper.getEventInfo(_))
  }

  /**
   * cep / db 전송 데이터를 Kafka 로 전송
   *
   * @param dstream 정제 및 매핑 완료된 dstream
   * @param cepFormatter cep 포맷터
   * @param producerProp kafka producer 프로퍼티
   * @param cepTopic cep 전송 토픽
   * @param saveTopic save 전송 토픽
   */
  def sendToKafka(dstream: DStream[StreamData], cepFormatter: CEPFormatter, producerProp: Map[String, String], cepTopic: String, saveTopic: String): Unit = {
    dstream
      .foreachRDD(rdd => {
        rdd.foreachPartition(records => {
          val producer = StreamKafkaProducer.getOrCreateProducer(producerProp)
          records.foreach(record => {
            if (record.isCepSend) producer.send(new ProducerRecord(cepTopic, cepFormatter.convertToJson(record).getBytes))
            producer.send(new ProducerRecord(saveTopic, record.toJsonString.getBytes))
          })
        })
      })
  }

}

/**
 * TRMSStreamApp
 */
object TRMSStreamApp {
  def main(args: Array[String]): Unit = {
    new TRMSStreamApp(ConfigManager(getClass.getSimpleName.stripSuffix("$"))).start
  }
}