package io.airlift.airline

import java.util.concurrent.Callable
import java.util.{List, Map}
import javax.inject.Inject

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.{ImmutableList, ImmutableMap}
import io.airlift.airline.ParserUtil.createInstance
import io.airlift.airline.model.{CommandGroupMetadata, CommandMetadata, GlobalMetadata, MetadataLoader, OptionMetadata}

import scala.collection.JavaConverters._

@Command(name = "suggest") object SuggestCommand {
  private val BUILTIN_SUGGESTERS: Map[Context, Class[_ <: Suggester]] =
    ImmutableMap.builder[Context, Class[_ <: Suggester]]
      .put(Context.GLOBAL, classOf[GlobalSuggester])
      .put(Context.GROUP, classOf[GroupSuggester])
      .put(Context.COMMAND, classOf[CommandSuggester])
      .build
}

@Command(name = "suggest") class SuggestCommand extends Runnable with Callable[Void] {
  @Inject var metadata: GlobalMetadata = null
  @Arguments var arguments: List[String] = newArrayList()

  @VisibleForTesting def generateSuggestions: java.lang.Iterable[String] = {
    val parser = new Parser
    val state = parser.parse(metadata, arguments)
    val suggesterClass: Class[_ <: Suggester] = SuggestCommand.BUILTIN_SUGGESTERS.get(state.getLocation)
    if (suggesterClass != null) {
      val suggesterMetadata = MetadataLoader.loadSuggester(suggesterClass)
      if (suggesterMetadata != null) {
        val bindings: ImmutableMap.Builder[Class[_], AnyRef] = ImmutableMap.builder[Class[_], AnyRef].put(classOf[GlobalMetadata], metadata)
        if (state.getGroup != null) {
          bindings.put(classOf[CommandGroupMetadata], state.getGroup)
        }
        if (state.getCommand != null) {
          bindings.put(classOf[CommandMetadata], state.getCommand)
        }
        val suggester: Suggester = createInstance(
          suggesterMetadata.getSuggesterClass.asInstanceOf[Class[_ >: Null]],
          ImmutableList.of[OptionMetadata],
          null, null, null,
          suggesterMetadata.getMetadataInjections,
          bindings.build
        )
        return suggester.suggest
      }
    }
    scala.List[String]().toIterable.asJava
  }

  def run() {
    System.out.println(generateSuggestions.asScala.mkString("\n"))
  }

  def call: Void = {
    run()
    null
  }
}