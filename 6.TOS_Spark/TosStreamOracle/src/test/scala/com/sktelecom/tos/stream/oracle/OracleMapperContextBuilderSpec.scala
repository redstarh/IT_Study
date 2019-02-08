package com.sktelecom.tos.stream.oracle

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec

class OracleMapperContextBuilderSpec extends FlatSpec {

  class Fixture {

    val spark =
      SparkSession.builder()
        .master("local[2]")
        .appName("Kafka2OracleMetaLoaderSpec")
        .getOrCreate()

    val metaInfoConfig = """
     jdbcUrl: "jdbc:oracle:thin:@150.2.181.115:1521:CLIT"
     connProp {
       driver: "oracle.jdbc.driver.OracleDriver"
       user: "apps"
       password: "dhvjfld12#"
     }"""

    import net.ceedubs.ficus.Ficus._
    import scala.collection.JavaConversions._

    val config = ConfigFactory.parseString(metaInfoConfig)

    val jdbcConnUrl = config.getString("jdbcUrl")
    val jdbcConnProp = new Properties()
    jdbcConnProp.putAll(config.as[Map[String, String]]("connProp"))

    val mapper = OracleTransformer
    val metaManager = new OracleTransformerMetaManager(spark, jdbcConnUrl, jdbcConnProp)

    val metaHead  = metaManager.selectKafka2OracleMetaMeta()
    val metaDetail = metaManager.selectKafka2OracleMeta(metaHead)
  }

  def f = new Fixture

  behavior of "A Kafka2OracleMetaLoader"

  it should "크기가 0" in {

    val k2oMap = f.metaManager.selectKafka2OracleMetaMeta()

    println(s"size of k2oMap: ${k2oMap.size}")

    k2oMap.foreach(r => {
      println(s"\t${r._1}")
    })

    val k1 = "EH0001"
    val k2 = "**"
    val k3 = "SWG"
    val k4 = "1"

    f.mapper.variableKeys(f.metaHead, f.metaDetail, k1, k2, k3, k4) match {
      case Some(mappings) =>
        println(s"number of column mappings: ${mappings.size}")

        mappings.foreach(r => {
          println(s"\t${r._1} -> ${r._2}")
        })
      case None =>
    }

    assert(Set.empty.size == 0)
  }

  it should "head 를 호출하면 NoSuchElementException 을 발사" in {
    assertThrows[NoSuchElementException] {
      Set.empty.head
    }
  }
}
