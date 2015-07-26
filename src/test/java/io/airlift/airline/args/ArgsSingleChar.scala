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
import io.airlift.airline.{Arguments, Command, Option}

@Command(name = "ArgsSingleChar") class ArgsSingleChar {
  @Arguments var parameters: util.List[String] = Lists.newArrayList()
  @Option(name = Array("-l"), description = "Long") var l: Boolean = false
  @Option(name = Array("-g"), description = "Global") var g: Boolean = false
  @Option(name = Array("-d"), description = "Debug mode") var d: Boolean = false
  @Option(name = Array("-s"), description = "A string") var s: String = null
  @Option(name = Array("-p"), description = "A path") var p: String = null
  @Option(name = Array("-n"), description = "No action") var n: Boolean = false
  @Option(name = Array("-2"), description = "Two") var two: Boolean = false
  @Option(name = Array("-f"), description = "A filename") var f: String = null
  @Option(name = Array("-z"), description = "Compress") var z: Boolean = false
  @Option(name = Array("--D"), description = "Directory") var dir: String = null
}