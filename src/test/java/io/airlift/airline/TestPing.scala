package io.airlift.airline

import com.google.common.base.Joiner
import org.testng.annotations.Test

import scala.collection.JavaConversions._

class TestPing {
  @Test def test() {
    ping()
    ping("-c", "5")
    ping("--count", "9")
    ping("-h")
    ping("--help")
  }

  private def ping(args: String*) {
    println("$ ping " + args.mkString(" "))
    Ping.main(args: _*)
    println()
  }
}