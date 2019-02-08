package com.sktelecom.tos.stream.base.meta

import com.typesafe.config.ConfigFactory
import org.junit.Before
import org.apache.spark.sql.SparkSession
import org.junit.Test
import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.Utils
import org.junit.Ignore
import com.sktelecom.tos.stream.base.TosConstants

/**
 * CEPFormatter TestCase
 */
class CEPFormatterTest {

  var eventMapper: EventMapper = null
  var cepFormatter: CEPFormatter = null

  /**
   * EventMapper, cepFormatter 생성용
   */
  def initMeta(srcSysName: String): Unit = {

    val metaInfoConfig = s"""
     srcSysName: "$srcSysName"
     isLoop: false
     jdbcUrl: "jdbc:oracle:thin:@150.2.181.115:1521:CLIT"
     connProp {
       driver: "oracle.jdbc.driver.OracleDriver"
       user: "apps"
       password: "dhvjfld12#"
     }"""

    val spark = SparkSession
      .builder().master("local[2]")
      .appName(getClass().getName)
      .getOrCreate()

    val metaInfoLoader = MetaInfoLoader(spark, ConfigFactory.parseString(metaInfoConfig))

    val metaInfo = metaInfoLoader.start()

    eventMapper = new EventMapper(metaInfo)

    cepFormatter = new CEPFormatter(metaInfo)

  }

  @Ignore
  @Test
  def r2kConvertToJson(): Unit = {

    initMeta("R2K")

    val ER0001_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_ONLINE_LOG", null, Utils.getDate, null, Map("RSP_CODE" -> "00")))

    assert(ER0001_01.isCepSend)

    val ER0001_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_OFFLINE_LOG", null, Utils.getDate, null, Map("RSP_CODE" -> "00")))

    assert(ER0001_02.isCepSend)


  }

  @Ignore
  @Test
  def urtConvertToJson(): Unit = {

    initMeta("URT")

    val EU0011_100 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, Utils.getDate, null, Map("DATA_EXHST_RT" -> "100")))

    val EU0011_80 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, Utils.getDate, null, Map("DATA_EXHST_RT" -> "80")))

    val EU0011_50 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, Utils.getDate, null, Map("DATA_EXHST_RT" -> "50")))

    assert(EU0011_100.isCepSend)
    assert(EU0011_80.isCepSend)
    assert(EU0011_50.isCepSend)

  }

}