package io.airlift.airline

import java.util

import com.google.common.collect.{ImmutableList, ListMultimap}
import io.airlift.airline.model.{ArgumentsMetadata, OptionMetadata}
import scala.collection.JavaConversions._

object ParserUtil {
  def createInstance[T >: Null](`type`: Class[T]): T = {
    if (`type` == null) return null
    try {
      `type`.getConstructor().newInstance()
    }
    catch {
      case e: Exception =>
        throw new ParseException(e, "Unable to create instance %s", Array(`type`.getName): _*)
    }
  }

  def createInstance[T](`type`: Class[_ >: Null], options: java.lang.Iterable[OptionMetadata],
                        parsedOptions: ListMultimap[OptionMetadata, Any],
                        arguments: ArgumentsMetadata, parsedArguments: java.lang.Iterable[Any],
                        metadataInjection: java.lang.Iterable[Accessor],
                        bindings: util.Map[Class[_], AnyRef]): T = {
    val commandInstance: T = createInstance(`type`).asInstanceOf[T]
    for (option <- options) {
      var values: util.List[_] = parsedOptions.get(option)
      if (option.getArity > 1 && !values.isEmpty) {
        values = values.asInstanceOf[java.lang.Iterable[java.lang.Iterable[AnyRef]]].flatten.toList
      }
      if (values != null && !values.isEmpty) {
        for (accessor <- option.getAccessors) {
          accessor.addValues(commandInstance, values)
        }
      }
    }
    if (arguments != null && parsedArguments != null) {
      for (accessor <- arguments.getAccessors) {
        accessor.addValues(commandInstance, parsedArguments)
      }
    }
    for (accessor <- metadataInjection) {
      val injectee: AnyRef = bindings.get(accessor.getJavaType)
      if (injectee != null) {
        accessor.addValues(commandInstance, ImmutableList.of(injectee))
      }
    }
    commandInstance
  }
}

