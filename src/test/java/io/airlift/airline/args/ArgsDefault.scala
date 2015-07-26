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
package io.airlift.airline.args

import java.util

import com.google.common.collect.Lists
import io.airlift.airline.{Arguments, Option}

class ArgsDefault {
  @Arguments var parameters: util.List[String] = Lists.newArrayList()

  @Option(name = Array("-log"), description = "Level of verbosity")
  var log: java.lang.Integer = 1

  @Option(name = Array("-groups"), description = "Comma-separated list of group names to be run")
  var groups: String = null

  @Option(name = Array("-debug"), description = "Debug mode")
  var debug: java.lang.Boolean = false

  @Option(name = Array("-level"), description = "A long number")
  var level: java.lang.Long = 0L
}