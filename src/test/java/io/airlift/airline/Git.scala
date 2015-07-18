package io.airlift.airline

import java.util.List

import io.airlift.airline.OptionType.GLOBAL

object Git {
  def main(args: String*) {
    val builder = Cli.builder[Runnable]("git")
      .withDescription("the stupid content tracker")
      .withDefaultCommand(classOf[Help])
      .withCommand(classOf[Help]).withCommand(classOf[Git.Add])

    builder.withGroup("remote").withDescription("Manage set of tracked repositories")
      .withDefaultCommand(classOf[Git.RemoteShow])
      .withCommand(classOf[Git.RemoteShow])
      .withCommand(classOf[Git.RemoteAdd])

    val gitParser = builder.build
    gitParser.parse(args: _*).run()
  }

  class GitCommand extends Runnable {
    @Option(`type` = GLOBAL, name = Array("-v"), description = "Verbose mode")
    var verbose = false

    def run() {
      println(getClass.getSimpleName)
    }
  }

  @Command(name = "add", description = "Add file contents to the index") class Add extends GitCommand {
    @Arguments(description = "Patterns of files to be added")
    var patterns: List[String] = _
    @Option(name = Array("-i"), description = "Add modified contents interactively.")
    var interactive = false
  }

  @Command(name = "show", description = "Gives some information about the remote <name>") class RemoteShow extends GitCommand {
    @Option(name = Array("-n"), description = "Do not query remote heads")
    var noQuery = false
    @Arguments(description = "Remote to show")
    var remote: String = null
  }

  @Command(name = "add", description = "Adds a remote") class RemoteAdd extends GitCommand {
    @Option(name = Array("-t"), description = "Track only a specific branch")
    var branch: String = _
    @Arguments(description = "Remote repository to add")
    var remote: List[String] = _
  }

}

