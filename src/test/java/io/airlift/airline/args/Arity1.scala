package io.airlift.airline.args

import io.airlift.airline.{Command, Option}

@Command(name = "Arity1") class Arity1 {
  @Option(arity = 1, name = Array("-inspect"), description = "", required = false) var inspect: Boolean = false
}