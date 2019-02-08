package com.sktelecom.tos.stream.oracle

import java.sql.Timestamp

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.internal.Logging
import play.api.libs.json.{JsObject, JsResultException, Json}

import scala.collection.mutable

/**
  * Oracle 에 저장하기 위해서 필요한 각종 매퍼
  */
object OracleTransformer extends Logging {

  final val TosEventKey = "TOS_EVENT_KEY"
  final val TosEventDtm = "TOS_EVENT_DTM"
  final val TosSparkMapDtm = "TOS_SPARK_MAP_DTM"
  final val TosKafkaSendDtm = "TOS_KAFKA_SEND_DTM"

  /**
    * 접촉일자
    */
  final val ContDt = "CONT_DT"
  /**
    * 통합접촉이벤트ID
    */
  final val IntegContEvtId = "INTEG_CONT_EVT_ID"
  /**
    * 접촉식별구분코드
    */
  final val ContIdntClCd = "CONT_IDNT_CL_CD"
  /**
    * 접촉식별구분값
    */
  final val ContIdntClVal = "CONT_IDNT_CL_VAL"
  /**
    * 입력일련번호
    */
  final val InsSerNum = "INS_SER_NUM"
  /**
    * 최종변경일시
    */
  final val AuditDtm = "AUDIT_DTM"
  /**
    * 최종변경자ID
    */
  final val AuditId = "AUDIT_ID"
  /**
    * 접촉일시
    */
  final val ContDtm = "CONT_DTM"
  /**
    * 고객번호
    */
  final val CustNum = "CUST_NUM"
  /**
    * 서비스관리번호
    */
  final val SvcMgmtNum = "SVC_MGMT_NUM"
  /**
    * 계정번호
    */
  final val AcntNum = "ACNT_NUM"
  /**
    * TID
    */
  final val Tid = "TID"
  /**
    * TID번호
    */
  final val TIdNum = "T_ID_NUM"
  /**
    * 멤버십카드번호1
    */
  final val MbrCardNum1 = "MBR_CARD_NUM1"
  /**
    * 외부식별번호
    */
  final val OutIdntNum = "OUT_IDNT_NUM"
  /**
    * 통합접촉구분코드
    */
  final val IntegContClCd = "INTEG_CONT_CL_CD"
  /**
    * 통합접촉업무구분코드
    */
  final val IntegContOpClCd = "INTEG_CONT_OP_CL_CD"
  /**
    * 통합접촉사유코드
    */
  final val IntegContRsnCd = "INTEG_CONT_RSN_CD"
  /**
    * 캠페인번호
    */
  final val CmpgnNum = "CMPGN_NUM"
  /**
    * 원천시스템구분코드
    */
  final val SrcSysClCd = "SRC_SYS_CL_CD"
  /**
    * 원천소유자명
    */
  final val SrcOwnrNm = "SRC_OWNR_NM"
  /**
    * 원천대상명
    */
  final val SrcObjNm = "SRC_OBJ_NM"
  /**
    * 원천변경코드값
    */
  final val SrcChgCdVal = "SRC_CHG_CD_VAL"
  /**
    * 원천사유코드값
    */
  final val SrcRsnCdVal = "SRC_RSN_CD_VAL"
  /**
    * 원천접촉그룹ID
    */
  final val SrcContGrpId = "SRC_CONT_GRP_ID"
  /**
    * 원천일련번호
    */
  final val SrcSerNum = "SRC_SER_NUM"
  /**
    * 원천상세일련번호
    */
  final val SrcDtlSerNum = "SRC_DTL_SER_NUM"
  /**
    * 채널접촉수단코드
    */
  final val ChnlContWayCd = "CHNL_CONT_WAY_CD"
  /**
    * 회사구분코드
    */
  final val CoClCd = "CO_CL_CD"
  /**
    * 서비스구분코드
    */
  final val SvcCd = "SVC_CD"
  /**
    * 접수순번
    */
  final val RcvSeq = "RCV_SEQ"

