package com.sktelecom.tos.stream.base.meta

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.apache.log4j.Logger

import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.meta.info.MetaInfo
import com.sktelecom.tos.stream.base.Utils
import org.apache.commons.lang3.time.FastDateFormat

/**
 * TODO : 사이드 이펙트 제거, match case 적용, tuple 우아한 적용 필요함
 * StreamData로 이벤트 매핑을 하는 클래스
 *
 * @param metaInfo event mapping 정보와 cep 전송 여부, cep 전송 포맷을 가지고 있는 클래스
 */
class EventMapper(metaInfo: MetaInfo) extends Serializable {

  @transient lazy val Log = Logger.getLogger(getClass)

  /**
   * StreamData로 이벤트 매핑을 한다.
   * - OBJ_NAME으로 eventType 을 조회한다.
   * - eventType 에 맞는 테이블에서 데이터를 조회한다.
   *
   * @param streamData log 데이터가 파싱되어 저장된 클래스
   * @return 이벤트 코드, 사유 코드가 추가된 streamData
   */
  def getEventInfo(streamData: StreamData): StreamData = {

    val eventType = eventMappingType(streamData)

    streamData.sparkMappingDt = Utils.getDate()

    if (eventType != None) {

      if (eventType.get.equals("R")) { // 통합접촉사유명세(TCIC_INTEG_CONT_RSN_SPC) 이용하여 이벤트 매핑
        eventReason(streamData)
      } else if (eventType.get.equals("C")) { // 원천코드이벤트매핑(TCIC_SRC_CD_EVT_MAPP) 이용하여 이벤트 매핑
        eventCodeMapping(streamData)
      } else if (eventType.get.equals("G")) { // 통합접촉사유명세(TCIC_INTEG_CONT_RSN_SPC) 매핑 후 통합접촉원천필터상세(TCIC_INTEG_CONT_SRC_FLT_DTL) 이용하여 이벤트 매핑
        eventReasonFilter(streamData)
      } else if (eventType.get.equals("F")) { // 통합접촉원천필터상세(TCIC_INTEG_CONT_SRC_FLT_DTL) 매핑 후  통합접촉사유명세(TCIC_INTEG_CONT_RSN_SPC) 이용하여 이벤트 매핑
        eventFilterReason(streamData)
      } else if (eventType.get.equals("U")) { // 원천URL이벤트매핑(TCIC_SRC_URL_EVT_MAPP) 이용하여 이벤트 매핑
        eventUrl(streamData)
      } else {
        Log.info("eventType is '" + eventType + "' -> " + streamData.srcObjName)
      }
    } else {
      Log.info("eventType is None -> " + streamData.srcObjName)
    }

    checkCepSend(streamData)

    streamData

  }

  /**
   * event mapping type 을 조회한다.
   *
   * @param srcSysName 원천 시스템 구분
   * @param srcObjName 원천 대상 명
   * @return event mapping type Option[String]
   */
  private def eventMappingType(streamData: StreamData): Option[String] = {
    val data = metaInfo.objNameSpec.value.get(streamData.mainKey)
    if (data != None) {
      Option(data.get._1)
    } else {
      None
    }
  }

  /**
   * 이벤트 사유에 따른 이벤트 매핑을 한다.
   *
   * @param streamData log 데이터가 파싱되어 저장된 클래스
   */
  private def eventReason(streamData: StreamData): StreamData = {

    val eventReasonMap = metaInfo.eventReasonSpec.value.get(streamData.mainKey)

    if (eventReasonMap != None) {
      breakable {
        eventReasonMap.foreach(changeItemMap => {
          changeItemMap.foreach(changeItemInfo => {
            if (changeItemInfo._1._2.equals("**") || changeItemInfo._1._2.equals(streamData(changeItemInfo._1._1))) {
              changeItemInfo._2.foreach(rsnItemInfo => {
                if (rsnItemInfo._1._2.equals(streamData(rsnItemInfo._1._1))) {
                  streamData.srcChangeCode = changeItemInfo._1._2
                  streamData.srcReasonCode = rsnItemInfo._1._2
                  streamData.integEventId = rsnItemInfo._2._1
                  streamData.integReasonCode = rsnItemInfo._2._2
                  streamData.integDivCode = rsnItemInfo._2._3
                  streamData.integBizCode = rsnItemInfo._2._4
                  streamData.isDbSave = true
                  break;
                }
              })
            }
          })
        })
      }
    }

    streamData
  }

