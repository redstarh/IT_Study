package com.sktelecom.tos.stream.oracle

import java.util.Properties

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

/**
  * 매핑을 위한 메타 정보 관리자
  *
  * @param session
  * @param jdbcConnUrl
  * @param jdbcConnProp
  */
class OracleTransformerMetaManager(session: SparkSession, jdbcConnUrl: String, jdbcConnProp: Properties) extends Logging {
  final val IntegContMetTable = "TCIC.TCIC_INTEG_CONT_MET"
  final val IntegContRsnSpcTable = "TCIC.TCIC_INTEG_CONT_RSN_SPC"
  final val IntegContEvtId = "INTEG_CONT_EVT_ID"
  final val IntegContRsnCd = "INTEG_CONT_RSN_CD"
  final val SrcSysClCd = "SRC_SYS_CL_CD"
  final val RsnMetMgmtYn = "RSN_MET_MGMT_YN"
  final val ContRsnSubSerNum = "CONT_RSN_SUB_SER_NUM"
  final val ItmNm = "ITM_NM"
  final val StrdEngNm = "STRD_ENG_NM"

  @volatile private var instance: Broadcast[(Map[String, String], Map[String, List[(String, String)]])] = null

  def getInstance(): Broadcast[(Map[String, String], Map[String, List[(String, String)]])] = {
    if (instance == null) {
      val metaHead = selectKafka2OracleMetaMeta()
      val metaDetail = selectKafka2OracleMeta(metaHead)
      instance = session.sparkContext.broadcast((metaHead, metaDetail))
    }

    instance
  }

  def reload(): Unit = {
    instance = null
  }
/*

  def getInstance2(session: SparkSession, jdbcConnUrl: String, jdbcConnProp: Properties): (Map[String, String], Map[String, List[(String, String)]]) = {
    val metaHead = selectKafka2OracleMetaMeta(session, jdbcConnUrl, jdbcConnProp)
    val metaDetail = selectKafka2OracleMeta(session, jdbcConnUrl, jdbcConnProp, metaHead)
    (metaHead, metaDetail)
  }
*/

  /**
    * 이벤트를 저장할 경우, 메타정보의 사용할 것인가에 관한 정보를 로딩
    *
    * 'EJ0001' ~ 'EJ0223', 'EB0001' ~ 'EB0019' 이벤트를 제외한
    * 매핑을 구성
    *
    * @return
    */
  def selectKafka2OracleMetaMeta(): Map[String, String] = {

    // TODO : Channel 매핑용 쿼리 필요함.

    // 통합접촉사유명세(TCIC.TCIC_INTEG_CONT_RSN_SPC)
    /*
      전후사용구분코드(BAF_USE_CL_CD)
      사용여부(USE_YN)
     */
    // 통합접촉메타(TCIC.TCIC_INTEG_CONT_MET)
    /*
      항목명(ITM_NM)             통합접촉이력의 항목명
      표준영문명(STRD_ENG_NM)    원천 항목명
      표준한글명(STRD_HAN_NM)
     */

    // 메타 정보 사용 여부에 관한 정보 관리 Y, N
    val metaMetaMap = mutable.Map.empty[String, String]

    // 통합접촉사유명세
    val metaMeta = session.read.jdbc(jdbcConnUrl, IntegContRsnSpcTable, jdbcConnProp).collect()

    logInfo(s"loaded ${metaMeta.length} records from $IntegContRsnSpcTable")

    // 데이터의 양이 그다지 많지 않기 때문에 아래의 과정은 드라이버 노드에서 수행.
    // 만약, 필요하다면, 워커 노드로 분산할 필요가 있음.

    // 'EJ0001' ~ 'EJ0223', 'EB0001' ~ 'EB0019' 이벤트는 관리할 필요 없음
    metaMeta
      .filter {r =>
        val eventId = r.getAs[String](IntegContEvtId)
        r.getAs[String]("USE_YN") == "Y" &&
          ! ("EB0001" <= eventId && eventId <= "EB0019" || "EJ0001" <= eventId && eventId <= "EJ0223")
      }
      .foreach(r => {
        // INTEG_CONT_EVT_ID, INTEG_CONT_RSN_CD, SRC_SYS_CL_CD  -> BAF_USE_CL_CD
        val k1 = r.getAs[String](IntegContEvtId)
        val k2 = r.getAs[String](IntegContRsnCd)
        val k3 = r.getAs[String](SrcSysClCd)
        val v1 = r.getAs[String](RsnMetMgmtYn)   // Y, N
        //        val v2 = r.getAs[String]("BAF_USE_CL_CD")   // *, AF, ALL, BF

        // v1 == "N" 인 경우, k2 == "**" 를 사용해서 다시 검색해야 함.
        // 실제 모든 데이터가 "AF" 이어서, 이 코드를 그냥 사용해도 됨.
        metaMetaMap += s"$k1.$k2.$k3" -> v1
      })

    logInfo(s"constructed a map of ${metaMetaMap.size}")

    // convert to immutable map
    metaMetaMap.toMap
  }