  final val ContBfKeyCtt1 = "CONT_BF_KEY_CTT1"
  final val ContAfKeyCtt1 = "CONT_AF_KEY_CTT1"
  final val ContBfKeyCtt2 = "CONT_BF_KEY_CTT2"
  final val ContAfKeyCtt2 = "CONT_AF_KEY_CTT2"
  final val ContBfItm1 = "CONT_BF_ITM1"
  final val ContAfItm1 = "CONT_AF_ITM1"
  final val ContBfItm2 = "CONT_BF_ITM2"
  final val ContAfItm2 = "CONT_AF_ITM2"
  final val ContBfItm3 = "CONT_BF_ITM3"
  final val ContAfItm3 = "CONT_AF_ITM3"
  final val ContBfItm4 = "CONT_BF_ITM4"
  final val ContAfItm4 = "CONT_AF_ITM4"
  final val ContBfItm5 = "CONT_BF_ITM5"
  final val ContAfItm5 = "CONT_AF_ITM5"
  final val ContBfItm6 = "CONT_BF_ITM6"
  final val ContAfItm6 = "CONT_AF_ITM6"
  final val ContBfItm7 = "CONT_BF_ITM7"
  final val ContAfItm7 = "CONT_AF_ITM7"
  final val ContBfItm8 = "CONT_BF_ITM8"
  final val ContAfItm8 = "CONT_AF_ITM8"
  final val ContBfItm9 = "CONT_BF_ITM9"
  final val ContAfItm9 = "CONT_AF_ITM9"
  final val ContBfItm10 = "CONT_BF_ITM10"
  final val ContAfItm10 = "CONT_AF_ITM10"
  final val ContBfItm11 = "CONT_BF_ITM11"
  final val ContAfItm11 = "CONT_AF_ITM11"
  final val ContBfItm12 = "CONT_BF_ITM12"
  final val ContAfItm12 = "CONT_AF_ITM12"
  final val ContBfItm13 = "CONT_BF_ITM13"
  final val ContAfItm13 = "CONT_AF_ITM13"
  final val ContBfItm14 = "CONT_BF_ITM14"
  final val ContAfItm14 = "CONT_AF_ITM14"
  final val ContBfItm15 = "CONT_BF_ITM15"
  final val ContAfItm15 = "CONT_AF_ITM15"
  final val ContBfItm16 = "CONT_BF_ITM16"
  final val ContAfItm16 = "CONT_AF_ITM16"
  final val ContBfItm17 = "CONT_BF_ITM17"
  final val ContAfItm17 = "CONT_AF_ITM17"
  final val ContBfItm18 = "CONT_BF_ITM18"
  final val ContAfItm18 = "CONT_AF_ITM18"
  final val ContBfItm19 = "CONT_BF_ITM19"
  final val ContAfItm19 = "CONT_AF_ITM19"
  final val ContBfItm20 = "CONT_BF_ITM20"
  final val ContAfItm20 = "CONT_AF_ITM20"
  final val ContBfItm21 = "CONT_BF_ITM21"
  final val ContAfItm21 = "CONT_AF_ITM21"
  final val ContBfItm22 = "CONT_BF_ITM22"
  final val ContAfItm22 = "CONT_AF_ITM22"
  final val ContBfItm23 = "CONT_BF_ITM23"
  final val ContAfItm23 = "CONT_AF_ITM23"
  final val ContBfItm24 = "CONT_BF_ITM24"
  final val ContAfItm24 = "CONT_AF_ITM24"
  final val ContBfItm25 = "CONT_BF_ITM25"
  final val ContAfItm25 = "CONT_AF_ITM25"
  final val ContBfItm26 = "CONT_BF_ITM26"
  final val ContAfItm26 = "CONT_AF_ITM26"
  final val ContBfItm27 = "CONT_BF_ITM27"
  final val ContAfItm27 = "CONT_AF_ITM27"
  final val ContBfItm28 = "CONT_BF_ITM28"
  final val ContAfItm28 = "CONT_AF_ITM28"
  final val ContBfItm29 = "CONT_BF_ITM29"
  final val ContAfItm29 = "CONT_AF_ITM29"
  final val ContBfItm30 = "CONT_BF_ITM30"
  final val ContAfItm30 = "CONT_AF_ITM30"
  final val ContBfItm31 = "CONT_BF_ITM31"
  final val ContAfItm31 = "CONT_AF_ITM31"
  final val ContBfItm32 = "CONT_BF_ITM32"
  final val ContAfItm32 = "CONT_AF_ITM32"
  final val ContBfItm33 = "CONT_BF_ITM33"
  final val ContAfItm33 = "CONT_AF_ITM33"
  final val ContBfItm34 = "CONT_BF_ITM34"
  final val ContAfItm34 = "CONT_AF_ITM34"
  final val ContBfItm35 = "CONT_BF_ITM35"
  final val ContAfItm35 = "CONT_AF_ITM35"
  final val ContBfItm36 = "CONT_BF_ITM36"
  final val ContAfItm36 = "CONT_AF_ITM36"
  final val ContBfItm37 = "CONT_BF_ITM37"
  final val ContAfItm37 = "CONT_AF_ITM37"
  final val ContBfItm38 = "CONT_BF_ITM38"
  final val ContAfItm38 = "CONT_AF_ITM38"
  final val ContBfItm39 = "CONT_BF_ITM39"
  final val ContAfItm39 = "CONT_AF_ITM39"
  final val ContBfItm40 = "CONT_BF_ITM40"
  final val ContAfItm40 = "CONT_AF_ITM40"
  final val ContBfItm41 = "CONT_BF_ITM41"
  final val ContAfItm41 = "CONT_AF_ITM41"
  final val ContBfItm42 = "CONT_BF_ITM42"
  final val ContAfItm42 = "CONT_AF_ITM42"
  final val ContBfItm43 = "CONT_BF_ITM43"
  final val ContAfItm43 = "CONT_AF_ITM43"
  final val ContBfItm44 = "CONT_BF_ITM44"
  final val ContAfItm44 = "CONT_AF_ITM44"
  final val ContBfItm45 = "CONT_BF_ITM45"
  final val ContAfItm45 = "CONT_AF_ITM45"
  final val ContBfItm46 = "CONT_BF_ITM46"
  final val ContAfItm46 = "CONT_AF_ITM46"
  final val ContBfItm47 = "CONT_BF_ITM47"
  final val ContAfItm47 = "CONT_AF_ITM47"
  final val ContBfItm48 = "CONT_BF_ITM48"
  final val ContAfItm48 = "CONT_AF_ITM48"
  final val ContBfItm49 = "CONT_BF_ITM49"
  final val ContAfItm49 = "CONT_AF_ITM49"
  final val ContBfItm50 = "CONT_BF_ITM50"
  final val ContAfItm50 = "CONT_AF_ITM50"


  /**
    * 필수 필드 밸리데이션
    *
    * todo 현재는 디버깅을 위해서 모든 테스트를 수행하고 있지만, fast fail 형태로 바꿀 필요가 있음
    *
    * @param m
    * @return
    */
  def validate(m: Map[String, String]): Boolean = {
    logTrace(m.toString)

    var valid = true

    def checkNull(field: String): Unit = {
      m.get(field) match {
        case Some(x) =>
          if (x.length == 0) {
            valid = false
            logWarning(s"필요한 필드의 길이가 0임: $field")
          }
        case None =>
          valid = false
          logWarning(s"필요한 필드가 없음: $field")
      }
    }

    def checkLength(field: String, length: Int): Unit = {
      m.get(field) match {
        case Some(x) =>
          if (x.length > length) {
            valid = false
            logWarning(s"필드 값의 길이가 너무 김: 필드명=$field 요구길이=$length 실제길이=${x.length} 값=$x")
          }
        case None =>
      }
    }

    def checkLong(field: String): Unit = {
      m.get(field) match {
        case Some(x) =>
          if (x.length > 0) {
            try {
              x.toLong
            } catch {
              case _: Exception =>
                valid = false
                logWarning(s"필드 값의 정수(Long)가 아님: 필드명=$field")
            }
          }
        case None =>
      }
    }

    def checkInt(field: String): Unit = {
      m.get(field) match {
        case Some(x) =>
          if (x.length > 0) {
            try {
              x.toInt
            } catch {
              case _: Exception =>
                valid = false
                logWarning(s"필드 값의 정수(Int)가 아님: 필드명=$field")
            }
          }
        case None =>
      }
    }


    List(
      ContDt, IntegContEvtId, ContIdntClCd, ContIdntClVal,
      InsSerNum, AuditDtm, AuditId, ContDtm, IntegContClCd, 
      IntegContOpClCd, IntegContRsnCd, SrcSysClCd
    ).foreach(checkNull)


    List(
      (ContDt, 8),
      (IntegContEvtId, 6),
      (ContIdntClCd, 1),
      (ContIdntClVal, 20),
      (ContDtm, 14),
      (Tid, 50),
      (TIdNum, 19),
      (MbrCardNum1, 10),
      (OutIdntNum, 14),
      (IntegContClCd, 3),
      (IntegContOpClCd, 2),
      (IntegContRsnCd, 5),
      (CmpgnNum, 14),
      (SrcSysClCd, 3),
      (SrcOwnrNm, 100),
      (SrcObjNm, 100),
      (SrcChgCdVal, 50),
      (SrcRsnCdVal, 50),
      (SrcContGrpId, 32),
      (ChnlContWayCd, 1),
      (CoClCd, 1),
      (SvcCd, 1)
    ).foreach(e => checkLength(e._1, e._2))


    List(ContBfKeyCtt1, ContAfKeyCtt1, ContBfKeyCtt2, ContAfKeyCtt2).foreach(e => checkLength(e, 100))


    List(
      ContBfItm1, ContAfItm1,
      ContBfItm2, ContAfItm2,
      ContBfItm3, ContAfItm3,
      ContBfItm4, ContAfItm4,
      ContBfItm5, ContAfItm5,
      ContBfItm6, ContAfItm6,
      ContBfItm7, ContAfItm7,
      ContBfItm8, ContAfItm8,
      ContBfItm9, ContAfItm9,
      ContBfItm10, ContAfItm10,
      ContBfItm11, ContAfItm11,
      ContBfItm12, ContAfItm12,
      ContBfItm13, ContAfItm13,
      ContBfItm14, ContAfItm14,
      ContBfItm15, ContAfItm15,
      ContBfItm16, ContAfItm16,
      ContBfItm17, ContAfItm17,
      ContBfItm18, ContAfItm18,
      ContBfItm19, ContAfItm19,
      ContBfItm20, ContAfItm20,
      ContBfItm21, ContAfItm21,
      ContBfItm22, ContAfItm22,
      ContBfItm23, ContAfItm23,
      ContBfItm24, ContAfItm24,
      ContBfItm25, ContAfItm25,
      ContBfItm26, ContAfItm26,
      ContBfItm27, ContAfItm27,
      ContBfItm28, ContAfItm28,
      ContBfItm29, ContAfItm29,
      ContBfItm30, ContAfItm30,
      ContBfItm31, ContAfItm31,
      ContBfItm32, ContAfItm32,
      ContBfItm33, ContAfItm33,
      ContBfItm34, ContAfItm34,
      ContBfItm35, ContAfItm35,
      ContBfItm36, ContAfItm36,
      ContBfItm37, ContAfItm37,
      ContBfItm38, ContAfItm38,
      ContBfItm39, ContAfItm39,
      ContBfItm40, ContAfItm40,
      ContBfItm41, ContAfItm41,
      ContBfItm42, ContAfItm42,
      ContBfItm43, ContAfItm43,
      ContBfItm44, ContAfItm44,
      ContBfItm45, ContAfItm45,
      ContBfItm46, ContAfItm46,
      ContBfItm47, ContAfItm47,
      ContBfItm48, ContAfItm48,
      ContBfItm49, ContAfItm49,
      ContBfItm50, ContAfItm50
    ).foreach(e => checkLength(e, 500))


    List(CustNum, SvcMgmtNum, AcntNum, SrcSerNum, SrcDtlSerNum).foreach(checkLong)


    List(RcvSeq).foreach(checkInt)


    valid
  }

