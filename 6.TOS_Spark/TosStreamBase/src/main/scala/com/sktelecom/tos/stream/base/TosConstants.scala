package com.sktelecom.tos.stream.base

/**
 * TosStream 상수 object
 */
object TosConstants {
  
  /**
   * 원천 소스 명 object
   */
  object SrcSysName {

    // swing
    final val SWING = "SWG"

    // r2k
    final val R2K = "R2K"

    // bill letter
    final val BILL_LETTER = "BLT"

    // trms
    final val TRMS = "TRM"

    // urt
    final val URT = "URT"

    // t-world
    final val T_WORLD = "TWO"

    // t-world direct
    final val T_WORLD_DIRECT = "TWD"

    // membership
    final val MEMBER_SHIP = "MEM"

  }

  /**
   * Json Header
   */
  object JsonHeader {
    // 접촉일자
    final val CONT_DT = "CONT_DT"
    // 통합접촉이벤트ID
    final val INTEG_CONT_EVT_ID = "INTEG_CONT_EVT_ID"
    // 접촉식별구분코드
    final val CONT_IDNT_CL_CD = "CONT_IDNT_CL_CD"
    // 접촉식별구분값
    final val CONT_IDNT_CL_VAL = "CONT_IDNT_CL_VAL"
    // 접촉일련번호
    final val INS_SER_NUM = "INS_SER_NUM"
    // 최종변경일시
    final val AUDIT_DTM = "AUDIT_DTM"
    // 최종변경자ID
    final val AUDIT_ID = "AUDIT_ID"
    // 접촉일시
    final val CONT_DTM = "CONT_DTM"
    // 고객번호
    final val CUST_NUM = "CUST_NUM"
    // 서비스관리번호
    final val SVC_MGMT_NUM = "SVC_MGMT_NUM"
    // 계정번호
    final val ACNT_NUM = "ACNT_NUM"
    // TID
    final val TID = "TID"
    // TID번호
    final val T_ID_NUM = "T_ID_NUM"
    // 멤버십카드번호1
    final val MBR_CARD_NUM1 = "MBR_CARD_NUM1"
    // 외부식별번호
    final val OUT_IDNT_NUM = "OUT_IDNT_NUM"
    // 통합접촉구분코드
    final val INTEG_CONT_CL_CD = "INTEG_CONT_CL_CD"
    // 통합접촉업무구분코드
    final val INTEG_CONT_OP_CL_CD = "INTEG_CONT_OP_CL_CD"
    // 통합접촉사유코드
    final val INTEG_CONT_RSN_CD = "INTEG_CONT_RSN_CD"
    // 캠페인번호
    final val CMPGN_NUM = "CMPGN_NUM"
    // 원천시스템구분코드
    final val SRC_SYS_CL_CD = "SRC_SYS_CL_CD"
    // 원천소유자명
    final val SRC_OWNR_NM = "SRC_OWNR_NM"
    // 원천대상명
    final val SRC_OBJ_NM = "SRC_OBJ_NM"
    // 원천변경코드값
    final val SRC_CHG_CD_VAL = "SRC_CHG_CD_VAL"
    // 원천사유코드값
    final val SRC_RSN_CD_VAL = "SRC_RSN_CD_VAL"
    // 원천접촉그룹ID
    final val SRC_CONT_GRP_ID = "SRC_CONT_GRP_ID"
    // 원천일련번호
    final val SRC_SER_NUM = "SRC_SER_NUM"
    // 원천상세일련번호
    final val SRC_DTL_SER_NUM = "SRC_DTL_SER_NUM"
    // 채널접촉수단코드
    final val CHNL_CONT_WAY_CD = "CHNL_CONT_WAY_CD"
    // 회사구분코드
    final val CO_CL_CD = "CO_CL_CD"
    // 서비스구분코드
    final val SVC_CD = "SVC_CD"
    // 접수순번
    final val RCV_SEQ = "RCV_SEQ"
    
    // TOS EVENT KEY
    final val TOS_EVENT_KEY = "TOS_EVENT_KEY"
    // TOS EVENT DTM
    final val TOS_EVENT_DTM = "TOS_EVENT_DTM"
    // DB SAVE 여부
    final val TOS_IS_DB_SAVE = "TOS_IS_DB_SAVE"
    // CEP 전송 여부
    final val TOS_IS_CEP_SEND = "TOS_IS_CEP_SEND"
    // 이벤트 매핑 시간
    final val TOS_SPARK_MAP_DTM = "TOS_SPARK_MAP_DTM"
    // kafka 전송 시간
    final val TOS_KAFKA_SEND_DTM = "TOS_KAFKA_SEND_DTM"
  }

}