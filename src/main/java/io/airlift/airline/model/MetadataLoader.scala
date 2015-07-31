package io.airlift.airline.model

import java.lang.reflect.Field
import java.util
import javax.inject.Inject

import com.google.common.base.Preconditions
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps.newHashMap
import com.google.common.collect._
import io.airlift.airline._

import scala.collection.JavaConversions._

object MetadataLoader {
  def loadGlobal(name: String, description: String, defaultCommand: CommandMetadata,
                 defaultGroupCommands: java.lang.Iterable[CommandMetadata],
                 groups: java.lang.Iterable[CommandGroupMetadata]): GlobalMetadata = {

    val globalOptionsBuilder: ImmutableList.Builder[OptionMetadata] = ImmutableList.builder()
    if (defaultCommand != null) {
      globalOptionsBuilder.addAll(defaultCommand.getGlobalOptions)
    }
    for (command <- defaultGroupCommands) {
      globalOptionsBuilder.addAll(command.getGlobalOptions)
    }
    for (group <- groups) {
      for (command <- group.getCommands) {
        globalOptionsBuilder.addAll(command.getGlobalOptions)
      }
    }
    val globalOptions = mergeOptionSet(globalOptionsBuilder.build)

    new GlobalMetadata(name, description, globalOptions, defaultCommand, defaultGroupCommands, groups)
  }

  def loadCommandGroup(name: String, description: String, defaultCommand: CommandMetadata,
                       commands: java.lang.Iterable[CommandMetadata]): CommandGroupMetadata = {
    val groupOptionsBuilder: ImmutableList.Builder[OptionMetadata] = ImmutableList.builder()
    if (defaultCommand != null) {
      groupOptionsBuilder.addAll(defaultCommand.getGroupOptions)
    }
    for (command <- commands) {
      groupOptionsBuilder.addAll(command.getGroupOptions)
    }
    val groupOptions = mergeOptionSet(groupOptionsBuilder.build)
    new CommandGroupMetadata(name, description, groupOptions, defaultCommand, commands)
  }

  def loadCommands[T](defaultCommands: java.lang.Iterable[Class[_ <: T]]): util.List[CommandMetadata] = {
    defaultCommands.map(loadCommand).toList
  }

  def loadCommand(commandType: Class[_]): CommandMetadata = {
    var command: Command = null
    var cls: Class[_] = commandType
    while (command == null && !(classOf[Object] == cls)) {
      command = cls.getAnnotation(classOf[Command])
      cls = cls.getSuperclass
    }
    Preconditions.checkArgument(command != null, "Command %s is not annotated with @Command", commandType.getName)

    val name = command.name
    val description = if (command.description.isEmpty) null else command.description
    val hidden = command.hidden
    val injectionMetadata = loadInjectionMetadata(commandType)
    val commandMetadata = new CommandMetadata(
      name, description, hidden,
      injectionMetadata.globalOptions,
      injectionMetadata.groupOptions,
      injectionMetadata.commandOptions,
      Iterables.getFirst(injectionMetadata.arguments, null),
      injectionMetadata.metadataInjections,
      commandType
    )
    commandMetadata
  }

  def loadSuggester(suggesterClass: Class[_ <: Suggester]): SuggesterMetadata = {
    val injectionMetadata: MetadataLoader.InjectionMetadata = loadInjectionMetadata(suggesterClass)
    new SuggesterMetadata(suggesterClass, injectionMetadata.metadataInjections)
  }

  def loadInjectionMetadata(`type`: Class[_]): MetadataLoader.InjectionMetadata = {
    val injectionMetadata = new MetadataLoader.InjectionMetadata
    loadInjectionMetadata(`type`, injectionMetadata, ImmutableList.of[Field])
    injectionMetadata.compact()
    injectionMetadata
  }