  /**
    * Map 포맷의 레코드를 Oracle 저장에 사용하는 오브젝트로 변환.
    *
    * @param m
    * @return
    */
  def fromMapToOracleRecord(m: Map[String, String]): OracleRecord = {
    logTrace(m.toString)

    // 맵에서 필드 추출

    val CONT_DT = m.getOrElse(ContDt, null)
    val INTEG_CONT_EVT_ID = m.getOrElse(IntegContEvtId, null)
    val CONT_IDNT_CL_CD = m.getOrElse(ContIdntClCd, null)
    val CONT_IDNT_CL_VAL = m.getOrElse(ContIdntClVal, null)
    val INS_SER_NUM = m.getOrElse(InsSerNum, null)
    val AUDIT_DTM = m.getOrElse(AuditDtm, null)
    val AUDIT_ID = m.getOrElse(AuditId, null)
    val CONT_DTM = m.getOrElse(ContDtm, null)
    val CUST_NUM = m.getOrElse(CustNum, null)
    val SVC_MGMT_NUM = m.getOrElse(SvcMgmtNum, null)
    val ACNT_NUM = m.getOrElse(AcntNum, null)
    val TID = m.getOrElse(Tid, null)
    val T_ID_NUM = m.getOrElse(TIdNum, null)
    val MBR_CARD_NUM1 = m.getOrElse(MbrCardNum1, null)
    val OUT_IDNT_NUM = m.getOrElse(OutIdntNum, null)
    val INTEG_CONT_CL_CD = m.getOrElse(IntegContClCd, null)
    val INTEG_CONT_OP_CL_CD = m.getOrElse(IntegContOpClCd, null)
    val INTEG_CONT_RSN_CD = m.getOrElse(IntegContRsnCd, null)
    val CMPGN_NUM = m.getOrElse(CmpgnNum, null)
    val SRC_SYS_CL_CD = m.getOrElse(SrcSysClCd, null)
    val SRC_OWNR_NM = m.getOrElse(SrcOwnrNm, null)
    val SRC_OBJ_NM = m.getOrElse(SrcObjNm, null)
    val SRC_CHG_CD_VAL = m.getOrElse(SrcChgCdVal, null)
    val SRC_RSN_CD_VAL = m.getOrElse(SrcRsnCdVal, null)
    val SRC_CONT_GRP_ID = m.getOrElse(SrcContGrpId, null)
    val SRC_SER_NUM = m.getOrElse(SrcSerNum, null)
    val SRC_DTL_SER_NUM = m.getOrElse(SrcDtlSerNum, null)
    val CHNL_CONT_WAY_CD = m.getOrElse(ChnlContWayCd, null)
    val CO_CL_CD = m.getOrElse(CoClCd, null)
    val SVC_CD = m.getOrElse(SvcCd, null)
    val RCV_SEQ = m.getOrElse(RcvSeq, null)
    val CONT_BF_KEY_CTT1 = m.getOrElse(ContBfKeyCtt1, null)
    val CONT_AF_KEY_CTT1 = m.getOrElse(ContAfKeyCtt1, null)
    val CONT_BF_KEY_CTT2 = m.getOrElse(ContBfKeyCtt2, null)
    val CONT_AF_KEY_CTT2 = m.getOrElse(ContAfKeyCtt2, null)
    val CONT_BF_ITM1 = m.getOrElse(ContBfItm1, null)
    val CONT_AF_ITM1 = m.getOrElse(ContAfItm1, null)
    val CONT_BF_ITM2 = m.getOrElse(ContBfItm2, null)
    val CONT_AF_ITM2 = m.getOrElse(ContAfItm2, null)
    val CONT_BF_ITM3 = m.getOrElse(ContBfItm3, null)
    val CONT_AF_ITM3 = m.getOrElse(ContAfItm3, null)
    val CONT_BF_ITM4 = m.getOrElse(ContBfItm4, null)
    val CONT_AF_ITM4 = m.getOrElse(ContAfItm4, null)
    val CONT_BF_ITM5 = m.getOrElse(ContBfItm5, null)
    val CONT_AF_ITM5 = m.getOrElse(ContAfItm5, null)
    val CONT_BF_ITM6 = m.getOrElse(ContBfItm6, null)
    val CONT_AF_ITM6 = m.getOrElse(ContAfItm6, null)
    val CONT_BF_ITM7 = m.getOrElse(ContBfItm7, null)
    val CONT_AF_ITM7 = m.getOrElse(ContAfItm7, null)
    val CONT_BF_ITM8 = m.getOrElse(ContBfItm8, null)
    val CONT_AF_ITM8 = m.getOrElse(ContAfItm8, null)
    val CONT_BF_ITM9 = m.getOrElse(ContBfItm9, null)
    val CONT_AF_ITM9 = m.getOrElse(ContAfItm9, null)
    val CONT_BF_ITM10 = m.getOrElse(ContBfItm10, null)
    val CONT_AF_ITM10 = m.getOrElse(ContAfItm10, null)
    val CONT_BF_ITM11 = m.getOrElse(ContBfItm11, null)
    val CONT_AF_ITM11 = m.getOrElse(ContAfItm11, null)
    val CONT_BF_ITM12 = m.getOrElse(ContBfItm12, null)
    val CONT_AF_ITM12 = m.getOrElse(ContAfItm12, null)
    val CONT_BF_ITM13 = m.getOrElse(ContBfItm13, null)
    val CONT_AF_ITM13 = m.getOrElse(ContAfItm13, null)
    val CONT_BF_ITM14 = m.getOrElse(ContBfItm14, null)
    val CONT_AF_ITM14 = m.getOrElse(ContAfItm14, null)
    val CONT_BF_ITM15 = m.getOrElse(ContBfItm15, null)
    val CONT_AF_ITM15 = m.getOrElse(ContAfItm15, null)
    val CONT_BF_ITM16 = m.getOrElse(ContBfItm16, null)
    val CONT_AF_ITM16 = m.getOrElse(ContAfItm16, null)
    val CONT_BF_ITM17 = m.getOrElse(ContBfItm17, null)
    val CONT_AF_ITM17 = m.getOrElse(ContAfItm17, null)
    val CONT_BF_ITM18 = m.getOrElse(ContBfItm18, null)
    val CONT_AF_ITM18 = m.getOrElse(ContAfItm18, null)
    val CONT_BF_ITM19 = m.getOrElse(ContBfItm19, null)
    val CONT_AF_ITM19 = m.getOrElse(ContAfItm19, null)
    val CONT_BF_ITM20 = m.getOrElse(ContBfItm20, null)
    val CONT_AF_ITM20 = m.getOrElse(ContAfItm20, null)
    val CONT_BF_ITM21 = m.getOrElse(ContBfItm21, null)
    val CONT_AF_ITM21 = m.getOrElse(ContAfItm21, null)
    val CONT_BF_ITM22 = m.getOrElse(ContBfItm22, null)
    val CONT_AF_ITM22 = m.getOrElse(ContAfItm22, null)
    val CONT_BF_ITM23 = m.getOrElse(ContBfItm23, null)
    val CONT_AF_ITM23 = m.getOrElse(ContAfItm23, null)
    val CONT_BF_ITM24 = m.getOrElse(ContBfItm24, null)
    val CONT_AF_ITM24 = m.getOrElse(ContAfItm24, null)
    val CONT_BF_ITM25 = m.getOrElse(ContBfItm25, null)
    val CONT_AF_ITM25 = m.getOrElse(ContAfItm25, null)
    val CONT_BF_ITM26 = m.getOrElse(ContBfItm26, null)
    val CONT_AF_ITM26 = m.getOrElse(ContAfItm26, null)
    val CONT_BF_ITM27 = m.getOrElse(ContBfItm27, null)
    val CONT_AF_ITM27 = m.getOrElse(ContAfItm27, null)
    val CONT_BF_ITM28 = m.getOrElse(ContBfItm28, null)
    val CONT_AF_ITM28 = m.getOrElse(ContAfItm28, null)
    val CONT_BF_ITM29 = m.getOrElse(ContBfItm29, null)
    val CONT_AF_ITM29 = m.getOrElse(ContAfItm29, null)
    val CONT_BF_ITM30 = m.getOrElse(ContBfItm30, null)
    val CONT_AF_ITM30 = m.getOrElse(ContAfItm30, null)
    val CONT_BF_ITM31 = m.getOrElse(ContBfItm31, null)
    val CONT_AF_ITM31 = m.getOrElse(ContAfItm31, null)
    val CONT_BF_ITM32 = m.getOrElse(ContBfItm32, null)
    val CONT_AF_ITM32 = m.getOrElse(ContAfItm32, null)
    val CONT_BF_ITM33 = m.getOrElse(ContBfItm33, null)
    val CONT_AF_ITM33 = m.getOrElse(ContAfItm33, null)
    val CONT_BF_ITM34 = m.getOrElse(ContBfItm34, null)
    val CONT_AF_ITM34 = m.getOrElse(ContAfItm34, null)
    val CONT_BF_ITM35 = m.getOrElse(ContBfItm35, null)
    val CONT_AF_ITM35 = m.getOrElse(ContAfItm35, null)
    val CONT_BF_ITM36 = m.getOrElse(ContBfItm36, null)
    val CONT_AF_ITM36 = m.getOrElse(ContAfItm36, null)
    val CONT_BF_ITM37 = m.getOrElse(ContBfItm37, null)
    val CONT_AF_ITM37 = m.getOrElse(ContAfItm37, null)
    val CONT_BF_ITM38 = m.getOrElse(ContBfItm38, null)
    val CONT_AF_ITM38 = m.getOrElse(ContAfItm38, null)
    val CONT_BF_ITM39 = m.getOrElse(ContBfItm39, null)
    val CONT_AF_ITM39 = m.getOrElse(ContAfItm39, null)
    val CONT_BF_ITM40 = m.getOrElse(ContBfItm40, null)
    val CONT_AF_ITM40 = m.getOrElse(ContAfItm40, null)
    val CONT_BF_ITM41 = m.getOrElse(ContBfItm41, null)
    val CONT_AF_ITM41 = m.getOrElse(ContAfItm41, null)
    val CONT_BF_ITM42 = m.getOrElse(ContBfItm42, null)
    val CONT_AF_ITM42 = m.getOrElse(ContAfItm42, null)
    val CONT_BF_ITM43 = m.getOrElse(ContBfItm43, null)
    val CONT_AF_ITM43 = m.getOrElse(ContAfItm43, null)
    val CONT_BF_ITM44 = m.getOrElse(ContBfItm44, null)
    val CONT_AF_ITM44 = m.getOrElse(ContAfItm44, null)
    val CONT_BF_ITM45 = m.getOrElse(ContBfItm45, null)
    val CONT_AF_ITM45 = m.getOrElse(ContAfItm45, null)
    val CONT_BF_ITM46 = m.getOrElse(ContBfItm46, null)
    val CONT_AF_ITM46 = m.getOrElse(ContAfItm46, null)
    val CONT_BF_ITM47 = m.getOrElse(ContBfItm47, null)
    val CONT_AF_ITM47 = m.getOrElse(ContAfItm47, null)
    val CONT_BF_ITM48 = m.getOrElse(ContBfItm48, null)
    val CONT_AF_ITM48 = m.getOrElse(ContAfItm48, null)
    val CONT_BF_ITM49 = m.getOrElse(ContBfItm49, null)
    val CONT_AF_ITM49 = m.getOrElse(ContAfItm49, null)
    val CONT_BF_ITM50 = m.getOrElse(ContBfItm50, null)
    val CONT_AF_ITM50 = m.getOrElse(ContAfItm50, null)

    // 케이스클래스로 변환

    val undefInt = null.asInstanceOf[Int]
    val undefLong = null.asInstanceOf[Long]

    OracleRecord(
      CONT_DT,
      INTEG_CONT_EVT_ID,
      CONT_IDNT_CL_CD,
      CONT_IDNT_CL_VAL,
      if (INS_SER_NUM != null && INS_SER_NUM.length > 0) INS_SER_NUM.toLong else undefLong,
      if (AUDIT_DTM != null && AUDIT_DTM.length > 0) new Timestamp(AUDIT_DTM.toLong) else new Timestamp(0L),
      AUDIT_ID,
      CONT_DTM,
      if (CUST_NUM != null && CUST_NUM.length > 0) CUST_NUM.toLong else undefLong,
      if (SVC_MGMT_NUM != null && SVC_MGMT_NUM.length > 0) SVC_MGMT_NUM.toLong else undefLong,
      if (ACNT_NUM != null && ACNT_NUM.length > 0) ACNT_NUM.toLong else undefLong,
      TID,
      T_ID_NUM,
      MBR_CARD_NUM1,
      OUT_IDNT_NUM,
      INTEG_CONT_CL_CD,
      INTEG_CONT_OP_CL_CD,
      INTEG_CONT_RSN_CD,
      CMPGN_NUM,
      SRC_SYS_CL_CD,
      SRC_OWNR_NM,
      SRC_OBJ_NM,
      SRC_CHG_CD_VAL,
      SRC_RSN_CD_VAL,
      SRC_CONT_GRP_ID,
      if (SRC_SER_NUM != null && SRC_SER_NUM.length > 0) SRC_SER_NUM.toLong else undefLong,
      if (SRC_DTL_SER_NUM != null && SRC_DTL_SER_NUM.length > 0) SRC_DTL_SER_NUM.toLong else undefLong,
      CHNL_CONT_WAY_CD,
      CO_CL_CD,
      SVC_CD,
      if (RCV_SEQ != null) RCV_SEQ.toInt else undefInt,
      CONT_BF_KEY_CTT1, CONT_AF_KEY_CTT1,
      CONT_BF_KEY_CTT2, CONT_AF_KEY_CTT2,
      CONT_BF_ITM1, CONT_AF_ITM1, CONT_BF_ITM2, CONT_AF_ITM2,
      CONT_BF_ITM3, CONT_AF_ITM3, CONT_BF_ITM4, CONT_AF_ITM4,
      CONT_BF_ITM5, CONT_AF_ITM5, CONT_BF_ITM6, CONT_AF_ITM6,
      CONT_BF_ITM7, CONT_AF_ITM7, CONT_BF_ITM8, CONT_AF_ITM8,
      CONT_BF_ITM9, CONT_AF_ITM9, CONT_BF_ITM10, CONT_AF_ITM10,
      CONT_BF_ITM11, CONT_AF_ITM11, CONT_BF_ITM12, CONT_AF_ITM12,
      CONT_BF_ITM13, CONT_AF_ITM13, CONT_BF_ITM14, CONT_AF_ITM14,
      CONT_BF_ITM15, CONT_AF_ITM15, CONT_BF_ITM16, CONT_AF_ITM16,
      CONT_BF_ITM17, CONT_AF_ITM17, CONT_BF_ITM18, CONT_AF_ITM18,
      CONT_BF_ITM19, CONT_AF_ITM19, CONT_BF_ITM20, CONT_AF_ITM20,
      CONT_BF_ITM21, CONT_AF_ITM21, CONT_BF_ITM22, CONT_AF_ITM22,
      CONT_BF_ITM23, CONT_AF_ITM23, CONT_BF_ITM24, CONT_AF_ITM24,
      CONT_BF_ITM25, CONT_AF_ITM25, CONT_BF_ITM26, CONT_AF_ITM26,
      CONT_BF_ITM27, CONT_AF_ITM27, CONT_BF_ITM28, CONT_AF_ITM28,
      CONT_BF_ITM29, CONT_AF_ITM29, CONT_BF_ITM30, CONT_AF_ITM30,
      CONT_BF_ITM31, CONT_AF_ITM31, CONT_BF_ITM32, CONT_AF_ITM32,
      CONT_BF_ITM33, CONT_AF_ITM33, CONT_BF_ITM34, CONT_AF_ITM34,
      CONT_BF_ITM35, CONT_AF_ITM35, CONT_BF_ITM36, CONT_AF_ITM36,
      CONT_BF_ITM37, CONT_AF_ITM37, CONT_BF_ITM38, CONT_AF_ITM38,
      CONT_BF_ITM39, CONT_AF_ITM39, CONT_BF_ITM40, CONT_AF_ITM40,
      CONT_BF_ITM41, CONT_AF_ITM41, CONT_BF_ITM42, CONT_AF_ITM42,
      CONT_BF_ITM43, CONT_AF_ITM43, CONT_BF_ITM44, CONT_AF_ITM44,
      CONT_BF_ITM45, CONT_AF_ITM45, CONT_BF_ITM46, CONT_AF_ITM46,
      CONT_BF_ITM47, CONT_AF_ITM47, CONT_BF_ITM48, CONT_AF_ITM48,
      CONT_BF_ITM49, CONT_AF_ITM49, CONT_BF_ITM50, CONT_AF_ITM50
    )

  }

