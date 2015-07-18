package io.airlift.airline

import javax.inject.Inject

import io.airlift.airline.model.GlobalMetadata

import scala.collection.JavaConverters._

class GlobalSuggester extends Suggester {
  @Inject var metadata: GlobalMetadata = null

  def suggest: java.lang.Iterable[String] = {
    List(
      metadata.getCommandGroups.asScala.map{_.getName},
      metadata.getDefaultGroupCommands.asScala.map{_.getName},
      metadata.getOptions.asScala.flatMap{_.getOptions.asScala}
    ).flatten.asJava
  }
}