package com.sktelecom.tos.stream.base.meta

import org.apache.spark.sql.SparkSession
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

/**
 * MetaInfoLoader TestCase
 */
class MetaInfoLoaderTest {

  val srcSysName = "SWG"

  val metaInfoConfig = s"""
     srcSysName: "$srcSysName"
     isLoop: false
     jdbcUrl: "jdbc:oracle:thin:@150.2.181.115:1521:CLIT"
     connProp {
       driver: "oracle.jdbc.driver.OracleDriver"
       user: "apps"
       password: "dhvjfld12#"
     }"""

  var sparkSession: SparkSession = null

  var metaInfoLoader: MetaInfoLoader = null

  @Ignore
  @Before
  def before(): Unit = {
    sparkSession = SparkSession
      .builder().master("local[2]")
      .appName(getClass.getName)
      .getOrCreate()
    metaInfoLoader = MetaInfoLoader(sparkSession, ConfigFactory.parseString(metaInfoConfig))
  }

  @Ignore
  @Test
  def selectObjNameSpec(): Unit = {
    metaInfoLoader.selectObjNameSpec().foreach(println(_))
  }

  @Ignore
  @Test
  def selectEventReasonSpecTest(): Unit = {

    metaInfoLoader.selectEventReasonSpec().foreach(println(_))

    //    // depth 1 -> OBJ_NAME
    //    metaInfoLoader.selectEventReasonSpec().foreach(record => {
    //      println(record._1)
    //    })
    //
    //    // depth 2 -> OBJ_NM, CHG_ITEM_NM, CHG_CD_VAL
    //    metaInfoLoader.selectEventReasonSpec().foreach(record => {
    //      record._2.foreach(item => {
    //        println(record._1 + "\t" + item._1._1 + "\t" + item._1._2)
    //      })
    //    })
    //
    //    // depth 3 -> OBJ_NM, CHG_ITEM_NM, CHG_CD_VAL, RSN_ITM_NM, RSN_CD_VAL, EVENT_ID, RSN_CD
    //    metaInfoLoader.selectEventReasonSpec().foreach(record => {
    //      record._2.foreach(item => {
    //        item._2.foreach(rsn => {
    //          println(rsn._2._1 + "\t" + rsn._2._2 + "\t" + record._1 + "\t" + item._1._1 + "\t" + item._1._2 + "\t" + rsn._1._1 + "\t" + rsn._1._2)
    //        })
    //      })
    //    })

  }

  @Ignore
  @Test
  def selectEventMappingSpecTest(): Unit = {

    metaInfoLoader.selectEventCodeMappingSpec().foreach(println(_))

    //    // depth 1 -> OBJ_NAME
    //    metaInfoLoader.selectEventCodeMappingSpec().foreach(record => {
    //      println(record._1)
    //    })
    //
    //    // depth 2 -> OBJ_NM, ITEM_NM1, CD_VAL1
    //    metaInfoLoader.selectEventCodeMappingSpec().foreach(record => {
    //      record._2.foreach(item1 => {
    //        println(record._1 + "\t" + item1._1._1 + "\t" + item1._1._2)
    //      })
    //    })
    //
    //    // depth 3 -> OBJ_NM, ITEM_NM1, CD_VAL1, ITEM_NM2, CD_VAL2
    //    metaInfoLoader.selectEventCodeMappingSpec().foreach(record => {
    //      record._2.foreach(item1 => {
    //        item1._2.foreach(item2 => {
    //          println(item2._2._1 + "\t" + item2._2._2 + "\t" + record._1 + "\t" + item1._1._1 + "\t" + item1._1._2 + "\t" + item2._1._1 + "\t" + item2._1._2)
    //        })
    //      })
    //    })

  }

  @Ignore
  @Test
  def selectEventOperationSpecTest(): Unit = {
    //    metaInfoLoader.selectEventMappingSpec().foreach(println(_))
  }

  @Ignore
  @Test
  def selectEventUrlSpecTest(): Unit = {
    //    metaInfoLoader.selectEventUrlSpec().foreach(println(_))
  }

  @Ignore
  @Test
  def selectCepEventTest(): Unit = {
    metaInfoLoader.selectCepEvent().foreach(println(_))
  }

  @Ignore
  @Test
  def selectCepFormatTest(): Unit = {
    metaInfoLoader.selectCepEventFormat().foreach(println(_))
  }

}
