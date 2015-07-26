package io.airlift.airline

import java.util
import java.util.regex.Pattern

import com.google.common.base.Predicates.{compose, equalTo}
import com.google.common.base.Splitter
import com.google.common.collect.Iterables.find
import com.google.common.collect.{ImmutableList, Iterators, PeekingIterator}
import io.airlift.airline.model.{ArgumentsMetadata, CommandGroupMetadata, CommandMetadata, GlobalMetadata, OptionMetadata}

import scala.collection.JavaConverters._

object Parser {
  private val SHORT_OPTIONS_PATTERN = Pattern.compile("-[^-].*")
}

class Parser {
  def parse(metadata: GlobalMetadata, params: String*): ParseState = {
    parse(metadata, params.toList.asJava)
  }

  def parse(metadata: GlobalMetadata, params: java.lang.Iterable[String]): ParseState = {
    val tokens = Iterators.peekingIterator(params.iterator)
    var state = ParseState.newInstance().pushContext(Context.GLOBAL)
    state = parseOptions(tokens, state, metadata.getOptions)
    if (tokens.hasNext) {
      val group = metadata.getCommandGroups.asScala.find { it => it.getName == tokens.peek() }.orNull
      if (group != null) {
        tokens.next
        state = state.withGroup(group).pushContext(Context.GROUP)
        state = parseOptions(tokens, state, state.getGroup.getOptions)
      }
    }
    var expectedCommands = metadata.getDefaultGroupCommands
    if (state.getGroup != null) {
      expectedCommands = state.getGroup.getCommands
    }
    if (tokens.hasNext) {
      val command = find(expectedCommands, compose(equalTo(tokens.peek), CommandMetadata.nameGetter), null)
      if (command == null) {
        while (tokens.hasNext) {
          state = state.withUnparsedInput(tokens.next)
        }
      } else {
        tokens.next
        state = state.withCommand(command).pushContext(Context.COMMAND)
        while (tokens.hasNext) {
          state = parseOptions(tokens, state, command.getCommandOptions)
          state = parseArgs(state, tokens, command.getArguments)
        }
      }
    }
    state
  }

  def parseCommand(command: CommandMetadata, params: java.lang.Iterable[String]): ParseState = {
    val tokens = Iterators.peekingIterator(params.iterator)
    var state = ParseState.newInstance().pushContext(Context.GLOBAL).withCommand(command)
    while (tokens.hasNext) {
      state = parseOptions(tokens, state, command.getCommandOptions)
      state = parseArgs(state, tokens, command.getArguments)
    }
    state
  }

  private def parseOptions(tokens: PeekingIterator[String], stateArg: ParseState, allowedOptions: util.List[OptionMetadata]): ParseState = {
    var state = stateArg
    val break = false
    while (tokens.hasNext && !break) {
      var nextState = parseSimpleOption(tokens, state, allowedOptions)
      if (nextState != null) {
        state = nextState
        // continue
      } else {
        nextState = parseLongGnuGetOpt(tokens, state, allowedOptions)
        if (nextState != null) {
          state = nextState
          // continue
        } else {
          nextState = parseClassicGetOpt(tokens, state, allowedOptions)
          if (nextState != null) {
            state = nextState
            // continue
          } else {
            return state //break
          }
        }
      }
    }
    state
  }

  private def parseSimpleOption(tokens: PeekingIterator[String], stateArg: ParseState, allowedOptions: util.List[OptionMetadata]): ParseState = {
    var state = stateArg
    val option = findOption(allowedOptions, tokens.peek)
    if (option == null) {
      return null
    }
    tokens.next
    state = state.pushContext(Context.OPTION).withOption(option)
    var value: Any = null
    if (option.getArity == 0) {
      state = state.withOptionValue(option, java.lang.Boolean.TRUE).popContext
    }
    else if (option.getArity == 1) {
      if (tokens.hasNext) {
        value = TypeConverter.newInstance.convert(option.getTitle, option.getJavaType, tokens.next)
        state = state.withOptionValue(option, value).popContext
      }
    }
    else {
      var values = scala.List[Any]()
      var count = 0
      while (count < option.getArity && tokens.hasNext) {
        values = values :+ TypeConverter.newInstance.convert(option.getTitle, option.getJavaType, tokens.next)
        count += 1
      }
      if (count == option.getArity) {
        state = state.withOptionValue(option, values.asJava).popContext
      }
    }
    state
  }

