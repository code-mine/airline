package io.airlift.airline

import java.util.List
import java.util.concurrent.atomic.AtomicInteger

import com.google.common.base.Splitter
import com.google.common.collect.Lists.newArrayList

object UsagePrinter {
  private def spaces(count: Int): String = {
    " " * count
  }
}

class UsagePrinter(out: java.lang.StringBuilder, maxSize: Int = 79, indent: Int = 0,
                   hangingIndent: Int = 0, currentPosition: AtomicInteger = new AtomicInteger()) {

  def newIndentedPrinter(size: Int): UsagePrinter = {
    new UsagePrinter(out, maxSize, indent + size, hangingIndent, currentPosition)
  }

  def newPrinterWithHangingIndent(size: Int): UsagePrinter = {
    new UsagePrinter(out, maxSize, indent, hangingIndent + size, currentPosition)
  }

  def newline: UsagePrinter = {
    out.append("\n")
    currentPosition.set(0)
    this
  }

  def appendTable(table: java.lang.Iterable[_ <: java.lang.Iterable[String]]): UsagePrinter = {
    import scala.collection.JavaConversions._

    val columnSizes: List[Integer] = newArrayList()
    for (row <- table) {
      var column: Int = 0
      for (value <- row) {
        while (column >= columnSizes.size) {
          columnSizes.add(0)
        }
        columnSizes.set(column, Math.max(value.length, columnSizes.get(column)))
        column += 1
      }
    }
    if (currentPosition.get != 0) {
      currentPosition.set(0)
      out.append("\n")
    }

    for (row <- table) {
      var column: Int = 0
      val line: StringBuilder = new StringBuilder
      for (value <- row) {
        val columnSize: Int = columnSizes.get(column)
        line.append(value)
        line.append(UsagePrinter.spaces(columnSize - value.length))
        line.append("   ")
        column += 1
      }
      out.append(UsagePrinter.spaces(indent)).append(line.toString().trim).append("\n")
    }
    this
  }

  def append(value: String): UsagePrinter = {
    if (value == null) {
      return this
    }
    appendWords(Splitter.onPattern("\\s+").omitEmptyStrings.trimResults.split(String.valueOf(value)))
  }

  def appendWords(words: java.lang.Iterable[String]): UsagePrinter = {
    import scala.collection.JavaConversions._

    for (word <- words) {
      if (currentPosition.get == 0) {
        out.append(UsagePrinter.spaces(indent))
        currentPosition.getAndAdd(indent)
      }
      else if (word.length > maxSize || currentPosition.get + word.length <= maxSize) {
        out.append(" ")
        currentPosition.getAndIncrement
      }
      else {
        out.append("\n").append(UsagePrinter.spaces(indent)).append(UsagePrinter.spaces(hangingIndent))
        currentPosition.set(indent)
      }
      out.append(word)
      currentPosition.getAndAdd(word.length)
    }
    this
  }
}