  /**
    * 문자열 값을 갖는 키 목록
    */
  val headerKeys1 = List(
    IntegContEvtId, IntegContClCd, IntegContOpClCd, IntegContRsnCd,
    SrcSysClCd, SrcOwnrNm, SrcObjNm, SrcChgCdVal, SrcRsnCdVal, ContDt, ContDtm,
    TosEventKey, TosEventDtm, TosSparkMapDtm, TosKafkaSendDtm
  )

  /**
    * Boolean 값을 갖는 키 목록
    */
  val headerKeys2 = List("TOS_IS_DB_SAVE", "TOS_IS_CEP_SEND")

  /**
    * JSON 포맷의 메세지를 Map 으로 변환
    *
    * {
    *   "HEADER": {
    *     "CONT_DT": "",
    *     "INTEG_CONT_EVT_ID": "",
    *     "CONT_IDNT_CL_CD": "",
    *     "CONT_IDNT_CL_VAL": "",
    *     "INS_SER_NUM": "",
    *     "AUDIT_DTM": "",
    *     "AUDIT_ID": "",
    *     "CONT_DTM": "",
    *     "CUST_NUM": "",
    *     "SVC_MGMT_NUM": "",
    *     "ACNT_NUM": "",
    *     "TID": "",
    *     "T_ID_NUM": "",
    *     "MBR_CARD_NUM1": "",
    *     "OUT_IDNT_NUM": "",
    *     "INTEG_CONT_CL_CD": "",
    *     "INTEG_CONT_OP_CL_CD": "",
    *     "INTEG_CONT_RSN_CD": "",
    *     "CMPGN_NUM": "",
    *     "SRC_SYS_CL_CD": "",
    *     "SRC_OWNR_NM": "",
    *     "SRC_OBJ_NM": "",
    *     "SRC_CHG_CD_VAL": "",
    *     "SRC_RSN_CD_VAL": "",
    *     "SRC_CONT_GRP_ID": "",
    *     "SRC_SER_NUM": "",
    *     "SRC_DTL_SER_NUM": "",
    *     "CHNL_CONT_WAY_CD": "",
    *     "CO_CL_CD": "",
    *     "SVC_CD": "",
    *     "RCV_SEQ": "",
    *     "TOS_EVENT_KEY": "",
    *     "TOS_EVENT_DTM": "",
    *     "TOS_IS_DB_SAVE": true,
    *     "TOS_IS_CEP_SEND": true,
    *     "TOS_SPARK_MAP_DTM": "",
    *     "TOS_KAFKA_SEND_DTM": ""
    *   },
    *   "BODY": {
    *     rawMap to json string
    *   }
    * }
    *
    * @param s
    * @return
    */
  def fromJsonToMap(s: ConsumerRecord[String, Array[Byte]]): Map[String, String] = {
    logTrace(s.toString)

    val message = new String(s.value())

    logTrace(message)

    val jsonValue = Json.parse(message)

    // 임시로 사용할 가변 맵

    val m = mutable.Map.empty[String, String]

    // 헤더 필드를 맵으로
    // TODO 헤더 필드는 필수 필드로 생각해, 없는 경우, 에러를 발생시켜야 하는가?

    try {
      val head = (jsonValue \ "HEADER").as[JsObject]

      head.fields.foreach(f =>
        if (headerKeys2.contains(f._1)) {
          m += (f._1 -> f._2.as[Boolean].toString)
        } else {
          m += (f._1 -> f._2.as[String])
        }
      )
    } catch {
      case ex: JsResultException =>
        ex.printStackTrace()
        logWarning(ex.getLocalizedMessage)
    }

    // 바디 필드를 맵으로

    try {
      val body = (jsonValue \ "BODY").as[JsObject]

      body.fields.foreach(f =>
        m += (f._1 -> f._2.as[String])
      )
    } catch {
      case ex: JsResultException =>
        ex.printStackTrace()
        logWarning(ex.getLocalizedMessage)
    }

    // 기본 필드 추가

    m += "kafka.partition" -> s.partition().toString
    m += "kafka.offset" -> s.offset().toString

    // 불변 맵으로 변환

    m.toMap
  }

