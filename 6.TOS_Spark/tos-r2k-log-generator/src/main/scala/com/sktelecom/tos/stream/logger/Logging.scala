package com.sktelecom.tos.stream.logger

import org.apache.log4j.Logger


trait Logging {

  final val logger = Logger.getLogger(getClass)

  def logTrace(msg: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg)
  }

  def logInfo(msg: => String): Unit = {
    if (logger.isInfoEnabled) logger.info(msg)
  }

}
