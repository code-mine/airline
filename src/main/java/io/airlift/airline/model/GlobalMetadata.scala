package io.airlift.airline.model

import scala.collection.JavaConverters._


class GlobalMetadata(name: String, description: String, options: java.lang.Iterable[OptionMetadata],
                     defaultCommand: CommandMetadata, defaultGroupCommands: java.lang.Iterable[CommandMetadata],
                     commandGroups: java.lang.Iterable[CommandGroupMetadata]) {

  val optionsValue = options.asScala.toList
  val defaultGroupCommandsValue = defaultGroupCommands.asScala.toList
  val commandGroupsValue = commandGroups.asScala.toList


  def getName: String = {
    name
  }

  def getDescription: String = {
    description
  }

  def getOptions: java.util.List[OptionMetadata] = {
    optionsValue.asJava
  }

  def getDefaultCommand: CommandMetadata = {
    defaultCommand
  }

  def getDefaultGroupCommands: java.util.List[CommandMetadata] = {
    defaultGroupCommandsValue.asJava
  }

  def getCommandGroups: java.util.List[CommandGroupMetadata] = {
    commandGroupsValue.asJava
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("GlobalMetadata")
    sb.append("{name='").append(name).append('\'')
    sb.append(", description='").append(description).append('\'')
    sb.append(", options=").append(options)
    sb.append(", defaultCommand=").append(defaultCommand)
    sb.append(", defaultGroupCommands=").append(defaultGroupCommands)
    sb.append(", commandGroups=").append(commandGroups)
    sb.append('}')
    sb.toString()
  }
}