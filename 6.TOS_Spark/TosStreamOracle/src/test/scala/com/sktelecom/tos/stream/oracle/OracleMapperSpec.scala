package com.sktelecom.tos.stream.oracle

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec

/*
ConsumerRecord(topic = save, partition = 0, offset = 161007, CreateTime = 1539158772673, checksum = 3513780267, serialized key size = -1, serialized value size = 653, key = null, value = [B@5b148a81)
Map(CONT_AF_ITM2 -> 000000007500, SRC_RSN_CD_VAL -> SRC_RSN_CD_VAL, CONT_AF_ITM10 -> M, CONT_AF_ITM11 -> 00000000, INS_SER_NUM -> 161007, CONT_AF_ITM1 -> 000020, AUDIT_DTM -> 1539158772673, CONT_AF_ITM7 -> 1001, INTEG_CONT_RSN_CD -> INTEG_CONT_RSN_CD, CONT_AF_ITM12 -> 0000000000, CONT_AF_ITM8 -> 00, INTEG_CONT_EVT_ID -> INTEG_CONT_EVT_ID, AUDIT_ID -> SPARK, CONT_IDNT_CL_VAL -> 2463070768050519, CONT_AF_ITM4 -> 2463070768050519, SRC_OWNR_NM -> SRC_OWNR_NM, SRC_SYS_CL_CD -> SRC_SYS_CL_CD, INTEG_CONT_CL_CD -> INTEG_CONT_CL_CD, CONT_DTM -> 20181010170718, MBR_CARD_NUM11 -> 2463070768050519, CONT_AF_ITM9 -> 00000000, CONT_AF_ITM3 -> 20181010170718, CONT_DT -> 20181010170718, CONT_AF_ITM13 -> 0000170160, SRC_OBJ_NM -> SRC_OBJ_NM, INTEG_CONT_OP_CL_CD -> INTEG_CONT_OP_CL_CD, CONT_IDNT_CL_CD -> M)
Map(CONT_AF_ITM2 -> 000000007500, SRC_RSN_CD_VAL -> SRC_RSN_CD_VAL, CONT_AF_ITM10 -> M, CONT_AF_ITM11 -> 00000000, INS_SER_NUM -> 161007, CONT_AF_ITM1 -> 000020, AUDIT_DTM -> 1539158772673, CONT_AF_ITM7 -> 1001, INTEG_CONT_RSN_CD -> INTEG_CONT_RSN_CD, CONT_AF_ITM12 -> 0000000000, CONT_AF_ITM8 -> 00, INTEG_CONT_EVT_ID -> INTEG_CONT_EVT_ID, AUDIT_ID -> SPARK, CONT_IDNT_CL_VAL -> 2463070768050519, CONT_AF_ITM4 -> 2463070768050519, SRC_OWNR_NM -> SRC_OWNR_NM, SRC_SYS_CL_CD -> SRC_SYS_CL_CD, INTEG_CONT_CL_CD -> INTEG_CONT_CL_CD, CONT_DTM -> 20181010170718, MBR_CARD_NUM11 -> 2463070768050519, CONT_AF_ITM9 -> 00000000, CONT_AF_ITM3 -> 20181010170718, CONT_DT -> 20181010170718, CONT_AF_ITM13 -> 0000170160, SRC_OBJ_NM -> SRC_OBJ_NM, INTEG_CONT_OP_CL_CD -> INTEG_CONT_OP_CL_CD, CONT_IDNT_CL_CD -> M)
OracleRecord(20181010170718,INTEG_CONT_EVT_ID,M,2463070768050519,161007,2018-10-10,SPARK,20181010170718,-1,-1,-1,null,null,null,null,INTEG_CONT_CL_CD,INTEG_CONT_OP_CL_CD,INTEG_CONT_RSN_CD,null,SRC_SYS_CL_CD,SRC_OWNR_NM,SRC_OBJ_NM,null,SRC_RSN_CD_VAL,null,-1,-1,null,null,null,-1,null,null,null,null,null,000020,null,000000007500,null,20181010170718,null,2463070768050519,null,null,null,null,null,1001,null,00,null,00000000,null,M,null,00000000,null,0000000000,null,0000170160,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
 */
class OracleMapperSpec extends FlatSpec {

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

    // todo 새로운 메시지 포맷의 샘플 사용
    val jsonString = """
  {
    "header":{
      "srcEventDt":"20181010171455.000",
      "srcEventKey":"2833866650120323",
      "kafkaSendDt":"20181010171348.033",
      "reasonCode":"01",
      "srcSysName":"R2K",
      "eventId":"ER0001",
      "isCepSend":false,
      "srcObjName":"R2K_ONLINE_LOG",
      "isDbSave":true,
      "channelAccessType":"",
      "sparkMappingDt":""
      },
      "body":{
        "EXP_CODE":"S",
        "PAY_AMT":"0000000000",
        "MIL_POINT":"0000068785",
        "ACK_NUM":"00000000",
        "SKT_CARDNO":"2833866650120323",
        "USE_AMT":"000000000000",
        "DIS_AMT":"00000000",
        "VAN_UNQCD":"67",
        "TRML_NUM":"1000000000",
        "VAN_UNQNO":"8967240717",
        "RSP_CODE":"00",
        "JOIN_NUM1":"V274",
        "JOIN_NUM2":"1001",
        "USE_TYPE":"000020",
        "MSG_TYPE":"0210",
        "SND_TIME":"20181010171455",
        "RBP_DCFLAG":"M",
        "GOODS_CD":"0000"
        }
      }

"""

    val r = new ConsumerRecord[String, Array[Byte]]("save", 0, 1, null, jsonString.getBytes)

  }

  def fixture = new Fixture

  behavior of "A OracleSchemaMapper"

  it should "parse JSON" in {
    val f = fixture


    val v1 = OracleTransformer.fromJsonToMap(f.r)

    println(v1)
  }

  it should "map to a Oracle record" in {
    val f = fixture

    val v1 = OracleTransformer.fromJsonToMap(f.r)

    println(v1)

    val v2 = f.mapper.fromMapOfKafkaToMapOfOracle(f.metaHead, f.metaDetail)(v1)

    println(v2)
  }

  it should "convert to a Oracle record" in {
    val f = fixture

    val v1 = OracleTransformer.fromJsonToMap(f.r)

    val v2 = f.mapper.fromMapOfKafkaToMapOfOracle(f.metaHead, f.metaDetail)(v1)

    val v3 = OracleTransformer.fromMapToOracleRecord(v2)

    println(v3)
  }

}
