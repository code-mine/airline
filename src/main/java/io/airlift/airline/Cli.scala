/**
 * Copyright (C) 2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.airline

import java._
import java.util._

import _root_.io.airlift.airline.ParserUtil.createInstance
import _root_.io.airlift.airline.model.MetadataLoader.{loadCommand, loadCommandGroup, loadCommands}
import _root_.io.airlift.airline.model._
import com.google.common.collect.{ImmutableList, ImmutableMap}
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps.newHashMap

import scala.collection.JavaConverters._

object Cli {
  def builder[T](name: String): Cli.CliBuilder[T] = {
    Preconditions.checkNotNull(name, "name is null")
    new Cli.CliBuilder[T](name)
  }

  class CliBuilder[C](name: String) {
    protected var description: String = null
    protected var typeConverter: TypeConverter = new TypeConverter
    protected var optionSeparators: String = null
    private var defaultCommand: Class[_ <: C] = null
    private val defaultCommandGroupCommands: java.util.List[Class[_ <: C]] = newArrayList()
    protected val groups: java.util.Map[String, Cli.GroupBuilder[C]] = newHashMap()

    Preconditions.checkNotNull(name, "name is null")
    Preconditions.checkArgument(!name.isEmpty, "name is empty")


    def withDescription(description: String): Cli.CliBuilder[C] = {
      Preconditions.checkNotNull(description, "description is null")
      Preconditions.checkArgument(!description.isEmpty, "description is empty")
      this.description = description
      this
    }

    def withDefaultCommand(defaultCommand: Class[_ <: C]): Cli.CliBuilder[C] = {
      this.defaultCommand = defaultCommand
      this
    }

    def withCommand(command: Class[_ <: C]): Cli.CliBuilder[C] = {
      this.defaultCommandGroupCommands.add(command)
      this
    }

    def withCommands(command: Class[_ <: C], moreCommands: Class[_ <: C]*): Cli.CliBuilder[C] = {
      this.defaultCommandGroupCommands.add(command)
      this.defaultCommandGroupCommands.addAll(moreCommands.asJava)
      this
    }

    def withCommands2(command: Class[_], moreCommands: Class[_]*): Cli.CliBuilder[_] = {
      this.defaultCommandGroupCommands.add(command.asInstanceOf[Class[_ <: C]])
      this.defaultCommandGroupCommands.addAll(moreCommands.asJava.asInstanceOf[util.Collection[_ <: Class[_ <: C]]])
      this
    }

    def withCommands(commands: java.lang.Iterable[Class[_ <: C]]): Cli.CliBuilder[C] = {
      this.defaultCommandGroupCommands.addAll(ImmutableList.copyOf(commands))
      this
    }

    def withGroup(name: String): Cli.GroupBuilder[C] = {
      Preconditions.checkNotNull(name, "name is null")
      Preconditions.checkArgument(!name.isEmpty, "name is empty")
      if (groups.containsKey(name)) {
        return groups.get(name)
      }
      val group = new Cli.GroupBuilder[C](name)
      groups.put(name, group)
      group
    }

    def build: Cli[C] = {
      new Cli[C](name, description, typeConverter, defaultCommand, defaultCommandGroupCommands, groups.values)
    }
  }

  class GroupBuilder[C](val name: String) {
    var description: String = null
    var defaultCommand: Class[_ <: C] = null
    val commands: java.util.List[Class[_ <: C]] = newArrayList()

    Preconditions.checkNotNull(name, "name is null")


    def withDescription(description: String): Cli.GroupBuilder[C] = {
      Preconditions.checkNotNull(description, "description is null")
      Preconditions.checkArgument(!description.isEmpty, "description is empty")
      Preconditions.checkState(this.description == null, "description is already set")
      this.description = description
      this
    }

    def withDefaultCommand(defaultCommand: Class[_ <: C]): Cli.GroupBuilder[C] = {
      Preconditions.checkNotNull(defaultCommand, "defaultCommand is null")
      Preconditions.checkState(this.defaultCommand == null, "defaultCommand is already set")
      this.defaultCommand = defaultCommand
      this
    }

    def withCommand(command: Class[_ <: C]): Cli.GroupBuilder[C] = {
      Preconditions.checkNotNull(command, "command is null")
      commands.add(command)
      this
    }

    def withCommands(command: Class[_ <: C], moreCommands: Class[_ <: C]*): Cli.GroupBuilder[C] = {
      this.commands.add(command)
      this.commands.addAll(moreCommands.asJava)
      this
    }

    def withCommands(commands: java.lang.Iterable[Class[_ <: C]]): Cli.GroupBuilder[C] = {
      this.commands.addAll(commands.asScala.toList.asJava)
      this
    }
  }

}

class Cli[C](name: String, description: String, typeConverter: TypeConverter, defaultCommand: Class[_ <: C],
             defaultGroupCommands: java.lang.Iterable[Class[_ <: C]], groups: java.lang.Iterable[Cli.GroupBuilder[C]]) {
  Preconditions.checkNotNull(name, "name is null")
  Preconditions.checkNotNull(typeConverter, "typeConverter is null")

  var defaultCommandMetadata: CommandMetadata = null
  if (defaultCommand != null) {
    defaultCommandMetadata = loadCommand(defaultCommand)
  }
  val defaultCommandGroup: util.List[CommandMetadata] = loadCommands[C](defaultGroupCommands)
  val commandGroups: util.List[CommandGroupMetadata] = groups.asScala.map { group =>
    loadCommandGroup(group.name, group.description, loadCommand(group.defaultCommand), loadCommands[C](group.commands))
  }.toList.asJava
  val metadata: GlobalMetadata = MetadataLoader.loadGlobal(name, description, defaultCommandMetadata, defaultCommandGroup, commandGroups)


  def getMetadata: GlobalMetadata = {
    metadata
  }

  def parse(args: String*): C = {
    parse(args.asJava)
  }

  def parse(args: java.lang.Iterable[String]): C = {
    Preconditions.checkNotNull(args, "args is null")
    val parser = new Parser
    var state = parser.parse(metadata, args)
    if (state.getCommand == null) {
      if (state.getGroup != null) {
        state = state.withCommand(state.getGroup.getDefaultCommand)
      }
      else {
        state = state.withCommand(metadata.getDefaultCommand)
      }
    }
    validate(state)

    val command = state.getCommand
    createInstance(
      command.getType.asInstanceOf[Class[_ >: Null]],
      command.getAllOptions,
      state.getParsedOptions,
      command.getArguments,
      state.getParsedArguments,
      command.getMetadataInjections,
      ImmutableMap.of[Class[_], AnyRef](classOf[GlobalMetadata], metadata)
    )
  }

  private def validate(state: ParseState) {
    val command = state.getCommand
    if (command == null) {
      val unparsedInput = state.getUnparsedInput
      if (unparsedInput.isEmpty) {
        throw new ParseCommandMissingException
      } else {
        throw new ParseCommandUnrecognizedException(unparsedInput)
      }
    }
    val arguments = command.getArguments
    if (state.getParsedArguments.isEmpty && arguments != null && arguments.isRequired) {
      throw new ParseArgumentsMissingException(arguments.getTitle)
    }
    if (!state.getUnparsedInput.isEmpty) {
      throw new ParseArgumentsUnexpectedException(state.getUnparsedInput)
    }
    if (state.getLocation eq Context.OPTION) {
      throw new ParseOptionMissingValueException(state.getCurrentOption.getTitle)
    }
    for (option: OptionMetadata <- command.getAllOptions.asScala) {
      if (option.isRequired && !state.getParsedOptions.containsKey(option)) {
        throw new ParseOptionMissingException(option.getOptions.iterator.next)
      }
    }
  }
}