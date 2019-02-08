package com.sktelecom.tos.stream.base.meta

import org.apache.spark.sql.SparkSession
import org.junit.Assert._
import org.junit.Ignore
import org.junit.Test

import com.sktelecom.tos.stream.base.TosConstants
import com.sktelecom.tos.stream.base.data.StreamData
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * EventMapper TestCase
 */
class EventMapperTest {

  /**
   * EventMapper 생성용
   */
  def getEventMapper(srcSysName: String): EventMapper = {

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

    new EventMapper(metaInfoLoader.start())

  }

  @Ignore
  @Test
  def checkCepSendTimeTest(): Unit = {

    def checkCepSendTime(cepSendTime: (String, String), currentTime: Int): Boolean = {
      if (cepSendTime._1.toInt < cepSendTime._2.toInt) {
        ((cepSendTime._1.toInt <= currentTime) && (cepSendTime._2.toInt > currentTime))
      } else {
        ((cepSendTime._1.toInt <= currentTime) || (cepSendTime._2.toInt > currentTime))
      }
    }

    for (hour <- 0 to 23) {
      for (minute <- 0 to 59) {
        var currentTime = new StringBuilder()
        if (hour < 10) {
          currentTime.append("0")
        }
        currentTime.append(hour.toString())
        if (minute < 10) {
          currentTime.append("0")
        }
        currentTime.append(minute.toString())

        println(currentTime + " : " + checkCepSendTime(("0900", "0800"), currentTime.toInt))
      }
    }

  }

  @Ignore
  @Test
  def swingReasonTest(): Unit = {

    val eventMapper = getEventMapper("SWG")

    val EA0001_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_CUST_INFO_PRA_HST", null, null, null, Map("INFO_PRA_AGR_YN" -> "Y")))
    assertEquals("EA0001", EA0001_01.integEventId)
    assertEquals("01", EA0001_01.integReasonCode)

    val EA0001_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_CUST_INFO_PRA_HST", null, null, null, Map("INFO_PRA_AGR_YN" -> "N")))
    assertEquals("EA0001", EA0001_02.integEventId)
    assertEquals("02", EA0001_02.integReasonCode)

    val EA0001_03 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_PSNINF_ITM_PRA_HST", null, null, null, Map("PSNINF_PAGR_CL_CD" -> "01")))
    assertEquals("EA0001", EA0001_03.integEventId)
    assertEquals("03", EA0001_03.integReasonCode)

    val EA0001_04 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_PSNINF_ITM_PRA_HST", null, null, null, Map("PSNINF_PAGR_CL_CD" -> "02")))
    assertEquals("EA0001", EA0001_04.integEventId)
    assertEquals("04", EA0001_04.integReasonCode)

    val EB0001_A1 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_ACNT_CHG_HST", null, null, null, Map("ACNT_CHG_CD" -> "A1", "ACNT_CHG_RSN_CD" -> "A1")))
    assertEquals("EB0001", EB0001_A1.integEventId)
    assertEquals("A1", EB0001_A1.integReasonCode)

    val EB0001_M7 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_ACNT_CHG_HST", null, null, null, Map("ACNT_CHG_CD" -> "A1", "ACNT_CHG_RSN_CD" -> "M7")))
    assertEquals("EB0001", EB0001_M7.integEventId)
    assertEquals("M7", EB0001_M7.integReasonCode)

    val EB0002_P4 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_ACNT_CHG_HST", null, null, null, Map("ACNT_CHG_CD" -> "A2", "ACNT_CHG_RSN_CD" -> "P4")))
    assertEquals("EB0002", EB0002_P4.integEventId)
    assertEquals("P4", EB0002_P4.integReasonCode)

    val EJ0002_14 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_CUST_CHG_HST", null, null, null, Map("CUST_CHG_CD" -> "A3", "CUST_CHG_RSN_CD" -> "14")))
    assertEquals("EJ0002", EJ0002_14.integEventId)
    assertEquals("14", EJ0002_14.integReasonCode)

    val EJ0023_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_SVC_CHG_HST", null, null, null, Map("SVC_CHG_CD" -> "A8", "SVC_CHG_RSN_CD" -> "02")))
    assertEquals("EJ0023", EJ0023_02.integEventId)
    assertEquals("02", EJ0023_02.integReasonCode)

    val EM0004_R04 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZMBR.ZMBR_MBR_CARD_HST", null, null, null, Map("MBR_CHG_CD" -> "C02", "MBR_CHG_RSN_CD" -> "R04")))
    assertEquals("EM0004", EM0004_R04.integEventId)
    assertEquals("R04", EM0004_R04.integReasonCode)

    val EU0012_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_COPN_OP_HST", null, null, null, Map("COPN_OPER_ST_CD" -> "A10")))
    assertEquals("EU0012", EU0012_01.integEventId)
    assertEquals("01", EU0012_01.integReasonCode)

    val EU0014_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZORD.ZORD_DATA_GIFT_HST", null, null, null, Map("DATA_GIFT_OP_ST_CD" -> "2", "DATA_GIFT_TYP_CD" -> "G1")))
    assertEquals("EU0014", EU0014_01.integEventId)
    assertEquals("01", EU0014_01.integReasonCode)

  }

