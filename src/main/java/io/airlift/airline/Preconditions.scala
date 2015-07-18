package io.airlift.airline

import javax.annotation.Nullable


object Preconditions {
  def checkNotNull[T](reference: T, @Nullable errorMessage: String): T = {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage))
    }
    else {
      reference
    }
  }

  def checkNotNull[T](reference: T, @Nullable errorMessageTemplate: String, @Nullable errorMessageArgs: AnyRef*): T = {
    if (reference == null) {
      throw new NullPointerException(errorMessageTemplate.format(errorMessageArgs))
    }
    else {
      reference
    }
  }

  def checkArgument(expression: Boolean, @Nullable errorMessage: AnyRef) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage))
    }
  }
}