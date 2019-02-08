package com.sktelecom.tos.stream.base

import com.typesafe.config.{Config, ConfigFactory}

/**
  * 설정정보(configuration)를 값 객체(value object)에 저장하는 configMager.scala
  * data type 별로 정의
  * typesafe.config wrapping class
  *
  * @param config
  */
class ConfigManager(config: Config) extends Serializable {

  import net.ceedubs.ficus.Ficus._

  /**
    * Int 형 프로퍼티를 조회
    * @param key 프로퍼티 명
    * @return 프로퍼티 값
    */
  def getBoolean(key: String): Boolean = {
    config.as[Boolean](key)
  }

  /**
    * Int 형 프로퍼티를 조회
    * @param key 프로퍼티 명
    * @return 프로퍼티 값
    */
  def getInt(key: String): Int = {
    config.as[Int](key)
  }

  /**
    * String 형 포로퍼티를 조회
    * @param key 프로퍼티 명
    * @return 프로퍼티 값
    */
  def getString(key: String): String = {
    config.as[String](key)
  }

  /**
    * Map 형 포로퍼티를 조회
    * @param key 프로퍼티 명
    * @return 프로퍼티 값
    */
  def getMap(key: String): Map[String, String] = {
    config.as[Map[String, String]](key)
  }

  /**
    * Config 형 포로퍼티를 조회
    * @param key 프로퍼티 명
    * @return 프로퍼티 값
    */
  def getConfig(key: String): Config = {
    config.getConfig(key)
  }

}

/**
  * companion object
  */
object ConfigManager {
  def apply(appName: String): ConfigManager = {
    val appConfig = ConfigFactory.load
    new ConfigManager(appConfig.getConfig(appName))
  }
}
