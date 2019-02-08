package com.sktelecom.tos.stream.base.meta

import java.util.Properties

import scala.collection.mutable

import org.apache.log4j.Logger
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

import com.sktelecom.tos.stream.base.meta.info.MetaInfo
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * Database 에서 event mapping 정보와 cep 전송 여부, cep 전송 포맷을 조회하여
 * MetaInfo 클래스에 데이터를 저장하는 역할을 한다.
 *
 * @param spark   SparkSession
 * @param metaInfoConf database 접속 정보
 * @param metaInfoSql sql 정보
 */
class MetaInfoLoader(spark: SparkSession, metaInfoConf: Config, meatInfoSql: Config) {

  @transient lazy val Log = Logger.getLogger(getClass)

  import net.ceedubs.ficus.Ficus._
  import scala.collection.JavaConversions._

  // jdbc url
  val jdbcUrl = metaInfoConf.as[String]("jdbcUrl")

  // metainfo reload 여부
  val isLoop = metaInfoConf.as[Boolean]("isLoop")

  // 원천 시스템 명
  // SWG, R2K, BLT, TRM, URT, TWO, TWD, MEM, WIN, LAK
  val srcSysName = metaInfoConf.as[String]("srcSysName")

  // jdbc properties
  val jdbcConnProp = new Properties()
  jdbcConnProp.putAll(metaInfoConf.as[Map[String, String]]("connProp"))

  /**
   * Database 에서 event mapping 정보와 cep 전송 여부, cep 전송 포맷을 조회하여
   * MetaInfo 클래스에 데이터를 저장하고 isLoop 값에 따라 동적 재로딩을 한다.
   *
   * @return broadcast 변수로 이루어진 meta 정보
   */
  def start(): MetaInfo = {

    // meta loader
    val metaInfo: MetaInfo = new MetaInfo()

    var cepStartTime = if (metaInfoConf.hasPath("cepStartTime")) metaInfoConf.as[String]("cepStartTime") else "0000"
    var cepEndTime = if (metaInfoConf.hasPath("cepEndTime")) metaInfoConf.as[String]("cepEndTime") else "0000"

    metaInfo.cepSendTime = spark.sparkContext.broadcast((cepStartTime, cepEndTime))

    metaInfo.objNameSpec = spark.sparkContext.broadcast(selectObjNameSpec())
    metaInfo.eventReasonSpec = spark.sparkContext.broadcast(selectEventReasonSpec())
    metaInfo.eventCodeMappingSpec = spark.sparkContext.broadcast(selectEventCodeMappingSpec())
    metaInfo.eventFilterSpec = spark.sparkContext.broadcast(selectEventFilterSpec())
    // TODO : DB 설계 후 추가 개발 필요
    //    metaInfo.eventUrlSpec = spark.sparkContext.broadcast(selectEventUrlSpec())

    metaInfo.cepEvent = spark.sparkContext.broadcast(selectCepEvent())
    metaInfo.cepEventFormat = spark.sparkContext.broadcast(selectCepEventFormat())

    metaInfo
  }

  /**
   * $srcSysName 변수를 mapping 처리하고,
   * queryId 쿼리를 spark.read.jdbc 를 이용하여 실행한다.
   *
   * @return 쿼리 실행 결과
   */
  private def selectQuery(queryId: String): Array[Row] = {

    var inValue: String = ""
    srcSysName.split(",", -1).foreach(record => {
      if (inValue.length() > 0) {
        inValue = inValue + ","
      }
      inValue = inValue + "'" + record.trim() + "'"
    })

    val query = meatInfoSql.as[String](queryId).replaceAll("\\$srcSysName", inValue)
    Log.info(s"queryId: $queryId -> $query")
    spark.read.jdbc(jdbcUrl, query, jdbcConnProp).collect()
  }

