package com.sktelecom.tos.stream.base

import org.apache.commons.lang3.time.FastDateFormat

/**
  * OfferApp 의 Util object
  */
object Utils {

  /**
   * tos 날짜 포맷
   */
  final val TosDateFormat = "yyyyMMddHHmmss.SSS"
  
  /**
    * format에 맞는 날짜 시간 값을 반환한다.
    *
    * @param format
    * @return
    */
  def getDate(format: String): String = FastDateFormat.getInstance(format).format(System.currentTimeMillis())

  /**
    * 현재 시간을 yyyyMMddHHmmss.SSS 포맷 형태의 값으로 반환한다.
    *
    * @return
    */
  def getDate(): String = getDate(TosDateFormat)

  /**
    * inputFormat 의 strDate 를 outputFormat 으로 변환한다.
    *
    * @param strDate input date
    * @param inputFormat
    * @param outputFormat
    * @return
    */
  def convertToDate(strDate: String, inputFormat: String, outputFormat: String): String = {
    FastDateFormat.getInstance(outputFormat).format(FastDateFormat.getInstance(inputFormat).parse(strDate))
  }

  /**
    * inputFormat 의 strDate 를 'yyyyMMddHHmmss.SSS' 으로 변환한다.
    *
    * @param strDate input date
    * @param inputFormat
    * @return
    */
  def convertToDate(strDate: String, inputFormat: String): String = {
    return convertToDate(strDate, inputFormat, TosDateFormat)
  }

}