  private def parseLongGnuGetOpt(tokens: PeekingIterator[String], stateArg: ParseState, allowedOptions: util.List[OptionMetadata]): ParseState = {
    val parts = ImmutableList.copyOf(Splitter.on('=').limit(2).split(tokens.peek))
    if (parts.size != 2) {
      return null
    }
    val option = findOption(allowedOptions, parts.get(0))
    if (option == null || option.getArity != 1) {
      return null
    }
    tokens.next

    var state = stateArg
    state = state.pushContext(Context.OPTION).withOption(option)
    val value = TypeConverter.newInstance.convert(option.getTitle, option.getJavaType, parts.get(1))
    state = state.withOption(option).withOptionValue(option, value).popContext
    state
  }

  private def parseClassicGetOpt(tokens: PeekingIterator[String], state: ParseState, allowedOptions: util.List[OptionMetadata]): ParseState = {
    if (!Parser.SHORT_OPTIONS_PATTERN.matcher(tokens.peek).matches) {
      return null
    }
    var remainingToken = tokens.peek.substring(1)
    var nextState = state
    while (!remainingToken.isEmpty) {
      val tokenCharacter = remainingToken.charAt(0)
      val option = findOption(allowedOptions, "-" + tokenCharacter)
      if (option == null) {
        return null
      }
      nextState = nextState.pushContext(Context.OPTION).withOption(option)
      remainingToken = remainingToken.substring(1)
      if (option.getArity == 0) {
        nextState = nextState.withOptionValue(option, java.lang.Boolean.TRUE).popContext
        // continue
      } else {
        if (option.getArity == 1) {
          tokens.next
          if (!remainingToken.isEmpty) {
            val value = TypeConverter.newInstance.convert(option.getTitle, option.getJavaType, remainingToken)
            nextState = nextState.withOptionValue(option, value).popContext
          }
          else if (tokens.hasNext) {
            val value = TypeConverter.newInstance.convert(option.getTitle, option.getJavaType, tokens.next)
            nextState = nextState.withOptionValue(option, value).popContext
          }
          return nextState
        }
        throw new UnsupportedOperationException("Short options style can not be used with option " + option.allowedValues)
      }
    }
    tokens.next
    nextState
  }

  private def parseArgs(stateArg: ParseState, tokens: PeekingIterator[String], arguments: ArgumentsMetadata): ParseState = {
    var state = stateArg
    if (tokens.hasNext) {
      if (tokens.peek == "--") {
        state = state.pushContext(Context.ARGS)
        tokens.next
        while (tokens.hasNext) {
          state = parseArg(state, tokens, arguments)
        }
      } else {
        state = parseArg(state, tokens, arguments)
      }
    }
    state
  }

  private def parseArg(stateArg: ParseState, tokens: PeekingIterator[String], arguments: ArgumentsMetadata): ParseState = {
    var state = stateArg
    if (arguments != null) {
      state = state.withArgument(TypeConverter.newInstance.convert(arguments.getTitle, arguments.getJavaType, tokens.next))
    } else {
      state = state.withUnparsedInput(tokens.next)
    }
    state
  }

  private def findOption(options: util.List[OptionMetadata], param: String): OptionMetadata = {
    import scala.collection.JavaConversions._
    for (optionMetadata <- options) {
      if (optionMetadata.getOptions.contains(param)) {
        return optionMetadata
      }
    }
    null
  }
}