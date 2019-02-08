package com.sktelecom.tos.stream.swing

import com.sktelecom.tos.stream.base.Utils
import com.sktelecom.tos.stream.base.data.StreamData

import scala.collection._
import com.sktelecom.tos.stream.base.TosConstants
import org.apache.log4j.Logger

/**
  * Swing Data를 저장하는 클래스
  * @param srcObjName object 명
  * @param srcEventKey 이벤트 key(svcMgmtNum, 카드 번호 등 사용자를 구분 할 수 있는 key)
  * @param srcEventDt  이벤트 발생 시간(yyyyMMddHHmmss.SSS)
  * @param rawLine  원본 line
  * @param rawMap   raw 데이터를 key, value 형태로 추출한 Map
  */
class SwingData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 
  extends StreamData(srcSysName: String, srcObjName: String, srcEventKey: String, srcEventDt: String, rawLine: String, rawMap: immutable.Map[String, String]) 

/**
  * campanion object
  * swing raw 데이터를 key, value 의 map 에 저장하는 클래스
  */
object SwingData {

  /**
    * swing raw 데이터를 아래 규격에 맞게 자른 후 key, value 의 map 에 저장
    * swing 포맷 : <처리유형, I(insert)/D(delete)/V(Update)>,<B(before)/A(after)>,<테이블명>,<commit time stamp>,<컬럼 데이터>,...반복
    * IA,VA 만 처리 나머지는 SKIP
    * 
    * @param rawLine swing raw log
    * @return SwingData
    */

  val Log = Logger.getLogger(getClass)

