package com.sktelecom.tos.stream.channel

import com.sktelecom.tos.stream.base.Utils
import com.sktelecom.tos.stream.base.data.StreamData

import scala.collection._
import com.sktelecom.tos.stream.base.TosConstants

/**
  * channel Data를 저장하는 클래스
  *
  * @param eventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자를 구분 할 수 있는 key)
  * @param eventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
  * @param rawLine  원본 line
  * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
  */
class ChannelData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 
  extends StreamData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 

/**
  * campanion object
  * channel raw 데이터를 key, value 의 map 에 저장하는 클래스
  */
object ChannelData {

  /**
    * channel
    *
    * @param rawLine channel raw log
    * @return ChannelData
    */
  def apply(rawLine: String): ChannelData = {

    // TODO : parse
    // rawLine.split/substring/..
    val svcMgmtNo = "rawLine.eventKey"
    val chgDt = "rawLine.chgDt"
    val logSysName = "rawLine.logSysName"
    val logAccessType = "rawLine.accessType"

    var rawMap = immutable.Map[String, String]()

    rawMap += "SVC_MGMT_NO" -> svcMgmtNo
    rawMap += "CHG_DT" -> chgDt

    // TODO : inputFormat 전문에 맞게 변경
    val eventDt = Utils.convertToDate(chgDt, "yyyyMMddHHmmss")
    val sparkInputDt = Utils.getDate()
    
    var srcSysName = "";
    
    // TODO : channel log 에 따라 srcSysName 을 알맞게 넣어 준다.
    if(logSysName.equals(TosConstants.SrcSysName.T_WORLD)) {
      srcSysName = TosConstants.SrcSysName.T_WORLD
    } else if (logSysName.equals(TosConstants.SrcSysName.T_WORLD_DIRECT)) { 
      srcSysName = TosConstants.SrcSysName.T_WORLD_DIRECT
    } else {
      srcSysName = TosConstants.SrcSysName.MEMBER_SHIP
    }
    
    var channelAccessType = ""
    
    // TODO : channel 은 log 를 확인하여 access type 을 넣어준다.
    if(logAccessType.equals("PC Web")) {
      channelAccessType = "P"
    } else if (logAccessType.equals("Mobile Web")) { 
      channelAccessType = "M"
    } else {  // Mobile App
      channelAccessType = "A"
    }

    val channelData = new ChannelData(srcSysName, "OBJ_NM", svcMgmtNo, eventDt, rawLine, rawMap)
    channelData.integChannelAccessType = channelAccessType

    channelData
    
  }
}


