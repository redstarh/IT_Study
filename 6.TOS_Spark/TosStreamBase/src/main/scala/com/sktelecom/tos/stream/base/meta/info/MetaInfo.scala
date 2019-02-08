package com.sktelecom.tos.stream.base.meta.info

import scala.collection.mutable

import org.apache.spark.broadcast.Broadcast

/**
 * event mapping 정보와 cep 전송 여부, cep 전송 포맷을 가지고 있는 클래스
 * driver node에서 각 broadcast 변수에 값 주입을 하며,
 * worker node에서 해당 값을 이용하여 각종 mapping과 cep 전송 포맷 변경 처리에 이용함
 * 
 */
class MetaInfo extends Serializable {

  /**
   * cep 전송 시간
   */
  var cepSendTime: Broadcast[(String, String)] = null
  
  /**
   * object name spec 정보를 가지고 있는 broadcast 변수
   */
  var objNameSpec: Broadcast[mutable.Map[String, (String, String, String, String)]] = null

  /**
   * event reason spec 정보를 가지고 있는 broadcast 변수
   */
  var eventReasonSpec: Broadcast[mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]]] = null

  /**
   * event code mapping spec 정보를 가지고 있는 broadcast 변수
   */
  var eventCodeMappingSpec: Broadcast[mutable.Map[String, mutable.Map[(String, String), mutable.Map[(String, String), (String, String, String, String)]]]] = null

  /**
   * event filter spec 정보를 가지고 있는 broadcast 변수
   */
  var eventFilterSpec: Broadcast[mutable.Map[String, mutable.Map[(String, String), Seq[(String, String, String, String, Map[String,String] => String)]]]] = null

  /**
   * event url spec 정보를 가지고 있는 broadcast 변수
   */
  var eventUrlSpec: Broadcast[mutable.Map[String, mutable.Map[String, mutable.Map[String, String]]]] = null

  /**
   * cep event 정보를 가지고 있는 broadcast 변수
   */
  var cepEvent: Broadcast[Seq[String]] = null

  /**
   * cep event format 정보를 가지고 있는 broadcast 변수
   */
  var cepEventFormat: Broadcast[mutable.Map[String, mutable.Map[String, String]]] = null

}
