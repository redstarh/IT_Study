package com.sktelecom.tos.stream.jmx

import java.lang.management.ManagementFactory

import com.sktelecom.tos.stream.oracle.OracleTransformerMetaManager
import javax.management._
import org.apache.spark.internal.Logging

class MetaSuperManager(manager: OracleTransformerMetaManager) extends MetaSuperManagerMBean with Logging {

  override def reload(): Unit = {
    logInfo("reloading the meta")
    manager.reload()
  }

}

object MetaSuperManager {
  val defaultName = "com.sktelecom.tos.stream:type=meta"

  def register(manager: OracleTransformerMetaManager) {
    register(defaultName, manager)
  }

  def register(name: String, manager: OracleTransformerMetaManager) {

    val obj = new MetaSuperManager(manager)

    val mbs = ManagementFactory.getPlatformMBeanServer

    try {
      val on = new ObjectName(name)
      mbs.registerMBean(obj, on)
    } catch {
      case e: InstanceAlreadyExistsException =>
        e.printStackTrace()
      case e: MBeanRegistrationException =>
        e.printStackTrace()
      case e: NotCompliantMBeanException =>
        e.printStackTrace()
      case e: MalformedObjectNameException =>
        e.printStackTrace()
    }

  }

}
