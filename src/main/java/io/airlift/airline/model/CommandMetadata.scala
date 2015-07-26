package io.airlift.airline.model

import java.util

import com.google.common.collect.ImmutableList
import io.airlift.airline.Accessor

import scala.collection.JavaConverters._


class CommandMetadata(name: String, description: String, hidden: Boolean, globalOptions: java.lang.Iterable[OptionMetadata],
                      groupOptions: java.lang.Iterable[OptionMetadata], commandOptions: java.lang.Iterable[OptionMetadata],
                      arguments: ArgumentsMetadata, metadataInjections: java.lang.Iterable[Accessor], `type`: Class[_]) {
  def getName: String = {
    name
  }

  def getDescription: String = {
    description
  }

  def isHidden: Boolean = {
    hidden
  }

  def getAllOptions: util.List[OptionMetadata] = {
    ImmutableList.builder[OptionMetadata].addAll(globalOptions).addAll(groupOptions).addAll(commandOptions).build
  }

  def getGlobalOptions: util.List[OptionMetadata] = {
    globalOptions.asScala.toList.asJava
  }

  def getGroupOptions: util.List[OptionMetadata] = {
    groupOptions.asScala.toList.asJava
  }

  def getCommandOptions: util.List[OptionMetadata] = {
    commandOptions.asScala.toList.asJava
  }

  def getArguments: ArgumentsMetadata = {
    arguments
  }

  def getMetadataInjections: util.List[Accessor] = {
    metadataInjections.asScala.toList.asJava
  }

  def getType: Class[_] = {
    `type`
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("CommandMetadata")
    sb.append("{name='").append(name).append('\'')
    sb.append(", description='").append(description).append('\'')
    sb.append(", globalOptions=").append(globalOptions)
    sb.append(", groupOptions=").append(groupOptions)
    sb.append(", commandOptions=").append(commandOptions)
    sb.append(", arguments=").append(arguments)
    sb.append(", metadataInjections=").append(metadataInjections)
    sb.append(", type=").append(`type`)
    sb.append('}')
    sb.toString()
  }
}