package com.sktelecom.tos.stream.base.spark

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream

import com.sktelecom.tos.stream.base.data.StreamData
import com.sktelecom.tos.stream.base.kafka.StreamKafkaRecord

/**
 * Hadoop 저장 Test 용
 */
trait SaveToHadoop {

  def hadoopBasePath: String = "/offer_data"

  def hadoopRawPath: String = "raw"

  def hadoopCleanPath: String = "clean"

  def hadoopMappingPath: String = "mapping"

  def fastDateFormat = FastDateFormat.getInstance("yyyyMMdd/HH/mm")

  def datePath: String = fastDateFormat.format(System.currentTimeMillis())

  /**
   * 원본 데이터를 Hadoop 에 저장한다.
   *
   * @param ss
   * @param dstream
   * @param srcSysName
   */
  final def saveToHadoopWithRaw(ss: SparkSession, dstream: DStream[StreamKafkaRecord], srcSysName: String): Unit = {
    dstream.map(_.value)
      .foreachRDD(rdd => {
        if (rdd.count() > 0) {
          import ss.implicits._
          rdd.toDF.write.mode(SaveMode.Append).text(hadoopBasePath + "/" + srcSysName + "/" + hadoopRawPath + "/" + datePath)
        }
      })
  }

  final def saveToHadoopWithCleaning(ss: SparkSession, dstream: DStream[String], srcSysName: String): Unit = {
    dstream.foreachRDD(rdd => {
      if (rdd.count() > 0) {
        import ss.implicits._
        rdd.toDF.write.mode(SaveMode.Append).text(hadoopBasePath + "/" + srcSysName + "/" + hadoopCleanPath + "/" + datePath)
      }
    })
  }

  final def saveToHadoopWithMapping(ss: SparkSession, dstream: DStream[StreamData], srcSysName: String): Unit = {
    dstream.map(_.toJsonString())
      .foreachRDD(rdd => {
        if (rdd.count() > 0) {
          import ss.implicits._
          rdd.toDF.write.mode(SaveMode.Append).text(hadoopBasePath + "/" + srcSysName + "/" + hadoopMappingPath + "/" + datePath)
        }
      })
  }

}
