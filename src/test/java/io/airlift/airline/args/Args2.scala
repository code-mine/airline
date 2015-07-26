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

import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.{Arguments, Command, Option}

@Command(name = "Args2") class Args2 {
  @Arguments(description = "List of parameters") var parameters: util.List[String] = newArrayList()
  @Option(name = Array("-log", "-verbose"), description = "Level of verbosity") var verbose: Integer = 1
  @Option(name = Array("-groups"), description = "Comma-separated list of group names to be run") var groups: String = null
  @Option(name = Array("-debug"), description = "Debug mode") var debug: Boolean = false
  @Option(name = Array("-host"), description = "The host") var hosts: util.List[String] = newArrayList()
}