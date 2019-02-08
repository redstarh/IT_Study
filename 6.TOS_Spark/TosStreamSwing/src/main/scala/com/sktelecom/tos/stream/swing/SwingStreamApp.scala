package com.sktelecom.tos.stream.swing

import com.sktelecom.tos.stream.base.ConfigManager
import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.kafka.{StreamKafkaConsumer, StreamKafkaProducer, StreamKafkaRecord}
import com.sktelecom.tos.stream.base.meta.info.MetaInfo
import com.sktelecom.tos.stream.base.meta.{CEPFormatter, EventMapper, MetaInfoLoader}
import com.sktelecom.tos.stream.base.spark.TosStreamApp
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.spark.streaming.dstream.DStream

/**
  * Swing log 를 수신하여 정제 및 매핑하여
  * CEP Topic 및 Save Topic에 전송한다
  *
  * @param configManager tos.stream의 Config를 관리하는 클래스
  */
class SwingStreamApp(configManager: ConfigManager) extends TosStreamApp with Serializable {

  // application.conf spark 환경변수 선언
  override def sparkConfig: Map[String, String] = configManager.getMap("sparkConfig")

  // application.conf batch 실행주기(초) 지정 변수 선언
  override def batchDuration: Int = configManager.getInt("batchDuration")

  // rdd check point directory 지정 변수 선언
  override def checkpointDir: String = configManager.getString("checkpointDir")

