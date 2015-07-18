package io.airlift.airline

import java.util

import com.google.common.collect.{ArrayListMultimap, ImmutableList, ImmutableListMultimap, ListMultimap}
import io.airlift.airline.model.{CommandGroupMetadata, CommandMetadata, OptionMetadata}

import scala.collection.JavaConverters._

object ParseState {
  def newInstance: ParseState = {
    new ParseState(null, null,
      ArrayListMultimap.create[OptionMetadata, AnyRef], ImmutableList.of[Context], scala.List[AnyRef]().asJava, null, ImmutableList.of[String])
  }
}

class ParseState(group: CommandGroupMetadata, command: CommandMetadata, parsedOptions: ListMultimap[OptionMetadata, AnyRef],
                 locationStack: java.util.List[Context], parsedArguments: java.util.List[AnyRef],
                 currentOption: OptionMetadata, unparsedInput: java.util.List[String]) {

  def pushContext(location: Context): ParseState = {
    val locationStack = ImmutableList.builder[Context].addAll(this.locationStack).add(location).build
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput)
  }

  def popContext: ParseState = {
    val locationStack = new util.ArrayList[Context](this.locationStack.subList(0, this.locationStack.size() - 1))
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput)
  }

  def withOptionValue(option: OptionMetadata, value: AnyRef): ParseState = {
    val newOptions = ImmutableListMultimap.builder[OptionMetadata, AnyRef].putAll(parsedOptions).put(option, value).build
    new ParseState(group, command, newOptions, locationStack, parsedArguments, currentOption, unparsedInput)
  }

  def withGroup(group: CommandGroupMetadata): ParseState = {
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput)
  }

  def withCommand(command: CommandMetadata): ParseState = {
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput)
  }

  def withOption(option: OptionMetadata): ParseState = {
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, option, unparsedInput)
  }

  def withArgument(argument: AnyRef): ParseState = {
    val newArguments = new util.ArrayList[AnyRef]((parsedArguments.asScala :+ argument).asJava)
    new ParseState(group, command, parsedOptions, locationStack, newArguments, currentOption, unparsedInput)
  }

  def withUnparsedInput(input: String): ParseState = {
    val newUnparsedInput = ImmutableList.builder[String].addAll(unparsedInput).add(input).build
    new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, newUnparsedInput)
  }

  override def toString: String = {
    "ParseState{" +
      "locationStack=" + locationStack +
      ", group=" + group +
      ", command=" + command +
      ", parsedOptions=" + parsedOptions +
      ", parsedArguments=" + parsedArguments +
      ", currentOption=" + currentOption +
      ", unparsedInput=" + unparsedInput +
      '}'
  }

  def getLocation: Context = {
    locationStack.asScala.last
  }

  def getGroup: CommandGroupMetadata = {
    group
  }

  def getCommand: CommandMetadata = {
    command
  }

  def getCurrentOption: OptionMetadata = {
    currentOption
  }

  def getParsedOptions: ListMultimap[OptionMetadata, AnyRef] = {
    parsedOptions
  }

  def getParsedArguments: java.util.List[AnyRef] = {
    parsedArguments
  }

  def getUnparsedInput: java.util.List[String] = {
    unparsedInput
  }
}