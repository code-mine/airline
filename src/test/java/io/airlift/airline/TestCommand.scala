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

import java.math.BigDecimal
import java.util

import com.google.common.base.Predicates.{compose, equalTo}
import com.google.common.collect.Iterables.find
import io.airlift.airline.TestingUtil.singleCommandParser
import io.airlift.airline.args._
import io.airlift.airline.command.{CommandAdd, CommandCommit}
import io.airlift.airline.model.CommandMetadata
import org.testng.Assert._
import org.testng.annotations.{DataProvider, Test}

import scala.collection.JavaConverters._

object TestCommand {
  @DataProvider def f = {
    Array[Array[Integer]](
      Array[Integer](3, 5, 1),
      Array[Integer](3, 8, 1),
      Array[Integer](3, 12, 2),
      Array[Integer](8, 12, 2),
      Array[Integer](9, 10, 1)
    )
  }

  private val V: util.List[String] = util.Arrays.asList("a", "b", "c", "d")
}

class TestCommand {
  @Test
  @throws(classOf[ParseException])
  def simpleArgs() {
    val args = singleCommandParser(classOf[Args1]).parse("Args1", "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4", "-groups", "unit", "a", "b", "c")
    assertTrue(args.debug)
    assertEquals(args.verbose.intValue, 2)
    assertEquals(args.groups, "unit")
    assertEquals(args.parameters, util.Arrays.asList("a", "b", "c"))
    assertEquals(args.floa, 1.2f, 0.1f)
    assertEquals(args.doub, 1.3f, 0.1f)
    assertEquals(args.bigd, new BigDecimal("1.4"))
  }

  @Test
  @throws(classOf[ParseException])
  def equalsArgs() {
    val args = singleCommandParser(classOf[Args1]).parse("Args1", "-debug", "-log=2", "-float=1.2", "-double=1.3", "-bigdecimal=1.4", "-groups=unit", "a", "b", "c")
    assertTrue(args.debug)
    assertEquals(args.verbose.intValue, 2)
    assertEquals(args.groups, "unit")
    assertEquals(args.parameters, util.Arrays.asList("a", "b", "c"))
    assertEquals(args.floa, 1.2f, 0.1f)
    assertEquals(args.doub, 1.3f, 0.1f)
    assertEquals(args.bigd, new BigDecimal("1.4"))
  }

  @Test
  @throws(classOf[ParseException])
  def classicGetoptArgs() {
    val args = singleCommandParser(classOf[ArgsSingleChar]).parse("ArgsSingleChar", "-lg", "-dsn", "-pa-p", "-2f", "-z", "--Dfoo")
    assertTrue(args.l)
    assertTrue(args.g)
    assertTrue(args.d)
    assertEquals(args.s, "n")
    assertEquals(args.p, "a-p")
    assertFalse(args.n)
    assertTrue(args.two)
    assertEquals(args.f, "-z")
    assertFalse(args.z)
    assertEquals(args.dir, null)
    assertEquals(args.parameters, util.Arrays.asList("--Dfoo"))
  }

  @Test
  @throws(classOf[ParseException])
  def classicGetoptFailure() {
    val args = singleCommandParser(classOf[ArgsSingleChar]).parse("ArgsSingleChar", "-lgX")
    assertFalse(args.l)
    assertFalse(args.g)
    assertEquals(args.parameters, util.Arrays.asList("-lgX"))
  }

  /**
   * Make sure that if there are args with multiple names (e.g. "-log" and "-verbose"),
   * the usage will only display it once.
   */
  @Test def repeatedArgs() {
    val parser = singleCommandParser(classOf[Args1])
    val command = find(parser.getMetadata.getDefaultGroupCommands, compose(equalTo("Args1"), CommandMetadata.nameGetter))
    assertEquals(command.getAllOptions.size, 8)
  }

