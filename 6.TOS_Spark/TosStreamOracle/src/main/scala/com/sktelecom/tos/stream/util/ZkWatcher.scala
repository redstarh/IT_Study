package com.sktelecom.tos.stream.util

import org.apache.spark.internal.Logging
import org.apache.zookeeper.{WatchedEvent, Watcher}

class ZkWatcher extends Watcher with Logging {
  override def process(watchedEvent: WatchedEvent): Unit = {
    logInfo(watchedEvent.toString)
  }
}

object ZkWatcher {
  def apply(): ZkWatcher = new ZkWatcher()
}
