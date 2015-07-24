package io.airlift.airline.model

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables
import io.airlift.airline.{Preconditions, Accessor, OptionType}
import java.lang.reflect.Field
import java.util.Set
import com.google.common.collect.Sets.newHashSet
import scala.collection.JavaConversions._

object OptionMetadata {

  def apply(options: java.lang.Iterable[OptionMetadata]): OptionMetadata = {

    Preconditions.checkNotNull(options, "options is null")
    Preconditions.checkArgument(!Iterables.isEmpty(options), "options is empty")

    val option = options.iterator.next()
    var allowedValues: java.util.Set[String] = null
    if (option.allowedValues != null) {
      allowedValues = ImmutableSet.copyOf(option.allowedValues)
    } else {
      allowedValues = null
    }
    val accessors: java.util.Set[Accessor] = newHashSet()
    for (other <- options) {
      Preconditions.checkArgument(option == other, s"Conflicting options definitions: ${option}, ${other}")
      accessors.addAll(other.getAccessors)
    }
    new OptionMetadata(
      option.optionType,
      option.options,
      option.title,
      option.description,
      option.arity,
      option.required,
      option.hidden,
      allowedValues,
      accessors
    )
  }

}

class OptionMetadata(val optionType: OptionType, val options: java.lang.Iterable[String], val title: String, val description: String,
                     val arity: Int, val required: Boolean, val hidden: Boolean, val allowedValues: java.lang.Iterable[String],
                     accessors: java.util.Set[Accessor] = newHashSet()) {

  Preconditions.checkNotNull(optionType, "optionType is null")
  Preconditions.checkNotNull(options, "options is null")
  Preconditions.checkArgument(!Iterables.isEmpty(options), "options is empty")
  Preconditions.checkNotNull(title, "title is null")
  Preconditions.checkNotNull(accessors, "accessors is null")
  Preconditions.checkArgument(!Iterables.isEmpty(accessors), "accessors is empty");

  val optionsSet: java.util.Set[String] = newHashSet(options)

  def getOptionType: OptionType = {
    optionType
  }

  def getOptions: java.util.Set[String] = {
    optionsSet
  }

  def getTitle: String = {
    title
  }

  def getDescription: String = {
    description
  }

  def getArity: Int = {
    arity
  }

  def isRequired: Boolean = {
    required
  }

  def isHidden: Boolean = {
    hidden
  }

  def isMultiValued: Boolean = {
    accessors.iterator.next.isMultiValued
  }

  def getJavaType: Class[_] = {
    accessors.iterator.next.getJavaType
  }

  def getAccessors: Set[Accessor] = {
    accessors
  }

  override def equals(o: Any): Boolean = {
    if (this.eq(o.asInstanceOf[AnyRef])) {
      return true
    }
    if (o == null || getClass != o.getClass) {
      return false
    }
    val that = o.asInstanceOf[OptionMetadata]
    if (arity != that.arity) {
      return false
    }
    if (hidden != that.hidden) {
      return false
    }
    if (required != that.required) {
      return false
    }
    if (if (allowedValues != null) !(allowedValues == that.allowedValues) else that.allowedValues != null) {
      return false
    }
    if (if (description != null) !(description == that.description) else that.description != null) {
      return false
    }
    if (optionType ne that.optionType) {
      return false
    }
    if (!(options == that.options)) {
      return false
    }
    if (!(title == that.title)) {
      return false
    }
    true
  }

  override def hashCode: Int = {
    var result = optionType.hashCode
    result = 31 * result + options.hashCode
    result = 31 * result + title.hashCode
    result = 31 * result + (if (description != null) description.hashCode else 0)
    result = 31 * result + arity
    result = 31 * result + (if (required) 1 else 0)
    result = 31 * result + (if (hidden) 1 else 0)
    result = 31 * result + (if (allowedValues != null) allowedValues.hashCode else 0)
    result
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("OptionMetadata")
    sb.append("{optionType=").append(optionType)
    sb.append(", options=").append(options)
    sb.append(", title='").append(title).append('\'')
    sb.append(", description='").append(description).append('\'')
    sb.append(", arity=").append(arity)
    sb.append(", required=").append(required)
    sb.append(", hidden=").append(hidden)
    sb.append(", accessors=").append(accessors)
    sb.append('}')
    sb.toString()
  }
}