  /**
   * Event Type 을 조회한다.
   *
   * @return event type Map
   *         mutable.Map["OBJ_NM", "EVT_MAPP_TYP_CD"]
   */
  def selectObjNameSpec(): mutable.Map[String, (String, String, String, String)] = {

    val objNameSpecRow = selectQuery("objNameSpec.select")

    val objNameSpecMap = mutable.Map[String, (String, String, String, String)]()

    objNameSpecRow.foreach(record => {

      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val ownrName = record.getAs[String]("SRC_OWNR_NM").trim
      var objName = record.getAs[String]("SRC_OBJ_NM").trim
      if (ownrName != null && !ownrName.equals("**")) objName = ownrName + "." + objName
      val mappingTypeCode = record.getAs[String]("EVT_MAPP_TYP_CD").trim
      val cepHstFilterYn = record.getAs[String]("CEP_HST_FLT_YN").trim
      var cepHstFilterName = ""
      var cepHstFilterVal = ""
      if(cepHstFilterYn.equals("Y")) {
        cepHstFilterName = record.getAs[String]("CEP_HST_FLT_COL_NM")
        cepHstFilterVal = record.getAs[String]("CEP_HST_FLT_VAL")
      }

      val mapKey = srcName + "_" + objName
      objNameSpecMap += mapKey -> (mappingTypeCode, cepHstFilterYn, cepHstFilterName, cepHstFilterVal)
    })

    objNameSpecMap

  }

