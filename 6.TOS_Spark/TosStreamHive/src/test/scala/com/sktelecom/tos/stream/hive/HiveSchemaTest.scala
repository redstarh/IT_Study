package com.sktelecom.tos.stream.hive

import org.junit.Test
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.JsLookupResult
import play.api.libs.json.JsBoolean
import com.sktelecom.tos.stream.base.TosConstants

class HiveSchemaTest {

  @Test
  def saveJsonParseTest(): Unit = {

    val jsonString = """
          {
            "HEADER" : {
              "SRC_SER_NUM" : "",
              "TOS_IS_CEP_SEND" : true,
              "SRC_RSN_CD_VAL" : "**",
              "MBR_CARD_NUM1" : "2832935872",
              "SRC_CHG_CD_VAL" : "00",
              "TOS_SPARK_MAP_DTM" : "20181017161515.073",
              "TOS_EVENT_DTM" : "20181017161346.000",
              "INTEG_CONT_RSN_CD" : "01",
              "INTEG_CONT_EVT_ID" : "ER0001",
              "CONT_IDNT_CL_VAL" : "2832935872201320",
              "SRC_OWNR_NM" : "**",
              "SRC_SYS_CL_CD" : "R2K",
              "TOS_EVENT_KEY" : "2832935872201320",
              "INTEG_CONT_CL_CD" : "MRK",
              "CONT_DTM" : "20181017161346",
              "CONT_DT" : "20181017",
              "TOS_KAFKA_SEND_DTM" : "20181017161515.074",
              "SRC_OBJ_NM" : "R2K_ONLINE_LOG",
              "INTEG_CONT_OP_CL_CD" : "22",
              "CONT_IDNT_CL_CD" : "M",
              "TOS_IS_DB_SAVE" : true
            },
            "BODY" : {
              "JOIN_NUM2" : "1001",
              "EXP_CODE" : "G",
              "PAY_AMT" : "0000000825",
              "MIL_POINT" : "0000090575",
              "ACK_NUM" : "17244952",
              "SKT_CARDNO" : "2832935872201320",
              "USE_AMT" : "000000001650",
              "DIS_AMT" : "00000825",
              "VAN_UNQCD" : "67",
              "TRML_NUM" : "1000000000",
              "VAN_UNQNO" : "0000000000",
              "RSP_CODE" : "00",
              "JOIN_NUM1" : "V706",
              "USE_TYPE" : "000010",
              "MSG_TYPE" : "0210",
              "SND_TIME" : "20181017161346",
              "RBP_DCFLAG" : "M",
              "GOODS_CD" : "1001"
            }
          }
      """

    val jsonValue = Json.parse(jsonString).as[JsValue]

    val header = jsonValue \ "HEADER"
    val body = jsonValue \ "BODY"

    println(header.get.toString())
    println(body.get.toString())
    
  }


}