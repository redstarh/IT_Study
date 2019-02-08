package com.sktelecom.tos.stream.base.meta

import scala.collection.immutable

import org.apache.log4j.Logger

import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.meta.info.MetaInfo

import play.api.libs.json.JsString
import play.api.libs.json.Json

/**
 * StreamData를 cep 에 보낼 json 포멧의 데이터로 변환하는 클래스
 *
 * @param metaInfo event mapping 정보와 cep 전송 여부, cep 전송 포맷을 가지고 있는 클래스
 */
class CEPFormatter(metaInfo: MetaInfo) extends Serializable {

  @transient lazy val Log = Logger.getLogger(getClass)
  
  // 통합접촉이벤트ID
  private final val INTEG_CONT_EVT_ID = "INTEG_CONT_EVT_ID"
  // 통합접촉사유항목코드
  private final val INTEG_CONT_RSN_CD = "INTEG_CONT_RSN_CD"
  // 통합접촉이벤트발생일시
  private final val INTEG_CONT_EVT_DTM = "INTEG_CONT_EVT_DTM"
  // 원천시스템구분코드
  private final val SRC_SYS_CL_CD = "SRC_SYS_CL_CD"
  // 채널접촉수단코드
  private final val CHNL_CONT_WAY_CD = "CHNL_CONT_WAY_CD"

  /**
   * StreamData 를 output format 의 json 데이터로 변환한다.
   *
   * @param streamData rawData 가 저장된 클래스
   * @return json string
   */
  def convertToJson(streamData: StreamData): String = {

    var cepEventFormat = metaInfo.cepEventFormat.value.get(streamData.srcSysName + "_" + streamData.integEventId + "_" + streamData.integReasonCode)

    if (cepEventFormat == None) {
      cepEventFormat = metaInfo.cepEventFormat.value.get(streamData.srcSysName + "_" + streamData.integEventId + "_**")
    }

    var convertData = immutable.Map[String, JsString]()

    convertData += SRC_SYS_CL_CD -> JsString(streamData.srcSysName)
    convertData += INTEG_CONT_EVT_ID -> JsString(streamData.integEventId)
    convertData += INTEG_CONT_RSN_CD -> JsString(streamData.integReasonCode)
    convertData += INTEG_CONT_EVT_DTM -> JsString(streamData.srcEventDt)
    
    // Channel 일 경우만 유효함
    if(!streamData.integChannelAccessType.equals("")) {
      convertData += CHNL_CONT_WAY_CD -> JsString(streamData.integChannelAccessType)
    }
    
    if (cepEventFormat != None) {
      cepEventFormat.get.foreach(record => {
        convertData += record._2 -> JsString(streamData(record._1))
      })
    } else {
      Log.warn("CEP Format data is None, eventId: " + streamData.integEventId + ", reasonCode:" + streamData.integReasonCode)
    }

    Json.toJson(convertData).toString()

  }

}