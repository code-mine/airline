package io.airlift.airline

import java.util.{Collections, Comparator}
import javax.annotation.Nullable

import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.UsageHelper.DEFAULT_OPTION_COMPARATOR
import io.airlift.airline.model.{CommandGroupMetadata, CommandMetadata, GlobalMetadata, OptionMetadata}

class GlobalUsage(columnSize: Int = 79, @Nullable optionComparator: Comparator[OptionMetadata] = DEFAULT_OPTION_COMPARATOR) {
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

    out.append("NAME").newline
    out.newIndentedPrinter(8).append(global.getName).append("-").append(global.getDescription).newline.newline
    out.append("SYNOPSIS").newline
    out.newIndentedPrinter(8).newPrinterWithHangingIndent(8)
      .append(global.getName)
      .appendWords(UsageHelper.toSynopsisUsage(global.getOptions))
      .append("<command> [<args>]")
      .newline.newline

    val options = newArrayList(global.getOptions)
    if (options.size > 0) {
      if (optionComparator != null) {
        Collections.sort(options, optionComparator)
      }
      out.append("OPTIONS").newline
      for (option <- options) {
        val optionPrinter = out.newIndentedPrinter(8)
        optionPrinter.append(UsageHelper.toDescription(option)).newline
        val descriptionPrinter = optionPrinter.newIndentedPrinter(4)
        descriptionPrinter.append(option.getDescription).newline
        descriptionPrinter.newline
      }
    }
    out.append("COMMANDS").newline
    val commandPrinter: UsagePrinter = out.newIndentedPrinter(8)
    for (command <- global.getDefaultGroupCommands) {
      printCommandDescription(commandPrinter, null, command)
    }
    for (group <- global.getCommandGroups) {
      for (command <- group.getCommands) {
        printCommandDescription(commandPrinter, group, command)
      }
    }
  }

  private def printCommandDescription(commandPrinter: UsagePrinter, @Nullable group: CommandGroupMetadata, command: CommandMetadata) {
    if (group != null) {
      commandPrinter.append(group.getName)
    }
    commandPrinter.append(command.getName).newline
    if (command.getDescription != null) {
      commandPrinter.newIndentedPrinter(4).append(command.getDescription).newline
    }
    commandPrinter.newline
  }
}