package com.sktelecom.tos.stream.r2k

import com.sktelecom.tos.stream.base.{TosConstants, Utils}
import com.sktelecom.tos.stream.base.data.StreamData

import scala.collection._
import org.apache.log4j.Logger
import org.omg.PortableInterceptor.NON_EXISTENT



/**
  * r2k Data를 저장하는 클래스
  *
  * @param eventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자를 구분 할 수 있는 key)
  * @param eventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
  * @param rawLine  원본 line
  * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
  */
class R2KData(srcSysName:String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String])
  extends StreamData(srcSysName:String,srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String])


/**
  * campanion object
  * r2k raw 데이터를 key, value 의 map 에 저장하는 클래스
  */
object R2KData {

  /**
    * r2k
    *immutable
    * @param rawLine r2k raw log
    * @return R2KData
    */

  val Log = Logger.getLogger(getClass)


  def apply(rawLine: String): R2KData = {

  //   println("\n ** 원본 =" + rawLine)
    val header = rawLine.substring(0,32)

    val onoff = rawLine.substring(32,36).trim       //on/off 구분
    val offMsgtype = rawLine.substring(44,48).trim  //offline 일경우 전문구분 체크
    val body = rawLine.substring(32)

    var rawMap = immutable.Map.empty[String, String]

    //online
    if (onoff.equals("0210")) {
      rawMap += "MSG_TYPE" -> body.substring(0,4).trim          //전문구분
      rawMap += "USE_TYPE" -> body.substring(4,10).trim         //거래구분 코드 (00010 : 승인 요청시만 처리)                   | CONT_ITM1
      rawMap += "USE_AMT" -> body.substring(10,22).trim         //거래금액                                                    | CONT_ITM2
      rawMap += "SND_TIME" -> body.substring(22,36).trim        //전문전송일시                                                | CONT_ITM3
      rawMap += "SKT_CARDNO" -> body.substring(36,52).trim      //Skt 멤버쉽 카드번호 : Membership 번호                       | CONT_ITM4
      rawMap += "VAN_UNQCD"  -> body.substring(52,54).trim       //거래고유번호'nn' VAN사코드
      rawMap += "VAN_UNQNO" -> body.substring(54,64).trim        //거래 고유번호'nn'+VAN사고유번호
      rawMap += "TRML_NUM" -> body.substring(64,74).trim         //단말번호
      rawMap += "JOIN_NUM1" -> body.substring(74,78).trim        //가맹점 번호 (제휴사 4자리)                                 | CONT_ITM5
      rawMap += "JOIN_NUM2" -> body.substring(78,82).trim        //가맹점 번호 (가맹점 4자리)                                 | CONT_ITM6
                                                                 // * 2자리 (83~84) byte skip
      rawMap += "GOODS_CD"  -> body.substring(84,88).trim       //SpeedMate 상품유형 코드                                     | CONT_ITM7
      rawMap += "RSP_CODE" -> body.substring(88,90).trim        //응답 코드: 승인-->'00',한도초과 오류코드 '40~45', 기타 거절코드 : 2018.10.4 추가  | CONT_ITM8
      rawMap += "ACK_NUM" -> body.substring(90,98).trim         //승인 번호(8자리)                                                                | CONT_ITM9
      rawMap += "RBP_DCFLAG" -> body.substring(98,99).trim      //정산 멤버십 승인'M',사용'L',사용후 'R'                                          | CONT_ITM10
      rawMap += "EXP_CODE" -> body.substring(99,100).trim       //확장 코드 현재 ' ' // 'V’ : VIP 카드, ‘A’ :일반 카드,  ‘G’ :골드 카드, ‘ S’ :실버 카드 추가
      rawMap += "DIS_AMT" -> body.substring(100,108).trim       //할인금액 (거래금액 * 할인율) OR 고정거래금액                                    | CONT_ITM11
      rawMap += "PAY_AMT" -> body.substring(108,118).trim       //지불금액 (거래금액-할인금액)                                                   | CONT_ITM12
      rawMap += "MIL_POINT" -> body.substring(118,128).trim     //마일리지,잔여한도                                                             | CONT_ITM13

  /*  val srcRspcode = rawMap("RSP_CODE")
      val srcEventDt = rawMap("SND_TIME")
      val sendTimeExists =  rawMap.get("SND_TIME")
      println(s"${sendTimeExists.isDefined}")
  */
      //offline
    } else if (onoff.contains("ISO") && (offMsgtype.equals("0210")))  {
      rawMap += "ISO" -> body.substring(0,12).trim             //ISO 8583 Header
      rawMap += "MSG_TYPE" -> body.substring(12,16).trim        //전문구분 코드
      rawMap += "PRY_BITMAP" -> body.substring(16,32).trim       //Primary Bit Map
      rawMap += "USE_TYPE" -> body.substring(32,38).trim        //거래구분 코드 : 000010 : 승인 요청시만 처리
      rawMap += "USE_AMT"-> body.substring(38,50).trim         //거래금액
      rawMap += "SND_TIME" -> body.substring(50,64).trim       //전문 전송 일시
      rawMap += "VAN_CODE" -> body.substring(64,72).trim        //취급기관 코드
      rawMap += "TRK_PRE2" -> body.substring(72,74).trim        //Track 2 선두 "37"
      rawMap += "TRK_CARDCNO" -> body.substring(74,90).trim      //카드번호
      rawMap += "TRK_FIL" -> body.substring(90,111).trim         //기타 정보 후미
      rawMap += "VAN_UNQCD"  -> body.substring(111,113).trim      //거래고유번호'nn' VAN사코드
      rawMap += "VAN_UNQNO" -> body.substring(113,123).trim       //거래 고유번호'nn'+VAN사고유번호
      rawMap += "RSP_CODE" -> body.substring(123,125).trim        //응답 코드: 승인-->'00',한도초과 오류코드 '40~45', 기타 거절코드 : 2018.10.4 추가
      rawMap += "TRML_NUM" -> body.substring(125,135).trim        //단말번호
      rawMap += "JOIN_NUM1" -> body.substring(135,139).trim        //가맹점 번호 (제휴사 4자리)
      rawMap += "JOIN_NUM2" -> body.substring(139,143).trim        //가맹점 번호 (가맹점 4자리)
                                                                    // * 2자리 (144~145) byte skip
      rawMap += "SKT_CARDNO"  -> body.substring(145,161).trim       //Skt 멤버쉽 카드번호 : Membership 번호
      rawMap += "GOODS_CD"  -> body.substring(161,165).trim       //SpeedMate 상품코드
      rawMap += "ACK_NUM" -> body.substring(165,176).trim         //승인 번호(008+'승인번호')
      rawMap += "RBP_DCFLAG" -> body.substring(176,177).trim      //정산 멤버십 승인'M',사용'L',사용후 'R'
      rawMap += "EXP_CODE" -> body.substring(177,178).trim       //확장 코드 현재 ' ' // 'V’ : VIP 카드, ‘A’ :일반 카드,  ‘G’ :골드 카드, ‘ S’ :실버 카드 추가
      rawMap += "DIS_AMT" -> body.substring(178,186).trim       //할인금액 (거래금액 * 할인율) OR 고정거래금액
      rawMap += "PAY_AMT" -> body.substring(186,196).trim       //지불금액 (거래금액-할인금액)
      rawMap += "MIL_POINT" -> body.substring(196,206).trim     //마일리지,잔여한도
      rawMap += "EVT_TEXT" -> body.substring(206,226).trim      //이벤트 내용
      rawMap += "RST_HANDO" -> body.substring(226,236).trim     //한도초과시 잔여한도
      rawMap += "CARDS_DATE" -> body.substring(236,242).trim    //카드사용시작 가능일자
      rawMap += "IC_MCARD" -> body.substring(242,258).trim      //ic 멤버쉽 카드
      rawMap += "IC_DATA" -> body.substring(258,280).trim       //기타 데이터 거절코드
    }
    else {
      false
    }

    val srcEventKey = rawMap("SKT_CARDNO")  //  srcEventKey = "SKT Membership CARDNO"
    val srcEventDt = rawMap("SND_TIME")     //srcEventDt    전문 전송일시
    val srcRspcode = rawMap("RSP_CODE")     //srcRspcode   원천항목명 : 응답코드

    var eventDt:String = null
    eventDt = Utils.convertToDate(srcEventDt, "yyyyMMddHHmmss")

    var srcObjName:String =null             //srcObjName = TCIC_INTEG_CONT_HST.SRC_OBJ_NM 원천대상명 (통합접촉이력)

    if  (onoff.contains("0210")) {
      srcObjName ="R2K_ONLINE_LOG"
    }else if (onoff.contains("ISO") && (offMsgtype.contains("0210"))) {
      srcObjName ="R2K_OFFLINE_LOG"
    }
    Log.warn()
    Log.warn( if (srcObjName.equals("R2K_ONLINE_LOG")) {s"========= Parsing :  $srcObjName ($onoff + $srcRspcode, $eventDt, $srcEventKey), header/rawMap : $header, $rawMap =====================================" }
              else if(srcObjName.equals("R2K_OFFLINE_LOG")) {s"========= Parsing :  $srcObjName ($onoff + $offMsgtype + $srcRspcode, $eventDt, $srcEventKey), header/rawMap : $header, $rawMap =====================================" })


  /*   oracle 저장 header 추가
     공통모듈 매핑 제외된 r2k 매핑 값들
  */
    val r2kData = new R2KData(TosConstants.SrcSysName.R2K, srcObjName, srcEventKey, eventDt, rawLine, rawMap)

  // rawMap 에 등록된 데이터중에 찾아서 가공 후 넣는 경우
    r2kData.addHeader(TosConstants.JsonHeader.CONT_DT, r2kData.getBody("SND_TIME", { a => a.replaceAll("-", "").substring(0, 8) })) //접촉일자
  // 상수 등 정해진 값을 넣는 경우
    r2kData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "M")                                                 // 접촉식별구분 코드
  // rawMap 에 등록된 데이터중에 찾아서 넣는 경우
    r2kData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, r2kData.getBody("SKT_CARDNO"))                //접촉식별 구분값
    r2kData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, r2kData.getBody("SER_NUM"))                        // 원천일련번호(입력일련번호)
    r2kData.addHeader(TosConstants.JsonHeader.CONT_DTM, r2kData.getBody("SND_TIME"))                          //접촉일시
  // rawMap 에 등록된 데이터중에 찾아서 가공 후 넣는 경우
    r2kData.addHeader(TosConstants.JsonHeader.MBR_CARD_NUM1, r2kData.getBody("SKT_CARDNO", { a => a.replaceAll("-", "").substring(0, 10) })) //멤버쉽 카드번호 1

  // object return
    r2kData
  }

}


