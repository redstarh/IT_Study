package com.sktelecom.tos.stream.trms

import com.sktelecom.tos.stream.base.Utils
import com.sktelecom.tos.stream.base.data.StreamData

import scala.collection._
import com.sktelecom.tos.stream.base.TosConstants

/**
  * trms Data를 저장하는 클래스
  * @param srcObjName object 명
  * @param srcEventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자를 구분 할 수 있는 key)
  * @param srcEventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
  * @param rawLine  원본 line
  * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
  */
class TRMSData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 
  extends StreamData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 

/**
  * campanion object
  * trms raw 데이터를 key, value 의 map 에 저장하는 클래스
  */
object TRMSData {

  /**
    * trms
    *
    * @param rawLine trms raw log
    * @return TRMSData
    */
  def apply(rawLine: String): TRMSData = {

    // TODO : parse
    val srcObjName = "rawLine.srcObjName"
    val srcEventKey = "rawLine.srcEventKey"
    val srcEventDt = "rawLine.srcEventDt"
    
    var rawMap = immutable.Map.empty[String, String]

    rawMap += "SVC_MGMT_NO" -> srcEventKey
    rawMap += "CHG_DT" -> srcEventDt

    // TODO : inputFormat 전문에 맞게 변경
    val eventDt = Utils.getDate()

    new TRMSData(TosConstants.SrcSysName.TRMS, srcObjName, srcEventKey, eventDt, rawLine, rawMap)

  }
  
}