  /**
    * 고정 칼럼 매핑 정보
    */
  private val constantKeys = List(
    ContDt -> ContDt, // 접촉일자
    IntegContEvtId -> IntegContEvtId, // 통합접촉이벤트ID
    ContIdntClCd -> ContIdntClCd, // 접촉식별구분코드
    ContIdntClVal -> ContIdntClVal, // 접촉식별구분값
//    InsSerNum -> InsSerNum, // 입력일련번호
//    AuditDtm -> AuditDtm, // 최종변경자ID
//    AuditId -> AuditId, // 최종변경일시
    ContDtm -> ContDtm, // 접촉일시
    CustNum -> CustNum,
    SvcMgmtNum -> SvcMgmtNum,
    AcntNum -> AcntNum,
    Tid -> Tid,
    TIdNum -> TIdNum,
    MbrCardNum1 -> MbrCardNum1,
    OutIdntNum -> OutIdntNum,
    IntegContClCd -> IntegContClCd, // 통합접촉구분코드, 메타에서 가져옴
    IntegContOpClCd -> IntegContOpClCd, // 통합접촉업무구분코드, 메타에서 가져옴
    IntegContRsnCd -> IntegContRsnCd, // 통합접촉사유코드
    CmpgnNum -> CmpgnNum,
    SrcSysClCd -> SrcSysClCd, //
    SrcOwnrNm -> SrcOwnrNm,
    SrcObjNm -> SrcObjNm,
    SrcChgCdVal -> SrcChgCdVal,
    SrcRsnCdVal -> SrcRsnCdVal,
    SrcContGrpId -> SrcContGrpId,
    SrcSerNum -> SrcSerNum,
    SrcDtlSerNum -> SrcDtlSerNum,
    ChnlContWayCd -> ChnlContWayCd,
    CoClCd -> CoClCd,
    SvcCd -> SvcCd,
    RcvSeq -> RcvSeq
  )

