package com.sktelecom.tos.stream.util

import java.io.IOException
import java.net.MalformedURLException

import com.sktelecom.tos.stream.oracle.HelpException
import com.sktelecom.tos.stream.jmx.StreamingContextManagerMBean
import javax.management.remote.{JMXConnector, JMXConnectorFactory, JMXServiceURL}
import javax.management.{JMX, MalformedObjectNameException, ObjectName}
import org.apache.spark.internal.Logging
import org.apache.zookeeper.ZooKeeper

object StreamingContextStopper extends Logging {
  final val defaultStopGracefully = true
  final val defaultHost = "localhost"
  final val defaultPort = 9999
  final val defaultObjectName = "com.sktelecom.tos.stream"

  val zNodeRootPath = "/spark-apps/"

  val usage =
    """
       Usage: StreamingContextStopper  [--zookeeper <zookeeper> --znode <znode>] | [ [--stopGracefully | --stopDisgracefully] [--host <host>] [--port <port>] [--object-name <object name>] ]
      """.stripMargin

  def main(args: Array[String]) {

    val argList = args.toList

    type OptionMap = Map[Symbol, Any]

    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      def isSwitch(s: String) = s(0) == '-'

      list match {
        case Nil =>
          logTrace("end")
          map
        case "-h" :: tail =>
          logTrace("help")
          throw new HelpException
        case "--help" :: tail =>
          logTrace("help")
          throw new HelpException
        case "--zookeeper" :: value :: tail if !isSwitch(value) =>
          logTrace("zookeeper")
          nextOption(map ++ Map('Zookeeper -> value), tail)
        case "--znode" :: value :: tail if !isSwitch(value) =>
          logTrace("znode")
          nextOption(map ++ Map('ZNode -> value), tail)
        case "--stopGracefully" :: tail =>
          logTrace("gracefully")
          nextOption(map ++ Map('StopGracefully -> true), tail)
        case "--stopDisgracefully" :: tail =>
          logTrace("disgracefully")
          nextOption(map ++ Map('StopGracefully -> false), tail)
        case "--host" :: value :: tail if !isSwitch(value) =>
          logTrace("host")
          nextOption(map ++ Map('Host -> value), tail)
        case "--port" :: value :: tail if !isSwitch(value) =>
          logTrace("port")
          nextOption(map ++ Map('Port -> value.toInt), tail)
        case "--object-name" :: value :: tail if !isSwitch(value) =>
          logTrace("object-name")
          nextOption(map ++ Map('ObjectName -> value), tail)
        case option :: tail =>
          logTrace(s"Unknown option: $option")
          throw new Exception(s"Unknown option: $option")
      }
    }

    try {
      val options = nextOption(Map.empty, argList)

      val zookeeper = options.get('Zookeeper) match {
        case Some(x) => x.asInstanceOf[String]
        case None => ""
      }

      val zNode = options.get('ZNode) match {
        case Some(x) => x.asInstanceOf[String]
        case None => ""
      }

      val stopGracefully = options.get('StopGracefully) match {
        case Some(x) => x.asInstanceOf[Boolean]
        case None => defaultStopGracefully
      }

      var host = options.get('Host) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultHost
      }

      var port = options.get('Port) match {
        case Some(x) => x.asInstanceOf[Int]
        case None => 0
      }

      var objectName = options.get('ObjectName) match {
        case Some(x) => x.asInstanceOf[String]
        case None => defaultObjectName
      }

      if (zookeeper == "" && zNode == "") {
        if (port == 0) {
          throw new Exception("port 옵션은 필수입니다.")
        }
      }

      if (port == 0) {
        if (zookeeper == "") {
          throw new Exception("zookeeper 옵션은 필수입니다.")
        }
        if (zNode == "") {
          throw new Exception("znode 옵션은 필수입니다.")
        }
      }

      if (zookeeper != "" && zNode != "" && port != 0) {
        throw new Exception("zookeeper 또는 직접 지정 옵션들은 상호배타적입니다.")
      }

      if (zookeeper != "") {
        val zk = new ZooKeeper(zookeeper, 3000, ZkWatcher())

        val data = zk.getData(zNodeRootPath + zNode, false, null)

        val textData = new String(data).split(",")

        host = textData(0)
        port = textData(1).toInt
        objectName = textData(2)
      }

      process(host, port, objectName + ":type=context", stopGracefully)
    } catch {
      case _: HelpException =>
        println(usage)
      case e: Throwable =>
        println(usage)
        e.printStackTrace()
    }
  }

  def process(host: String, port: Int, objectName: String, stopGracefully: Boolean): Unit = {

    var jmxc: JMXConnector = null

    val jmxServiceUrl = s"service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi"

    try {
      val url = new JMXServiceURL(jmxServiceUrl)
      jmxc = JMXConnectorFactory.connect(url, null)
      val mbsc = jmxc.getMBeanServerConnection
      val mbeanName = new ObjectName(objectName)
      val mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, classOf[StreamingContextManagerMBean])

      logInfo("stopping the streaming context")
      mbeanProxy.stop(stopGracefully)
      logInfo("stopped the streaming context")
    } catch {
      case e: RuntimeException =>
        e.printStackTrace()
      case e: MalformedObjectNameException =>
        e.printStackTrace()
      case e: MalformedURLException =>
        e.printStackTrace()
      case e: IOException =>
        e.printStackTrace()
    } finally {
      if (jmxc != null) {
        try {
          jmxc.close()
        } catch {
          case e: Throwable =>
            e.printStackTrace()
        }
      }
    }
  }

}
