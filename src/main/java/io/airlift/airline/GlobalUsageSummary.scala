package io.airlift.airline

import java.util
import java.util.Map.Entry

import com.google.common.base.Objects
import com.google.common.collect.Maps.newTreeMap
import io.airlift.airline.UsageHelper.toUsage
import io.airlift.airline.model.{GlobalMetadata, OptionMetadata}

class GlobalUsageSummary(columnSize: Int = 79) {
  Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0")

  /**
   * Display the help on System.out.
   */
  def usage(global: GlobalMetadata) {
    val stringBuilder = new java.lang.StringBuilder
    usage(global, stringBuilder)
    System.out.println(stringBuilder.toString)
  }

  /**
   * Store the help in the passed string builder.
   */
  def usage(global: GlobalMetadata, out: java.lang.StringBuilder) {
    usage(global, new UsagePrinter(out, columnSize))
  }

  def usage(global: GlobalMetadata, out: UsagePrinter) {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._

    val commandArguments = global.getOptions.asScala.map{ option: OptionMetadata =>
      if (option.isHidden) null else toUsage(option)
    }.asJava
    out.newPrinterWithHangingIndent(8)
      .append("usage:")
      .append(global.getName)
      .appendWords(commandArguments)
      .append("<command> [<args>]")
      .newline.newline

    val commands = newTreeMap[String, String]()
    for (commandMetadata <- global.getDefaultGroupCommands) {
      if (!commandMetadata.isHidden) {
        commands.put(commandMetadata.getName, commandMetadata.getDescription)
      }
    }
    for (commandGroupMetadata <- global.getCommandGroups) {
      commands.put(commandGroupMetadata.getName, commandGroupMetadata.getDescription)
    }
    out.append("The most commonly used ").append(global.getName).append(" commands are:").newline

    val strings = commands.entrySet().toList.map { entry: Entry[String, String] =>
      util.Arrays.asList(entry.getKey, Objects.firstNonNull(entry.getValue, ""))
    }.asJava
    out.newIndentedPrinter(4).appendTable(strings)

    out.newline
    out.append("See")
      .append("'" + global.getName)
      .append("help <command>' for more information on a specific command.")
      .newline
  }
}