  /**
    * 변동 칼럼 매핑에서
    * EB0001-EB0019 와 EJ0001-EJ0223 이벤트가
    * 사용하는 매핑 스펙
    *
    * TODO 변수명 변경
    */
  private val constantMap = List(
    "BCHG_KEY_CTT1" -> ContBfKeyCtt1,
    "ACHG_KEY_CTT1" -> ContAfKeyCtt1,
    "BCHG_KEY_CTT2" -> ContBfKeyCtt2,
    "ACHG_KEY_CTT2" -> ContAfKeyCtt2,
    "BCHG_ITM1" -> ContBfItm1,
    "ACHG_ITM1" -> ContAfItm1,
    "BCHG_ITM2" -> ContBfItm2,
    "ACHG_ITM2" -> ContAfItm2,
    "BCHG_ITM3" -> ContBfItm3,
    "ACHG_ITM3" -> ContAfItm3,
    "BCHG_ITM4" -> ContBfItm4,
    "ACHG_ITM4" -> ContAfItm4,
    "BCHG_ITM5" -> ContBfItm5,
    "ACHG_ITM5" -> ContAfItm5,
    "BCHG_ITM6" -> ContBfItm6,
    "ACHG_ITM6" -> ContAfItm6,
    "BCHG_ITM7" -> ContBfItm7,
    "ACHG_ITM7" -> ContAfItm7,
    "BCHG_ITM8" -> ContBfItm8,
    "ACHG_ITM8" -> ContAfItm8,
    "BCHG_ITM9" -> ContBfItm9,
    "ACHG_ITM9" -> ContAfItm9,
    "BCHG_ITM10" -> ContBfItm10,
    "ACHG_ITM10" -> ContAfItm10,
    "BCHG_ITM11" -> ContBfItm11,
    "ACHG_ITM11" -> ContAfItm11,
    "BCHG_ITM12" -> ContBfItm12,
    "ACHG_ITM12" -> ContAfItm12,
    "BCHG_ITM13" -> ContBfItm13,
    "ACHG_ITM13" -> ContAfItm13,
    "BCHG_ITM14" -> ContBfItm14,
    "ACHG_ITM14" -> ContAfItm14,
    "BCHG_ITM15" -> ContBfItm15,
    "ACHG_ITM15" -> ContAfItm15,
    "BCHG_ITM16" -> ContBfItm16,
    "ACHG_ITM16" -> ContAfItm16,
    "BCHG_ITM17" -> ContBfItm17,
    "ACHG_ITM17" -> ContAfItm17,
    "BCHG_ITM18" -> ContBfItm18,
    "ACHG_ITM18" -> ContAfItm18,
    "BCHG_ITM19" -> ContBfItm19,
    "ACHG_ITM19" -> ContAfItm19,
    "BCHG_ITM20" -> ContBfItm20,
    "ACHG_ITM20" -> ContAfItm20,
    "BCHG_ITM21" -> ContBfItm21,
    "ACHG_ITM21" -> ContAfItm21,
    "BCHG_ITM22" -> ContBfItm22,
    "ACHG_ITM22" -> ContAfItm22,
    "BCHG_ITM23" -> ContBfItm23,
    "ACHG_ITM23" -> ContAfItm23,
    "BCHG_ITM24" -> ContBfItm24,
    "ACHG_ITM24" -> ContAfItm24,
    "BCHG_ITM25" -> ContBfItm25,
    "ACHG_ITM25" -> ContAfItm25,
    "BCHG_ITM26" -> ContBfItm26,
    "ACHG_ITM26" -> ContAfItm26,
    "BCHG_ITM27" -> ContBfItm27,
    "ACHG_ITM27" -> ContAfItm27,
    "BCHG_ITM28" -> ContBfItm28,
    "ACHG_ITM28" -> ContAfItm28,
    "BCHG_ITM29" -> ContBfItm29,
    "ACHG_ITM29" -> ContAfItm29,
    "BCHG_ITM30" -> ContBfItm30,
    "ACHG_ITM30" -> ContAfItm30,
    "BCHG_ITM31" -> ContBfItm31,
    "ACHG_ITM31" -> ContAfItm31,
    "BCHG_ITM32" -> ContBfItm32,
    "ACHG_ITM32" -> ContAfItm32,
    "BCHG_ITM33" -> ContBfItm33,
    "ACHG_ITM33" -> ContAfItm33,
    "BCHG_ITM34" -> ContBfItm34,
    "ACHG_ITM34" -> ContAfItm34,
    "BCHG_ITM35" -> ContBfItm35,
    "ACHG_ITM35" -> ContAfItm35,
    "BCHG_ITM36" -> ContBfItm36,
    "ACHG_ITM36" -> ContAfItm36,
    "BCHG_ITM37" -> ContBfItm37,
    "ACHG_ITM37" -> ContAfItm37,
    "BCHG_ITM38" -> ContBfItm38,
    "ACHG_ITM38" -> ContAfItm38,
    "BCHG_ITM39" -> ContBfItm39,
    "ACHG_ITM39" -> ContAfItm39,
    "BCHG_ITM40" -> ContBfItm40,
    "ACHG_ITM40" -> ContAfItm40,
    "BCHG_ITM41" -> ContBfItm41,
    "ACHG_ITM41" -> ContAfItm41,
    "BCHG_ITM42" -> ContBfItm42,
    "ACHG_ITM42" -> ContAfItm42,
    "BCHG_ITM43" -> ContBfItm43,
    "ACHG_ITM43" -> ContAfItm43,
    "BCHG_ITM44" -> ContBfItm44,
    "ACHG_ITM44" -> ContAfItm44,
    "BCHG_ITM45" -> ContBfItm45,
    "ACHG_ITM45" -> ContAfItm45,
    "BCHG_ITM46" -> ContBfItm46,
    "ACHG_ITM46" -> ContAfItm46,
    "BCHG_ITM47" -> ContBfItm47,
    "ACHG_ITM47" -> ContAfItm47,
    "BCHG_ITM48" -> ContBfItm48,
    "ACHG_ITM48" -> ContAfItm48,
    "BCHG_ITM49" -> ContBfItm49,
    "ACHG_ITM49" -> ContAfItm49,
    "BCHG_ITM50" -> ContBfItm50,
    "ACHG_ITM50" -> ContAfItm50
  )

