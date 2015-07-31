package io.airlift.airline.model

import java.util

import com.google.common.collect.{ImmutableSet, Iterables}
import com.google.common.collect.Sets.newHashSet
import io.airlift.airline.{Accessor, Preconditions}

import scala.collection.JavaConversions._

object ArgumentsMetadata {
  def apply(arguments: java.lang.Iterable[ArgumentsMetadata]): ArgumentsMetadata = {
    Preconditions.checkNotNull(arguments, "arguments is null")
    Preconditions.checkArgument(!Iterables.isEmpty(arguments), "arguments is empty")

    val first = arguments.iterator.next()
    val accessors: util.Set[Accessor] = newHashSet()
    for (other <- arguments) {
      Preconditions.checkArgument(first == other, s"Conflicting arguments definitions: $first, $other")
      accessors.addAll(other.getAccessors)
    }
    new ArgumentsMetadata(first.getTitle, first.getDescription, first.getUsage, first.isRequired,
      ImmutableSet.copyOf(accessors.asInstanceOf[java.lang.Iterable[Accessor]]))

  }
}

class ArgumentsMetadata(val title: String, val description: String, val usage: String, val required: Boolean,
                        val accessors: java.util.Set[Accessor] = newHashSet()) {
  Preconditions.checkNotNull(title, "title is null")
  Preconditions.checkNotNull(accessors, "accessors is null")
  Preconditions.checkArgument(!Iterables.isEmpty(accessors), "accessors is empty")

  def getTitle: String = {
    title
  }

  def getDescription: String = {
    description
  }

  def getUsage: String = {
    usage
  }

  def isRequired: Boolean = {
    required
  }

  def getAccessors: util.Set[Accessor] = {
    accessors
  }

  def isMultiValued: Boolean = {
    accessors.iterator.next.isMultiValued
  }

  def getJavaType: Class[_] = {
    accessors.iterator.next.getJavaType
  }

  override def equals(o: Any): Boolean = {
    if (this.eq(o.asInstanceOf[AnyRef])) {
      return true
    }
    if (o == null || getClass != o.getClass) {
      return false
    }
    val that: ArgumentsMetadata = o.asInstanceOf[ArgumentsMetadata]
    if (required != that.required) {
      return false
    }
    if (if (description != null) !(description == that.description) else that.description != null) {
      return false
    }
    if (!(title == that.title)) {
      return false
    }
    if (if (usage != null) !(usage == that.usage) else that.usage != null) {
      return false
    }
    true
  }

  override def hashCode: Int = {
    var result: Int = title.hashCode
    result = 31 * result + (if (description != null) description.hashCode else 0)
    result = 31 * result + (if (usage != null) usage.hashCode else 0)
    result = 31 * result + (if (required) 1 else 0)
    result
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("ArgumentsMetadata")
    sb.append("{title='").append(title).append('\'')
    sb.append(", description='").append(description).append('\'')
    sb.append(", usage='").append(usage).append('\'')
    sb.append(", required=").append(required)
    sb.append(", accessors=").append(accessors)
    sb.append('}')
    sb.toString()
  }
}