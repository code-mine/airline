package io.airlift.airline

import org.testng.annotations.Test

class TestGit {
  @Test def test() {
    git("add", "-p", "file")
    git("remote", "add", "origin", "git@github.com:airlift/airline.git")
    git("-v", "remote", "show", "origin")
    git()
    git("help")
    git("help", "git")
    git("help", "add")
    git("help", "remote")
    git("help", "remote", "show")
  }

  private def git(args: String*) {
    println("$ git " + args.mkString(" "))
    Git.main(args: _*)
    println()
  }
}
