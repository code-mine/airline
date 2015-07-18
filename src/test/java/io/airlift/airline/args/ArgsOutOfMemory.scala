package io.airlift.airline.args

import io.airlift.airline.{Command, Option}

@Command(name = "ArgsOutOfMemory") class ArgsOutOfMemory {

  @Option(name = Array("-p", "--pattern"),
    description = "pattern used by 'tail'. See http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout " +
      "and http://logback.qos.ch/manual/layouts.html#AccessPatternLayout")
  var pattern: String = null

  @Option(name = Array("-q"), description = "Filler arg") var filler: String = null
}