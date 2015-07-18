package io.airlift.airline

import java.util.concurrent.Callable
import javax.inject.Inject

import io.airlift.airline.model.{CommandMetadata, GlobalMetadata}

import scala.collection.JavaConversions._

@Command(name = "help", description = "Display help information")
object Help {
  def help(command: CommandMetadata) {
    val stringBuilder = new java.lang.StringBuilder
    help(command, stringBuilder)
    System.out.println(stringBuilder.toString)
  }

  def help(command: CommandMetadata, out: java.lang.StringBuilder) {
    new CommandUsage().usage(null, null, command.getName, command, out)
  }

  def help(global: GlobalMetadata, commandNames: java.util.List[String]) {
    val stringBuilder = new java.lang.StringBuilder
    help(global, commandNames, stringBuilder)
    System.out.println(stringBuilder.toString)
  }

  def help(global: GlobalMetadata, commandNames: java.util.List[String], out: java.lang.StringBuilder) {
    if (commandNames.isEmpty) {
      new GlobalUsageSummary().usage(global, out)
      return
    }
    val name: String = commandNames.head
    if (name == global.getName) {
      new GlobalUsage().usage(global, out)
      return
    }
    for (command <- global.getDefaultGroupCommands) {
      if (name == command.getName) {
        new CommandUsage().usage(global.getName, null, command.getName, command, out)
        return
      }
    }
    for (group <- global.getCommandGroups) {
      if (name.endsWith(group.getName)) {
        if (commandNames.size == 1) {
          new CommandGroupUsage().usage(global, group, out)
          return
        }
        else {
          val commandName: String = commandNames.get(1)
          for (command <- group.getCommands) {
            if (commandName == command.getName) {
              new CommandUsage().usage(global.getName, group.getName, command.getName, command, out)
              return
            }
          }
          System.out.println("Unknown command " + name + " " + commandName)
        }
      }
    }
    System.out.println("Unknown command " + name)
  }
}

@Command(name = "help", description = "Display help information") class Help extends Runnable with Callable[Void] {
  @Inject var global: GlobalMetadata = null
  @Arguments var command = new java.util.ArrayList[String]()

  def run() {
    Help.help(global, command)
  }

  def call: Void = {
    run()
    null
  }
}
