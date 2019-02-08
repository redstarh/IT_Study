package com.sktelecom.tos.stream.base

import org.junit.Test

/**
 * Utils TestCase
 */
class UtilsTest {
  
  @Test
  def getDateTest(): Unit = {
    println(Utils.getDate())
    println(Utils.getDate("yyyyMMdd"))
    println(Utils.getDate("HHmmss"))
    println(Utils.getDate("yyyy-MM-dd HH:mm:ss"))
    println(Utils.convertToDate("2018-09-14 13:25", "yyyy-MM-dd HH:mm"))
  }

}
