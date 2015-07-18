package io.airlift.airline

import java.util.{Comparator, List, Set}

import com.google.common.collect.ComparisonChain
import io.airlift.airline.model.{ArgumentsMetadata, CommandMetadata, OptionMetadata}

import scala.collection.JavaConverters._


object UsageHelper {
  val DEFAULT_OPTION_COMPARATOR: Comparator[OptionMetadata] = new Comparator[OptionMetadata] {
    def compare(o1: OptionMetadata, o2: OptionMetadata): Int = {
      var option1 = o1.getOptions.iterator.next
      option1 = option1.replaceFirst("^-+", "")
      var option2 = o2.getOptions.iterator.next
      option2 = option2.replaceFirst("^-+", "")
      ComparisonChain.start
        .compare(option1.toLowerCase, option2.toLowerCase)
        .compare(option2, option1)
        .compare(System.identityHashCode(o1), System.identityHashCode(o2))
        .result
    }
  }
  val DEFAULT_COMMAND_COMPARATOR: Comparator[CommandMetadata] = new  Comparator[CommandMetadata] {
    def compare(o1: CommandMetadata, o2: CommandMetadata): Int = {
      ComparisonChain.start
        .compare(o1.getName.toLowerCase, o2.getName.toLowerCase)
        .compare(o2.getName, o1.getName)
        .compare(System.identityHashCode(o1), System.identityHashCode(o2))
        .result
    }
  }

  def toDescription(option: OptionMetadata): String = {
    val options: Set[String] = option.getOptions
    var argumentString: String = null
    if (option.getArity > 0) {
      argumentString = Seq(option.getTitle).map("<" + _ + ">").mkString(" ")
    } else {
      argumentString = null
    }
    options.asScala.toList
      .map { option: String =>
        if (argumentString != null) option + " " + argumentString else option
      }
      .mkString(", ")
  }

  def toDescription(arguments: ArgumentsMetadata): String = {
    if (!arguments.getUsage.isEmpty) {
      return arguments.getUsage
    }
    "<" + arguments.getTitle + ">"
  }

  def toUsage(option: OptionMetadata): String = {
    val options: Set[String] = option.getOptions
    val required: Boolean = option.isRequired
    val stringBuilder = new java.lang.StringBuilder
    if (!required) {
      stringBuilder.append('[')
    }
    if (options.size > 1) {
      stringBuilder.append('(')
    }
    var argumentString: String = null
    if (option.getArity > 0) {
      argumentString = Seq(option.getTitle).map("<" + _ + ">").mkString(" ")
    }
    else {
      argumentString = null
    }
    val optionsAsString = options.asScala.toList
      .map { option: String =>
        if (argumentString != null) {
          option + " " + argumentString
        } else {
          option
        }
      }
      .mkString(" | ")

    stringBuilder.append(optionsAsString)

    if (options.size > 1) {
      stringBuilder.append(')')
    }
    if (option.isMultiValued) {
      stringBuilder.append("...")
    }
    if (!required) {
      stringBuilder.append(']')
    }
    stringBuilder.toString
  }

  def toUsage(arguments: ArgumentsMetadata): String = {
    if (!arguments.getUsage.isEmpty) {
      return arguments.getUsage
    }
    val required: Boolean = arguments.isRequired
    val stringBuilder = new java.lang.StringBuilder
    if (!required) {
      stringBuilder.append('[')
    }
    stringBuilder.append("<").append(arguments.getTitle).append(">")
    if (arguments.isMultiValued) {
      stringBuilder.append("...")
    }
    if (!required) {
      stringBuilder.append(']')
    }
    stringBuilder.toString
  }

  def toSynopsisUsage(options: List[OptionMetadata]): List[String] = {
    options.asScala.filter(!_.isHidden).map(toUsage).asJava
  }
}

