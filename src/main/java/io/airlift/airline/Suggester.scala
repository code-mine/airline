package io.airlift.airline

trait Suggester {
  def suggest: java.lang.Iterable[String]
}