  /**
   * 이벤트 사유 목록을 조회한다.
   *
   * @return 이벤트 사유 목록
   *         mutable.Map["OBJ_NM", mutable.Map[("SRC_CHG_ITM_NM", "SRC_CHG_CD_VAL"), mutable.Map[("SRC_RSN_ITM_NM", "SRC_RSN_CD_VAL"), ("INTEG_CONT_EVT_ID", "INTEG_CONT_RSN_CD")]]]
   */
  def selectEventReasonSpec(): mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]] = {
    val eventReasonSpecRow = selectQuery("eventReasonSpec.select")

    val eventReasonSpecMap = mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]]()

    eventReasonSpecRow.foreach(record => {

      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val ownrName = record.getAs[String]("SRC_OWNR_NM").trim
      var objName = record.getAs[String]("SRC_OBJ_NM").trim
      if (ownrName != null && !ownrName.equals("**")) objName = ownrName + "." + objName

      val chgItemName = record.getAs[String]("SRC_CHG_ITM_NM").trim
      val chgCdVal = record.getAs[String]("SRC_CHG_CD_VAL").trim
      val rsnItemName = record.getAs[String]("SRC_RSN_ITM_NM").trim
      val rsnCdVal = record.getAs[String]("SRC_RSN_CD_VAL").trim

      val eventId = record.getAs[String]("INTEG_CONT_EVT_ID").trim
      val reasonCode = record.getAs[String]("INTEG_CONT_RSN_CD").trim

      val integCode = record.getAs[String]("INTEG_CONT_CL_CD").trim
      val integBizCode = record.getAs[String]("INTEG_CONT_OP_CL_CD").trim

      val mapKey = srcName + "_" + objName
      val changeItemMap = eventReasonSpecMap.getOrElseUpdate(mapKey, mutable.Map((chgItemName, chgCdVal) -> mutable.Map((rsnItemName, rsnCdVal) -> (eventId, reasonCode, integCode, integBizCode))))
      val rsnItemMap = changeItemMap.getOrElseUpdate((chgItemName, chgCdVal), mutable.Map((rsnItemName, rsnCdVal) -> (eventId, reasonCode, integCode, integBizCode)))
      rsnItemMap += (rsnItemName, rsnCdVal) -> (eventId, reasonCode, integCode, integBizCode)

    })

    eventReasonSpecMap
  }

  /**
   * 이벤트 코드 목록을 조회한다.
   *
   * @return 이벤트 코드 목록
   *         mutable.Map["OBJ_NM", mutable.Map[("SRC_ITM_NM1", "SRC_CD_VAL1"), mutable.Map[("SRC_ITM_NM2", "SRC_CD_VAL2"), ("INTEG_CONT_EVT_ID", "INTEG_CONT_RSN_CD")]]]
   */
  def selectEventCodeMappingSpec(): mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]] = {
    val eventCodeMappingSpecRow = selectQuery("eventCodeMappingSpec.select")

    val eventCodeMappingSpecMap = mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]]()

    eventCodeMappingSpecRow.foreach(record => {

      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val ownrName = record.getAs[String]("SRC_OWNR_NM").trim
      var objName = record.getAs[String]("SRC_OBJ_NM").trim
      if (ownrName != null && !ownrName.equals("**")) objName = ownrName + "." + objName

      val itemName1 = record.getAs[String]("SRC_ITM_NM1").trim
      val itemCdVal1 = record.getAs[String]("SRC_CD_VAL1").trim
      val itemName2 = record.getAs[String]("SRC_ITM_NM2").trim
      val itemCdVal2 = record.getAs[String]("SRC_CD_VAL2").trim

      val eventId = record.getAs[String]("INTEG_CONT_EVT_ID").trim
      val reasonCode = record.getAs[String]("INTEG_CONT_RSN_CD").trim

      val integCode = record.getAs[String]("INTEG_CONT_CL_CD").trim
      val integBizCode = record.getAs[String]("INTEG_CONT_OP_CL_CD").trim

      val mapKey = srcName + "_" + objName

      val itemName1Map = eventCodeMappingSpecMap.getOrElseUpdate(mapKey, mutable.Map((itemName1, itemCdVal1) -> mutable.Map((itemName2, itemCdVal2) -> (eventId, reasonCode, integCode, integBizCode))))
      val itemName2Map = itemName1Map.getOrElseUpdate((itemName1, itemCdVal1), mutable.Map((itemName2, itemCdVal2) -> (eventId, reasonCode, integCode, integBizCode)))
      itemName2Map += (itemName2, itemCdVal2) -> (eventId, reasonCode, integCode, integBizCode)

    })

    eventCodeMappingSpecMap

  }

  /**
   * TODO : 추가 개발 필요
   * 이벤트 필터 목록을 조회한다. (INTEG_CONT_EVT_ID == 'EU0013')
   *
   * @return 이벤트 필터 목록
   *         mutable.Map["OBJ_NM", mutable.Map[("INTEG_CONT_EVT_ID", "INTEG_CONT_RSN_CD"), Seq[("SRC_ITM_NM", "SRC_FLT_CALCU_CD", "SRC_FLT_VAL")]]]
   */
  def selectEventFilterSpec(): mutable.Map[String, mutable.Map[(String, String), Seq[(String, String, String, String, Map[String,String] => String)]]] = {
    val eventFilterSpecRow = selectQuery("eventFilterSpec.select")

    val eventFilterSpecMap = mutable.Map[String, mutable.Map[(String, String), Seq[(String, String, String, String, Map[String,String] => String)]]]()

    eventFilterSpecRow.foreach(record => {

      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val ownrName = record.getAs[String]("SRC_OWNR_NM").trim
      var objName = record.getAs[String]("SRC_OBJ_NM").trim
      if (ownrName != null && !ownrName.equals("**")) objName = ownrName + "." + objName

      
      val itemName = record.getAs[String]("SRC_ITM_NM").trim
      val itemFltCalCd = record.getAs[String]("SRC_FLT_CALCU_CD").trim
      val itemFltVal = record.getAs[String]("SRC_FLT_VAL")

      val itemFltColUseYn = record.getAs[String]("FLT_COL_USE_YN").trim
      var itemFltColInfo:Map[String,String] => String = null
      if(itemFltColUseYn.equals("Y")) {
        itemFltColInfo = _compile(record.getAs[String]("SRC_FLT_COL_INFO").trim)
        
      }
             
      val eventId = record.getAs[String]("INTEG_CONT_EVT_ID").trim
      val reasonCode = record.getAs[String]("INTEG_CONT_RSN_CD").trim
          
      val mapKey = srcName + "_" + objName

      val eventMap = eventFilterSpecMap.getOrElseUpdate(mapKey, mutable.Map((eventId, reasonCode) -> Seq((itemName, itemFltCalCd, itemFltColUseYn, itemFltVal, itemFltColInfo))))
      var filterSeq = eventMap.getOrElseUpdate((eventId, reasonCode), Seq((itemName, itemFltCalCd, itemFltColUseYn, itemFltVal, itemFltColInfo)))
      filterSeq = filterSeq :+ (itemName, itemFltCalCd, itemFltColUseYn, itemFltVal, itemFltColInfo)

    })

    eventFilterSpecMap

  }

  /**
   * TODO : DB 설계 후 코딩 진행
   * 이벤트 URL 목록을 조회한다.
   *
   * @return 이벤트 URL 목록
   */
  def selectEventUrlSpec(): Unit = {
    val eventUrlSpecRow = selectQuery("eventUrlSpec.select")
  }

  /**
   * cep 전송 이벤트 목록을 조회한다.
   *
   * @return cep 이벤트 목록
   *         Seq["INTEG_CONT_EVT_ID"_"INTEG_CONT_RSN_CD"]
   */
  def selectCepEvent(): Seq[String] = {

    val cepEventRow = selectQuery("cepEvent.select")

    var cepEventSeq = Seq[String]()

    cepEventRow.foreach(record => {
      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val eventId = record.getAs[String]("INTEG_CONT_EVT_ID").trim
      val rsnCode = record.getAs[String]("INTEG_CONT_RSN_CD").trim
      cepEventSeq = cepEventSeq :+ srcName + "_" + eventId + "_" + rsnCode
    })

    cepEventSeq
  }

  /**
   * cep formatting 목록을 조회한다.
   *
   * @return cep format 목록
   *         mutable.Map["INTEG_CONT_EVT_ID"_"INTEG_CONT_RSN_CD", mutable.Map["SRC_ITM_NM", "ITM_STRD_ENG_NM"]]
   */
  def selectCepEventFormat(): mutable.Map[String, mutable.Map[String, String]] = {

    val cepEventFormatRow = selectQuery("cepEventFormat.select")

    val cepEventFormatMap = mutable.Map[String, mutable.Map[String, String]]()

    cepEventFormatRow.foreach(record => {
      val srcName = record.getAs[String]("SRC_SYS_CL_CD").trim
      val eventId = record.getAs[String]("INTEG_CONT_EVT_ID").trim
      val rsnCode = record.getAs[String]("INTEG_CONT_RSN_CD").trim
      val srcItemName = record.getAs[String]("SRC_ITM_NM").trim
      val strdItemName = record.getAs[String]("ITM_STRD_ENG_NM").trim
      val mapKey = srcName + "_" + eventId + "_" + rsnCode

      val formatEvent = cepEventFormatMap.getOrElseUpdate(mapKey, mutable.Map(srcItemName -> strdItemName))
      formatEvent += srcItemName -> strdItemName
    })

    cepEventFormatMap

  }
  
  def _compile(code:String): (Map[String, String] => String) = {
    
    import scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    
    val toolbox  = currentMirror.mkToolBox()
    toolbox.eval(toolbox.parse(code)).asInstanceOf[Map[String, String] => String]    
    
  }
  

}

/**
 * campanion object
 */
object MetaInfoLoader {
  def apply(spark: SparkSession, metaInfo: Config): MetaInfoLoader = {
    new MetaInfoLoader(spark, metaInfo, ConfigFactory.load("meta-info-sql.conf").getConfig("MetaInfoSql"))
  }
}