  /**
    * 변동 칼럼 매핑 정보
    *
    * @param mainEventId 통합접촉이벤트ID
    * @param subEventId 통합접촉사유코드
    * @param upstreamId 원천시스템구분코드
    * @param dsubEventId 접촉사유하위일련번호
    * @return
    */
  def variableKeys(metaHead: Map[String, String], metaDetail: Map[String, List[(String, String)]], mainEventId: String, subEventId: String, upstreamId: String, dsubEventId: String): Option[List[(String, String)]] = {
    if ("EB0001" <= mainEventId && mainEventId <= "EB0019" || "EJ0001" <= mainEventId && mainEventId <= "EJ0223")
      Some(constantMap)
    else {
      val key = metaHead.get(s"$mainEventId.$subEventId.$upstreamId") match {
        case Some("Y") => s"$mainEventId.$subEventId.$upstreamId.$dsubEventId"
        case Some("N") => s"$mainEventId.**.$upstreamId.$dsubEventId"
        case _ =>
          throw new Exception(s"Dirty Meta: 이벤트($mainEventId-$subEventId($upstreamId))에 대한 메타 정보가 관리되지 않고 있음")
      }

      metaDetail.get(key)
    }
  }

  /**
    * 상류에서 사용하는 필드명을 사용하는 맵에서
    * 오라클에 저장할 필드명을 사용하는 맵으로 변환
    * 이벤트에 따라 항목 구성이 다름.
    * 이벤트별 항목 구성 정보는 메타 정보를 사용함.
    *
    * @param source
    * @return
    */
  def fromMapOfKafkaToMapOfOracle(metaHead: Map[String, String], metaDetail: Map[String, List[(String, String)]])(source: Map[String, String]): Map[String, String] = {
    logTrace(source.toString)

    val mainEventId = source.getOrElse(IntegContEvtId, null)
    val subEventId = source.getOrElse(IntegContRsnCd, null)
    val upstreamId = source.getOrElse(SrcSysClCd, null)
    val dsubEventId = source.getOrElse(SrcDtlSerNum, "1") // 유선서비스변경이력의 상세일련번호와 동일한 값임.

    // OracleRecord 에서 사용되는 칼럼으로 구성된 레코드를 위한 맵
    val target = mutable.Map.empty[String, String]

    // 고정 칼럼 매핑 정보를 활용해, OracleRecord 에서 사용되는 칼럼 구성으로 매핑함
    // TODO 필수 필드에 위반될 경우, 에러 처리해야 하는가? 일단, 필수 필드는 아닌 것으로 보임.
    constantKeys.foreach(key => {
      val v = source.get(key._1)

      v match {
        case Some(x) => target += (key._2 -> x)
        case None =>
      }
    })

    try {
      // 칼럼 매핑 정보를 활용해, OracleRecord 에서 사용되는 칼럼 구성으로 매핑함
      variableKeys(metaHead, metaDetail, mainEventId, subEventId, upstreamId, dsubEventId) match {
        case Some(mappings) =>
          mappings.foreach(key => {
            source.get(key._1) match {
              case Some(x) => target += (key._2 -> x)
              case None =>
            }
          })
        case None =>
      }
    } catch {
      case ex: Exception =>
        logWarning(ex.getLocalizedMessage)
        logWarning(source.toString)
    }

    // 감사용 칼럼 설정
    target += AuditDtm -> System.currentTimeMillis().toString
    target += AuditId -> "SPARK"

    // 입력일련번호

    val offset = source.get("kafka.offset") match {
      case Some(x) => x.toLong
      case None => 0L
    }

    val partition = source.get("kafka.partition") match {
      case Some(x) => x.toInt
      case None => 0
    }

    // 16 개의 파티션까지 허용 TODO 생산환경에서는 성능을 위해 쉬프트 연산자 사용
//    target += InsSerNum -> (offset << 4 + partition).toString
      target += InsSerNum -> (offset * 100 + partition).toString

    // 불변 맵으로 변환
    target.toMap
  }
}

