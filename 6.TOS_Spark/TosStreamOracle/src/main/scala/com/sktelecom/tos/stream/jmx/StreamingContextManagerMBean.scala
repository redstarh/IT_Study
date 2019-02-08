package com.sktelecom.tos.stream.jmx

trait StreamingContextManagerMBean {
  def stop(stopGracefully: Boolean): Unit
}