  /**
   * 이벤트 코드에 따른 이벤트 매핑을 한다.
   *
   * @param streamData log 데이터가 파싱되어 저장된 클래스
   */
  private def eventCodeMapping(streamData: StreamData): StreamData = {

    val eventCodeMappingMap = metaInfo.eventCodeMappingSpec.value.get(streamData.mainKey)

    if (eventCodeMappingMap != None) {
      breakable {
        eventCodeMappingMap.foreach(changeItemMap => {
          changeItemMap.foreach(item1Info => {
            if (item1Info._1._2.equals(streamData(item1Info._1._1))) {
              item1Info._2.foreach(item2Info => {
                if (item2Info._1._2.equals("**") || item2Info._1._2.equals(streamData(item2Info._1._1))) {
                  // 기본 값 **
                  streamData.srcChangeCode = "**"
                  streamData.srcReasonCode = item2Info._1._2
                  streamData.integEventId = item2Info._2._1
                  streamData.integReasonCode = item2Info._2._2
                  streamData.integDivCode = item2Info._2._3
                  streamData.integBizCode = item2Info._2._4
                  streamData.isDbSave = true
                  break;
                }
              })
            }
          })
        })
      }
    }

    streamData
  }
  
  /**
   * TODO : 소스 코드 정규화 필요함
   * 통합접촉사유명세(TCIC_INTEG_CONT_RSN_SPC) 매핑 후
   * 통합접촉원천필터상세TCIC_INTEG_CONT_SRC_FLT_DTL)를 이용하여 이벤트 매핑
   *
   * @param streamData log 데이터가 파싱되어 저장된 클래스
   */
  private def eventReasonFilter(streamData: StreamData): StreamData = {

    eventReason(streamData)

    var isFilter = false

    if (!streamData.integEventId.equals("")) {
      val eventFilterMap = metaInfo.eventFilterSpec.value.get(streamData.mainKey)

      if (eventFilterMap != None) {

        var filterSeq = eventFilterMap.get((streamData.integEventId, "**"))
        if (filterSeq == None) {
          eventFilterMap.get((streamData.integEventId, streamData.integReasonCode))
        }
        
        if (filterSeq != None) {
          breakable {
            filterSeq.foreach(filter => {
              var pass = false
              val checkValue =  if(filter._3.equals("N")) filter._4 else filter._5(streamData.rawMap)
              if (filter._2.equals("EQ")) {                               
                pass = streamData.getBody(filter._1).equals(checkValue)
              } else {               
                pass = !streamData.getBody(filter._1).equals(checkValue)
              }

              if (!pass) {
                break
              }
            })
            if (filterSeq.size > 0) {
              isFilter = true
            }
          }
        }

      }
    }

    if (!isFilter) {
      streamData.srcChangeCode = ""
      streamData.srcReasonCode = ""
      streamData.integEventId = ""
      streamData.integReasonCode = ""
      streamData.integDivCode = ""
      streamData.integBizCode = ""
      streamData.isDbSave = false
    }

    streamData
  }

  private def eventFilterReason(streamData: StreamData): StreamData = {
    Log.warn("eventFilterReason dev....")
    streamData
  }
  
  /**
   * TODO : TCIC_SRC_URL_EVT_MAPP 테이브 설계 완료 후 개발
   * 이벤트 URL에 따른 이벤트 매핑을 한다.
   *
   * @param streamData log 데이터가 파싱되어 저장된 클래스
   */
  private def eventUrl(streamData: StreamData): StreamData = {
    streamData
  }

  /**
   * cep 전송 여부를 확인한다.
   *
   * @param srcSysName 원천 시스템 명
   * @param eventId 이벤트 아이디
   * @param reasonCode 사유 코드
   */
  private def checkCepSend(streamData: StreamData): Unit = {
    
    // TCIC_CEP_TRMS_EVT_DTL 테이블 확인
    val isCepSend = (metaInfo.cepEvent.value.contains(streamData.mainKey + "_" + streamData.integReasonCode) || metaInfo.cepEvent.value.contains(streamData.mainKey + "_**"))       

    if (isCepSend) {
      
      // TCIC_SRC_OBJ_SPC 테이블 확인
      val cepFilterInfo = metaInfo.objNameSpec.value.get(streamData.mainKey)
      var isCepFilter = true
      if(cepFilterInfo != None && cepFilterInfo.get._2.equals("Y")) {
        isCepFilter = streamData.getBody(cepFilterInfo.get._3).equals(cepFilterInfo.get._4)
      }
      
      // CEP 전송 유효 시간 체크
      if (isCepFilter) {        
        val currentTime = streamData.sparkMappingDt.substring(8, 12).toInt
        streamData.isCepSend = checkCepSendTime(metaInfo.cepSendTime.value, currentTime)
      }
      
    }

  }

  /**
   * cep 전송 시간을 확인한다.
   *
   * @param cepSendTime cep 전송 유효 시간(0900 ~ 1800/이상 ~ 미만)
   * @param currentTime 현재 시간(HHmm)
   */
  private def checkCepSendTime(cepSendTime: (String, String), currentTime: Int): Boolean = {
    if (cepSendTime._1.toInt < cepSendTime._2.toInt) {
      ((cepSendTime._1.toInt <= currentTime) && (cepSendTime._2.toInt > currentTime))
    } else {
      ((cepSendTime._1.toInt <= currentTime) || (cepSendTime._2.toInt > currentTime))
    }
  }
}