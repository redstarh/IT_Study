package com.sktelecom.tos.stream.jmx

import java.lang.management.ManagementFactory

import javax.management._
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.StreamingContext

class StreamingContextManager(ssc: StreamingContext) extends StreamingContextManagerMBean with Logging {

  override def stop(stopGracefully: Boolean): Unit = {
    logInfo("stopping the streaming context " + (if (stopGracefully) "gracefully" else "disgracefully"))
    ssc.stop(true, stopGracefully)
  }

}

object StreamingContextManager {
  val defaultName = "com.sktelecom.tos.stream:type=context"

  def register(ssc: StreamingContext): Unit = {
    register(defaultName, ssc)
  }

  def register(name: String, ssc: StreamingContext) {
    val obj = new StreamingContextManager(ssc)

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
