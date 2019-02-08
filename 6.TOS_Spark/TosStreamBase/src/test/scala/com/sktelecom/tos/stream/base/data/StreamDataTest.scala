package com.sktelecom.tos.stream.base.data

import scala.collection.immutable
import scala.collection.mutable

import org.junit.Assert.assertEquals
import org.junit.Test

import com.sktelecom.tos.stream.base.Utils

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import org.apache.log4j.Logger
import org.junit.Ignore
import com.sktelecom.tos.stream.base.TosConstants

/**
 * StreamData TestCase
 */
class StreamDataTest {

  @Ignore
  @Test
  def toJsonString(): Unit = {

    val Log = Logger.getLogger(getClass)

    Log.info("testttt")
    val srcObjName = "ZORD.ZORD_CONT_HST"
    val srcEventKey = "eventKey"
    val srcEventDt = Utils.getDate()
    val rawLine = "CONT_GRP_ID|7294609244112456BSKCC004|CHG_DT|20180911|SER_NUM|16458663515|DTL_SER_NUM|4|AUDIT_ID|SKCC004|AUDIT_DTM|2018-09-11:11:24:56|SVC_CD|E|SVC_MGMT_NUM|7294609244|SVC_CHG_CD|A1|MAIN_CHG_YN|Y|REQR_CTZ_SER_NUM|174595646|REQR_CTZ_NUM_PIN|F760118100000|CHG_DTM|20180911112456"
    var rawMap = immutable.Map[String, String]()

    val splitRaw = rawLine.split("\\|", -1)
    splitRaw.grouped(2).foreach(data => {
      rawMap += data(0) -> data(1)
    })

    val streamData = new StreamData(TosConstants.SrcSysName.SWING, srcObjName, srcEventKey, srcEventDt, rawLine, rawMap)

    // Oracle 저장을 위한 Header 값 추가 예시 입니다.
    // 아래 9개 필드는 event 매핑시 공통 모듈에서 세팅하니 제외하고 서비스에 맞는 값들만 채워 주세요
    // INTEG_CONT_EVT_ID, INTEG_CONT_CL_CD, INTEG_CONT_OP_CL_CD, INTEG_CONT_RSN_CD, SRC_SYS_CL_CD,  SRC_OWNR_NM, SRC_OBJ_NM, SRC_CHG_CD_VAL, SRC_RSN_CD_VAL

    // 상수 등 정해진 값을 넣는 경우
    streamData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "C")

    // rawMap 에 등록된 데이터중에 찾아서 넣는 경우
    streamData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, streamData.getBody("CUST_NUM"))
    streamData.addHeader(TosConstants.JsonHeader.SRC_CONT_GRP_ID, streamData.getBody("CONT_GRP_ID"))
    streamData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, streamData.getBody("SER_NUM"))
    streamData.addHeader(TosConstants.JsonHeader.SRC_DTL_SER_NUM, streamData.getBody("DTL_SER_NUM"))
    streamData.addHeader(TosConstants.JsonHeader.CO_CL_CD, streamData.getBody("CO_CL_CD"))

    // rawMap 에 등록된 데이터중에 찾아서 가공 후 넣는 경우
    streamData.addHeader(TosConstants.JsonHeader.CONT_DT, streamData.getBody("AUDIT_DTM", { a => a.replaceAll("-", "").substring(0, 8) }))

    println(streamData.toJsonString());

    assert(Json.parse(streamData.toJsonString()).isInstanceOf[JsValue])

  }

  @Ignore
  @Test
  def jsonParseTest(): Unit = {
    val jsonString = """
      {
         "header" : {
           "srcSysName" : "SWG",
           "srcObjName" : "ZORD.ZORD_ACNT_CHG_HST",
           "srcEventDt" : "20180913132446.372",
           "srcEventKey" : "eventKey",
           "eventId" : "",
           "isCepSend" : false,
           "isDbSave" : false,
           "kafkaSendDt" : "20180913132446.590",
           "sparkMappingDt" : ""
         },
         "body" : {
           "CONT_GRP_ID" : "7294609244112456BSKCC004",
           "SVC_MGMT_NUM" : "7294609244",
           "REQR_CTZ_NUM_PIN" : "F760118100000",
           "DTL_SER_NUM" : "4"
         }
      }
      """

    val jsonValue = Json.parse(jsonString)

    val header = jsonValue("header")
    val body = jsonValue("body")

    assert(jsonValue.isInstanceOf[JsValue])
    assertEquals("20180913132446.372", header("srcEventDt").as[String])
    assert(body.as[Map[String, String]].isInstanceOf[Map[String, String]])
  }

}