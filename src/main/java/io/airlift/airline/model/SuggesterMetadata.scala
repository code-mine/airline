package io.airlift.airline.model

import java.util.List

import io.airlift.airline.{Accessor, Suggester}

class SuggesterMetadata(suggesterClass: Class[_ <: Suggester], metadataInjections: List[Accessor]) {

  def getSuggesterClass: Class[_ <: Suggester] = {
    suggesterClass
  }

  def getMetadataInjections: List[Accessor] = {
    metadataInjections
  }
}