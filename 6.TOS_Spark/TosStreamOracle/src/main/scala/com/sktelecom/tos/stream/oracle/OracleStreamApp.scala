package com.sktelecom.tos.stream.oracle

import java.net.InetAddress
import java.util.Properties

import com.sktelecom.tos.stream.jmx.{MetaSuperManager, StreamingContextManager}
import com.sktelecom.tos.stream.util.ZkWatcher
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SaveMode, SparkSession}
import net.ceedubs.ficus.Ficus._
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.zookeeper.{CreateMode, ZooKeeper}
import org.apache.zookeeper.ZooDefs.Ids

import scala.collection.JavaConversions._


/**
  * 설정 파일(application.conf) 에 따라서 오브젝트를 생성
  */
object OracleStreamApp extends Logging {

  /**
    * 앱 컨피그
    */
  private val appConfig = ConfigFactory.load().getConfig("application")

//  private val appName = appConfig.getString("appName")

  /**
    * 주키퍼 컨피그
    */
  private val zkConfig = appConfig.as[Map[String, String]]("zookeeper")

  /**
    * 스파크 컨피그
    */
  private val sparkConfig = appConfig.as[Map[String, String]]("sparkConfig")

  /**
    * 배치 주기
    */
  private val batchDuration = appConfig.getInt("batchDuration")

  /**
    * JDBC 연결 URL
    */
  private val jdbcConnUrl = appConfig.getString("database.jdbcUrl")

  /**
    * JDBC 연결 프로파티
    */
  private val jdbcConnProp = {
    val x = appConfig.as[Map[String, String]]("database.connProp")
    val y = new Properties()
    y.putAll(x)
    y
  }

  /**
    * 카프카 컨슈머 프로파티
    */
  private val kafkaConsumerProp = appConfig.as[Map[String, String]]("kafkaConsumer")

  /**
    * 카프카 토픽
    */
  private val kafkaConsumerTopics = {
    appConfig.getString("input_topic.topics").split(",").toSet
  }

  /**
    * application.conf 의 설정에 따라서, 스트리밍칸텍스트를 생성
    *
    * @param master
    * @return
    */
  def createContext(master: Option[String] = None): StreamingContext = {
    logInfo("creating a streaming context")

    val sparkConf = new SparkConf()

//    sparkConf.setAppName(appName)

    sparkConfig.foreach( e => sparkConf.setIfMissing(e._1, e._2) )

    if (!master.isEmpty) {
      sparkConf.setMaster(master.get)
    }

/*
    sparkConfig.get("spark.sql.warehouse.dir") match {
      case None => SparkSession.builder().config(config).getOrCreate()
      case Some(_) => SparkSession.builder().config(config).enableHiveSupport().getOrCreate()
    }
*/

    val context = new StreamingContext(sparkConf, Seconds(batchDuration))
//    context.checkpoint(checkpointPath) // 디렉토리가 생성되는 부작용 때문에, 결과 타입이 Unit 인 것 같음

    val session = SparkSession.builder().config(sparkConf).getOrCreate()

    val metaManager = new OracleTransformerMetaManager(session, jdbcConnUrl, jdbcConnProp)

    // 메타정보 관리자 상관 등록
    MetaSuperManager.register(metaManager)
//    val metaSuperManager = new MetaSuperManager(metaManager)
//    MBeanRegister.register(MBeanRegister.META, metaSuperManager)

    // DAG 정의

    // 카프카에서 메시지를 가져와 RDD 로 공급하는 인풋디스트림
    val ids = KafkaUtils.createDirectStream[String, Array[Byte]](
      context,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, Array[Byte]](kafkaConsumerTopics, kafkaConsumerProp)
    )

