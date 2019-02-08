package com.sktelecom.tos.stream.base.spark

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * TOS Streaming Application 의 공통 trait 으로 상속 받아 사용
  */
trait TosStreamApp {

  /**
    * sparkConfig
    *
    * @return spark config
    */
  def sparkConfig: Map[String, String]

  /**
    * microbatch 주기
    *
    * @return batch 주기
    */
  def batchDuration: Int

  /**
    * checkpoint dir
    *
    * @return checkpoint dir
    */
  def checkpointDir: String

  @transient lazy val Log = Logger.getLogger(getClass)

  /**
    * streaming context 를 생성하고 기동
    *
    * @param f spark streaming 프로그램 함수
    */
  def streamContext(f: (SparkSession, StreamingContext) => Unit): Unit = {

    val conf = new SparkConf()    
    
    sparkConfig.foreach { case (k, v) => conf.setIfMissing(k, v) }

    var ss: SparkSession = null
    if (sparkConfig.get("spark.sql.warehouse.dir") != None) {
      ss = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()
    } else {
      ss = SparkSession.builder().config(conf).getOrCreate()
    }

    val sc = ss.sparkContext
    val ssc = new StreamingContext(sc, Seconds(batchDuration))
    ssc.checkpoint(checkpointDir)

    f(ss, ssc)

    ssc.start()
    ssc.awaitTermination()

  }

}