  @Ignore
  @Test
  def swingFilterTest(): Unit = {
    val eventMapper = getEventMapper("SWG")
    val EB0015_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINV.ZINV_BILL_ISUE_SPC", null, null, null, Map("REISU_ST_CD" -> "01", "REISU_REQ_PATH_CD" -> "01", "BILL_ISUE_SPC_CL_CD" -> "02")))
    assertEquals("EB0015", EB0015_01.integEventId)
    assertEquals("01", EB0015_01.integReasonCode)
  }

  @Ignore
  @Test
  def urtReasonTest(): Unit = {
    val eventMapper = getEventMapper("URT")
    val EU0011_100 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, null, null, Map("EXHST_RT" -> "100")))
    assertEquals("EU0011", EU0011_100.integEventId)
    assertEquals("100", EU0011_100.integReasonCode)
    val EU0011_80 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, null, null, Map("EXHST_RT" -> "80")))
    assertEquals("EU0011", EU0011_80.integEventId)
    assertEquals("80", EU0011_80.integReasonCode)
    val EU0011_50 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.URT, "URT_DATA_EXHST_LOG", null, null, null, Map("EXHST_RT" -> "50")))
    assertEquals("EU0011", EU0011_50.integEventId)
    assertEquals("50", EU0011_50.integReasonCode)

  }

  @Ignore
  @Test
  def swingCodeMappingTest(): Unit = {

    val eventMapper = getEventMapper("SWG")

    val EH0001_01_1 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3510255")))
    assertEquals("EH0001", EH0001_01_1.integEventId)
    assertEquals("01", EH0001_01_1.integReasonCode)

    val EH0001_01_2 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3504413")))
    assertEquals("EH0001", EH0001_01_2.integEventId)
    assertEquals("01", EH0001_01_2.integReasonCode)

    val EH0001_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3504404")))
    assertEquals("EH0001", EH0001_02.integEventId)
    assertEquals("02", EH0001_02.integReasonCode)

    val EH0002_08 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "2100656")))
    assertEquals("EH0002", EH0002_08.integEventId)
    assertEquals("08", EH0002_08.integReasonCode)

    val EH0003_04 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3504627")))
    assertEquals("EH0003", EH0003_04.integEventId)
    assertEquals("04", EH0003_04.integReasonCode)

    val EH0005_05 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3500264")))
    assertEquals("EH0005", EH0005_05.integEventId)
    assertEquals("05", EH0005_05.integReasonCode)

    val EH0006_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3510077")))
    assertEquals("EH0006", EH0006_02.integEventId)
    assertEquals("02", EH0006_02.integReasonCode)

    val EH0007_07 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3510027")))
    assertEquals("EH0007", EH0007_07.integEventId)
    assertEquals("07", EH0007_07.integReasonCode)

    val EH0008_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3507567")))
    assertEquals("EH0008", EH0008_01.integEventId)
    assertEquals("01", EH0008_01.integReasonCode)

    val EH0009_01_1 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3509714")))
    assertEquals("EH0009", EH0009_01_1.integEventId)
    assertEquals("01", EH0009_01_1.integReasonCode)

    val EH0009_01_2 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3501417")))
    assertEquals("EH0009", EH0009_01_2.integEventId)
    assertEquals("01", EH0009_01_2.integReasonCode)

    val EH0009_01_3 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3004212")))
    assertEquals("EH0009", EH0009_01_3.integEventId)
    assertEquals("01", EH0009_01_3.integReasonCode)

    val EH0009_01_4 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "2300014")))
    assertEquals("EH0009", EH0009_01_4.integEventId)
    assertEquals("01", EH0009_01_4.integReasonCode)

    val EH0009_01_5 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZINB.ZINB_INB_CNSL", null, null, null, Map("CNSL_CD_SEQ" -> "3509740")))
    assertEquals("EH0009", EH0009_01_5.integEventId)
    assertEquals("01", EH0009_01_5.integReasonCode)

    val EH0010_ADM = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "ADM")))
    assertEquals("EH0010", EH0010_ADM.integEventId)
    assertEquals("ADM", EH0010_ADM.integReasonCode)

    val EH0010_STP = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "STP")))
    assertEquals("EH0010", EH0010_STP.integEventId)
    assertEquals("STP", EH0010_STP.integReasonCode)

    val EH0010_UCT = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "UCT")))
    assertEquals("EH0010", EH0010_UCT.integEventId)
    assertEquals("UCT", EH0010_UCT.integReasonCode)

    val EH0011_B00 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "B00")))
    assertEquals("EH0011", EH0011_B00.integEventId)
    assertEquals("B00", EH0011_B00.integReasonCode)

    val EH0011_P06 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "P06")))
    assertEquals("EH0011", EH0011_P06.integEventId)
    assertEquals("P06", EH0011_P06.integReasonCode)

    val EH0012_DCT = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "DCT")))
    assertEquals("EH0012", EH0012_DCT.integEventId)
    assertEquals("DCT", EH0012_DCT.integReasonCode)

    val EH0013_RFB = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "RFB")))
    assertEquals("EH0013", EH0013_RFB.integEventId)
    assertEquals("RFB", EH0013_RFB.integReasonCode)

    val EH0014_ALL = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.SWING, "ZCOL.ZCOL_COL_CS_HST", null, null, null, Map("COL_ACT_CD" -> "ALL")))
    assertEquals("EH0014", EH0014_ALL.integEventId)
    assertEquals("ALL", EH0014_ALL.integReasonCode)

  }

  @Ignore
  @Test
  def r2kCodeMappingTest(): Unit = {
    val eventMapper = getEventMapper("R2K")

    val ER0001_01 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_ONLINE_LOG", null, null, null, Map("RSP_CODE" -> "00")))
    assertEquals("ER0001", ER0001_01.integEventId)
    assertEquals("01", ER0001_01.integReasonCode)

    val ER0001_02 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_OFFLINE_LOG", null, null, null, Map("RSP_CODE" -> "00")))
    assertEquals("ER0001", ER0001_02.integEventId)
    assertEquals("02", ER0001_02.integReasonCode)

    val ER0002_01_11 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_ONLINE_LOG", null, null, null, Map("RSP_CODE" -> "11")))
    assertEquals("ER0002", ER0002_01_11.integEventId)
    assertEquals("01", ER0002_01_11.integReasonCode)

    val ER0002_02_22 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_OFFLINE_LOG", null, null, null, Map("RSP_CODE" -> "22")))
    assertEquals("ER0002", ER0002_02_22.integEventId)
    assertEquals("02", ER0002_02_22.integReasonCode)

    val ER0002_03_40 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_ONLINE_LOG", null, null, null, Map("RSP_CODE" -> "40")))
    assertEquals("ER0002", ER0002_03_40.integEventId)
    assertEquals("03", ER0002_03_40.integReasonCode)

    val ER0002_04_41 = eventMapper.getEventInfo(new StreamData(TosConstants.SrcSysName.R2K, "R2K_OFFLINE_LOG", null, null, null, Map("RSP_CODE" -> "41")))
    assertEquals("ER0002", ER0002_04_41.integEventId)
    assertEquals("04", ER0002_04_41.integReasonCode)

  }

  @Ignore
  @Test
  def channelMappingTest(): Unit = {

  }

}