package io.airlift.airline

import javax.inject.Inject

@Command(name = "ping", description = "network test utility") object Ping {
  def main(args: String*) {
    val ping = SingleCommand.singleCommand(classOf[Ping]).parse(args: _*)
    if (!ping.helpOption.showHelpIfRequested) {
      ping.run()
    }
  }
}

@Command(name = "ping", description = "network test utility") class Ping {
  @Inject var helpOption: HelpOption = null
  @Option(name = Array("-c", "--count"), description = "Send count packets") var count: Int = 1

  def run() {
    println("Ping count: " + count)
  }
}
