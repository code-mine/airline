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

class ParseException(cause: Exception, string: String, args: Any*)
  extends RuntimeException(String.format(string, args), cause) {

  def this(string: String, arg1: AnyRef) {
    this(null.asInstanceOf[Exception], String.format(string, Array(arg1): _*))
  }

  def this(string: String, arg1: AnyRef, arg2: AnyRef, arg3: AnyRef) {
    this(null.asInstanceOf[Exception], String.format(string, Array(arg1, arg2, arg3): _*))
  }
}