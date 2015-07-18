/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.airline

import io.airlift.airline.SingleCommand.singleCommand
import io.airlift.airline.args.{Args1, Args2, ArgsArityString, ArgsBooleanArity, ArgsInherited, ArgsRequired, CommandHidden, OptionsHidden, OptionsRequired}
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

import scala.collection.JavaConversions._

@Test class TestHelp {
  @Test def testGit() {
    val builder: Cli.CliBuilder[Runnable] = Cli.builder[Runnable]("git").withDescription("the stupid content tracker").withDefaultCommand(classOf[Help]).withCommand(classOf[Help]).withCommand(classOf[Git.Add])
    builder.withGroup("remote").withDescription("Manage set of tracked repositories").withDefaultCommand(classOf[Git.RemoteShow]).withCommand(classOf[Git.RemoteShow]).withCommand(classOf[Git.RemoteAdd])
    val gitParser: Cli[Runnable] = builder.build
    var out = new java.lang.StringBuilder
    Help.help(gitParser.getMetadata, List[String](), out)
    assertEquals(out.toString, "usage: git [-v] <command> [<args>]\n" + "\n" + "The most commonly used git commands are:\n" + "    add      Add file contents to the index\n" + "    help     Display help information\n" + "    remote   Manage set of tracked repositories\n" + "\n" + "See 'git help <command>' for more information on a specific command.\n")
    out = new java.lang.StringBuilder
    Help.help(gitParser.getMetadata, List("add"), out)
    assertEquals(out.toString, "NAME\n" + "        git add - Add file contents to the index\n" + "\n" + "SYNOPSIS\n" + "        git [-v] add [-i] [--] [<patterns>...]\n" + "\n" + "OPTIONS\n" + "        -i\n" + "            Add modified contents interactively.\n" + "\n" + "        -v\n" + "            Verbose mode\n" + "\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <patterns>\n" + "            Patterns of files to be added\n" + "\n")
    out = new java.lang.StringBuilder
    Help.help(gitParser.getMetadata, List("remote"), out)
    assertEquals(out.toString, "NAME\n" + "        git remote - Manage set of tracked repositories\n" + "\n" + "SYNOPSIS\n" + "        git [-v] remote\n" + "        git [-v] remote add [-t <branch>]\n" + "        git [-v] remote show [-n]\n" + "\n" + "OPTIONS\n" + "        -v\n" + "            Verbose mode\n" + "\n" + "COMMANDS\n" + "        With no arguments, Gives some information about the remote <name>\n" + "\n" + "        show\n" + "            Gives some information about the remote <name>\n" + "\n" + "            With -n option, Do not query remote heads\n" + "\n" + "        add\n" + "            Adds a remote\n" + "\n" + "            With -t option, Track only a specific branch\n" + "\n")
  }

