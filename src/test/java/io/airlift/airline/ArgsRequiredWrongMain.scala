package io.airlift.airline

@Command(name = "ArgsRequiredWrongMain") class ArgsRequiredWrongMain {
  @Arguments(required = true)
  var file: Array[String] = null
}