  /**
    * 이벤트를 저장할 경우, 사용할 메타정보를 로딩
    *
    * @param metaMetaMap
    * @return
    */
  def selectKafka2OracleMeta(metaMetaMap: Map[String, String]): Map[String, List[(String, String)]] = {

    // TODO : Channel 매핑용 쿼리 필요함.

    // 통합접촉사유명세(TCIC.TCIC_INTEG_CONT_RSN_SPC)
    /*
      전후사용구분코드(BAF_USE_CL_CD)
      사용여부(USE_YN)
     */
    // 통합접촉메타(TCIC.TCIC_INTEG_CONT_MET)
    /*
      항목명(ITM_NM)             통합접촉이력의 항목명
      표준영문명(STRD_ENG_NM)    원천 항목명
      표준한글명(STRD_HAN_NM)
     */

    // 원시 칼럼에서 목적 칼럼으로 지정하는 맵

    val metaMap = mutable.Map.empty[String, mutable.ListBuffer[(String, String)]]

    // 통합접촉메타
    val meta = session.read.jdbc(jdbcConnUrl, IntegContMetTable, jdbcConnProp).collect()

    logInfo(s"loaded ${meta.length} records from $IntegContMetTable")

    // 데이터의 양이 그다지 많지 않기 때문에 아래의 과정은 드라이버 노드에서 수행.
    // 만약, 필요하다면, 워커 노드로 분산할 필요가 있음.

    meta
      .filter { r =>
        val eventId = r.getAs[String](IntegContEvtId)
        ! ("EB0001" <= eventId && eventId <= "EB0019" || "EJ0001" <= eventId && eventId <= "EJ0223")
      }
      .foreach(r => {

        val k1 = r.getAs[String](IntegContEvtId)
        val k2 = r.getAs[String](IntegContRsnCd)
        val k3 = r.getAs[String](SrcSysClCd)
        val k4 = r.getAs[java.math.BigDecimal](ContRsnSubSerNum).intValue()
        val k5 = r.getAs[String](ItmNm)
        val v = r.getAs[String](StrdEngNm)

        // 필드 매핑 타입
        val fsType = metaMetaMap.get(s"$k1.$k2.$k3") // Y, N

        val metaKey = fsType match {
          case Some("Y") =>
            s"$k1.$k2.$k3.$k4"
          case Some("N") =>
            s"$k1.**.$k3.$k4"
          case _ =>
            throw new Exception(s"Dirty Meta: 이벤트($k1-$k2($k3))에 대한 메타 정보가 관리되지 않고 있음")
        }

        val lb =
          if (metaMap.contains(metaKey))
            metaMap(metaKey)
          else {
            val lb = mutable.ListBuffer.empty[(String, String)]
            metaMap.put(metaKey, lb)
            lb
          }

        // 목적 칼럼명 사용방법에 따라 목적 칼럼명의 생성
        lb += v -> (k5.substring(0, 4) + "_AF" + k5.substring(4))

      })

    logInfo(s"constructed a map of ${metaMap.size} lists")

    // convert to immutable list, immutable map
    metaMap.map(e => {
      e._1 -> e._2.toList
    }).toMap
  }

}
