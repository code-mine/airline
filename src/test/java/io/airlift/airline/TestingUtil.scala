package io.airlift.airline

object TestingUtil {
  def singleCommandParser[T](commandClass: Class[T]): Cli[T] = {
    Cli.builder[T]("parser").withCommand(commandClass).build
  }
}