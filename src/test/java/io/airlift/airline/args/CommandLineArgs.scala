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

import java.util.List

import io.airlift.airline.{Arguments, Command, Option}

@Command(name = "CommandLineArgs") class CommandLineArgs {
  @Arguments(description = "The XML suite files to run") var suiteFiles: List[String] = com.google.common.collect.Lists.newArrayList()
  @Option(name = Array("-log", "-verbose"), description = "Level of verbosity") var verbose: Integer = null
  @Option(name = Array("-groups"), description = "Comma-separated list of group names to be run") var groups: String = null
  @Option(name = Array("-excludedgroups"), description = "Comma-separated list of group names to be " + "run") var excludedGroups: String = null
  @Option(name = Array("-d"), description = "Output directory") var outputDirectory: String = null
  @Option(name = Array("-junit"), description = "JUnit mode") var junit: Boolean = false
  @Option(name = Array("-listener"), description = "List of .class files or list of class names" + " implementing ITestListener or ISuiteListener") var listener: String = null
  @Option(name = Array("-methodselectors"), description = "List of .class files or list of class " + "names implementing IMethodSelector") var methodSelectors: String = null
  @Option(name = Array("-objectfactory"), description = "List of .class files or list of class " + "names implementing ITestRunnerFactory") var objectFactory: String = null
  @Option(name = Array("-parallel"), description = "Parallel mode (methods, tests or classes)") var parallelMode: String = null
  @Option(name = Array("-configfailurepolicy"), description = "Configuration failure policy (skip or continue)") var configFailurePolicy: String = null
  @Option(name = Array("-threadcount"), description = "Number of threads to use when running tests " + "in parallel") var threadCount: Integer = null
  @Option(name = Array("-dataproviderthreadcount"), description = "Number of threads to use when " + "running data providers") var dataProviderThreadCount: Integer = null
  @Option(name = Array("-suitename"), description = "Default name of test suite, if not specified " + "in suite definition file or source code") var suiteName: String = null
  @Option(name = Array("-testname"), description = "Default name of test, if not specified in suite" + "definition file or source code") var testName: String = null
  @Option(name = Array("-reporter"), description = "Extended configuration for custom report listener") var reporter: String = null
  /**
   * Used as map key for the complete list of report listeners provided with the above argument
   */
  @Option(name = Array("-reporterslist")) var reportersList: String = null
  @Option(name = Array("-usedefaultlisteners"), description = "Whether to use the default listeners") var useDefaultListeners: String = "true"
  @Option(name = Array("-skipfailedinvocationcounts")) var skipFailedInvocationCounts: Boolean = false
  @Option(name = Array("-testclass"), description = "The list of test classes") var testClass: String = null
  @Option(name = Array("-testnames"), description = "The list of test names to run") var testNames: String = null
  @Option(name = Array("-testjar"), description = "") var testJar: String = null
  @Option(name = Array("-testRunFactory"), description = "") var testRunFactory: String = null
  @Option(name = Array("-port"), description = "The port") var port: Integer = null
  @Option(name = Array("-host"), description = "The host") var host: String = null
  @Option(name = Array("-master"), description = "Host where the master is") var master: String = null
  @Option(name = Array("-slave"), description = "Host where the slave is") var slave: String = null
}