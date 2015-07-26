package io.airlift.airline

import java.lang.reflect.{Constructor, Method}

object TypeConverter {
  def newInstance: TypeConverter = new TypeConverter
}

class TypeConverter {
  def convert(name: String, `type`: Class[_], value: String): Any = {
    Preconditions.checkNotNull(name, "name is null")
    Preconditions.checkNotNull(`type`, "type is null")
    Preconditions.checkNotNull(value, "value is null")
    try {
      if (classOf[String].isAssignableFrom(`type`)) {
        return value
      }
      else if (classOf[Boolean].isAssignableFrom(`type`) || java.lang.Boolean.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Boolean.valueOf(value)
      }
      else if (classOf[Byte].isAssignableFrom(`type`) || java.lang.Byte.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Byte.valueOf(value)
      }
      else if (classOf[Short].isAssignableFrom(`type`) || java.lang.Short.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Short.valueOf(value)
      }
      else if (classOf[Integer].isAssignableFrom(`type`) || Integer.TYPE.isAssignableFrom(`type`)) {
        return Integer.valueOf(value)
      }
      else if (classOf[Long].isAssignableFrom(`type`) || java.lang.Long.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Long.valueOf(value)
      }
      else if (classOf[Float].isAssignableFrom(`type`) || java.lang.Float.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Float.valueOf(value)
      }
      else if (classOf[Double].isAssignableFrom(`type`) || java.lang.Double.TYPE.isAssignableFrom(`type`)) {
        return java.lang.Double.valueOf(value)
      }
    } catch {
      case ignored: Exception =>
    }

    try {
      val valueOf: Method = `type`.getMethod("fromString", classOf[String])
      if (valueOf.getReturnType.isAssignableFrom(`type`)) {
        return valueOf.invoke(null, value)
      }
    } catch {
      case ignored: Throwable =>
    }
    try {
      val valueOf: Method = `type`.getMethod("valueOf", classOf[String])
      if (valueOf.getReturnType.isAssignableFrom(`type`)) {
        return valueOf.invoke(null, value)
      }
    } catch {
      case ignored: Throwable =>
    }
    try {
      val constructor: Constructor[_] = `type`.getConstructor(classOf[String])
      return constructor.newInstance(value)
    } catch {
      case ignored: Throwable =>
    }
    throw new ParseOptionConversionException(name, value, `type`.getSimpleName)
  }
}