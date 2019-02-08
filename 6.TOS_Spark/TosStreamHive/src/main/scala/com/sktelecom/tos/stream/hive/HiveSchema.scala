package com.sktelecom.tos.stream.hive

import com.sktelecom.tos.stream.base.Utils
import play.api.libs.json.Json
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsLookupResult
import com.sktelecom.tos.stream.base.TosConstants


/**
 * Hive Schema 정의
 *
 * @param CONT_DT 접촉일자
 * @param INTEG_CONT_EVT_ID 통합접촉이벤트 아이디
 * @param CONT_IDNT_CL_CD 접촉식별구분코드
 * @param CONT_IDNT_CL_VAL 접촉식별구분값
 * @param CONT_DTM 접촉일시
 * @param CUST_NUM 고객번호
 * @param SVC_MGMT_NUM 서비스관리번호
 * @param ACNT_NUM 계정번호
 * @param TID TID
 * @param T_ID_NUM TID번호
 * @param MBR_CARD_NUM1 멤버십카드번호1
 * @param OUT_IDNT_NUM 외부식별번호
 * @param INTEG_CONT_CL_CD 통합접촉구분코드
 * @param INTEG_CONT_OP_CL_CD 통합접촉업무구분코드
 * @param INTEG_CONT_RSN_CD 통합접촉사유코드
 * @param CMPGN_NUM 캠페인번호
 * @param SRC_SYS_CL_CD 원천시스템구분코드
 * @param SRC_OWNR_NM 원천소유자명
 * @param SRC_OBJ_NM 원천대상명
 * @param SRC_CHG_CD_VAL 원천변경코드값
 * @param SRC_RSN_CD_VAL 원천사유코드값
 * @param SRC_CONT_GRP_ID 원천접촉그룹ID
 * @param SRC_SER_NUM 원천일련번호
 * @param SRC_DTL_SER_NUM 원천상세일련번호
 * @param CHNL_CONT_WAY_CD 채널접촉수단코드
 * @param CO_CL_CD 회사구분코드
 * @param SVC_CD 서비스구분코드
 * @param RCV_SEQ 접수순번
 * @param TOS_EVENT_DTM TOS EVENT DTM
 * @param TOS_IS_DB_SAVE DB SAVE 여부
 * @param TOS_IS_CEP_SEND CEP 전송 여부
 * @param TOS_SPARK_MAP_DTM 이벤트 매핑 시간
 * @param TOS_KAFKA_SEND_DTM kafka 전송 시간
 * @param TOS_HIVE_SAVE_DTM HIVE 저장 시간
 * @param HEADER JSON HEADER
 * @param BODY JSON BODY
 * @param dt         hive dt 파티션
 * @param hh         hive hh 파티션
 */
case class HiveSchema(
  CONT_DT:             String,
  INTEG_CONT_EVT_ID:   String,
  CONT_IDNT_CL_CD:     String,
  CONT_IDNT_CL_VAL:    String,
  CONT_DTM:            String,
  CUST_NUM:            String,
  SVC_MGMT_NUM:        String,
  ACNT_NUM:            String,
  TID:                 String,
  T_ID_NUM:            String,
  MBR_CARD_NUM1:       String,
  OUT_IDNT_NUM:        String,
  INTEG_CONT_CL_CD:    String,
  INTEG_CONT_OP_CL_CD: String,
  INTEG_CONT_RSN_CD:   String,
  CMPGN_NUM:           String,
  SRC_SYS_CL_CD:       String,
  SRC_OWNR_NM:         String,
  SRC_OBJ_NM:          String,
  SRC_CHG_CD_VAL:      String,
  SRC_RSN_CD_VAL:      String,
  SRC_CONT_GRP_ID:     String,
  SRC_SER_NUM:         String,
  SRC_DTL_SER_NUM:     String,
  CHNL_CONT_WAY_CD:    String,
  CO_CL_CD:            String,
  SVC_CD:              String,
  RCV_SEQ:             String,
  TOS_EVENT_KEY:       String,
  TOS_EVENT_DTM:       String,
  TOS_IS_DB_SAVE:      String,
  TOS_IS_CEP_SEND:     String,
  TOS_SPARK_MAP_DTM:   String,
  TOS_KAFKA_SEND_DTM:  String,
  TOS_HIVE_SAVE_DTM:   String,
  HEADER:              String,
  BODY:                String,
  dt:                  String,
  hh:                  String) {

  override def toString = s"{HEADER: $HEADER\nBODY: $BODY}"

}

