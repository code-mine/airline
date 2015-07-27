package io.airlift.airline

import java.lang.reflect.{TypeVariable, ParameterizedType, Type, Field, GenericDeclaration}
import java.util

import com.google.common.collect.Iterables

import scala.collection.JavaConversions._

object Accessor {
  private def newCollection(aClass: Class[_]): util.Collection[Any] = {
    if (classOf[util.Collection[Any]] == aClass || classOf[util.List[Any]] == aClass) {
      return new util.ArrayList[Any]
    }
    if (classOf[util.Set[Any]] == aClass) {
      return new util.HashSet[Any]
    }
    if (classOf[util.SortedSet[Any]] == aClass) {
      return new util.TreeSet()
    }

    try {
      aClass.getConstructor().newInstance().asInstanceOf[util.Collection[Any]]
    } catch {
      case ignored: Exception =>
    }
    throw new ParseException("Parameters of Collection type '%s' are not supported. Please use List or Set instead.", aClass.getSimpleName)
  }

  private def getOrCreateCollectionField(name: String, o: Any, field: Field): util.Collection[Any] = {
    var collection: util.Collection[Any] = null
    try {
      collection = field.get(o).asInstanceOf[util.Collection[Any]]
    } catch {
      case e: Exception =>
        throw new ParseException(e, s"Error getting collection field '${field.getName}' for argument '$name'")
    }

    if (collection == null) {
      try {
        collection = newCollection(field.getType)
        field.set(o, collection)
      } catch {
        case e: Exception =>
          throw new ParseException(e, s"Error setting collection field '${field.getName}' for argument '$name'")
      }
    }
    collection
  }

  private def getItemType(name: String, `type`: Type): Class[_] = {
    val rawClass: Class[_] = getRawType(`type`)
    if (rawClass == null) {
      throw new ParseException("Type of option %s be an exact type", name)
    }
    if (!classOf[util.Collection[_]].isAssignableFrom(rawClass)) {
      return rawClass
    }
    val types: Array[Type] = getTypeParameters(classOf[util.Collection[_]], `type`)
    if ((types == null) || (types.length != 1)) {
      throw new ParseException("Unable to get item type of Collection option %s", name)
    }
    val itemType: Type = types(0)
    if (!itemType.isInstanceOf[Class[_]]) {
      throw new ParseException("Collection type option %s must be an exact type", name)
    }
    itemType.asInstanceOf[Class[_]]
  }

  private def getRawType(`type`: Type): Class[_] = {
    if (`type`.isInstanceOf[Class[_]]) {
      return `type`.asInstanceOf[Class[_]]
    }
    if (`type`.isInstanceOf[ParameterizedType]) {
      val parameterizedType = `type`.asInstanceOf[ParameterizedType]
      return getRawType(parameterizedType.getRawType)
    }
    null
  }

  private def getTypeParameters(desiredType: Class[_], `type`: Type): Array[Type] = {
    if (`type`.isInstanceOf[Class[_]]) {
      val rawClass: Class[_] = `type`.asInstanceOf[Class[_]]
      if (desiredType == `type`) {
        return null
      }
      for (iface <- rawClass.getGenericInterfaces) {
        val collectionType = getTypeParameters(desiredType, iface)
        if (collectionType != null) {
          return collectionType
        }
      }
      return getTypeParameters(desiredType, rawClass.getGenericSuperclass)
    }

    if (`type`.isInstanceOf[ParameterizedType]) {
      val parameterizedType = `type`.asInstanceOf[ParameterizedType]
      val rawType = parameterizedType.getRawType
      if (desiredType == rawType) {
        return parameterizedType.getActualTypeArguments
      }
      val collectionTypes = getTypeParameters(desiredType, rawType).toBuffer
      if (collectionTypes != null) {
        var i = 0
        while (i < collectionTypes.length) {
          if (collectionTypes(i).isInstanceOf[TypeVariable[_ <: GenericDeclaration]]) {
            val typeVariable = collectionTypes(i).asInstanceOf[TypeVariable[_]]
            val rawTypeParams = rawType.asInstanceOf[Class[_]].getTypeParameters
            var j = 0
            while (j < rawTypeParams.length) {
              if (typeVariable.getName == rawTypeParams(j).getName) {
                collectionTypes.set(i, parameterizedType.getActualTypeArguments.toBuffer(j))
              }
              j += 1
            }
          }
          i += 1
        }
      }
      return collectionTypes.toArray
    }
    null
  }
}

class Accessor(pathIterable: java.lang.Iterable[Field]) {
  val path = pathIterable.toList
  Preconditions.checkNotNull(path, "path is null")
  Preconditions.checkArgument(!Iterables.isEmpty(path), "path is empty")

  private val name: String = path.head.getDeclaringClass.getSimpleName + "." + path.map(_.getName).mkString(".")
  private val javaType: Class[_] = Accessor.getItemType(name, path.last.getGenericType)
  private val multiValued: Boolean = classOf[util.Collection[_]].isAssignableFrom(path.last.getType)

  def getName: String = {
    name
  }

  def getJavaType: Class[_] = {
    javaType
  }

  def isMultiValued: Boolean = {
    multiValued
  }

  def getValue(instanceRef: Any): Any = {
    var instance = instanceRef

    val pathName = new StringBuilder
    for (intermediateField <- path.subList(0, path.size - 1)) {
      if (pathName.nonEmpty) {
        pathName.append(".")
      }
      pathName.append(intermediateField.getName)

      try {
        var nextInstance: Any = intermediateField.get(instance)
        if (nextInstance == null) {
          nextInstance = ParserUtil.createInstance(intermediateField.getType.asInstanceOf[Class[Any]])
          intermediateField.set(instance, nextInstance)
        }
        instance = nextInstance
      } catch {
        case e: Exception =>
          throw new ParseException(e, "Error getting value of %s", pathName)
      }
    }
    instance
  }

  def addValues(commandInstance: AnyRef, values: java.lang.Iterable[_]) {
    if (Iterables.isEmpty(values)) return

    val instance: Any = getValue(commandInstance)
    val field: Field = path.get(path.size - 1)
    field.setAccessible(true)

    if (classOf[util.Collection[_]].isAssignableFrom(field.getType)) {
      val collection = Accessor.getOrCreateCollectionField(name, instance, field)
      Iterables.addAll(collection, values)
    }
    else {
      try {
        field.set(instance, Iterables.getLast(values))
      } catch {
        case e: Exception =>
          throw new ParseException(e, "Error setting %s for argument %s", field.getName, name)
      }
    }
  }

  override def equals(o: Any): Boolean = {
    if (this.eq(o.asInstanceOf[AnyRef])) return true
    if (o == null || getClass != o.getClass) {
      return false
    }
    val accessor = o.asInstanceOf[Accessor]
    if (path != accessor.path) {
      return false
    }
    true
  }

  override def hashCode: Int = {
    path.hashCode()
  }

  override def toString: String = {
    name
  }
}