package io.airlift.airline

import javax.inject.Inject

import io.airlift.airline.model.CommandMetadata

class HelpOption {
  @Inject var commandMetadata: CommandMetadata = null
  @Option(name = Array("-h", "--help"), description = "Display help information")
  var help: Boolean = false

  def showHelpIfRequested: Boolean = {
    if (help) {
      Help.help(commandMetadata)
    }
    help
  }
}
