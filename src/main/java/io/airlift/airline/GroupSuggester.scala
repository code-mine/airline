package io.airlift.airline

import javax.inject.Inject

import io.airlift.airline.model.CommandGroupMetadata

import scala.collection.JavaConverters._

class GroupSuggester extends Suggester {
  @Inject var group: CommandGroupMetadata = null

  def suggest: java.lang.Iterable[String] = {
    List(
      group.getCommands.asScala.map{_.getName},
      group.getOptions.asScala.flatMap{_.getOptions.asScala}
    ).flatten.asJava
  }
}