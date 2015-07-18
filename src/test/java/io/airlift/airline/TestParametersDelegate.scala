package io.airlift.airline

import java.util.List
import javax.inject.Inject

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists.newArrayList
import io.airlift.airline.TestParametersDelegate.CombinedAndNestedDelegates.NestedDelegate2
import io.airlift.airline.TestParametersDelegate.CommandTest.Delegate
import io.airlift.airline.TestParametersDelegate.DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams.ComplexDelegate
import io.airlift.airline.TestParametersDelegate.DuplicateMainParametersAreAllowed.{Delegate1, Delegate2}
import io.airlift.airline.TestingUtil.singleCommandParser
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.Test

/**
 * @author dain
 * @author rodionmoiseev
 */
object TestParametersDelegate {

  @Command(name = "command") object DelegatingEmptyClassHasNoEffect {

    class EmptyDelegate {
      var nonParamString: String = "a"
    }

  }

  @Command(name = "command") class DelegatingEmptyClassHasNoEffect {
    @Option(name = Array("-a")) var isA: Boolean = false
    @Option(name = Array("-b", "--long-b")) var bValue: String = ""
    @Inject var delegate = new TestParametersDelegate.DelegatingEmptyClassHasNoEffect.EmptyDelegate
  }

  @Command(name = "command") object DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams {

    class ComplexDelegate {
      @Option(name = Array("-c")) var isC: Boolean = false
      @Option(name = Array("-d", "--long-d")) var d: Integer = null
    }

  }

  @Command(name = "command") class DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams {
    @Option(name = Array("-a")) var isA: Boolean = false
    @Option(name = Array("-b", "--long-b")) var bValue: String = ""
    @Inject var delegate = new ComplexDelegate
  }

  @Command(name = "command") object CombinedAndNestedDelegates {

    class LeafDelegate {
      @Option(name = Array("--list")) var list: List[String] = newArrayList("value1", "value2")
      @Option(name = Array("--bool")) var bool: Boolean = false
    }

    class NestedDelegate1 {
      @Inject var leafDelegate = new LeafDelegate
      @Option(name = Array("-d", "--long-d")) var d: Integer = null
    }

    class NestedDelegate2 {
      @Option(name = Array("-c")) var isC: Boolean = false
      @Inject var nestedDelegate1 = new NestedDelegate1
    }

  }

  @Command(name = "command") class CombinedAndNestedDelegates {
    @Option(name = Array("-a")) var isA: Boolean = false
    @Option(name = Array("-b", "--long-b")) var bValue: String = ""
    @Inject var nestedDelegate2 = new NestedDelegate2
  }

  @Command(name = "command") object CommandTest {

    class Delegate {
      @Option(name = Array("-a")) var a: String = "b"
    }

  }

  @Command(name = "command") class CommandTest {
    @Inject var delegate = new Delegate
  }

  @Command(name = "command") object NullDelegatesAreProhibited {

    class ComplexDelegate {
      @Option(name = Array("-a")) var a: Boolean = false
    }

  }

  @Command(name = "command") class NullDelegatesAreProhibited {
    @Inject var delegate: NullDelegatesAreProhibited.ComplexDelegate = null
  }

  @Command(name = "command") object DuplicateDelegateAllowed {

    class Delegate {
      @Option(name = Array("-a")) var a: String = null
    }

  }

  @Command(name = "command") class DuplicateDelegateAllowed {
    @Inject var d1 = new TestParametersDelegate.DuplicateDelegateAllowed.Delegate
    @Inject var d2 = new TestParametersDelegate.DuplicateDelegateAllowed.Delegate
  }

  @Command(name = "command") object DuplicateMainParametersAreAllowed {

    class Delegate1 {
      @Arguments var mainParams1: List[String] = newArrayList()
    }

    class Delegate2 {
      @Arguments var mainParams1: List[String] = newArrayList()
    }

  }

  @Command(name = "command") class DuplicateMainParametersAreAllowed {
    @Inject var delegate1 = new Delegate1
    @Inject var delegate2 = new Delegate2
  }

  @SuppressWarnings(Array("UnusedDeclaration"))
  @Command(name = "command") object ConflictingMainParametersAreNotAllowed {

    class Delegate1 {
      @Arguments(description = "foo") var mainParams1: List[String] = newArrayList()
    }

    class Delegate2 {
      @Arguments(description = "bar") var mainParams1: List[String] = newArrayList()
    }

  }

  @SuppressWarnings(Array("UnusedDeclaration"))
  @Command(name = "command") class ConflictingMainParametersAreNotAllowed {
    @Inject var delegate1 = new ConflictingMainParametersAreNotAllowed.Delegate1
    @Inject var delegate2 = new ConflictingMainParametersAreNotAllowed.Delegate2
  }

}

class TestParametersDelegate {
  @Test def delegatingEmptyClassHasNoEffect() {
    val p = Cli.builder[TestParametersDelegate.DelegatingEmptyClassHasNoEffect]("foo")
      .withCommand(classOf[TestParametersDelegate.DelegatingEmptyClassHasNoEffect]).build.parse("command", "-a", "-b", "someValue")
    assertTrue(p.isA)
    assertEquals(p.bValue, "someValue")
    assertEquals(p.delegate.nonParamString, "a")
  }

  @Test def delegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams() {
    val p = singleCommandParser(classOf[TestParametersDelegate.DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams])
      .parse("command", "-c", "--long-d", "123", "--long-b", "bValue")
    assertFalse(p.isA)
    assertEquals(p.bValue, "bValue")
    assertTrue(p.delegate.isC)
    assertEquals(p.delegate.d, Integer.valueOf(123))
  }

  @Test def combinedAndNestedDelegates() {
    val p = singleCommandParser(classOf[TestParametersDelegate.CombinedAndNestedDelegates])
      .parse("command", "-d", "234", "--list", "a", "--list", "b", "-a")
    assertEquals(p.nestedDelegate2.nestedDelegate1.leafDelegate.list, newArrayList("value1", "value2", "a", "b"))
    assertFalse(p.nestedDelegate2.nestedDelegate1.leafDelegate.bool)
    assertEquals(p.nestedDelegate2.nestedDelegate1.d, Integer.valueOf(234))
    assertFalse(p.nestedDelegate2.isC)
    assertTrue(p.isA)
    assertEquals(p.bValue, "")
  }

  @Test def commandTest() {
    val c = singleCommandParser(classOf[TestParametersDelegate.CommandTest]).parse("command", "-a", "a")
    assertEquals(c.delegate.a, "a")
  }

  @Test def nullDelegatesAreAllowed() {
    val value = singleCommandParser(classOf[TestParametersDelegate.NullDelegatesAreProhibited]).parse("command", "-a")
    assertEquals(value.delegate.a, true)
  }

  @Test def duplicateDelegateAllowed() {
    val value = singleCommandParser(classOf[TestParametersDelegate.DuplicateDelegateAllowed]).parse("command", "-a", "value")
    assertEquals(value.d1.a, "value")
    assertEquals(value.d2.a, "value")
  }

  @Test def duplicateMainParametersAreAllowed() {
    val value = singleCommandParser(classOf[TestParametersDelegate.DuplicateMainParametersAreAllowed]).parse("command", "main", "params")
    assertEquals(value.delegate1.mainParams1, ImmutableList.of("main", "params"))
    assertEquals(value.delegate2.mainParams1, ImmutableList.of("main", "params"))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def conflictingMainParametersAreNotAllowed() {
    singleCommandParser(classOf[TestParametersDelegate.ConflictingMainParametersAreNotAllowed]).parse("command", "main", "params")
  }
}