  @Test def testArgs1() {
    val builder = Cli
      .builder("test")
      .withDescription("Test commandline")
      .withDefaultCommand(classOf[Help])
      .withCommands2(classOf[Help], classOf[Args1])

    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("Args1"), out)
    assertEquals(out.toString,
      "NAME\n"
        + "        test Args1 - args1 description\n"
        + "\n" + "SYNOPSIS\n"
        + "        test Args1 [-bigdecimal <bigd>] [-date <date>] [-debug] [-double <doub>]\n"
        + "                [-float <floa>] [-groups <groups>]\n"
        + "                [(-log <verbose> | -verbose <verbose>)] [-long <l>] [--]\n"
        + "                [<parameters>...]\n" + "\n" + "OPTIONS\n" + "        -bigdecimal <bigd>\n" + "            A BigDecimal number\n" + "\n" + "        -date <date>\n"
        + "            An ISO 8601 formatted date.\n"
        + "\n"
        + "        -debug\n"
        + "            Debug mode\n"
        + "\n"
        + "        -double <doub>\n"
        + "            A double number\n"
        + "\n"
        + "        -float <floa>\n"
        + "            A float number\n"
        + "\n"
        + "        -groups <groups>\n"
        + "            Comma-separated list of group names to be run\n"
        + "\n"
        + "        -log <verbose>, -verbose <verbose>\n"
        + "            Level of verbosity\n"
        + "\n"
        + "        -long <l>\n"
        + "            A long number\n"
        + "\n"
        + "        --\n"
        + "            This option can be used to separate command-line options from the\n"
        + "            list of argument, (useful when arguments might be mistaken for\n"
        + "            command-line options\n"
        + "\n"
        + "        <parameters>\n"
        + "\n"
        + "\n")
  }

  @Test def testArgs2() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[Args2])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("Args2"), out)
    assertEquals(out.toString, "NAME\n" + "        test Args2 -\n" + "\n" + "SYNOPSIS\n" + "        test Args2 [-debug] [-groups <groups>] [-host <hosts>...]\n" + "                [(-log <verbose> | -verbose <verbose>)] [--] [<parameters>...]\n" + "\n" + "OPTIONS\n" + "        -debug\n" + "            Debug mode\n" + "\n" + "        -groups <groups>\n" + "            Comma-separated list of group names to be run\n" + "\n" + "        -host <hosts>\n" + "            The host\n" + "\n" + "        -log <verbose>, -verbose <verbose>\n" + "            Level of verbosity\n" + "\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <parameters>\n" + "            List of parameters\n" + "\n")
  }

  @Test def testArgsAritySting() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[ArgsArityString])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("ArgsArityString"), out)
    assertEquals(out.toString, "NAME\n" + "        test ArgsArityString -\n" + "\n" + "SYNOPSIS\n" + "        test ArgsArityString [-pairs <pairs>...] [--] [<rest>...]\n" + "\n" + "OPTIONS\n" + "        -pairs <pairs>\n" + "            Pairs\n" + "\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <rest>\n" + "            Rest\n" + "\n")
  }

  @Test def testArgsBooleanArity() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[ArgsBooleanArity])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("ArgsBooleanArity"), out)
    assertEquals(out.toString, "NAME\n" + "        test ArgsBooleanArity -\n" + "\n" + "SYNOPSIS\n" + "        test ArgsBooleanArity [-debug <debug>]\n" + "\n" + "OPTIONS\n" + "        -debug <debug>\n" + "\n" + "\n")
  }

  @Test def testArgsInherited() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[ArgsInherited])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("ArgsInherited"), out)
    assertEquals(out.toString, "NAME\n" + "        test ArgsInherited -\n" + "\n" + "SYNOPSIS\n" + "        test ArgsInherited [-child <child>] [-debug] [-groups <groups>]\n" + "                [-level <level>] [-log <log>] [--] [<parameters>...]\n" + "\n" + "OPTIONS\n" + "        -child <child>\n" + "            Child parameter\n" + "\n" + "        -debug\n" + "            Debug mode\n" + "\n" + "        -groups <groups>\n" + "            Comma-separated list of group names to be run\n" + "\n" + "        -level <level>\n" + "            A long number\n" + "\n" + "        -log <log>\n" + "            Level of verbosity\n" + "\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <parameters>\n" + "\n" + "\n")
  }

  @Test def testArgsRequired() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[ArgsRequired])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("ArgsRequired"), out)
    assertEquals(out.toString, "NAME\n" + "        test ArgsRequired -\n" + "\n" + "SYNOPSIS\n" + "        test ArgsRequired [--] <parameters>...\n" + "\n" + "OPTIONS\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <parameters>\n" + "            List of files\n" + "\n")
  }

  @Test def testOptionsRequired() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[OptionsRequired])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("OptionsRequired"), out)
    assertEquals(out.toString, "NAME\n" + "        test OptionsRequired -\n" + "\n" + "SYNOPSIS\n" + "        test OptionsRequired [--optional <optionalOption>]\n" + "                --required <requiredOption>\n" + "\n" + "OPTIONS\n" + "        --optional <optionalOption>\n" + "\n" + "\n" + "        --required <requiredOption>\n" + "\n" + "\n")
  }

  @Test def testOptionsHidden() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[OptionsHidden])
    val parser = builder.build
    val out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("OptionsHidden"), out)
    assertEquals(out.toString, "NAME\n" + "        test OptionsHidden -\n" + "\n" + "SYNOPSIS\n" + "        test OptionsHidden [--optional <optionalOption>]\n" + "\n" + "OPTIONS\n" + "        --optional <optionalOption>\n" + "\n" + "\n")
  }

  @Test def testCommandHidden() {
    val builder = Cli.builder("test").withDescription("Test commandline").withDefaultCommand(classOf[Help]).withCommands2(classOf[Help], classOf[ArgsRequired], classOf[CommandHidden])
    val parser = builder.build
    var out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List[String](), out)
    assertEquals(out.toString, "usage: test <command> [<args>]\n" + "\n" + "The most commonly used test commands are:\n" + "    ArgsRequired\n" + "    help           Display help information\n" + "\n" + "See 'test help <command>' for more information on a specific command.\n")
    out = new java.lang.StringBuilder
    Help.help(parser.getMetadata, List("CommandHidden"), out)
    assertEquals(out.toString, "NAME\n" + "        test CommandHidden -\n" + "\n" + "SYNOPSIS\n" + "        test CommandHidden [--optional <optionalOption>]\n" + "\n" + "OPTIONS\n" + "        --optional <optionalOption>\n" + "\n" + "\n")
  }

  @Test def testSingleCommandArgs1() {
    val command: SingleCommand[Args1] = singleCommand(classOf[Args1])
    val out = new java.lang.StringBuilder
    new CommandUsage().usage(null, null, "test", command.getCommandMetadata, out)
    assertEquals(out.toString, "NAME\n" + "        test - args1 description\n" + "\n" + "SYNOPSIS\n" + "        test [-bigdecimal <bigd>] [-date <date>] [-debug] [-double <doub>]\n" + "                [-float <floa>] [-groups <groups>]\n" + "                [(-log <verbose> | -verbose <verbose>)] [-long <l>] [--]\n" + "                [<parameters>...]\n" + "\n" + "OPTIONS\n" + "        -bigdecimal <bigd>\n" + "            A BigDecimal number\n" + "\n" + "        -date <date>\n" + "            An ISO 8601 formatted date.\n" + "\n" + "        -debug\n" + "            Debug mode\n" + "\n" + "        -double <doub>\n" + "            A double number\n" + "\n" + "        -float <floa>\n" + "            A float number\n" + "\n" + "        -groups <groups>\n" + "            Comma-separated list of group names to be run\n" + "\n" + "        -log <verbose>, -verbose <verbose>\n" + "            Level of verbosity\n" + "\n" + "        -long <l>\n" + "            A long number\n" + "\n" + "        --\n" + "            This option can be used to separate command-line options from the\n" + "            list of argument, (useful when arguments might be mistaken for\n" + "            command-line options\n" + "\n" + "        <parameters>\n" + "\n" + "\n")
  }
}