  def apply(rawLine: String): SwingData = {


    // Swing 테이블별 컬럼명 구조체 선언
    val headers: Map[String, List[String]] = Map(
      "ZORD.ZORD_PSNINF_ITM_PRA_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "PSNINF_PAGR_ITM_CD", "OP_DTM", "AUDIT_ID", "AUDIT_DTM", "CUST_NUM", "PSNINF_PAGR_FRMT_TYP_CD", "PSNINF_PAGR_FRMT_VER_NUM", "PSNINF_PAGR_CHNL_CD", "PSNINF_PAGR_ITM_VER_NUM", "PSNINF_PAGR_CL_CD", "INFO_PRA_AGR_OP_CHG_CD", "OPR_ID", "OP_SALE_ORG_ID"), // 개인정보항목활용이력
      "ZORD.ZORD_CUST_INFO_PRA_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "INFO_PRA_AGR_ITM_CD", "OP_DTM", "CUST_NUM", "INFO_PRA_AGR_YN", "INFO_PRA_AGR_OP_CHG_CD", "OP_AGN_ORG_ID", "OPR_ID", "AUDIT_ID", "AUDIT_DTM", "INFO_PRA_AGR_CURNT_CHG_CD", "INFO_PRA_AGR_CURNT_OP_DTM", "OK_AGREE_YN"), // 고객정보활용이력
      "ZORD.ZORD_CUST_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "CO_CL_CD", "CUST_NUM", "CUST_CHG_CD", "MAIN_CHG_YN", "CUST_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "CHG_DTM", "CNCL_DTM", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF"), // 고객변경이력
      "ZORD.ZORD_ACNT_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "ACNT_NUM", "CHG_DT", "ACNT_CHG_CD", "SER_NUM", "AUDIT_ID", "AUDIT_DTM", "MAIN_CHG_CD", "ACNT_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "CHG_DTM", "CNCL_DTM", "REQR_CL_CD", "REQR_NM", "REQR_CNTC_NUM", "REQR_REL_CD", "REQR_AG_LGRP_CTZ_NUM", "OP_SALE_ORG_ID", "REQ_SALE_ORG_ID", "REQ_SALE_BR_ORG_ID", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "OP_MEMO_ID", "SALE_CHNL_CL_CD", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50"), // 계정변경이력
      "ZORD.ZORD_SVC_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "SVC_CD", "SVC_MGMT_NUM", "SVC_CHG_CD", "MAIN_CHG_YN", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "CHG_DTM", "CNCL_DTM", "SALE_APRV_NUM", "SVC_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "MT_SVC_CL_CD", "MT_SVC_NUM", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20", "BCHG_ITM21", "ACHG_ITM21", "BCHG_ITM22", "ACHG_ITM22", "BCHG_ITM23", "ACHG_ITM23", "BCHG_ITM24", "ACHG_ITM24", "BCHG_ITM25", "ACHG_ITM25", "BCHG_ITM26", "ACHG_ITM26", "BCHG_ITM27", "ACHG_ITM27", "BCHG_ITM28", "ACHG_ITM28", "BCHG_ITM29", "ACHG_ITM29", "BCHG_ITM30", "ACHG_ITM30", "BCHG_ITM31", "ACHG_ITM31", "BCHG_ITM32", "ACHG_ITM32", "BCHG_ITM33", "ACHG_ITM33", "BCHG_ITM34", "ACHG_ITM34", "BCHG_ITM35", "ACHG_ITM35", "BCHG_ITM36", "ACHG_ITM36", "BCHG_ITM37", "ACHG_ITM37", "BCHG_ITM38", "ACHG_ITM38", "BCHG_ITM39", "ACHG_ITM39", "BCHG_ITM40", "ACHG_ITM40", "BCHG_ITM41", "ACHG_ITM41", "BCHG_ITM42", "ACHG_ITM42", "BCHG_ITM43", "ACHG_ITM43", "BCHG_ITM44", "ACHG_ITM44", "BCHG_ITM45", "ACHG_ITM45", "BCHG_ITM46", "ACHG_ITM46", "BCHG_ITM47", "ACHG_ITM47", "BCHG_ITM48", "ACHG_ITM48", "BCHG_ITM49", "ACHG_ITM49", "BCHG_ITM50", "ACHG_ITM50"), // 서비스변경이력
      "ZORD.ZORD_COPN_OP_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "COPN_ISUE_NUM", "EFF_END_DTM", "AUDIT_ID", "AUDIT_DTM", "COPN_OP_DTM", "SVC_MGMT_NUM", "CUST_NUM", "OP_SALE_ORG_ID", "OPR_ID", "EQP_MDL_CD", "EQP_SER_NUM", "COPN_OPER_ST_CD", "BF_COPN_OPER_ST_CD", "CNCL_YN"), // 쿠폰처리이력
      "ZORD.ZORD_DATA_GIFT_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "OP_DTM", "AUDIT_ID", "AUDIT_DTM", "BEFR_SVC_MGMT_NUM", "DATA_GIFT_OP_ST_CD", "GIFT_DATA_QTY", "OP_SALE_ORG_ID", "OPR_ID", "PROD_ID", "BEFR_PROD_ID", "CUST_NUM", "BEFR_CUST_NUM", "GIFT_IF_RSLT_CD", "DATA_GIFT_TYP_CD", "AFMLY_GIFT_YN", "GIFT_DAY_CL_CD", "RL_GIFT_DATA_QTY"), // 데이터선물이력
      "ZORD.ZORD_DATA_GIFT_ACHRG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "EFF_END_DTM", "AUTO_CHRG_OP_CD", "SER_NUM", "AUDIT_ID", "AUDIT_DTM", "EFF_STA_DTM", "APLY_STA_DT", "APLY_END_DT", "REQ_SALE_ORG_ID", "REQR_ID", "AUTO_CHRG_CYCL_CD", "CHRG_TM_CD", "CHRG_MD", "CHRG_SVC_MGMT_NUM", "CHRG_REQ_CTT", "CHRG_REQ_QTY", "CNCL_YN"), // 데이터선물자동충전이력
      "ZORD.ZORD_SUPL_SVC_COPN" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CARD_GIFT_NUM", "COPN_TYP_CD", "SVC_MGMT_NUM", "CARD_EXPIR_DT", "USE_YN", "RGST_CL_CD", "EFF_STA_DTM", "EFF_END_DTM", "SCRB_DT", "TERM_DT", "AUDIT_ID", "AUDIT_DTM", "OP_SALE_ORG_ID", "OPR_ID", "COPN_RL_GDS_CL_CD", "SALE_DTM", "RFND_DTM", "COPN_ISUE_NUM", "SELR_ID", "SALE_SALE_ORG_ID", "BUY_SVC_MGMT_NUM", "COPN_ISUE_DT", "COPN_OUT_DTM", "COPN_CO_CD", "COPN_CO_NM", "SYS_NC00026", "SYS_NC00027", "SYS_NC00028"), // 부가서비스쿠폰
      "ZMBR.ZMBR_MBR_CARD_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "MBR_CARD_NUM1", "CHG_DT", "OP_TM", "SVC_MGMT_NUM", "MBR_CHG_CD", "MBR_CHG_RSN_CD", "OP_ORG_ID", "DTL_CTT", "BCHG_MBR_CARD_NUM2", "BCHG_SVC_NUM", "BCHG_MBR_CUST_NM", "BCHG_MBR_ST_CD", "BCHG_MBR_TYP_CD", "BCHG_MBR_GR_CD", "BCHG_SVC_NOMINAL_REL_CD", "BCHG_SMS_AGREE_YN", "BCHG_OCB_ACCUM_AGREE_YN", "BCHG_SELF_AUTH_YN", "BCHG_ADDR_CD", "BCHG_INTEG_TXT_ADDR_ID", "BCHG_CNTC_NUM", "BCHG_CUST_EMAIL_ADDR", "BCHG_JOB_CD", "BCHG_JOB_KND_CD", "BCHG_SLNL_CD", "BCHG_BIRTH_DT", "BCHG_WEDD_ANNV_DT", "BCHG_HOBBY_CD1", "BCHG_HOBBY_CD2", "BCHG_HOBBY_CD3", "BCHG_MBR_REM_LMT_AMT", "BCHG_FMLY_CUST_NM", "ACHG_MBR_CARD_NUM2", "ACHG_SVC_NUM", "ACHG_MBR_CUST_NM", "ACHG_MBR_ST_CD", "ACHG_MBR_TYP_CD", "ACHG_MBR_GR_CD", "ACHG_SVC_NOMINAL_REL_CD", "ACHG_SMS_AGREE_YN", "ACHG_OCB_ACCUM_AGREE_YN", "ACHG_SELF_AUTH_YN", "ACHG_ADDR_CD", "ACHG_INTEG_TXT_ADDR_ID", "ACHG_CNTC_NUM", "ACHG_CUST_EMAIL_ADDR", "ACHG_JOB_CD", "ACHG_JOB_KND_CD", "ACHG_SLNL_CD", "ACHG_BIRTH_DT", "ACHG_WEDD_ANNV_DT", "ACHG_HOBBY_CD1", "ACHG_HOBBY_CD2", "ACHG_HOBBY_CD3", "ACHG_MBR_REM_LMT_AMT", "ACHG_FMLY_CUST_NM", "LEGAL_REPVE_NM", "LEGAL_REPVE_CNTC_NUM", "COLL_CHNL_CD", "AUDIT_ID", "AUDIT_DTM", "TRNSFR_SVC_NUM", "TRNSFE_SVC_NUM", "ACCUM_PT", "BCHG_T_TURPSS_AGR_YN", "ACHG_T_TURPSS_AGR_YN", "BCHG_MBR_FUNC_USE_YN", "ACHG_MBR_FUNC_USE_YN", "BCHG_MBR_CARD_IMG_NUM", "ACHG_MBR_CARD_IMG_NUM", "BCHG_MBR_GR_PLUS_YN", "ACHG_MBR_GR_PLUS_YN", "BCHG_MBR_CTZ_SER_NUM", "BCHG_MBR_CTZ_NUM_PINF", "BCHG_FMLY_CTZ_SER_NUM", "BCHG_FMLY_CTZ_NUM_PINF", "ACHG_MBR_CTZ_SER_NUM", "ACHG_MBR_CTZ_NUM_PINF", "ACHG_FMLY_CTZ_SER_NUM", "ACHG_FMLY_CTZ_NUM_PINF", "CTZ_NUM_AGREE_YN", "MKTG_AGREE_YN", "JNCARD_LINK_YN", "BCHG_CARD_ISUE_TYP_CD", "ACHG_CARD_ISUE_TYP_CD", "EMAIL_NEWSL_AGREE_YN", "EMAIL_PROD_AGREE_YN", "EMAIL_ETC_AGREE_YN", "SMS_MMS_AGREE_YN", "TM_RCV_YN", "BCHG_CTZ_NUM_AGREE_YN", "BCHG_MKTG_AGREE_YN", "BCHG_EMAIL_NEWSL_AGREE_YN", "BCHG_EMAIL_PROD_AGREE_YN", "BCHG_EMAIL_ETC_AGREE_YN", "BCHG_SMS_MMS_AGREE_YN", "BCHG_TM_RCV_YN", "BCHG_CASHB_SCRB_YN", "ACHG_CASHB_SCRB_YN", "BCHG_CASHB_CARD_IMG_NUM", "ACHG_CASHB_CARD_IMG_NUM"), // 멤버십카드이력
      "ZCOL.ZCOL_COL_CS_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "SVC_MGMT_NUM", "CNSL_DT", "CNSL_TM", "EVT_NUM", "ACNT_NUM", "CSR_ID", "CRDT_MGMT_CNTR_ORG_ID", "COL_GRP_CD", "COL_PART_ORG_ID", "COL_ACT_CD", "TC_PHON_CL_CD", "TC_PHON_NUM", "PAY_MTHD_CD", "PAY_APNT_DT", "PAY_APNT_COL_PAY_MTHD_CD", "PAY_APNT_AMT", "NXT_CNSL_OPER_DT", "COL_MEMO_EXIST_YN", "CNSL_TMS", "APNT_OBSRV_CD", "ACTL_PAY_AMT", "COL_AMT", "COL_MTH_CNT", "BOND_MTH_AGE", "TMTH_COL_AMT", "CRDT_SCOR", "LAST_INV_DT", "EVT_CRE_DT", "EVT_END_DT", "IO_CL_CD", "SVC_CD", "AUDIT_ID", "AUDIT_DTM", "PAY_CYCL_CD", "COL_DRW_INV_CYCL_CD", "SVC_CRDT_GR_CD", "CNTC_NUM_TYP_CD", "WIRE_COL_CS_OBJ_CL_CD", "WIRE_COL_CS_BRWS_RNK", "ACNT_COL_AMT", "ACNT_COL_SVC_CNT", "CO_CL_CD", "CNTRCT_MGMT_NUM", "USE_CNTRCT_CL_CD", "OCALL_TRMS_NUM", "BOND_ASGN_GRP_CD", "CUST_REL_CD"), // 미납상담이력
      "ZINB.ZINB_INB_CNSL" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CSR_ID", "CNSL_DT", "CNSL_TM", "AUDIT_ID", "AUDIT_DTM", "CO_CL_CD", "CUST_NUM", "CUST_NM", "SKT_CUST_YN", "CNTC_NUM", "TCR_NM", "NM_CUST_REL_CD", "CNSL_CD_SEQ", "OBD_FAIL_RSN_CD", "OBD_SUSS_TCR_CD", "DSAT_CL_CD", "CNSL_UNIT_CD", "CNSL_OP_ST_CD", "TAKE_SALE_ORG_ID", "TAKER_NM", "MEMO_YN", "MEMO_TYP_CD", "VOC_RCV_ID", "VOC_OP_CL_CD", "ORG_ID", "HO_IDNT_NUM", "CNSL_CONT_CL_CD", "CNSL_CHNL_CD", "CALLFLOW_CD", "AGREE_INFO_YN", "WAREA_CD", "AUTH_YN", "DUP_CNSL_CL_CD", "DSAT_GR_CD", "DSAT_INDC_ORG_ID", "DSAT_INDC_SUP_ORG_ID", "DSAT_INDC_DEPT_SEL_YN", "AGN_LIN_CALL_YN", "HSMS_TYP_NUM", "RSV_NUM", "SALE_BR_ORG_ID", "IVR_AUTH_CL_CD", "CNSL_CUST_SCRB_CL_CD", "SUP_CSR_ID", "SUP_CNSL_DT", "SUP_CNSL_TM", "ANALS_OBJ_YN", "INB_CNSL_GRP_NUM"), // INBOUND상담
      "ZINV.ZINV_BILL_ISUE_SPC" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "BILL_ISUE_SER_NUM", "ACNT_NUM", "SVC_MGMT_NUM", "INV_DT", "REQ_DT", "BILL_ISUE_SPC_CL_CD", "MAIL_TYP_CD", "REISU_ST_CD", "RESND_DT", "REISU_BILL_FRMT_STRD_CD", "REISU_REQ_PATH_CD", "ISUE_CL_CD", "REISU_INVR_NM", "REISU_ERR_CD", "RGMAIL_NUM", "INTEG_TXT_ADDR_ID", "ST_NM_LOTN_ADDR", "PAY_MTHD_CD", "BLMSG_CD", "MBL_BILL_RCV_PHON_NUM", "TERM_COL_CNTC_CL_CD", "ADDR_UNLIKE_YN", "OP_SALE_ORG_ID", "REISU_EMAIL_ADDR", "RGSTR_ID", "AUDIT_ID", "AUDIT_DTM", "LEGAL_REPVE_INCLD_YN", "LEGAL_REPVE_SVC_MGMT_NUM", "TAX_BILL_ISUE_YN", "INT_PHON_DTL_ISUE_YN", "SVC_LINE_CNT", "INTEG_DTL_ISUE_YN", "NEAT_BILL_YN", "COL_INCLD_YN", "PRD_PAY_DT", "REISU_RSN_CD", "BIZ_CL_CD", "INVR_DEPT_NM", "INVR_CHRGR_NM", "BILL_SND_CO_CD", "BILL_COL_FRMT_CL_CD", "CMBE_ACNT_NUM", "SCUR_BILL_YN", "PRT_SVC_NUM", "ATCH_DTL_INCLD_YN", "CUST_BRWS_NUM", "INV_AMT", "PAY_OP_YN", "REISU_GUID_MMS_SND_YN", "REISU_GUID_MMS_RCV_PHON_NUM"), // 청구서발행명세
      "ZORD.ZORD_WIRE_SVC_CHG_HST" -> List("OPER_TYPE", "BA_TYPE", "TABLE_NM", "COMMIT_TIME", "CONT_GRP_ID", "CHG_DT", "SER_NUM", "DTL_SER_NUM", "AUDIT_ID", "AUDIT_DTM", "SVC_CD", "SVC_MGMT_NUM", "SVC_CHG_CD", "MAIN_CHG_YN", "REQR_CTZ_SER_NUM", "REQR_CTZ_NUM_PINF", "CHG_DTM", "CNCL_DTM", "SALE_APRV_NUM", "SVC_CHG_RSN_CD", "MKTG_CHG_TYP_CD", "MKTG_CHG_RSN_CD", "OPING_CNCL_RSN_CD", "SVC_OPER_ST_CD", "RCV_SEQ", "RCV_DTM", "OSS_OP_RSLT_RSN_CD", "BCHG_KEY_CTT1", "ACHG_KEY_CTT1", "BCHG_KEY_CTT2", "ACHG_KEY_CTT2", "BCHG_ITM1", "ACHG_ITM1", "BCHG_ITM2", "ACHG_ITM2", "BCHG_ITM3", "ACHG_ITM3", "BCHG_ITM4", "ACHG_ITM4", "BCHG_ITM5", "ACHG_ITM5", "BCHG_ITM6", "ACHG_ITM6", "BCHG_ITM7", "ACHG_ITM7", "BCHG_ITM8", "ACHG_ITM8", "BCHG_ITM9", "ACHG_ITM9", "BCHG_ITM10", "ACHG_ITM10", "BCHG_ITM11", "ACHG_ITM11", "BCHG_ITM12", "ACHG_ITM12", "BCHG_ITM13", "ACHG_ITM13", "BCHG_ITM14", "ACHG_ITM14", "BCHG_ITM15", "ACHG_ITM15", "BCHG_ITM16", "ACHG_ITM16", "BCHG_ITM17", "ACHG_ITM17", "BCHG_ITM18", "ACHG_ITM18", "BCHG_ITM19", "ACHG_ITM19", "BCHG_ITM20", "ACHG_ITM20") // 유선서비스변경이력
    )