  /**
    * SwingStreamApp 을 기동하는 함수
    */
  def start(): Unit = {

    // spark context 생성
    streamContext { (ss, ssc) =>

      // 기준정보 로딩 공통 모듈 호출
      val metaInfoConfig = configManager.getConfig("metaInfo")

      val metaInfo: MetaInfo = MetaInfoLoader(ss, metaInfoConfig).start()

      // cep topic 전송용 데이터 전문 생성
      val eventMapper = new EventMapper(metaInfo)
      val cepFormatter = new CEPFormatter(metaInfo)

      // kafka consumer 환경 세팅
      val kafkaConsumerProp = configManager.getMap("kafkaConsumer")
      val kafkaConsumerTopics = configManager.getString("input_topic.topics")
      val kafkaConsumerCharset = configManager.getString("input_topic.charset")

      // kafka producer 환경 세팅
      val kafkaProducerProp = configManager.getMap("kafkaProducer")
      val kafkaProducerCepTopic = configManager.getString("output_topic.cepTopic")
      val kafkaProducerSaveTopic = configManager.getString("output_topic.saveTopic")

      // Direct Kafka Consumer 생성 ( swing 실시간 ogg topic )
      val kafkaDStream = StreamKafkaConsumer(kafkaConsumerProp)
        .createDirectStream(ssc, kafkaConsumerTopics)
        .map(record => StreamKafkaRecord(record.topic, record.key, new String(record.value, kafkaConsumerCharset)))

      // swing 실시간 ogg 데이터의 기본적인 필터링 수행
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

    // Swing 테이블별 컬럼명 구조체 선언. 15종
    val headers: Map[String, List[String]] = Map(
      "ZORD.ZORD_PSNINF_ITM_PRA_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "PSNINF_PAGR_ITM_CD", "OP_DTM", "AUDIT_ID", "AUDIT_DTM", "CUST_NUM", "PSNINF_PAGR_FRMT_TYP_CD", "PSNINF_PAGR_FRMT_VER_NUM", "PSNINF_PAGR_CHNL_CD", "PSNINF_PAGR_ITM_VER_NUM", "PSNINF_PAGR_CL_CD", "INFO_PRA_AGR_OP_CHG_CD", "OPR_ID", "OP_SALE_ORG_ID"), // 개인정보항목활용이력
      "ZORD.ZORD_CUST_INFO_PRA_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "INFO_PRA_AGR_ITM_CD", "OP_DTM", "CUST_NUM", "INFO_PRA_AGR_YN", "INFO_PRA_AGR_OP_CHG_CD", "OP_AGN_ORG_ID", "OPR_ID", "AUDIT_ID", "AUDIT_DTM", "INFO_PRA_AGR_CURNT_CHG_CD", "INFO_PRA_AGR_CURNT_OP_DTM", "OK_AGREE_YN"), // 고객정보활용이력
      "ZORD.ZORD_CUST_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "CO_CL_CD", "CUST_NUM", "CUST_CHG_CD", "MAIN_CHG_YN", "CUST_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "CHG_DTM", "CNCL_DTM", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF"), // 고객변경이력
      "ZORD.ZORD_ACNT_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "ACNT_NUM", "CHG_DT", "ACNT_CHG_CD", "SER_NUM", "AUDIT_ID", "AUDIT_DTM", "MAIN_CHG_CD", "ACNT_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "CHG_DTM", "CNCL_DTM", "REQR_CL_CD", "REQR_NM", "REQR_CNTC_NUM", "REQR_REL_CD", "REQR_AG_LGRP_CTZ_NUM", "OP_SALE_ORG_ID", "REQ_SALE_ORG_ID", "REQ_SALE_BR_ORG_ID", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "OP_MEMO_ID", "SALE_CHNL_CL_CD", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50"), // 계정변경이력
      "ZORD.ZORD_SVC_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "SVC_CD", "SVC_MGMT_NUM", "SVC_CHG_CD", "MAIN_CHG_YN", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "CHG_DTM", "CNCL_DTM", "SALE_APRV_NUM", "SVC_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "MT_SVC_CL_CD", "MT_SVC_NUM", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50"), // 서비스변경이력
      "ZORD.ZORD_COPN_OP_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "COPN_ISUE_NUM", "EFF_END_DTM", "AUDIT_ID", "AUDIT_DTM", "COPN_OP_DTM", "SVC_MGMT_NUM", "CUST_NUM", "OP_SALE_ORG_ID", "OPR_ID", "EQP_MDL_CD", "EQP_SER_NUM", "COPN_OPER_ST_CD", "BF_COPN_OPER_ST_CD", "CNCL_YN"), // 쿠폰처리이력
      "ZORD.ZORD_DATA_GIFT_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "OP_DTM", "AUDIT_ID", "AUDIT_DTM", "BEFR_SVC_MGMT_NUM", "DATA_GIFT_OP_ST_CD", "GIFT_DATA_QTY", "OP_SALE_ORG_ID", "OPR_ID", "PROD_ID", "BEFR_PROD_ID", "CUST_NUM", "BEFR_CUST_NUM", "GIFT_IF_RSLT_CD", "DATA_GIFT_TYP_CD", "AFMLY_GIFT_YN", "GIFT_DAY_CL_CD", "RL_GIFT_DATA_QTY"), // 데이터선물이력
      "ZORD.ZORD_DATA_GIFT_ACHRG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "EFF_END_DTM", "AUTO_CHRG_OP_CD", "SER_NUM", "AUDIT_ID", "AUDIT_DTM", "EFF_STA_DTM", "APLY_STA_DT", "APLY_END_DT", "REQ_SALE_ORG_ID", "REQR_ID", "AUTO_CHRG_CYCL_CD", "CHRG_TM_CD", "CHRG_MD", "CHRG_SVC_MGMT_NUM", "CHRG_REQ_CTT", "CHRG_REQ_QTY", "CNCL_YN"), // 데이터선물자동충전이력
      "ZORD.ZORD_SUPL_SVC_COPN" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CARD_GIFT_NUM", "COPN_TYP_CD", "SVC_MGMT_NUM", "CARD_EXPIR_DT", "USE_YN", "RGST_CL_CD", "EFF_STA_DTM", "EFF_END_DTM", "SCRB_DT", "TERM_DT", "AUDIT_ID", "AUDIT_DTM", "OP_SALE_ORG_ID", "OPR_ID", "COPN_RL_GDS_CL_CD", "SALE_DTM", "RFND_DTM", "COPN_ISUE_NUM", "SELR_ID", "SALE_SALE_ORG_ID", "BUY_SVC_MGMT_NUM", "COPN_ISUE_DT", "COPN_OUT_DTM", "COPN_CO_CD", "COPN_CO_NM", "SYS_NC00026", "SYS_NC00027", "SYS_NC00028"), // 부가서비스쿠폰
      "ZMBR.ZMBR_MBR_CARD_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "MBR_CARD_NUM1", "CHG_DT", "OP_TM", "SVC_MGMT_NUM", "MBR_CHG_CD", "MBR_CHG_RSN_CD", "OP_ORG_ID", "DTL_CTT", "BCHG_MBR_CARD_NUM2", "BCHG_SVC_NUM", "BCHG_MBR_CUST_NM", "BCHG_MBR_ST_CD", "BCHG_MBR_TYP_CD", "BCHG_MBR_GR_CD", "BCHG_SVC_NOMINAL_REL_CD", "BCHG_SMS_AGREE_YN", "BCHG_OCB_ACCUM_AGREE_YN", "BCHG_SELF_AUTH_YN", "BCHG_ADDR_CD", "BCHG_INTEG_TXT_ADDR_ID", "BCHG_CNTC_NUM", "BCHG_CUST_EMAIL_ADDR", "BCHG_JOB_CD", "BCHG_JOB_KND_CD", "BCHG_SLNL_CD", "BCHG_BIRTH_DT", "BCHG_WEDD_ANNV_DT", "BCHG_HOBBY_CD1", "BCHG_HOBBY_CD2", "BCHG_HOBBY_CD3", "BCHG_MBR_REM_LMT_AMT", "BCHG_FMLY_CUST_NM", "ACHG_MBR_CARD_NUM2", "ACHG_SVC_NUM", "ACHG_MBR_CUST_NM", "ACHG_MBR_ST_CD", "ACHG_MBR_TYP_CD", "ACHG_MBR_GR_CD", "ACHG_SVC_NOMINAL_REL_CD", "ACHG_SMS_AGREE_YN", "ACHG_OCB_ACCUM_AGREE_YN", "ACHG_SELF_AUTH_YN", "ACHG_ADDR_CD", "ACHG_INTEG_TXT_ADDR_ID", "ACHG_CNTC_NUM", "ACHG_CUST_EMAIL_ADDR", "ACHG_JOB_CD", "ACHG_JOB_KND_CD", "ACHG_SLNL_CD", "ACHG_BIRTH_DT", "ACHG_WEDD_ANNV_DT", "ACHG_HOBBY_CD1", "ACHG_HOBBY_CD2", "ACHG_HOBBY_CD3", "ACHG_MBR_REM_LMT_AMT", "ACHG_FMLY_CUST_NM", "LEGAL_REPVE_NM", "LEGAL_REPVE_CNTC_NUM", "COLL_CHNL_CD", "AUDIT_ID", "AUDIT_DTM", "TRNSFR_SVC_NUM", "TRNSFE_SVC_NUM", "ACCUM_PT", "BCHG_T_TURPSS_AGR_YN", "ACHG_T_TURPSS_AGR_YN", "BCHG_MBR_FUNC_USE_YN", "ACHG_MBR_FUNC_USE_YN", "BCHG_MBR_CARD_IMG_NUM", "ACHG_MBR_CARD_IMG_NUM", "BCHG_MBR_GR_PLUS_YN", "ACHG_MBR_GR_PLUS_YN", "BCHG_MBR_CTZ_SER_NUM", "BCHG_MBR_CTZ_NUM_PINF", "BCHG_FMLY_CTZ_SER_NUM", "BCHG_FMLY_CTZ_NUM_PINF", "ACHG_MBR_CTZ_SER_NUM", "ACHG_MBR_CTZ_NUM_PINF", "ACHG_FMLY_CTZ_SER_NUM", "ACHG_FMLY_CTZ_NUM_PINF", "CTZ_NUM_AGREE_YN", "MKTG_AGREE_YN", "JNCARD_LINK_YN", "BCHG_CARD_ISUE_TYP_CD", "ACHG_CARD_ISUE_TYP_CD", "EMAIL_NEWSL_AGREE_YN", "EMAIL_PROD_AGREE_YN", "EMAIL_ETC_AGREE_YN", "SMS_MMS_AGREE_YN", "TM_RCV_YN", "BCHG_CTZ_NUM_AGREE_YN", "BCHG_MKTG_AGREE_YN", "BCHG_EMAIL_NEWSL_AGREE_YN", "BCHG_EMAIL_PROD_AGREE_YN", "BCHG_EMAIL_ETC_AGREE_YN", "BCHG_SMS_MMS_AGREE_YN", "BCHG_TM_RCV_YN", "BCHG_CASHB_SCRB_YN", "ACHG_CASHB_SCRB_YN", "BCHG_CASHB_CARD_IMG_NUM", "ACHG_CASHB_CARD_IMG_NUM"), // 멤버십카드이력
      "ZCOL.ZCOL_COL_CS_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "CNSL_DT", "CNSL_TM", "EVT_NUM", "ACNT_NUM", "CSR_ID", "CRDT_MGMT_CNTR_ORG_ID", "COL_GRP_CD", "COL_PART_ORG_ID", "COL_ACT_CD", "TC_PHON_CL_CD", "TC_PHON_NUM", "PAY_MTHD_CD", "PAY_APNT_DT", "PAY_APNT_COL_PAY_MTHD_CD", "PAY_APNT_AMT", "NXT_CNSL_OPER_DT", "COL_MEMO_EXIST_YN", "CNSL_TMS", "APNT_OBSRV_CD", "ACTL_PAY_AMT", "COL_AMT", "COL_MTH_CNT", "BOND_MTH_AGE", "TMTH_COL_AMT", "CRDT_SCOR", "LAST_INV_DT", "EVT_CRE_DT", "EVT_END_DT", "IO_CL_CD", "SVC_CD", "AUDIT_ID", "AUDIT_DTM", "PAY_CYCL_CD", "COL_DRW_INV_CYCL_CD", "SVC_CRDT_GR_CD", "CNTC_NUM_TYP_CD", "WIRE_COL_CS_OBJ_CL_CD", "WIRE_COL_CS_BRWS_RNK", "ACNT_COL_AMT", "ACNT_COL_SVC_CNT", "CO_CL_CD", "CNTRCT_MGMT_NUM", "USE_CNTRCT_CL_CD", "OCALL_TRMS_NUM", "BOND_ASGN_GRP_CD", "CUST_REL_CD"), // 미납상담이력
      "ZINB.ZINB_INB_CNSL" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CSR_ID", "CNSL_DT", "CNSL_TM", "AUDIT_ID", "AUDIT_DTM", "CO_CL_CD", "CUST_NUM", "CUST_NM", "SKT_CUST_YN", "CNTC_NUM", "TCR_NM", "NM_CUST_REL_CD", "CNSL_CD_SEQ", "OBD_FAIL_RSN_CD", "OBD_SUSS_TCR_CD", "DSAT_CL_CD", "CNSL_UNIT_CD", "CNSL_OP_ST_CD", "TAKE_SALE_ORG_ID", "TAKER_NM", "MEMO_YN", "MEMO_TYP_CD", "VOC_RCV_ID", "VOC_OP_CL_CD", "ORG_ID", "HO_IDNT_NUM", "CNSL_CONT_CL_CD", "CNSL_CHNL_CD", "CALLFLOW_CD", "AGREE_INFO_YN", "WAREA_CD", "AUTH_YN", "DUP_CNSL_CL_CD", "DSAT_GR_CD", "DSAT_INDC_ORG_ID", "DSAT_INDC_SUP_ORG_ID", "DSAT_INDC_DEPT_SEL_YN", "AGN_LIN_CALL_YN", "HSMS_TYP_NUM", "RSV_NUM", "SALE_BR_ORG_ID", "IVR_AUTH_CL_CD", "CNSL_CUST_SCRB_CL_CD", "SUP_CSR_ID", "SUP_CNSL_DT", "SUP_CNSL_TM", "ANALS_OBJ_YN", "INB_CNSL_GRP_NUM"), // INBOUND상담
      "ZINV.ZINV_BILL_ISUE_SPC" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "BILL_ISUE_SER_NUM", "ACNT_NUM", "SVC_MGMT_NUM", "INV_DT", "REQ_DT", "BILL_ISUE_SPC_CL_CD", "MAIL_TYP_CD", "REISU_ST_CD", "RESND_DT", "REISU_BILL_FRMT_STRD_CD", "REISU_REQ_PATH_CD", "ISUE_CL_CD", "REISU_INVR_NM", "REISU_ERR_CD", "RGMAIL_NUM", "INTEG_TXT_ADDR_ID", "ST_NM_LOTN_ADDR", "PAY_MTHD_CD", "BLMSG_CD", "MBL_BILL_RCV_PHON_NUM", "TERM_COL_CNTC_CL_CD", "ADDR_UNLIKE_YN", "OP_SALE_ORG_ID", "REISU_EMAIL_ADDR", "RGSTR_ID", "AUDIT_ID", "AUDIT_DTM", "LEGAL_REPVE_INCLD_YN", "LEGAL_REPVE_SVC_MGMT_NUM", "TAX_BILL_ISUE_YN", "INT_PHON_DTL_ISUE_YN", "SVC_LINE_CNT", "INTEG_DTL_ISUE_YN", "NEAT_BILL_YN", "COL_INCLD_YN", "PRD_PAY_DT", "REISU_RSN_CD", "BIZ_CL_CD", "INVR_DEPT_NM", "INVR_CHRGR_NM", "BILL_SND_CO_CD", "BILL_COL_FRMT_CL_CD", "CMBE_ACNT_NUM", "SCUR_BILL_YN", "PRT_SVC_NUM", "ATCH_DTL_INCLD_YN", "CUST_BRWS_NUM", "INV_AMT", "PAY_OP_YN", "REISU_GUID_MMS_SND_YN", "REISU_GUID_MMS_RCV_PHON_NUM"), // 청구서발행명세
      "ZORD.ZORD_WIRE_SVC_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "SVC_CD", "SVC_MGMT_NUM", "SVC_CHG_CD", "MAIN_CHG_YN", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "CHG_DTM", "CNCL_DTM", "SALE_APRV_NUM", "SVC_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "OPING_CNCL_RSN_CD", "SVC_OPER_ST_CD", "RCV_SEQ", "RCV_DTM", "OSS_OP_RSLT_RSN_CD", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20") // 유선서비스변경이력
    )
//2018.10.11. 실시간대상제외      "ZORD.ZORD_CONT_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "CONT_HST_CD", "AUDIT_ID", "AUDIT_DTM", "CUST_NUM", "SVC_MGMT_NUM", "CONT_CHG_CD", "CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "CO_CL_CD", "OP_CO_CL_CD", "CHNL_CL_CD", "CHG_DTM", "CNCL_DTM", "REQR_CL_CD", "REQR_NM", "REQR_CNTC_NUM", "REQR_SVC_REL_CD", "REQR_REL_CD", "OP_SALE_ORG_ID", "REQ_SALE_ORG_ID", "REQ_SALE_BR_ORG_ID", "REQR_AG_LGRP_CTZ_NUM", "OP_MEMO_ID", "SALE_CHNL_CL_CD", "CONT_CHG_CTT"), // 접촉이력
//2018.10.11. 실시간대상제외      "ZORD.ZORD_SVC_PROD_GRP" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_PROD_GRP_CD", "SVC_PROD_GRP_ID", "EFF_END_DTM", "EFF_STA_DTM", "AUDIT_ID", "AUDIT_DTM", "NM_CUST_NUM", "MOTHER_SVC_MGMT_NUM", "BZRING_BILL_MTHD_CD", "BZRING_REP_SCRB_DT", "BZRING_REP_TERM_DT", "MNGR_NM", "CNTC_NUM", "LAST_OP_SALE_ORG_ID", "RGST_OPR_ID", "RGST_SALE_ORG_ID", "RGST_DT", "TERM_OPR_ID", "TERM_SALE_ORG_ID", "TERM_DT", "BAS_SUPL_SVC_CD", "DTL_ADD_SVC_CD", "CNCL_YN", "BCHG_GRP_TOT_SCRB_YR_CNT", "ACHG_GRP_TOT_SCRB_YR_CNT", "BCHG_GRP_BAS_FEE_DC_RT", "ACHG_GRP_BAS_FEE_DC_RT", "LAST_MEMB_CHG_DT", "LAST_LOSS_ASMPT_AMT", "SCH_ID", "IF_PROD_GRP_CD", "CORP_MNGR_CUST_NUM", "OP_CTT_DESC", "ACNT_NUM", "CORP_CUST_NUM", "BENF_TYP_CD", "GRP_OFR_PT", "COMB_CO_CL_CD", "BIZ_NUM"), // 서비스상품그룹
//2018.10.11. 실시간대상제외      "ZORD.ZORD_SVC_PROD_GRP_MEMB" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_PROD_GRP_CD", "SVC_PROD_GRP_ID", "CHLD_SVC_MGMT_NUM", "EFF_STA_DTM", "EFF_END_DTM", "TERM_RSN_CD", "GRP_REL_CL_CD", "BILL_STA_DT", "BZRING_ST_CD", "AGREE_MTHD_CD", "RGST_OPR_ID", "RGST_SALE_ORG_ID", "RGST_DT", "TERM_OPR_ID", "TERM_SALE_ORG_ID", "LAST_OP_SALE_ORG_ID", "TERM_DT", "CNCL_YN", "AUDIT_ID", "AUDIT_DTM", "STRD_YM", "RCV_DT", "RSV_MGMT_YN", "RSV_PROD_OP_ST_CD", "RSN_CTT", "MGMT_CTT", "COMB_CO_CL_CD", "SVC_PROD_GRP_ATTR_CD", "SVC_CD", "RCV_SEQ", "CCUR_RCV_NUM", "MBL_PHON_SVC_MGMT_NUM", "TB_COMB_BENF_CL_CD", "FMLY_GRP_AGREE_YN", "INV_CNT", "LAST_INV_DT", "CHSVC_AGREE_ST_CD", "CHSVC_AGREE_ST_CHG_DTM", "CHSVC_AGREE_OP_ORG_ID", "CHSVC_AGREE_OPR_ID", "COMB_REL_SVC_MGMT_NUM", "COMB_DC_BENF_CL_CD", "BF_MBL_PHON_SVC_MGMT_NUM"), // 서비스상품그룹멤버
//2018.10.11. 실시간대상제외      "ZCAM.ZCAM_CMPGN_OBJ_CONT" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CMPGN_NUM", "EXEC_SCHD_NUM", "CMPGN_OBJ_NUM", "EXTRT_SEQ", "CONT_DT", "CONT_TM", "CONT_CHNL_CD", "CONT_YN", "SUSS_YN", "RACT_TYP_CD", "TCR_TYP_CD", "SEL_GIFT_CD", "CSR_ID", "CONT_SALE_ORG_ID", "AUDIT_ID", "AUDIT_DTM", "ERR_CD", "EXEC_BF_EXL_RSN_CD", "EXEC_BF_EXL_SER_NUM"), // 캠페인대상접촉
//2018.10.11. 실시간대상제외      "ZCAM.ZCAM_CMPGN_OBJ_CONT_OFER" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CMPGN_NUM", "EXEC_SCHD_NUM", "CMPGN_OBJ_NUM", "EXTRT_SEQ", "OFER_CD", "OFER_TRY_DT", "OFER_TRY_TM", "OFER_TRY_SUM_YN", "CONT_CHNL_CD", "OFER_SUSS_YN", "OFER_PRFER_DEGRE_CD", "CSR_ID", "CONT_SALE_ORG_ID", "AUDIT_ID", "AUDIT_DTM", "CSR_INS_OFER_SUSS_YN"), // 캠페인대상접촉오퍼

    // 현재 처리 중인 데이터의 컬럼정보 저장 변수 선언
    var header: List[String] = null
    
    dstream.map(_.value)
      .filter(record => {
        // delimiter : ^\ 에 의해 데이터를 분리
        val splitLine = record.split("\u001C", -1)
        
        //Log.debug("[0] Input 0 " + splitLine(0))
        //Log.debug(s"[0] Input => $record")
        //Log.debug("[0] Input splitLine.length : " + splitLine.length )

        // 앞자리가 I, V 인 경우 EVENT 처리. B,C,D SKIP
        if (splitLine(0).equals("I") || splitLine(0).equals("V")) {

          // 앞 두자리가 IA, VA 인 경우 EVENT 처리
          if (splitLine(1).equals("A")) {

            // 데이터 3번째 항목이 테이블명이므로 항목수가 최소 3개 이상이어야 정상.
            if (splitLine.length > 2) {

              // swing ogg 실시간 테이블 목록에 존재하면 처리
              if ( headers.contains(splitLine(2)) ) {  //headers.exists(_.equals(splitLine(2)))
                // 3번째컬럼 즉, 테이블명을 가지고 header정보를 가져옴
                header = headers(splitLine(2))
                
                // 헤더갯수가 데이터갯수와 같으면 정상, 데이터갯수가 많으면 헤더수만큼만 파싱 그외는 오류
                if (splitLine.size < header.size) {
                  Log.warn("[1] Error : header count large ( " + splitLine(2) + " hcnt : " + header.size + ", dcnt : " + splitLine.size + " => " + record)
                  false
                } else if (splitLine.size > header.size) {
                  Log.warn("[1] Warnning : data count large ( " + splitLine(2) + " hcnt : " + header.size + ", dcnt : " + splitLine.size + " => " + record)
                  true
                } else {
                  true
                }
              } else {
                // swing ogg 테이블 목록에 미존재 SKIP
                //Log.warn("[1] Skip table : - " + splitLine(2) + " is not in RealTime Ogg TableList => " + record)
                false
              }
            } else {
              // 항목수가 최소 3개 미만이면 오류 데이터 SKIP
              Log.warn("[1] Error too short column => " + record)
              false
            }

          } else {
            // 앞 두자리가 IA, VA 아니면 SKIP. IB, VB
            // Log.debug("[1] Skip IB, VB data => " + record)
            false
          }

        } else if (splitLine(0).equals("B") || splitLine(0).equals("C") || splitLine(0).equals("D")) {
          // 앞 두자리가 B,C,D 경우 SKIP 처리
          // Log.debug("[1] Skip B,C,D data => " + record)
          false
        } else {
          // 앞 두자리가 I,V,B,C,D 가 아닌 경우 SKIP 처리. 오류건발생.
          Log.warn("[1] Error DATA occurs => " + record)
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
      // 컬럼명->컬럼값 형태의 map으로 치환
      .map(SwingData(_))
      // cep전송여부등의 header정보 생성
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
              //Log.debug(s"[3] Send CEP => $record")
            }
            producer.send(new ProducerRecord(saveTopic, record.toJsonString.getBytes))
            //Log.debug(s"[4] Send SAVE => $record")
          })
        })
      })
  }

}

/**
  * SwingStreamApp
  */
object SwingStreamApp {
  def main(args: Array[String]): Unit = {
    new SwingStreamApp(ConfigManager(getClass.getSimpleName.stripSuffix("$"))).start
  }
}