/**
 * companion object
 */
object HiveSchema {

  def apply(line: String): HiveSchema = {

    val jsonValue = Json.parse(line)

    val header = jsonValue \ "HEADER"
    val body = jsonValue \ "BODY"

    val eventDtm = getValue(header, TosConstants.JsonHeader.TOS_EVENT_DTM)

    val dt = eventDtm.substring(0, 8)
    val hh = eventDtm.substring(8, 10)
  
    new HiveSchema(
      getValue(header, TosConstants.JsonHeader.CONT_DT),
      getValue(header, TosConstants.JsonHeader.INTEG_CONT_EVT_ID),
      getValue(header, TosConstants.JsonHeader.CONT_IDNT_CL_CD),
      getValue(header, TosConstants.JsonHeader.CONT_IDNT_CL_VAL),
      getValue(header, TosConstants.JsonHeader.CONT_DTM),
      getValue(header, TosConstants.JsonHeader.CUST_NUM),
      getValue(header, TosConstants.JsonHeader.SVC_MGMT_NUM),
      getValue(header, TosConstants.JsonHeader.ACNT_NUM),
      getValue(header, TosConstants.JsonHeader.TID),
      getValue(header, TosConstants.JsonHeader.T_ID_NUM),
      getValue(header, TosConstants.JsonHeader.MBR_CARD_NUM1),
      getValue(header, TosConstants.JsonHeader.OUT_IDNT_NUM),
      getValue(header, TosConstants.JsonHeader.INTEG_CONT_CL_CD),
      getValue(header, TosConstants.JsonHeader.INTEG_CONT_OP_CL_CD),
      getValue(header, TosConstants.JsonHeader.INTEG_CONT_RSN_CD),
      getValue(header, TosConstants.JsonHeader.CMPGN_NUM),
      getValue(header, TosConstants.JsonHeader.SRC_SYS_CL_CD),
      getValue(header, TosConstants.JsonHeader.SRC_OWNR_NM),
      getValue(header, TosConstants.JsonHeader.SRC_OBJ_NM),
      getValue(header, TosConstants.JsonHeader.SRC_CHG_CD_VAL),
      getValue(header, TosConstants.JsonHeader.SRC_RSN_CD_VAL),
      getValue(header, TosConstants.JsonHeader.SRC_CONT_GRP_ID),
      getValue(header, TosConstants.JsonHeader.SRC_SER_NUM),
      getValue(header, TosConstants.JsonHeader.SRC_DTL_SER_NUM),
      getValue(header, TosConstants.JsonHeader.CHNL_CONT_WAY_CD),
      getValue(header, TosConstants.JsonHeader.CO_CL_CD),
      getValue(header, TosConstants.JsonHeader.SVC_CD),
      getValue(header, TosConstants.JsonHeader.RCV_SEQ),
      getValue(header, TosConstants.JsonHeader.TOS_EVENT_KEY),
      getValue(header, TosConstants.JsonHeader.TOS_EVENT_DTM),
      getValue(header, TosConstants.JsonHeader.TOS_IS_DB_SAVE),
      getValue(header, TosConstants.JsonHeader.TOS_IS_CEP_SEND),
      getValue(header, TosConstants.JsonHeader.TOS_SPARK_MAP_DTM),
      getValue(header, TosConstants.JsonHeader.TOS_KAFKA_SEND_DTM),
      Utils.getDate(),
      header.get.toString(),
      body.get.toString(),
      dt,
      hh)

  }

  private def getValue(json: JsLookupResult, key: String): String = {
    val value = json \ key
    if (value.isDefined) {
      if (value.get.isInstanceOf[JsBoolean]) {
        if (value.as[Boolean]) "Y" else "N"
      } else {
        value.as[String]
      }
    } else {
      null
    }
  }

}