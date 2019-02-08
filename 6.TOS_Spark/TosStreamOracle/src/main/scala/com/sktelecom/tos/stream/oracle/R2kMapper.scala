package com.sktelecom.tos.stream.oracle

import org.apache.spark.internal.Logging

import scala.collection.mutable

@deprecated
abstract class Mapper {
  def map(source: Map[String, String], target: mutable.Map[String, String])
}

/**
  * R2K의 2개 메세지 매핑
  * 온라인, 오프라인
  */
@deprecated
object R2kMapper extends Mapper with Logging {

  /*
  TODO 포맷 변경 후의 실제 샘플 메시지로 업데이트
  {
    "header":{
      "srcEventDt":"20181010171455.000", "srcEventKey":"2833866650120323", "kafkaSendDt":"20181010171348.033",
      "reasonCode":"01",
      "srcSysName":"R2K",
      "eventId":"ER0001", "isCepSend":false,
      "srcObjName":"R2K_ONLINE_LOG", "isDbSave":true, "channelAccessType":"", "sparkMappingDt":""
      },
      "body":{
        "EXP_CODE":"S",
        "PAY_AMT":"0000000000",
        "MIL_POINT":"0000068785",
        "ACK_NUM":"00000000",
        "SKT_CARDNO":"2833866650120323",
        "USE_AMT":"000000000000",
        "DIS_AMT":"00000000",
        "VAN_UNQCD":"67",
        "TRML_NUM":"1000000000",
        "VAN_UNQNO":"8967240717",
        "RSP_CODE":"00",
        "JOIN_NUM1":"V274",
        "JOIN_NUM2":"1001",
        "USE_TYPE":"000020",
        "MSG_TYPE":"0210",
        "SND_TIME":"20181010171455",
        "RBP_DCFLAG":"M",
        "GOODS_CD":"0000"
        }
      }

   */
  /**
    * R2K의 2개 메세지 매핑
    * 온라인, 오프라인
    *
    * @param source
    * @param target
    */
  override def map(source: Map[String, String], target: mutable.Map[String, String]) {
    def mapField(from: String, to: String) {
      source.get(from) match {
        case Some(x) =>
          target += (to -> x)
        case None =>
      }
    }
    def mapFieldWithLen(from: String, to: String, len: Int) {
      source.get(from) match {
        case Some(x) =>
          target += (to -> x.substring(0, len))
        case None =>
      }
    }

    logInfo("R2K 매핑 적용")

    // TODO 값이 없을 경우, 에러 처리 적용. 현재는 필요한 값이 모두 들어오지 않고 있기 때문에 일단 보류.

    // 원천 필드를 활용한 매핑

    mapFieldWithLen("SND_TIME", "CONT_DT", 8)
    mapField("SND_TIME", "CONT_DTM")
    target += "CONT_IDNT_CL_CD" -> "M"
    mapField("SKT_CARDNO", "CONT_IDNT_CL_VAL")
    mapFieldWithLen("SKT_CARDNO", "MBR_CARD_NUM1", 10)

    // 원천코드이벤트매핑 필드를 활용한 매핑

    mapField("SRC_CD_VAL1", "SRC_RSN_CD_VAL")

/*
    source.get("SRC_OBJ_NM") match {
      case Some(x) =>
        target += "SRC_OWNR_NM" -> x.substring(0, 4)
        target += "SRC_OBJ_NM" -> x
      case None =>
    }
*/

  }

}
