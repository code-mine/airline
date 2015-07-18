package io.airlift.airline

import java.util.{ArrayList, Collections, Comparator, List}
import javax.annotation.Nullable

import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.UsageHelper.{DEFAULT_OPTION_COMPARATOR, toSynopsisUsage}
import io.airlift.airline.model.{CommandMetadata, OptionMetadata}

import scala.collection.JavaConverters._

class CommandUsage(columnSize: Int = 79, @Nullable optionComparator: Comparator[OptionMetadata] = DEFAULT_OPTION_COMPARATOR) {
  Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0")

  /**
   * Display the help on System.out.
   */
  def usage(@Nullable programName: String, @Nullable groupName: String, commandName: String, command: CommandMetadata) {
    val stringBuilder = new java.lang.StringBuilder
    usage(programName, groupName, commandName, command, stringBuilder)
    System.out.println(stringBuilder.toString)
  }

  /**
   * Store the help in the passed string builder.
   */
  def usage(@Nullable programName: String, @Nullable groupName: String, commandName: String, command: CommandMetadata, out: java.lang.StringBuilder) {
    usage(programName, groupName, commandName, command, new UsagePrinter(out, columnSize))
  }

  def usage(@Nullable programName: String, @Nullable groupName: String, commandName: String, command: CommandMetadata, out: UsagePrinter) {

    out.append("NAME").newline
    out.newIndentedPrinter(8).append(programName).append(groupName).append(commandName).append("-").append(command.getDescription).newline.newline
    out.append("SYNOPSIS").newline

    val synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8)
    var options: List[OptionMetadata] = newArrayList[OptionMetadata]()
    if (programName != null) {
      synopsis.append(programName).appendWords(toSynopsisUsage(sortOptions(command.getGlobalOptions)))
      options.addAll(command.getGlobalOptions)
    }
    if (groupName != null) {
      synopsis.append(groupName).appendWords(toSynopsisUsage(sortOptions(command.getGroupOptions)))
      options.addAll(command.getGroupOptions)
    }
    synopsis.append(commandName).appendWords(toSynopsisUsage(sortOptions(command.getCommandOptions)))
    options.addAll(command.getCommandOptions)

    val arguments = command.getArguments
    if (arguments != null) {
      synopsis.append("[--]").append(UsageHelper.toUsage(arguments))
    }
    synopsis.newline
    synopsis.newline

    if (options.size > 0 || arguments != null) {
      options = sortOptions(options)
      out.append("OPTIONS").newline

      for (option <- options.asScala) {
        if (!option.isHidden) {
          val optionPrinter = out.newIndentedPrinter(8)
          optionPrinter.append(UsageHelper.toDescription(option)).newline
          val descriptionPrinter = optionPrinter.newIndentedPrinter(4)
          descriptionPrinter.append(option.getDescription).newline
          descriptionPrinter.newline
        }
      }

      if (arguments != null) {
        val optionPrinter = out.newIndentedPrinter(8)
        optionPrinter.append("--").newline

        val descriptionPrinter = optionPrinter.newIndentedPrinter(4)
        descriptionPrinter.append("This option can be used to separate command-line options from the " + "list of argument, (useful when arguments might be mistaken for command-line options").newline
        descriptionPrinter.newline

        optionPrinter.append(UsageHelper.toDescription(arguments)).newline
        descriptionPrinter.append(arguments.getDescription).newline
        descriptionPrinter.newline
      }
    }
  }

  private def sortOptions(options: List[OptionMetadata]): List[OptionMetadata] = {
    if (optionComparator != null) {
      val sortedOptions = new ArrayList[OptionMetadata](options)
      Collections.sort(sortedOptions, optionComparator)
      sortedOptions
    } else {
      options
    }
  }
}