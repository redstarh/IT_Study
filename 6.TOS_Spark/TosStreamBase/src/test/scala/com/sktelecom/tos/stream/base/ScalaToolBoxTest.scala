package com.sktelecom.tos.stream.base

//import scala.reflect.runtime.universe.{ Quasiquote, runtimeMirror }
//import scala.tools.reflect.ToolBox
import org.junit.Test
import org.junit.Ignore

class ScalaToolBoxTest {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println("elapsed time: " + (t1 - t0) + "ns")
    result
  }
  
  @Ignore
  @Test
  def test(): Unit = {
//    val mirror = runtimeMirror(getClass.getClassLoader)
//    val tb = ToolBox(mirror).mkToolBox()
//    val data = Array(1, 2, 3)
//
//    val functionWrapper = """
//      object FunctionWrapper { 
//        def apply(x: Int): Int = x + 2 
//      }"""
//    
//    val functionSymbol = tb.define(tb.parse(functionWrapper).asInstanceOf[tb.u.ImplDef])
//   
//
////    val result = time {data.map(x => tb.eval(q"$functionSymbol.apply($x)"))}
////    println(s"Data after function applied on it: '${result.toList}'")
////    
//    
//    val func = time {tb.eval(q"$functionSymbol.apply _").asInstanceOf[Int => Int]}
//    val result2 = time {data.map(func)}
    
  }
  
  
  
  def _compile(code:String): (Map[String, String] => String) = {
    
    import scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    
    val toolbox  = currentMirror.mkToolBox()
    toolbox.eval(toolbox.parse(code)).asInstanceOf[Map[String, String] => String]    
    
  }
    
  @Test
  def compileTest(): Unit = {
  
    val code = """
        (body:Map[String, String]) => {
          import com.sktelecom.tos.stream.base.Utils
          val auditDtm = body.getOrElse("AUDIT_DTM", "")
          Utils.convertToDate(auditDtm, "yyyy-MM-dd:HH:mm:ss", "yyyyMMddHHmmss")
        }
      """
    
    val func = _compile(code)
    
    println(func(Map("AUDIT_DTM" -> "2018-10-26:17:29:00")))
  }
  
  
  @Ignore
  @Test
  def test2(): Unit = {
    
//    import scala.reflect.runtime.universe
    import scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    
    val t0 = System.currentTimeMillis()
    val toolbox  = currentMirror.mkToolBox()
    val func = {
      toolbox.eval(toolbox.parse("(s:String) => s")).asInstanceOf[String => Int]
    }

    val t1 = System.currentTimeMillis()
    println("compile : " + (t1 - t0))
    
    println(func("123"))
    println("execute : " + (System.currentTimeMillis() - t1))
    
    println(func("456"))
    println("execute : " + (System.currentTimeMillis() - t1))
    
  }
  
}