    // Swing 테이블별 srcEventKey 구조체 선언. default SVC_MGMT_NUM 그외 변경
    val eventKeyMap: Map[String, String] = Map(
      "ZORD.ZORD_PSNINF_ITM_PRA_HST" -> "SVC_MGMT_NUM", // 개인정보항목활용이력
      "ZORD.ZORD_CUST_INFO_PRA_HST" -> "SVC_MGMT_NUM", // 고객정보활용이력
      "ZORD.ZORD_CUST_CHG_HST" -> "CUST_NUM", // 고객변경이력. 고객번호
      "ZORD.ZORD_ACNT_CHG_HST" -> "ACNT_NUM", // 계정변경이력. 계정번호
      "ZORD.ZORD_SVC_CHG_HST" -> "SVC_MGMT_NUM", // 서비스변경이력
      "ZORD.ZORD_COPN_OP_HST" -> "SVC_MGMT_NUM", // 쿠폰처리이력
      "ZORD.ZORD_DATA_GIFT_HST" -> "SVC_MGMT_NUM", // 데이터선물이력
      "ZORD.ZORD_DATA_GIFT_ACHRG_HST" -> "SVC_MGMT_NUM", // 데이터선물자동충전이력
      "ZORD.ZORD_SUPL_SVC_COPN" -> "SVC_MGMT_NUM", // 부가서비스쿠폰
      "ZMBR.ZMBR_MBR_CARD_HST" -> "MBR_CARD_NUM1", // 멤버십카드이력. 멤버십카드번호1
      "ZCOL.ZCOL_COL_CS_HST" -> "SVC_MGMT_NUM", // 미납상담이력
      "ZINB.ZINB_INB_CNSL" -> "SVC_MGMT_NUM", // INBOUND상담
      "ZINV.ZINV_BILL_ISUE_SPC" -> "CUST_NUM", // 청구서발행명세. 고객번호
      "ZORD.ZORD_WIRE_SVC_CHG_HST" -> "SVC_MGMT_NUM" // 유선서비스변경이력
    )

