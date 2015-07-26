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

import java.math.BigDecimal
import java.util
import java.util.Date

import com.google.common.collect.Lists
import io.airlift.airline.{Arguments, Command, Option}

@Command(name = "Args1", description = "args1 description") class Args1 {
  @Arguments var parameters: util.List[String] = Lists.newArrayList()
  @Option(name = Array("-log", "-verbose"), description = "Level of verbosity") var verbose: Integer = 1
  @Option(name = Array("-groups"), description = "Comma-separated list of group names to be run") var groups: String = null
  @Option(name = Array("-debug"), description = "Debug mode") var debug: Boolean = false
  @Option(name = Array("-long"), description = "A long number") var l: Long = 0L
  @Option(name = Array("-double"), description = "A double number") var doub: Double = .0
  @Option(name = Array("-float"), description = "A float number") var floa: Float = 0.0f
  @Option(name = Array("-bigdecimal"), description = "A BigDecimal number") var bigd: BigDecimal = null
  @Option(name = Array("-date"), description = "An ISO 8601 formatted date.") var date: Date = null
}