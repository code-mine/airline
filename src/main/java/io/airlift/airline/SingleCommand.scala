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

import java.util.List

import com.google.common.collect.ImmutableMap
import io.airlift.airline.ParserUtil.createInstance
import io.airlift.airline.Preconditions.checkNotNull
import io.airlift.airline.model.{ArgumentsMetadata, CommandMetadata, MetadataLoader}

import scala.collection.JavaConverters._


object SingleCommand {
  def singleCommand[C](command: Class[C]): SingleCommand[C] = {
    return new SingleCommand[C](command)
  }
}

class SingleCommand[C] {
  private final var commandMetadata: CommandMetadata = null

  private def this(command: Class[C]) {
    this()
    checkNotNull(command, "command is null")
    commandMetadata = MetadataLoader.loadCommand(command)
  }

  def getCommandMetadata: CommandMetadata = {
    return commandMetadata
  }

  def parse(args: String*): C = {
    return parse(args)
  }

  def parse(args: Iterable[String]): C = {
    checkNotNull(args, "args is null")
    val parser: Parser = new Parser
    val state: ParseState = parser.parseCommand(commandMetadata, args.asJava)
    validate(state)
    val command: CommandMetadata = state.getCommand
    return createInstance(command.getType, command.getAllOptions, state.getParsedOptions, command.getArguments, state.getParsedArguments, command.getMetadataInjections, ImmutableMap.of[Class[_], AnyRef](classOf[CommandMetadata], commandMetadata))
  }

  private def validate(state: ParseState) {
    val command: CommandMetadata = state.getCommand
    if (command == null) {
      val unparsedInput: List[String] = state.getUnparsedInput
      if (unparsedInput.isEmpty) {
        throw new ParseCommandMissingException
      }
      else {
        throw new ParseCommandUnrecognizedException(unparsedInput)
      }
    }
    val arguments: ArgumentsMetadata = command.getArguments
    if (state.getParsedArguments.isEmpty && arguments != null && arguments.isRequired) {
      throw new ParseArgumentsMissingException(arguments.getTitle)
    }
    if (!state.getUnparsedInput.isEmpty) {
      throw new ParseArgumentsUnexpectedException(state.getUnparsedInput)
    }
    if (state.getLocation eq Context.OPTION) {
      throw new ParseOptionMissingValueException(state.getCurrentOption.getTitle)
    }
    import scala.collection.JavaConversions._
    for (option <- command.getAllOptions) {
      if (option.isRequired && !state.getParsedOptions.containsKey(option)) {
        throw new ParseOptionMissingException(option.getOptions.iterator.next)
      }
    }
  }
}
