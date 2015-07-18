package io.airlift.airline

import java.util.{Collections, Comparator, List}
import javax.annotation.Nullable

import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.UsageHelper.{DEFAULT_COMMAND_COMPARATOR, DEFAULT_OPTION_COMPARATOR}
import io.airlift.airline.model.{CommandGroupMetadata, CommandMetadata, GlobalMetadata, OptionMetadata}

object CommandGroupUsage {
  private def longest(iterable: Iterable[String]): String = {
    var longest = ""
    for (value <- iterable) {
      if (value.length > longest.length) {
        longest = value
      }
    }
    longest
  }
}

class CommandGroupUsage(columnSize: Int = 79, hideGlobalOptions: Boolean = false,
                        @Nullable optionComparator: Comparator[OptionMetadata] = DEFAULT_OPTION_COMPARATOR) {
  Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0")

  private final val commandComparator = DEFAULT_COMMAND_COMPARATOR

  /**
   * Display the help on System.out.
   */
  def usage(@Nullable global: GlobalMetadata, group: CommandGroupMetadata) {
    val stringBuilder = new java.lang.StringBuilder
    usage(global, group, stringBuilder)
    System.out.println(stringBuilder.toString)
  }

  /**
   * Store the help in the passed string builder.
   */
  def usage(@Nullable global: GlobalMetadata, group: CommandGroupMetadata, out: java.lang.StringBuilder) {
    usage(global, group, new UsagePrinter(out, columnSize))
  }

  def usage(@Nullable global: GlobalMetadata, group: CommandGroupMetadata, out: UsagePrinter) {
    import scala.collection.JavaConversions._

    out.append("NAME").newline
    out.newIndentedPrinter(8).append(global.getName).append(group.getName).append("-").append(group.getDescription).newline.newline
    out.append("SYNOPSIS").newline
    val synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8)
    val commands: List[CommandMetadata] = newArrayList(group.getCommands)
    Collections.sort(commands, commandComparator)

    if (group.getDefaultCommand != null) {
      val command = group.getDefaultCommand
      if (global != null) {
        synopsis.append(global.getName)
        if (!hideGlobalOptions) {
          synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions))
        }
      }
      synopsis.append(group.getName).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions))
      synopsis.newline
    }

    for (command <- commands) {
      if (global != null) {
        synopsis.append(global.getName)
        if (!hideGlobalOptions) {
          synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions))
        }
      }
      synopsis.append(group.getName).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions))
      synopsis.append(command.getName).appendWords(UsageHelper.toSynopsisUsage(command.getCommandOptions))
      synopsis.newline
    }
    synopsis.newline

    val options: List[OptionMetadata] = newArrayList()
    options.addAll(group.getOptions)
    if (global != null && !hideGlobalOptions) {
      options.addAll(global.getOptions)
    }
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
    if (commands.size > 0 || group.getDefaultCommand != null) {
      out.append("COMMANDS").newline
      val commandPrinter = out.newIndentedPrinter(8)
      if (group.getDefaultCommand != null && group.getDefaultCommand.getDescription != null) {
        commandPrinter.append("With no arguments,").append(group.getDefaultCommand.getDescription).newline.newline
      }
      for (command <- group.getCommands) {
        commandPrinter.append(command.getName).newline
        val descriptionPrinter = commandPrinter.newIndentedPrinter(4)
        descriptionPrinter.append(command.getDescription).newline.newline
        for (option <- command.getCommandOptions) {
          if (!option.isHidden && option.getDescription != null) {
            descriptionPrinter.append("With").append(CommandGroupUsage.longest(option.getOptions))
              .append("option,").append(option.getDescription).newline.newline
          }
        }
      }
    }
  }
}