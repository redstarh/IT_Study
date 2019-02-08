package com.sktelecom.tos.stream.hive

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream

import com.sktelecom.tos.stream.base.ConfigManager
import com.sktelecom.tos.stream.base.kafka.StreamKafkaConsumer
import com.sktelecom.tos.stream.base.spark.TosStreamApp 




/**
  * Hive 저장이 필요한 데이터를 수신하는 Kafka Consumer 생성하여
  * 수신한 데이터 Hive에 저장한다.
  *
  * @param configManager tos.stream의 Config를 관리하는 클래스
  */
class HiveStreamApp(configManager: ConfigManager) extends TosStreamApp with Serializable {

  override def sparkConfig: Map[String, String] = configManager.getMap("sparkConfig")

  override def batchDuration: Int = configManager.getInt("batchDuration")

  override def checkpointDir: String = configManager.getString("checkpointDir")

  /**
    * DataStoreApp 기동
    */
  def start(): Unit = {

    streamContext { (ss, ssc) =>

      ss.sqlContext.setConf("hive.exec.dynamic.partition", "true")
      ss.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

      val kafkaConsumerProp = configManager.getMap("kafkaConsumer")
      val kafkaConsumerTopics = configManager.getString("input_topic.topics")
      val kafkaConsumerCharset = configManager.getString("input_topic.charset")

      val kafkaDStream = StreamKafkaConsumer(kafkaConsumerProp)
        .createDirectStream(ssc, kafkaConsumerTopics)
        .map(record => new String(record.value, kafkaConsumerCharset))

      saveToHive(ss, kafkaDStream)

    }
  }

  /**
    * kafka를 통해 전달된 DStream을 Hive에 저장한다.
    *
    * @param ss
    * @param kafkaDStream
    */
  def saveToHive(ss: SparkSession, kafkaDStream: DStream[String]): Unit = {

    kafkaDStream.map(HiveSchema(_)).persist()
      .foreachRDD(rdd => {
        // TODO : persist 적용 필요함
        if (rdd.count() > 0) {
          val df = ss.createDataFrame(rdd)
          df.write.format("hive").mode(SaveMode.Append)
            .insertInto("tos.tcic_integ_cont_hst")
        }
      })
      
  }

}

/**
  * HiveStreamApp
  */
object HiveStreamApp {
  def main(args: Array[String]): Unit = {
    new HiveStreamApp(ConfigManager(getClass.getSimpleName.stripSuffix("$"))).start()
  }
}