  /**
   * Getting the description of a nonexistent command should throw an exception.
   */
  @Test(expectedExceptions = Array(classOf[ParseException]), expectedExceptionsMessageRegExp = "No command specified") def nonexistentCommandShouldThrow() {
    singleCommandParser(classOf[Args1]).parse()
  }

  /**
   * Getting the description of a nonexistent command should throw an exception.
   */
  @Test(expectedExceptions = Array(classOf[ParseException]), expectedExceptionsMessageRegExp = "Command 'foo' not recognized") def unrecognizedCommandShouldThrow() {
    singleCommandParser(classOf[Args1]).parse("foo")
  }

  /**
   * Required options with multiple names should work with all names.
   */
  private def multipleNames(option: String) {
    val args = singleCommandParser(classOf[Args1]).parse("Args1", option, "2")
    assertEquals(args.verbose.intValue, 2)
  }

  @Test def multipleNames1() {
    multipleNames("-log")
  }

  @Test def multipleNames2() {
    multipleNames("-verbose")
  }

  @Test def arityString() {
    val args: ArgsArityString = singleCommandParser(classOf[ArgsArityString]).parse("ArgsArityString", "-pairs", "pair0", "pair1", "rest")
    assertEquals(args.pairs.size, 2)
    assertEquals(args.pairs.get(0), "pair0")
    assertEquals(args.pairs.get(1), "pair1")
    assertEquals(args.rest.size, 1)
    assertEquals(args.rest.get(0), "rest")
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def arity2Fail() {
    singleCommandParser(classOf[ArgsArityString]).parse("ArgsArityString", "-pairs", "pair0")
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def multipleUnparsedFail() {
    singleCommandParser(classOf[ArgsMultipleUnparsed]).parse()
  }

  @Test def privateArgs() {
    val args = singleCommandParser(classOf[ArgsPrivate]).parse("ArgsPrivate", "-verbose", "3")
    assertEquals(args.getVerbose.intValue, 3)
  }

  private def argsBoolean1(params: Array[String], expected: Boolean) {
    val values = List("ArgsBooleanArity") ++ params
    val args = singleCommandParser(classOf[ArgsBooleanArity]).parse(values.asJava)
    assertEquals(args.debug, expected)
  }

  private def argsBoolean0(params: Array[String], expected: Boolean) {
    val values = List("ArgsBooleanArity0") ++ params
    val args = singleCommandParser(classOf[ArgsBooleanArity0]).parse(values.asJava)
    assertEquals(args.debug, expected)
  }

  @Test def booleanArity1() {
    argsBoolean1(Array[String](), java.lang.Boolean.FALSE)
    argsBoolean1(Array[String]("-debug", "true"), java.lang.Boolean.TRUE)
  }

  @Test def booleanArity0() {
    argsBoolean0(Array[String](), java.lang.Boolean.FALSE)
    argsBoolean0(Array[String]("-debug"), java.lang.Boolean.TRUE)
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def badParameterShouldThrowParameter1Exception() {
    singleCommandParser(classOf[Args1]).parse("Args1", "-log", "foo")
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def badParameterShouldThrowParameter2Exception() {
    singleCommandParser(classOf[Args1]).parse("Args1", "-long", "foo")
  }

  @Test def listParameters() {
    val a = singleCommandParser(classOf[Args2]).parse("Args2", "-log", "2", "-groups", "unit", "a", "b", "c", "-host", "host2")
    assertEquals(a.verbose.intValue, 2)
    assertEquals(a.groups, "unit")
    assertEquals(a.hosts, util.Arrays.asList("host2"))
    assertEquals(a.parameters, util.Arrays.asList("a", "b", "c"))
  }

  @Test def inheritance() {
    val args = singleCommandParser(classOf[ArgsInherited]).parse("ArgsInherited", "-log", "3", "-child", "2")
    assertEquals(args.child.intValue, 2)
    assertEquals(args.log.intValue, 3)
  }

  @Test def negativeNumber() {
    val a = singleCommandParser(classOf[Args1]).parse("Args1", "-verbose", "-3")
    assertEquals(a.verbose.intValue, -3)
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def requiredMainParameters() {
    singleCommandParser(classOf[ArgsRequired]).parse("ArgsRequired")
  }

  @Test(expectedExceptions = Array(classOf[ParseException]), expectedExceptionsMessageRegExp = ".*option.*missing.*") def requiredOptions() {
    singleCommandParser(classOf[OptionsRequired]).parse("OptionsRequired")
  }

  @Test def ignoresOptionalOptions() {
    singleCommandParser(classOf[OptionsRequired]).parse("OptionsRequired", "--required", "foo")
  }

  private def verifyCommandOrdering(commandNames: Array[String], commands: Class[_]*) {
    var builder = Cli.builder[Any]("foo")
    for (command <- commands) {
      builder = builder.withCommand(command)
    }
    val parser: Cli[_] = builder.build
    val commandParsers = parser.getMetadata.getDefaultGroupCommands
    assertEquals(commandParsers.size, commands.length)

    var i = 0
    for (commandParser <- commandParsers.asScala) {
      assertEquals(commandParser.getName, commandNames(i))
      i += 1
    }
  }

  @Test def commandsShouldBeShownInOrderOfInsertion() {
    verifyCommandOrdering(Array[String]("add", "commit"), classOf[CommandAdd], classOf[CommandCommit])
    verifyCommandOrdering(Array[String]("commit", "add"), classOf[CommandCommit], classOf[CommandAdd])
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def arity1Fail() {
    singleCommandParser(classOf[Arity1]).parse("Arity1", "-inspect")
  }

  @Test def arity1Success1() {
    val arguments: Arity1 = singleCommandParser(classOf[Arity1]).parse("Arity1", "-inspect", "true")
    assertTrue(arguments.inspect)
  }

  @Test def arity1Success2() {
    val arguments: Arity1 = singleCommandParser(classOf[Arity1]).parse("Arity1", "-inspect", "false")
    assertFalse(arguments.inspect)
  }

  @Test(expectedExceptions = Array(classOf[ParseException]), description = "Verify that the main parameter's type is checked to be a List")
  def wrongMainTypeShouldThrow() {
    singleCommandParser(classOf[ArgsRequiredWrongMain]).parse("f1", "f2")
  }

  @Test(description = "This used to run out of memory")
  def oom() {
    singleCommandParser(classOf[ArgsOutOfMemory]).parse("ArgsOutOfMemory")
  }

  @Test def getParametersShouldNotNpe() {
    singleCommandParser(classOf[Args1]).parse("Args1")
  }

  @DataProvider def variable = {
    Array(
      Array(0, TestCommand.V.subList(0, 0), TestCommand.V),
      Array(1, TestCommand.V.subList(0, 1), TestCommand.V.subList(1, 4)),
      Array(2, TestCommand.V.subList(0, 2), TestCommand.V.subList(2, 4)),
      Array(3, TestCommand.V.subList(0, 3), TestCommand.V.subList(3, 4)),
      Array(4, TestCommand.V.subList(0, 4), TestCommand.V.subList(4, 4))
    )
  }

  @Test def enumArgs() {
    val args = singleCommandParser(classOf[ArgsEnum]).parse("ArgsEnum", "-choice", "ONE")
    assertEquals(args.choice, ArgsEnum.ChoiceType.ONE)
  }

  @Test(expectedExceptions = Array(classOf[ParseException])) def enumArgsFail() {
    singleCommandParser(classOf[ArgsEnum]).parse("-choice", "A")
  }

  @SuppressWarnings(Array("UnusedDeclaration"))
  @Test(expectedExceptions = Array(classOf[ParseException])) def shouldThrowIfUnknownOption() {
    @Command(name = "A") class A {
      @Option(name = Array("-long")) var l: Long = 0L
    }
    singleCommandParser(classOf[A]).parse("-lon", "32")
  }
}