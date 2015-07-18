package io.airlift.airline

import java.util.List
import javax.inject.Inject

import com.google.common.base.MoreObjects.{firstNonNull, toStringHelper}
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.OptionType.GLOBAL
import org.testng.annotations.Test

object TestGalaxyCommandLineParser {

  class GlobalOptions {
    @Option(`type` = GLOBAL, name = Array("--debug"), description = "Enable debug messages") var debug: Boolean = false
    @Option(`type` = GLOBAL, name = Array("--coordinator"), description = "Galaxy coordinator host (overrides GALAXY_COORDINATOR)") var coordinator: String = firstNonNull(System.getenv("GALAXY_COORDINATOR"), "http://localhost:64000")

    override def toString: String = {
      toStringHelper(this).add("debug", debug).add("coordinator", coordinator).toString
    }
  }

  class SlotFilter {
    @Option(name = Array("-b", "--binary"), description = "Select slots with a given binary") var binary: List[String] = null
    @Option(name = Array("-c", "--config"), description = "Select slots with a given configuration") var config: List[String] = null
    @Option(name = Array("-i", "--host"), description = "Select slots on the given host") var host: List[String] = null
    @Option(name = Array("-I", "--ip"), description = "Select slots at the given IP address") var ip: List[String] = null
    @Option(name = Array("-u", "--uuid"), description = "Select slot with the given UUID") var uuid: List[String] = null
    @Option(name = Array("-s", "--state"), description = "Select 'r{unning}', 's{topped}' or 'unknown' slots") var state: List[String] = null

    override def toString: String = {
      toStringHelper(this).add("binary", binary).add("config", config).add("host", host).add("ip", ip).add("uuid", uuid).add("state", state).toString
    }
  }

  class AgentFilter {
    @Option(name = Array("-i", "--host"), description = "Select slots on the given host") final val host: List[String] = newArrayList()
    @Option(name = Array("-I", "--ip"), description = "Select slots at the given IP address") final val ip: List[String] = newArrayList()
    @Option(name = Array("-u", "--uuid"), description = "Select slot with the given UUID") final val uuid: List[String] = newArrayList()
    @Option(name = Array("-s", "--state"), description = "Select 'r{unning}', 's{topped}' or 'unknown' slots") final val state: List[String] = newArrayList()

    override def toString: String = {
      toStringHelper(this).add("host", host).add("ip", ip).add("uuid", uuid).add("state", state).toString
    }
  }

  abstract class GalaxyCommand {
    @Inject var globalOptions: TestGalaxyCommandLineParser.GlobalOptions = new TestGalaxyCommandLineParser.GlobalOptions

    def execute {
      System.out.println(this)
    }
  }

  @Command(name = "help", description = "Display help information about galaxy") class HelpCommand extends GalaxyCommand {
    @Inject var help: Help = null

    override def execute {
      help.call
    }
  }

  @Command(name = "show", description = "Show state of all slots") class ShowCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "install", description = "Install software in a new slot") class InstallCommand extends GalaxyCommand {
    @Option(name = Array("--count"), description = "Number of instances to install") var count: Int = 1
    @Inject final val agentFilter: TestGalaxyCommandLineParser.AgentFilter = new TestGalaxyCommandLineParser.AgentFilter
    @Arguments(usage = "<groupId:artifactId[:packaging[:classifier]]:version> @<component:pools:version>", description = "The binary and @configuration to install.  The default packaging is tar.gz") final val assignment: List[String] = Lists.newArrayList()

    override def toString: String = {
      toStringHelper(this).add("count", count).add("agentFilter", agentFilter).add("assignment", assignment).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "upgrade", description = "Upgrade software in a slot") class UpgradeCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter
    @Arguments(usage = "[<binary-version>] [@<config-version>]", description = "Version of the binary and/or @configuration") final val versions: List[String] = Lists.newArrayList()

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("versions", versions).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "terminate", description = "Terminate (remove) a slot") class TerminateCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "start", description = "Start a server") class StartCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "stop", description = "Stop a server") class StopCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "restart", description = "Restart server") class RestartCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "reset-to-actual", description = "Reset slot expected state to actual") class ResetToActualCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "ssh", description = "ssh to slot installation") class SshCommand extends GalaxyCommand {
    @Inject final val slotFilter: TestGalaxyCommandLineParser.SlotFilter = new TestGalaxyCommandLineParser.SlotFilter
    @Arguments(description = "Command to execute on the remote host") var command: String = null

    override def toString: String = {
      toStringHelper(this).add("slotFilter", slotFilter).add("command", command).toString
    }
  }

  @Command(name = "add", description = "Provision a new agent") class AgentAddCommand extends GalaxyCommand {
    @Option(name = Array("--count"), description = "Number of agents to provision") var count: Int = 1
    @Option(name = Array("--availability-zone"), description = "Availability zone to provision") var availabilityZone: String = null
    @Arguments(usage = "[<instance-type>]", description = "Instance type to provision") var instanceType: String = null

    override def toString: String = {
      toStringHelper(this).add("count", count).add("availabilityZone", availabilityZone).add("instanceType", instanceType).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "show", description = "Show agent details") class AgentShowCommand extends GalaxyCommand {
    @Inject final val agentFilter: TestGalaxyCommandLineParser.AgentFilter = new TestGalaxyCommandLineParser.AgentFilter

    override def toString: String = {
      toStringHelper(this).add("agentFilter", agentFilter).add("globalOptions", globalOptions).toString
    }
  }

  @Command(name = "terminate", description = "Provision a new agent") class AgentTerminateCommand extends GalaxyCommand {
    @Arguments(title = "agent-id", description = "Agent to terminate", required = true) var agentId: String = null

    override def toString: String = {
      toStringHelper(this).add("agentId", agentId).add("globalOptions", globalOptions).toString
    }
  }

}

