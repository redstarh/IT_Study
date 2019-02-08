package com.sktelecom.tos.stream.oracle

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

import scala.io.Source

class ConfigSpec extends FlatSpec {

  behavior of "A Config"

  it should "load conf" in {
    import net.ceedubs.ficus.Ficus._
    println(new File(".").getAbsolutePath)
//    Source.
    val f = Source.fromFile("application.conf", "utf-8")
    val appConfig = ConfigFactory.parseReader(f.reader()).getConfig("application")
    println(appConfig.toString)
    val sparkConfig = appConfig.as[Map[String, String]]("sparkConfig")
  }
}
