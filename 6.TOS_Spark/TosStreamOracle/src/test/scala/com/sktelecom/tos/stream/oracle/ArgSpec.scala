package com.sktelecom.tos.stream.oracle

import com.sktelecom.tos.stream.util.{MetaReloader, StreamingContextStopper}
import org.scalatest.FlatSpec

class ArgSpec extends FlatSpec {

  it should "parsing arguments with StreamingContextStopper" in {
    StreamingContextStopper.main("--stopGracefully --stopDisgracefully --host host --port 8".split(" "))
  }

  it should "parsing arguments with MetaReloader" in {
    MetaReloader.main("--host host --port 8".split(" "))
  }

}
