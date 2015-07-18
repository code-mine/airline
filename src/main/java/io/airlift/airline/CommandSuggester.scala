package io.airlift.airline

import javax.inject.Inject

import io.airlift.airline.model.CommandMetadata

import scala.collection.JavaConverters._

class CommandSuggester extends Suggester {
  @Inject var command: CommandMetadata = null

  def suggest: java.lang.Iterable[String] = {
    val suggestions = command.getCommandOptions.asScala.flatMap(_.getOptions.asScala).asJava
    if (command.getArguments != null) {
      suggestions.add("--")
    }
    suggestions
  }
}