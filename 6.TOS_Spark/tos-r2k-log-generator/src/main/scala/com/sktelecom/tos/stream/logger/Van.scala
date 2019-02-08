package com.sktelecom.tos.stream.logger

import java.io.{File, FileWriter}
import java.util.Date

import scala.util.Random

/**
  * 로그 출력
  */
object Van {
  var number = 0

  def syncCallToApi(req: Message): Int = {
    val pw = new FileWriter(new File(req.target, vanPath), true)
    pw.write(req.line)
    pw.write("\n")
    pw.close()

    number += 1

    number
  }

  def vanPath: String = {
    "VAN%02d.%2$tm%2$td".format(van, new Date())
  }

  def van: Int = {
    Random.nextInt(100)
  }
}

case class Message(target: String, line: String)
