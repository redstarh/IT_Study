package com.sktelecom.tos.stream.r2k

import com.sktelecom.tos.stream.base.ConfigManager
import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.kafka.{StreamKafkaConsumer, StreamKafkaProducer, StreamKafkaRecord}
import com.sktelecom.tos.stream.base.meta.info.MetaInfo
import com.sktelecom.tos.stream.base.meta.{CEPFormatter, EventMapper, MetaInfoLoader}
import com.sktelecom.tos.stream.base.spark.TosStreamApp
import com.sktelecom.tos.stream.r2k.R2KData.getClass
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.log4j.Logger
import org.apache.spark.streaming.dstream.DStream

/**
  * R2K Log 를 수신하여 정제 및 매핑하여
  * CEP Topic 및 Save Topic에 전송한다
  *
  * @param configManager tos.stream의 Config를 관리하는 클래스
  */
class R2KStreamApp(configManager: ConfigManager) extends TosStreamApp with Serializable {

  //
  override def sparkConfig: Map[String, String] = configManager.getMap("sparkConfig")

  override def batchDuration: Int = configManager.getInt("batchDuration")

  override def checkpointDir: String = configManager.getString("checkpointDir")

  /**
    * R2KStreamApp 을 기동한다.
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

      // Direct Kafka Consumer 생성 - kafka direct API 구현
      val kafkaDStream = StreamKafkaConsumer(kafkaConsumerProp)
        .createDirectStream(ssc, kafkaConsumerTopics)
        .map(record => StreamKafkaRecord(record.topic, record.key, new String(record.value, kafkaConsumerCharset)))

      val cleaningDStream = dataCleaning(kafkaDStream)

      // meta 데이터와 비교 event 정보 생성 및 cep 전송 정보 생성
      val mappingDStream = dataMapping(cleaningDStream, eventMapper)

      // cep 전송 여부에 따라 cep 토픽 전송 및 db 저장 topic 전송
      sendToKafka(mappingDStream, cepFormatter, kafkaProducerProp, kafkaProducerCepTopic, kafkaProducerSaveTopic)

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

        val recordLengthcheck = record.length  // recordLengthcheck  = 160 OR 312
        val headerRecord = record.substring(0,32)
        val bodyRecord = record.substring(32)

        val onoff = record.substring(32,36).trim  // ONOFF
        val offMsgtype = record.substring(44,48).trim    // OFFCheck 전문

        val onUsetype = record.substring(36,42).trim     // online usetype 승인요청(000010)
        val offUsetype = record.substring(64,70).trim   // offline usetype 승인요청(000010)

        //online
        if (onoff.equals("0210") && onUsetype.equals("000010") && recordLengthcheck.equals(160)) {

          val srcSendtimeExists = bodyRecord.substring(22, 36).trim         //SND_TIME : srcEventDt  값
          val srcOnlineSktcardnoExists = bodyRecord.substring(36, 52).trim  // srcOnlineSktcardno :  Skt 멤버쉽 카드번호 여부 (srcEventKey)
          val srcOnlineRspcodeExists = bodyRecord.substring(88, 90).trim    // //응답 코드 여부: 승인-->'00',한도초과 오류코드 '40~45', 기타 거절코드


          if ((srcSendtimeExists != null) && (srcOnlineSktcardnoExists != null) && (srcOnlineRspcodeExists != null)) {
            Log.warn(s"===== R2K Start ***** =============================================================================================================================================================================")
            Log.warn(s"===== DataCleaning : ONLINE - [$onoff, $onUsetype, $recordLengthcheck = 160], SendtimeExists -> $srcSendtimeExists, OnlineSktcardnoExists -> $srcOnlineSktcardnoExists, OnlineRspcodeExists -> $srcOnlineRspcodeExists")
            true
          } else {
            Log.warn(s" xxxxxxxxxxxxxxxxxx  R2K_ONLINE_LOG Exception Data (Null) : ($onoff, [$recordLengthcheck != 160], [$onUsetype], SendtimeExists -> $srcSendtimeExists,  OnlineSktcardnoExists -> $srcOnlineSktcardnoExists, OnlineRspcodeExists -> $srcOnlineRspcodeExists, headerRecord -> $headerRecord  xxxxxxxxxxxxxxxxxx\n")
            false
          }

          //offline
        }else if (onoff.contains("ISO") && (offMsgtype.equals("0210")) && offUsetype.equals("000010") && (recordLengthcheck.equals(312))) {

          val srcSendtimeExists = bodyRecord.substring(50,64).trim            //SND_TIME : srcEventDt  값 여부
          val srcOfflineSktcardnoExists = bodyRecord.substring(145,161).trim  //srcOfflineSktcardno : Skt 멤버쉽 카드번호 여부(srcEventKey)
          val srcOfflineRspcodeExists = bodyRecord.substring(123,125).trim    //응답 코드 여부: 승인-->'00',한도초과 오류코드 '40~45', 기타 거절코드 : 2018.10.4 추가


          if ((srcSendtimeExists != null)  && (srcOfflineSktcardnoExists != null) && (srcOfflineRspcodeExists != null)) {
            Log.warn(s"===== R2K Start ***** =============================================================================================================================================================================")
            Log.warn(s"===== DataCleaning : OFFLINE - [$onoff, $offMsgtype, $offUsetype, $recordLengthcheck = 312], SendtimeExists -> $srcSendtimeExists, srcOfflineSktcardnoExists -> $srcOfflineSktcardnoExists, OfflineRspcodeExists -> $srcOfflineRspcodeExists")
            true
          }else{
            Log.warn(s" xxxxxxxxxxxxxxxxxx  R2K_OFFLINE_LOG Exception Data (Null) : ($onoff, [$recordLengthcheck != 312], [$offMsgtype +$offUsetype], SendtimeExists -> $srcSendtimeExists,  OfflineSktcardnoExists -> $srcOfflineSktcardnoExists, OfflineRspcodeExists -> $srcOfflineRspcodeExists, headerRecord -> $headerRecord  xxxxxxxxxxxxxxxxxx\n" )
            false
          }
        } else {
          Log.warn( s" xxxxxxxxxxxxxxxxxx  R2K Exception Data : R2K_LOG (onoff -> $onoff, [$recordLengthcheck != 160/320], [on: $onUsetype, (off: $offMsgtype/ $offUsetype)], headerRecord -> $headerRecord  xxxxxxxxxxxxxxxxxx\n")
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
      .map(R2KData(_))
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

            if (record.isCepSend) {
               producer.send(new ProducerRecord(cepTopic, cepFormatter.convertToJson(record).getBytes))
              Log.warn(s"===== Send CEP  Topic ==== isCepSend -> ${record.isCepSend} ")
            }
              producer.send(new ProducerRecord(saveTopic, record.toJsonString.getBytes))
              Log.warn(s"===== Send SAVE Topic ==== record -> :[ $record ]")
              Log.warn(s"===== R2K Data END  ***** =========================================================================================================================================================================  \n")
          })
        })
      })
  }

}

/**
  * R2KStreamApp
  */
object R2KStreamApp {
  def main(args: Array[String]): Unit = {
    new R2KStreamApp(ConfigManager(getClass.getSimpleName.stripSuffix("$"))).start
  }
}