  def loadInjectionMetadata(`type`: Class[_], injectionMetadata: MetadataLoader.InjectionMetadata, fields: util.List[Field]) {
    var cls: Class[_] = `type`
    while (cls != classOf[Object]) {
      for (field <- cls.getDeclaredFields) {
        field.setAccessible(true)
        val path = concat(fields, field)
        val injectAnnotation = field.getAnnotation(classOf[Inject])
        if (injectAnnotation != null) {
          if (field.getType == classOf[GlobalMetadata] ||
              field.getType == classOf[CommandGroupMetadata] ||
              field.getType == classOf[CommandMetadata]) {
            injectionMetadata.metadataInjections.add(new Accessor(path))
          } else {
            loadInjectionMetadata(field.getType, injectionMetadata, path)
          }
        }

        val optionAnnotation = field.getAnnotation(classOf[Option])
        if (optionAnnotation != null) {
          val optionType = optionAnnotation.`type`
          var name: String = null
          if (!optionAnnotation.title.isEmpty) {
            name = optionAnnotation.title
          } else {
            name = field.getName
          }
          val options = ImmutableList.copyOf(optionAnnotation.name)
          val description = optionAnnotation.description
          var arity = optionAnnotation.arity
          Preconditions.checkArgument(arity >= 0 || arity == Integer.MIN_VALUE, "Invalid arity for option %s", name)

          if (optionAnnotation.arity >= 0) {
            arity = optionAnnotation.arity
          } else {
            val fieldType: Class[_] = field.getType
            if (classOf[java.lang.Boolean].isAssignableFrom(fieldType) || java.lang.Boolean.TYPE.isAssignableFrom(fieldType)) {
              arity = 0
            } else {
              arity = 1
            }
          }
          val required = optionAnnotation.required
          val hidden = optionAnnotation.hidden
          var allowedValues: util.List[String] = ImmutableList.copyOf(optionAnnotation.allowedValues)
          if (allowedValues.isEmpty) {
            allowedValues = null
          }
          val optionMetadata = new OptionMetadata(optionType, options, name, description, arity, required, hidden, allowedValues, ImmutableSet.of(new Accessor(path)))
          optionType match {
            case OptionType.GLOBAL => injectionMetadata.globalOptions.add(optionMetadata)
            case OptionType.GROUP => injectionMetadata.groupOptions.add(optionMetadata)
            case OptionType.COMMAND => injectionMetadata.commandOptions.add(optionMetadata)
          }
        }

        val argumentsAnnotation = field.getAnnotation(classOf[Arguments])
        if (field.isAnnotationPresent(classOf[Arguments])) {
          var title: String = null
          if (!argumentsAnnotation.title.isEmpty) {
            title = argumentsAnnotation.title
          } else {
            title = field.getName
          }
          val description = argumentsAnnotation.description
          val usage = argumentsAnnotation.usage
          val required = argumentsAnnotation.required
          injectionMetadata.arguments.add(new ArgumentsMetadata(title, description, usage, required, ImmutableSet.of(new Accessor(path))))
        }
      }
      cls = cls.getSuperclass
    }
  }

  private def mergeOptionSet(options: util.List[OptionMetadata]): util.List[OptionMetadata] = {
    val metadataIndex: ListMultimap[OptionMetadata, OptionMetadata] = ArrayListMultimap.create()
    for (option <- options) {
      metadataIndex.put(option, option)
    }
    val optionsList = metadataIndex.asMap.values.map(OptionMetadata.apply)
    val optionIndex: util.Map[String, OptionMetadata] = newHashMap()
    for (option <- optionsList) {
      for (optionName <- option.getOptions) {
        if (optionIndex.containsKey(optionName)) {
          throw new IllegalArgumentException(String.format("Fields %s and %s have conflicting definitions of option %s",
            optionIndex.get(optionName).getAccessors.iterator.next, option.getAccessors.iterator.next, optionName))
        }
        optionIndex.put(optionName, option)
      }
    }
    optionsList.toList
  }

  private def concat[T](iterable: java.lang.Iterable[T], item: T): ImmutableList[T] = {
    ImmutableList.builder[T].addAll(iterable).add(item).build
  }

  class InjectionMetadata {
    var globalOptions: util.List[OptionMetadata] = newArrayList()
    var groupOptions: util.List[OptionMetadata] = newArrayList()
    var commandOptions: util.List[OptionMetadata] = newArrayList()
    var arguments: util.List[ArgumentsMetadata] = newArrayList()
    var metadataInjections: util.List[Accessor] = newArrayList()

    def compact() {
      globalOptions = mergeOptionSet(globalOptions)
      groupOptions = mergeOptionSet(groupOptions)
      commandOptions = mergeOptionSet(commandOptions)
      if (arguments.size > 1) {
        arguments = ImmutableList.of(ArgumentsMetadata(arguments))
      }
    }
  }

}