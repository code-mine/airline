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
package io.airlift.airline.command

import java.util
import java.util.Arrays

import io.airlift.airline.Cli
import io.airlift.airline.TestingUtil.singleCommandParser
import org.testng.Assert.{assertEquals, assertNotNull, assertTrue}
import org.testng.annotations.Test

class CommandTest {
  @Test def namedCommandTest1() {
    val parser = Cli.builder("git")
      .withCommands2(classOf[CommandAdd])
      .withCommands2(classOf[CommandCommit]).build

    val command = parser.parse("add", "-i", "A.java")
    assertNotNull(command, "command is null")
    assertTrue(command.isInstanceOf[CommandAdd])

    val add: CommandAdd = command.asInstanceOf[CommandAdd]
    assertEquals(add.interactive.booleanValue, true)
    assertEquals(add.patterns, util.Arrays.asList("A.java"))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def shouldComplainIfNoAnnotations() {
    singleCommandParser(classOf[String])
  }

  @Test def commandTest2() {
    val parser = Cli.builder("git")
      .withCommands2(classOf[CommandAdd])
      .withCommands2(classOf[CommandCommit]).build

    parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java")

    val command = parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java")
    assertNotNull(command, "command is null")
    assertTrue(command.isInstanceOf[CommandCommit])

    val commit: CommandCommit = command.asInstanceOf[CommandCommit]
    assertTrue(commit.commandMain.verbose)
    assertTrue(commit.amend)
    assertEquals(commit.author, "cbeust")
    assertEquals(commit.files, Arrays.asList("A.java", "B.java"))
  }
}