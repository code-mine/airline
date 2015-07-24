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

import com.google.common.collect.{ImmutableList, ImmutableMap}
import io.airlift.airline.ParserUtil.createInstance
import io.airlift.airline.model.{CommandMetadata, MetadataLoader}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object SingleCommand {
  def singleCommand[C](command: Class[C]): SingleCommand[C] = {
    new SingleCommand[C](command)
  }
}

class SingleCommand[C](command: Class[C]) {
  val commandMetadata: CommandMetadata = MetadataLoader.loadCommand(command)

  io.airlift.airline.Preconditions.checkNotNull(command, "command is null")


  def getCommandMetadata: CommandMetadata = {
    commandMetadata
  }

  def parse(args: String*): C = {
    parse(ImmutableList.copyOf(args.toIterable))
  }

  def parse(args: Iterable[String]): C = {
    io.airlift.airline.Preconditions.checkNotNull(args, "args is null")

    val parser = new Parser
    val state = parser.parseCommand(commandMetadata, args.toList)
    validate(state)
    val command = state.getCommand
    createInstance(
      command.getType.asInstanceOf[Class[_ >: Null]],
      command.getAllOptions,
      state.getParsedOptions,
      command.getArguments,
      state.getParsedArguments,
      command.getMetadataInjections,
      //Map(classOf[CommandMetadata], commandMetadata).asJava.asInstanceOf[Map[Class[_], AnyRef]]
      ImmutableMap.of(classOf[CommandMetadata], commandMetadata)
    )
  }

  private def validate(state: ParseState) {
    val command = state.getCommand
    if (command == null) {
      val unparsedInput = state.getUnparsedInput
      if (unparsedInput.isEmpty) {
        throw new ParseCommandMissingException
      }
      else {
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
    for (option <- command.getAllOptions) {
      if (option.isRequired && !state.getParsedOptions.containsKey(option)) {
        throw new ParseOptionMissingException(option.getOptions.iterator.next)
      }
    }
  }
}