    // RDD 를 출력하는 포리치디스트림
    ids.foreachRDD{rdd =>
      logInfo(s"foreachRDDing")

      // 메타 정보 로딩
      val meta = metaManager.getInstance()

      logInfo(s"meta size: ${meta.value._1.size}")

      // 카프카에서 가져온 메시지를 데이터베이스 저장에 필요한 오브젝트로 변환
      // 입력: ConsumerRecord(topic = save, partition = 0, offset = 160951, CreateTime = 1539149004217, checksum = 3386847530, serialized key size = -1, serialized value size = 639, key = null, value = [B@5cca7d62)
      // 처리1: 문자열 데이터를 추출하여, JSON 포맷으로 되어 있는 데이터를 맵 포맷으로 변환
      // 처리2: 데이터베이스 저장 대상만 추출
      // 처리3: 통합접촉이력 메타테이블에서 관리되는 메타정보를 활용해, 데이터베이스 저장에 필요한 필드로 매핑. 현재는 매핑정의서에 의한 정의도 추가적으로 실행하고 있음.
      // 처리4: 데이터베이스 저장에 필요한 필드를 갖추고 있는가를 검증하여 양품만 추출. 불량품은 버림.
      // 처리5: 맵 오브젝트에서 데이터베이스 저장에 필요한 케이스클래스 오브젝트로 변환
      // 처리6: 알디디 캐슁
      val result = rdd
        .map(OracleTransformer.fromJsonToMap)
        .filter { m =>
          m.get("TOS_IS_DB_SAVE") match {
            case Some(x) => x == "true"
            case _ => false
          }
        }
        .map(OracleTransformer.fromMapOfKafkaToMapOfOracle(meta.value._1, meta.value._2))
        .filter(OracleTransformer.validate)
        .map(OracleTransformer.fromMapToOracleRecord)
        .persist()


      // 데이터가 존재할 때만 데이터베이스 저장
      if (result.count() > 0) {

        logInfo("Outputting results")

        if (isTraceEnabled()) {
          // 결과 출력
          result.take(10).foreach { r =>
            logTrace(r.toString)
          }
        }

        // 결과 출력: Oracle 에 저장
        session
          .createDataFrame(result)
          .write
          .mode(SaveMode.Append)
          .option("isolationLevel", "READ_COMMITTED")
          .jdbc(jdbcConnUrl, "TCIC.TCIC_INTEG_CONT_HST", jdbcConnProp)

        // 카프카에 오프셋 커밋

        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        // some time later, after outputs have completed
        ids.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      }
    }

    context
  }

  def preprocess(): Unit = {
    val zkConnectString = zkConfig.get("servers") match {
      case Some(x) => x
      case None =>
        throw new Exception()
    }

    val zNodeRootPath = zkConfig.get("znode-root") match {
      case Some(x) => x
      case None =>
        throw new Exception()
    }
    val zNodePath = zkConfig.get("znode-name") match {
      case Some(x) => x
      case None =>
        throw new Exception()
    }

    val host = InetAddress.getLocalHost.getHostAddress
    val port = System.getProperty("com.sun.management.jmxremote.port")

    val data = List(host, port, "com.sktelecom.tos.stream").mkString(",")

    val zk = new ZooKeeper(zkConnectString, 3000, ZkWatcher())
    zk.create(zNodeRootPath + zNodePath, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
  }
}

/**
  * master 를 지정하지 않고 스트리밍을 시작
  * spark-submit 용
  */
object AppMain extends Logging {

  /**
    * Kafka 에서 Oracle 로 저장하는 앱
    *
    * JMX 를 활성화하려면 JVM 옵션에 추가 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
    *
    * @param args 실행 파라미터
    */
  def main(args: Array[String]) {

    OracleStreamApp.preprocess()

    // StreamingContext 취득
    val context = OracleStreamApp.createContext()

    StreamingContextManager.register(context)
//    val sch = new StreamingContextManager(context)
//    MBeanRegister.register(MBeanRegister.CONTEXT, sch)

    // StreamingContext 실행
    context.start()

    //
    context.awaitTermination()
  }
}

/**
  * master 를 local[2] 로 지정해서 스트리밍을 시작
  * 단위 테스트 용
  *
  * JMX 를 활성화하려면 JVM 옵션에 추가 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
  */
object LocalAppMain extends Logging {

  /**
    * Kafka 에서 Oracle 로 저장하는 앱
    *
    * @param args 실행 파라미터
    */
  def main(args: Array[String]) {

    // 주키퍼에 host,port,objectname 등록
    OracleStreamApp.preprocess()

    // StreamingContext 취득
    val context = OracleStreamApp.createContext(Some("local[2]"))

    // 스트리밍칸텍스트 관리자 등록
    StreamingContextManager.register(context)
//    val sch = new StreamingContextManager(context)
//    MBeanRegister.register(MBeanRegister.CONTEXT, sch)

    // StreamingContext 실행
    context.start()

    //
    context.awaitTermination()
  }
}
