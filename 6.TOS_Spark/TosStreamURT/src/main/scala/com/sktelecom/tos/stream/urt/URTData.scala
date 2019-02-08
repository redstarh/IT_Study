package com.sktelecom.tos.stream.urt

import com.sktelecom.tos.stream.base.Utils
import com.sktelecom.tos.stream.base.data.StreamData

import scala.collection._
import com.sktelecom.tos.stream.base.TosConstants

import org.apache.log4j.Logger

/**
  * urt Data를 저장하는 클래스
  *
  * @param srcObjName object 명
  * @param srcEventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자를 구분 할 수 있는 key)
  * @param srcEventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
  * @param rawLine  원본 line
  * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
  */
class URTData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 
  extends StreamData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 

/**
  * campanion object
  * urt raw 데이터를 key, value 의 map 에 저장하는 클래스
  */
object URTData {

  /**
    * urt
    * URT_DATA_EXHST_LOG(URT데이터소진SMS전송로그)
    * @param rawLine urt raw log
    * @return URTData
    */
  def apply(rawLine: String): URTData = {

    val Log = Logger.getLogger(getClass)

    // raw data 를 구분자를 이용 split
    val line = rawLine.split("\\|", -1) // swing ogg delimiter : |

    // column name -> data 형태의 rawMap 생성
    var rawMap = immutable.Map.empty[String, String]

    rawMap += "SVC_MGMT_NUM" -> line(0)      //    SVC_MGMT_NUM	NUMBER(10)	서비스관리번호
    rawMap += "PRCPLN_PROD_ID" -> line(1)    //    PRCPLN_PROD_ID	VARCHAR2(10)	요금제상품ID
    rawMap += "EXHST_RT" -> line(2)  //    DATA_EXHST_RT_CD	VARCHAR2(3)	데이터소진율코드 (50,80,100)
    rawMap += "MSG_TR_DTM" -> line(3)        //    MSG_TR_DTM	VARCHAR2(14)	메시지전송일자(데이터 소진일시)

    // oracle debug
//    var ora_cols:String = ""
//    ora_cols += ", '" + line(0) + "' AS SVC_MGMT_NUM"
//    ora_cols += ", '" + line(1) + "' AS PRCPLN_PROD_ID"
//    ora_cols += ", '" + line(2) + "' AS EXHST_RT"
//    ora_cols += ", '" + line(3) + "' AS MSG_TR_DTM"
//    Log.debug(s"[2] ora_cols => $ora_cols")

    // srcObjName 추출
    val srcObjName = "URT_DATA_EXHST_LOG"

    // srcEventKey 지정
    val srcEventKey = "SVC_MGMT_NUM"
      
    // eventDt 추출
    val srcEventDt = line(3)
   
    var eventDt:String = null

    if (srcEventDt != null) {
      if (srcEventDt.length() == 8 ) eventDt = Utils.convertToDate(srcEventDt + "000000", "yyyyMMddHHmmss")
      else eventDt = Utils.convertToDate(srcEventDt, "yyyyMMddHHmmss")
      //Log.debug("[2] srcEventDt : "+ srcEventDt)
    } else {
      Log.warn("[2] srcEventDt is null : "+ srcEventDt)
      eventDt = "19000101000000.00"
    }
    
    // Oracle 저장을 위한 Header 값 추가
    // 아래 9개 필드는 event 매핑시 공통 모듈에서 세팅하니 제외하고 서비스에 맞는 값들만 세팅.
    // INTEG_CONT_EVT_ID, INTEG_CONT_CL_CD, INTEG_CONT_OP_CL_CD, INTEG_CONT_RSN_CD, SRC_SYS_CL_CD,  SRC_OWNR_NM, SRC_OBJ_NM, SRC_CHG_CD_VAL, SRC_RSN_CD_VAL
    
    val urtData = new URTData(TosConstants.SrcSysName.URT, srcObjName, srcEventKey, eventDt, rawLine, rawMap)

      urtData.addHeader(TosConstants.JsonHeader.CONT_DT, urtData.getBody("MSG_TR_DTM").substring(0, 8)) // 접촉일자(메시지전송일시)
      urtData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                   // 접촉식별구분코드(default setting)
      urtData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, urtData.getBody("SVC_MGMT_NUM"))      // 접촉식별구분값(서비스관리번호)
      urtData.addHeader(TosConstants.JsonHeader.CONT_DTM, urtData.getBody("MSG_TR_DTM"))                // 접촉일시(메시지전송일시)
      urtData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, urtData.getBody("SVC_MGMT_NUM"))          // 서비스관리번호(서비스관리번호)
      
      urtData
    
  }
}