    // raw data 를 구분자를 이용 split
    val line = rawLine.split("\u001C", -1) // swing ogg delimiter : ^\

    var rawMap = immutable.Map.empty[String, String]

    // 3번째컬럼 즉, 테이블명을 가지고 header정보를 가져옴
    val header = headers(line(2))

    // oracle debug
    //var ora_cols:String = ""
          
    // column name -> data 형태의 rawMap 생성
    for (i <- 0 until header.size) {
      rawMap += header(i) -> line(i)
      //debug oracle
      //ora_cols += ", '" + line(i) + "' AS " + header(i)
    }
    //debug oracle
    //Log.debug(s"[2] ora_cols => $ora_cols")
    
    // srcObjName 추출
    val srcObjName = rawMap("TABLE_NM")

    // srcEventKey 지정
    val srcEventKey = eventKeyMap(line(2))
      
    // eventDt 추출
    val srcEventDt = rawMap("AUDIT_DTM")
   
    var eventDt:String = null

    if (srcEventDt != null) {
      eventDt = Utils.convertToDate(srcEventDt, "yyyy-MM-dd:HH:mm:ss")
      //Log.debug("[2] srcEventDt : "+ srcEventDt)
    } else {
      Log.warn("[2] srcEventDt is null : "+ srcEventDt)
      eventDt = "19000101000000.00"
    }