class TestGalaxyCommandLineParser {
  @Test def test {
    parse()
    parse("help")
    parse("help", "galaxy")
    parse("help", "show")
    parse("help", "install")
    parse("help", "upgrade")
    parse("help", "upgrade")
    parse("help", "terminate")
    parse("help", "start")
    parse("help", "stop")
    parse("help", "restart")
    parse("help", "reset-to-actual")
    parse("help", "ssh")
    parse("help", "agent")
    parse("help", "agent", "show")
    parse("help", "agent", "add")
    parse("--debug", "show", "-u", "b2", "--state", "r")
    parse("--debug", "install", "com.proofpoint.discovery:discovery-server:1.1", "@discovery:general:1.0")
    parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0")
    parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0", "-s", "r")
    parse("--debug", "terminate", "-u", "b2")
    parse("--debug", "start", "-u", "b2")
    parse("--debug", "stop", "-u", "b2")
    parse("--debug", "restart", "-u", "b2")
    parse("--debug", "reset-to-actual", "-u", "b2")
    parse("--debug", "ssh")
    parse("--debug", "ssh", "-u", "b2", "--state", "r", "tail -F var/log/launcher.log")
    parse("--debug", "agent")
    parse("--debug", "agent", "show")
    parse("--debug", "agent", "add", "--count", "4", "t1.micro")
  }

  private def createParser: Cli[TestGalaxyCommandLineParser.GalaxyCommand] = {
    val builder: Cli.CliBuilder[TestGalaxyCommandLineParser.GalaxyCommand] = Cli.builder[TestGalaxyCommandLineParser.GalaxyCommand]("galaxy").withDescription("cloud management system").withDefaultCommand(classOf[TestGalaxyCommandLineParser.HelpCommand]).withCommand(classOf[TestGalaxyCommandLineParser.HelpCommand]).withCommand(classOf[TestGalaxyCommandLineParser.ShowCommand]).withCommand(classOf[TestGalaxyCommandLineParser.InstallCommand]).withCommand(classOf[TestGalaxyCommandLineParser.UpgradeCommand]).withCommand(classOf[TestGalaxyCommandLineParser.TerminateCommand]).withCommand(classOf[TestGalaxyCommandLineParser.StartCommand]).withCommand(classOf[TestGalaxyCommandLineParser.StopCommand]).withCommand(classOf[TestGalaxyCommandLineParser.RestartCommand]).withCommand(classOf[TestGalaxyCommandLineParser.SshCommand]).withCommand(classOf[TestGalaxyCommandLineParser.ResetToActualCommand])
    builder.withGroup("agent").withDescription("Manage agents").withDefaultCommand(classOf[TestGalaxyCommandLineParser.AgentShowCommand]).withCommand(classOf[TestGalaxyCommandLineParser.AgentShowCommand]).withCommand(classOf[TestGalaxyCommandLineParser.AgentAddCommand]).withCommand(classOf[TestGalaxyCommandLineParser.AgentTerminateCommand])
    builder.build
  }

  private def parse(args: String*) {
    System.out.println("$ galaxy " + args.mkString(" "))
    val command: TestGalaxyCommandLineParser.GalaxyCommand = createParser.parse(args: _*)
    command.execute
    System.out.println()
  }
}