package com.sktelecom.tos.stream.base.data

import scala.collection.immutable

import com.sktelecom.tos.stream.base.Utils

import play.api.libs.json.JsBoolean
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import com.sktelecom.tos.stream.base.TosConstants

/**
 * SWG, CHL, R2K, URT, TRMS 등의 수집된 Streaming Data 를 저장하는 클래스
 *
 * @param srcSysName 원천 시스템 명(SWG, R2K, BLT, TRM, URT, TWO, TWD, MEM)
 * @param srcObjName object 명(SWG - ZORD.ZORD_PSNINF_ITM_PRA_HST/ZORD.ZORD_CUST_INFO_PRA_HST/.., CHL - TB_WL_REFERER/TB_WL_URL_ACCESS/.., R2K - ONLINE/OFFLINE)
 * @param srcEventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자 unique key)
 * @param srcEventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
 * @param rawLine  원본 line
 * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
 */
case class StreamData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) extends Serializable {

  /**
   * Json Header 값
   */
  private var headerMap = immutable.Map[String, JsValue]()

  /**
   * 통합 접촉 이벤트 아이디 : EJ0001, EB0001, EM0001, ...
   */
  var integEventId: String = ""

  /**
   * 통합 접촉 사유 코드
   */
  var integReasonCode: String = ""

  /**
   * 통합 접촉 구분 코드
   */
  var integDivCode: String = ""

  /**
   * 통합 접촉 업무 구분 코드
   */
  var integBizCode: String = ""

  /**
   * 원천 변경 코드
   */
  var srcChangeCode: String = ""

  /**
   * 원천 변경 사유
   */
  var srcReasonCode: String = ""

  /**
   * 채널 Access 구분(TWO, TWM, MEM 만 해당)
   *
   * <pre>
   * P : PC Web
   * M : Mobile Web
   * A : Mobile App
   * </pre>
   */
  var integChannelAccessType: String = ""

  /**
   * CEP 전송 여부
   */
  var isCepSend: Boolean = false

  /**
   * DB 저장 여부
   */
  var isDbSave: Boolean = false

  /**
   * spark mapping datetime (yyyyMMddHHmmss.SSS)
   */
  var sparkMappingDt: String = ""

  /**
   * kafka send datetime (yyyyMMddHHmmss.SSS)
   */
  var kafkaSendDt: String = ""

  /**
   * srcSysName과 srcObjName 을 '_' 로 합쳐 주며
   * MetaInfo 객체의 broadcast 변수에 주 key 로 사용된다.
   *
   * @return srcSysName_srcObjName
   */
  final def mainKey(): String = {
    srcSysName + "_" + srcObjName
  }

  /**
   * TODO : 삭제 예정
   * Map 조회
   *
   * @param key search key
   * @return
   */
  final def apply(key: String): String = {
    val value = rawMap.get(key)
    if (value != None) {
      value.get
    } else {
      ""
    }
  }

  /**
   * oracle 저장을 위한 header 값을 추가해 준다.
   *
   * <pre>
   * 아래 필드는 event 매핑시 공통 모듈에서 세팅
   * ------------------------
   * INTEG_CONT_EVT_ID
   * INTEG_CONT_CL_CD
   * INTEG_CONT_OP_CL_CD
   * INTEG_CONT_RSN_CD
   * SRC_SYS_CL_CD
   * SRC_OWNR_NM
   * SRC_OBJ_NM
   * SRC_CHG_CD_VAL
   * SRC_RSN_CD_VAL
   * ------------------------
   * </pre>
   */
  final def addHeader(header: (String, String)): Unit = {
    headerMap += header._1 -> JsString(header._2)
  }

  /**
   * Map 조회
   *
   * @param key rawData.get
   * @return
   */
  final def getBody(key: String): String = {
    val value = rawMap.get(key)
    if (value != None) {
      value.get
    } else {
      ""
    }
  }

  /**
   * rowMap 의 데이터를 추출하여 fn 함수를 실행한다.
   */
  final def getBody(srcKey: String, fn: (String) => String): String = {
    fn(getBody(srcKey))
  }

  /**
   * StreamData 에 담긴 데이터를 내부 통신용 Json 포맷으로 변경하며 규격은 아래와 같다.
   *
   * <pre>
   * Header 는 TosConstants.JsonHeader 참조
   *
   * {
   *   "HEADER" : {
   *     "INTEG_CONT_EVT_ID": "",
   *     "INTEG_CONT_CL_CD": "",
   *     "INTEG_CONT_OP_CL_CD": "",
   *     "INTEG_CONT_RSN_CD": "",
   *     "SRC_SYS_CL_CD": "",
   *     "SRC_OWNR_NM": "",
   *     "SRC_OBJ_NM": "",
   *     "SRC_CHG_CD_VAL": "",
   *     "SRC_RSN_CD_VAL": "",
   *     "TOS_EVENT_KEY": "1234567890",
   *     "TOS_EVENT_DTM": "yyyyMMddHHmmss.SSS",
   *     "TOS_IS_DB_SAVE": true/false,
   *     "TOS_IS_CEP_SEND": true/false,
   *     "TOS_SPARK_MAP_DTM": "yyyyMMddHHmmss.SSS",
   *     "TOS_KAFKA_SEND_DTM": "yyyyMMddHHmmss.SSS",
   *     "":"", ... 서비스 별 헤더 추가
   *   },
   *   "BODY" : {
   *     rawMap to json string
   *   }
   * }
   * </pre>
   *
   * @return json string
   */
  final def toJsonString(): String = {

    kafkaSendDt = Utils.getDate()

    var header = immutable.Map[String, JsValue]()

    header += TosConstants.JsonHeader.INTEG_CONT_EVT_ID -> JsString(integEventId)

    header += TosConstants.JsonHeader.INTEG_CONT_CL_CD -> JsString(integDivCode)
    header += TosConstants.JsonHeader.INTEG_CONT_OP_CL_CD -> JsString(integBizCode)
    header += TosConstants.JsonHeader.INTEG_CONT_RSN_CD -> JsString(integReasonCode)

    header += TosConstants.JsonHeader.SRC_SYS_CL_CD -> JsString(srcSysName)

    if (srcObjName.indexOf(".") > -1) {
      header += TosConstants.JsonHeader.SRC_OWNR_NM -> JsString(srcObjName.substring(0, srcObjName.indexOf(".")))
      header += TosConstants.JsonHeader.SRC_OBJ_NM -> JsString(srcObjName.substring(srcObjName.indexOf(".") + 1, srcObjName.length()))
    } else {
      header += TosConstants.JsonHeader.SRC_OWNR_NM -> JsString("**")
      header += TosConstants.JsonHeader.SRC_OBJ_NM -> JsString(srcObjName)
    }

    header += TosConstants.JsonHeader.SRC_CHG_CD_VAL -> JsString(srcChangeCode)
    header += TosConstants.JsonHeader.SRC_RSN_CD_VAL -> JsString(srcReasonCode)

    // 내부 공통..
    header += TosConstants.JsonHeader.TOS_EVENT_KEY -> JsString(srcEventKey)
    header += TosConstants.JsonHeader.TOS_EVENT_DTM -> JsString(srcEventDt)
    header += TosConstants.JsonHeader.TOS_IS_DB_SAVE -> JsBoolean(isDbSave)
    header += TosConstants.JsonHeader.TOS_IS_CEP_SEND -> JsBoolean(isCepSend)
    header += TosConstants.JsonHeader.TOS_SPARK_MAP_DTM -> JsString(sparkMappingDt)
    header += TosConstants.JsonHeader.TOS_KAFKA_SEND_DTM -> JsString(kafkaSendDt)

    var jsonMap = immutable.Map[String, JsValue]()

    jsonMap += "HEADER" -> Json.toJson(header ++ headerMap)
    jsonMap += "BODY" -> Json.toJson(rawMap)

    return Json.toJson(jsonMap).toString()
  }

  override def toString: String = {
    s"header : $integEventId|$integReasonCode|$integDivCode|$integBizCode|$srcChangeCode|$srcReasonCode|$integChannelAccessType|$isCepSend|$isDbSave|$sparkMappingDt|$kafkaSendDt\nhaderMap : $headerMap\nrawMap : $rawMap"
  }

}