/**
  * 통합 접촉 이력 레코드 클래스
  */
case class OracleRecord(
  CONT_DT: String, // 접촉일자 not nullable
  INTEG_CONT_EVT_ID: String, // 통합접촉이벤트ID not nullable
  CONT_IDNT_CL_CD: String, // 접촉식별구분코드 not nullable
  CONT_IDNT_CL_VAL: String, // 접촉식별구분값 not nullable
  INS_SER_NUM: Long, // 입력일련번호 not nullable
  AUDIT_DTM: Timestamp, // 최종변경일시 not nullable
  AUDIT_ID: String, // 최종변경자ID not nullable
  CONT_DTM: String, // 접촉일시 not nullable
  CUST_NUM: Long, // 고객번호
  SVC_MGMT_NUM: Long, // 서비스관리번호
  ACNT_NUM: Long, // 계정번호
  TID: String,
  T_ID_NUM: String,
  MBR_CARD_NUM1: String, // 멤버쉽카드번호1
  OUT_IDNT_NUM: String, // 외부식별번호
  INTEG_CONT_CL_CD: String, // 통합접촉구분코드 not nullable
  INTEG_CONT_OP_CL_CD: String, // 통합접촉업무구분코드 not nullable
  INTEG_CONT_RSN_CD: String, // 통합접촉사유코드 not nullable
  CMPGN_NUM: String, // 캠페인번호
  SRC_SYS_CL_CD: String, // 원천시스템구분코드 not nullable
  SRC_OWNR_NM: String, // 원천소유자명
  SRC_OBJ_NM: String, // 원천대상명
  SRC_CHG_CD_VAL: String, // 원천변경코드값
  SRC_RSN_CD_VAL: String, // 원천사유코드값
  SRC_CONT_GRP_ID: String, // 원천접촉그룹ID
  SRC_SER_NUM: Long, // 원천일련번호
  SRC_DTL_SER_NUM: Long, // 원천상세일련번호
  CHNL_CONT_WAY_CD: String, // 채널접촉수단코드
  CO_CL_CD: String, // 회사구분코드
  SVC_CD: String, // 서비스구분코드
  RCV_SEQ: Int, // 접수순번
  CONT_BF_KEY_CTT1: String, // 접촉전키내용1
  CONT_AF_KEY_CTT1: String, // 접촉후키내용1
  CONT_BF_KEY_CTT2: String, // 접촉전키내용2
  CONT_AF_KEY_CTT2: String, // 접촉후키내용2
  CONT_BF_ITM1: String, // 접촉전항목1 ~ 50
  CONT_AF_ITM1: String, // 접촉후항목1 ~ 50
  CONT_BF_ITM2: String,
  CONT_AF_ITM2: String,
  CONT_BF_ITM3: String,
  CONT_AF_ITM3: String,
  CONT_BF_ITM4: String,
  CONT_AF_ITM4: String,
  CONT_BF_ITM5: String,
  CONT_AF_ITM5: String,
  CONT_BF_ITM6: String,
  CONT_AF_ITM6: String,
  CONT_BF_ITM7: String,
  CONT_AF_ITM7: String,
  CONT_BF_ITM8: String,
  CONT_AF_ITM8: String,
  CONT_BF_ITM9: String,
  CONT_AF_ITM9: String,
  CONT_BF_ITM10: String,
  CONT_AF_ITM10: String,
  CONT_BF_ITM11: String,
  CONT_AF_ITM11: String,
  CONT_BF_ITM12: String,
  CONT_AF_ITM12: String,
  CONT_BF_ITM13: String,
  CONT_AF_ITM13: String,
  CONT_BF_ITM14: String,
  CONT_AF_ITM14: String,
  CONT_BF_ITM15: String,
  CONT_AF_ITM15: String,
  CONT_BF_ITM16: String,
  CONT_AF_ITM16: String,
  CONT_BF_ITM17: String,
  CONT_AF_ITM17: String,
  CONT_BF_ITM18: String,
  CONT_AF_ITM18: String,
  CONT_BF_ITM19: String,
  CONT_AF_ITM19: String,
  CONT_BF_ITM20: String,
  CONT_AF_ITM20: String,
  CONT_BF_ITM21: String,
  CONT_AF_ITM21: String,
  CONT_BF_ITM22: String,
  CONT_AF_ITM22: String,
  CONT_BF_ITM23: String,
  CONT_AF_ITM23: String,
  CONT_BF_ITM24: String,
  CONT_AF_ITM24: String,
  CONT_BF_ITM25: String,
  CONT_AF_ITM25: String,
  CONT_BF_ITM26: String,
  CONT_AF_ITM26: String,
  CONT_BF_ITM27: String,
  CONT_AF_ITM27: String,
  CONT_BF_ITM28: String,
  CONT_AF_ITM28: String,
  CONT_BF_ITM29: String,
  CONT_AF_ITM29: String,
  CONT_BF_ITM30: String,
  CONT_AF_ITM30: String,
  CONT_BF_ITM31: String,
  CONT_AF_ITM31: String,
  CONT_BF_ITM32: String,
  CONT_AF_ITM32: String,
  CONT_BF_ITM33: String,
  CONT_AF_ITM33: String,
  CONT_BF_ITM34: String,
  CONT_AF_ITM34: String,
  CONT_BF_ITM35: String,
  CONT_AF_ITM35: String,
  CONT_BF_ITM36: String,
  CONT_AF_ITM36: String,
  CONT_BF_ITM37: String,
  CONT_AF_ITM37: String,
  CONT_BF_ITM38: String,
  CONT_AF_ITM38: String,
  CONT_BF_ITM39: String,
  CONT_AF_ITM39: String,
  CONT_BF_ITM40: String,
  CONT_AF_ITM40: String,
  CONT_BF_ITM41: String,
  CONT_AF_ITM41: String,
  CONT_BF_ITM42: String,
  CONT_AF_ITM42: String,
  CONT_BF_ITM43: String,
  CONT_AF_ITM43: String,
  CONT_BF_ITM44: String,
  CONT_AF_ITM44: String,
  CONT_BF_ITM45: String,
  CONT_AF_ITM45: String,
  CONT_BF_ITM46: String,
  CONT_AF_ITM46: String,
  CONT_BF_ITM47: String,
  CONT_AF_ITM47: String,
  CONT_BF_ITM48: String,
  CONT_AF_ITM48: String,
  CONT_BF_ITM49: String,
  CONT_AF_ITM49: String,
  CONT_BF_ITM50: String,
  CONT_AF_ITM50: String
)