    //Log.debug(s"[2] Return Main ....")

    // Oracle 저장을 위한 Header 값 추가
    // 아래 9개 필드는 event 매핑시 공통 모듈에서 세팅하니 제외하고 서비스에 맞는 값들만 세팅.
    // INTEG_CONT_EVT_ID, INTEG_CONT_CL_CD, INTEG_CONT_OP_CL_CD, INTEG_CONT_RSN_CD, SRC_SYS_CL_CD,  SRC_OWNR_NM, SRC_OBJ_NM, SRC_CHG_CD_VAL, SRC_RSN_CD_VAL
    
    val swingData = new SwingData(TosConstants.SrcSysName.SWING, srcObjName, srcEventKey, eventDt, rawLine, rawMap)

    if (srcObjName.equals("ZORD.ZORD_ACNT_CHG_HST")) { // ZORD_ACNT_CHG_HST(계정변경이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CHG_DT"))                 // 접촉일자(변경일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "A")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("ACNT_NUM"))      // 접촉식별구분값(계정번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CHG_DTM"))               // 접촉일시(변경일시)
      swingData.addHeader(TosConstants.JsonHeader.ACNT_NUM, swingData.getBody("ACNT_NUM"))              // 계정번호(계정번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, swingData.getBody("SER_NUM"))            // 원천일련번호(일련번호)
    } else if (srcObjName.equals("ZORD.ZORD_CUST_INFO_PRA_HST")) { // ZORD_CUST_INFO_PRA_HST(고객정보활용이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("OP_DTM").substring(0, 8)) // 접촉일자(앞8자리
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("OP_DTM"))                // 접촉일시(처리일시)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    } else if (srcObjName.equals("ZORD.ZORD_PSNINF_ITM_PRA_HST")) { // ZORD_PSNINF_ITM_PRA_HST(개인정보항목활용이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("OP_DTM").substring(0, 8)) // 접촉일자(앞8자리
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("OP_DTM"))                // 접촉일시(처리일시)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    } else if (srcObjName.equals("ZORD.ZORD_CUST_CHG_HST")) { // ZORD_CUST_CHG_HST(고객변경이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CHG_DT"))                 // 접촉일자(변경일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "C")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("CUST_NUM"))      // 접촉식별구분값(고객번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CHG_DTM"))               // 접촉일시(변경일시)
      swingData.addHeader(TosConstants.JsonHeader.CUST_NUM, swingData.getBody("CUST_NUM"))              // 고객번호(고객번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_CONT_GRP_ID, swingData.getBody("CONT_GRP_ID"))    // 원천접촉그룹ID(접촉그룹ID)
      swingData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, swingData.getBody("SER_NUM"))            // 원천일련번호(일련번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_DTL_SER_NUM, swingData.getBody("DTL_SER_NUM"))    // 원천상세일련번호(상세일련번호)
      swingData.addHeader(TosConstants.JsonHeader.CO_CL_CD, swingData.getBody("CO_CL_CD"))              // 회사구분코드(회사구분코드)
    } else if (srcObjName.equals("ZORD.ZORD_DATA_GIFT_HST")) { // ZORD_DATA_GIFT_HST(데이터선물이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("OP_DTM").substring(0, 8)) // 접촉일자(SUBSTR(처리일시, 1, 8)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("OP_DTM"))                // 접촉일시(처리일시)
      swingData.addHeader(TosConstants.JsonHeader.CUST_NUM, swingData.getBody("CUST_NUM"))              // 고객번호(고객번호)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    } else if (srcObjName.equals("ZORD.ZORD_DATA_GIFT_ACHRG_HST")) { // ZORD_DATA_GIFT_ACHRG_HST(데이터선물자동충전이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("EFF_STA_DTM").substring(0, 8)) // 접촉일자(SUBSTR(유효시작일시, 1, 8)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("EFF_STA_DTM"))           // 접촉일시(유효시작일시)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    } else if (srcObjName.equals("ZMBR.ZMBR_MBR_CARD_HST")) { // ZMBR_MBR_CARD_HST(멤버십카드이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CHG_DT"))                 // 접촉일자(변경일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CHG_DT") + swingData.getBody("OP_TM").substring(0, 6)) // 접촉일시(변경일자(CHG_DT)||처리시각(OP_TM)앞6자리
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.MBR_CARD_NUM1, swingData.getBody("MBR_CARD_NUM1"))    // 멤버십카드번호1(멤버십카드번호1)
    } else if (srcObjName.equals("ZCOL.ZCOL_COL_CS_HST")) { // ZCOL_COL_CS_HST(미납상담이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CNSL_DT"))                // 접촉일자(상담일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CNSL_DT") + swingData.getBody("CNSL_TM")) // 접촉일시(상담일자(CNSL_DT)||상담시각(CNSL_TM)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.ACNT_NUM, swingData.getBody("ACNT_NUM"))              // 계정번호(계정번호)
    } else if (srcObjName.equals("ZORD.ZORD_SVC_CHG_HST")) { // ZORD_SVC_CHG_HST(서비스변경이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CHG_DT"))                 // 접촉일자(변경일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CHG_DTM"))               // 접촉일시(변경일시)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_CONT_GRP_ID, swingData.getBody("CONT_GRP_ID"))    // 원천접촉그룹ID(접촉그룹ID)
      swingData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, swingData.getBody("SER_NUM"))            // 원천일련번호(일련번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_DTL_SER_NUM, swingData.getBody("DTL_SER_NUM"))    // 원천상세일련번호(상세일련번호)
      swingData.addHeader(TosConstants.JsonHeader.SVC_CD, swingData.getBody("SVC_CD"))                  // 서비스구분코드(서비스구분코드)
    } else if (srcObjName.equals("ZORD.ZORD_WIRE_SVC_CHG_HST")) { // ZORD_WIRE_SVC_CHG_HST(유선서비스변경이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CHG_DT"))                 // 접촉일자(변경일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CHG_DTM"))               // 접촉일시(변경일시)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_CONT_GRP_ID, swingData.getBody("CONT_GRP_ID"))    // 원천접촉그룹ID(접촉그룹ID)
      swingData.addHeader(TosConstants.JsonHeader.SRC_SER_NUM, swingData.getBody("SER_NUM"))            // 원천일련번호(일련번호)
      swingData.addHeader(TosConstants.JsonHeader.SRC_DTL_SER_NUM, swingData.getBody("DTL_SER_NUM"))    // 원천상세일련번호(상세일련번호)
      swingData.addHeader(TosConstants.JsonHeader.SVC_CD, swingData.getBody("SVC_CD"))                  // 서비스구분코드(서비스구분코드)
      swingData.addHeader(TosConstants.JsonHeader.RCV_SEQ, swingData.getBody("RCV_SEQ"))                // 접수순번(접수순번)
    } else if (srcObjName.equals("ZINB.ZINB_INB_CNSL")) { // ZINB_INB_CNSL(INBOUND상담)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("CNSL_DT"))                // 접촉일자(상담일자)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "C")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("CUST_NUM"))      // 접촉식별구분값(고객번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("CNSL_DT") + swingData.getBody("CNSL_TM")) // 접촉일시(상담일자(CNSL_DT)||상담시각(CNSL_TM)
      swingData.addHeader(TosConstants.JsonHeader.CUST_NUM, swingData.getBody("CUST_NUM"))              // 고객번호(고객번호)
    } else if (srcObjName.equals("ZINV.ZINV_BILL_ISUE_SPC")) { // ZINV_BILL_ISUE_SPC(청구서발행명세)
      // 재발행상태코드 : 01,02 만 처리
      val ReisuStCd = swingData.getBody("REISU_ST_CD")
      // 원천데이터에 시분초가 없어서 AUDIT_DTM 의 시분초 사용
      val Req_hms = swingData.getBody("AUDIT_DTM", { a => a.substring(11, 19).replaceAll(":", "") }) // AUDIT_DTM - 2017/05/19 15:10:24
      if (ReisuStCd.equals("01")) { // 재발행상태코드(REISU_ST_CD)가 01(신청)이면
        swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("REQ_DT"))               // 접촉일자(요청일자)
        swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("REQ_DT")+Req_hms)      // 접촉일시(요청일자(REQ_DT)||TO_CHAR(AUDIT_DTM, 'HH24MISS'))
      } else if (ReisuStCd.equals("02")) { //재발행상태코드(REISU_ST_CD)가 02(재발행완료)이면
        swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("RESND_DT"))             // 접촉일자(재발송일자)
        swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("RESND_DT")+Req_hms)    // 접촉일시(재발송일자(RESND_DT))||TO_CHAR(AUDIT_DTM, 'HH24MISS'))
      }
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.ACNT_NUM, swingData.getBody("ACNT_NUM"))              // 계정번호(계정번호)
    } else if (srcObjName.equals("ZORD.ZORD_COPN_OP_HST")) { // ZORD_COPN_OP_HST(쿠폰처리이력)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("COPN_OP_DTM").substring(0, 8)) // 접촉일자(SUBSTR(쿠폰처리일시, 1, 8)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("COPN_OP_DTM"))           // 접촉일시(쿠폰처리일시)
      swingData.addHeader(TosConstants.JsonHeader.CUST_NUM, swingData.getBody("CUST_NUM"))              // 고객번호(고객번호)
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    } else if (srcObjName.equals("ZINV.ZINV_BILL_ISUE_SPC")) { // ZINV_BILL_ISUE_SPC(부가서비스쿠폰)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DT, swingData.getBody("AUDIT_DTM", { a => a.substring(0, 10).replaceAll("/", "") })) // 접촉일자 TO_CHAR(AUDIT_DTM,'YYYYMMDD')
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_CD, "S")                                 // 접촉식별구분코드(default setting)
      swingData.addHeader(TosConstants.JsonHeader.CONT_IDNT_CL_VAL, swingData.getBody("SVC_MGMT_NUM"))  // 접촉식별구분값(서비스관리번호)
      swingData.addHeader(TosConstants.JsonHeader.CONT_DTM, swingData.getBody("AUDIT_DTM", { a => a.replaceAll(" ", "").replaceAll("/", "").replaceAll(":", "") })) // 접촉일시(쿠폰처리일시) TO_CHAR(AUDIT_DTM, 'YYYYMMDDHH24MISS')
      swingData.addHeader(TosConstants.JsonHeader.SVC_MGMT_NUM, swingData.getBody("SVC_MGMT_NUM"))      // 서비스관리번호(서비스관리번호)
    }

    // object return
    swingData
  }

}


