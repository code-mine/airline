package io.airlift.airline.model

import scala.collection.JavaConverters._

class CommandGroupMetadata(name: String, description: String, options: java.lang.Iterable[OptionMetadata],
                           defaultCommand: CommandMetadata, commands: java.lang.Iterable[CommandMetadata]) {
  def getName: String = { name }
  def getDescription: String = { description }
  def getOptions: java.util.List[OptionMetadata] = { options.asScala.toList.asJava }
  def getDefaultCommand: CommandMetadata = { defaultCommand }
  def getCommands: java.util.List[CommandMetadata] = { commands.asScala.toList.asJava }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("CommandGroupMetadata")
    sb.append("{name='").append(name).append('\'')
    sb.append(", description='").append(description).append('\'')
    sb.append(", options=").append(options)
    sb.append(", defaultCommand=").append(defaultCommand)
    sb.append(", commands=").append(commands)
    sb.append('}')
    sb.toString()
  }
}