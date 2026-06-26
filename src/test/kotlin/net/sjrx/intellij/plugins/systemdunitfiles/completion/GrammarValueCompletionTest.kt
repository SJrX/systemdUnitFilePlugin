package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/**
 * End-to-end grammar-based completion (#467 / #343), behind the experimental flag.
 */
class GrammarValueCompletionTest : AbstractUnitFileTest() {

  // The light-test project is shared across classes; don't leak the opt-in.
  override fun tearDown() {
    try {
      ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = false
    } finally {
      super.tearDown()
    }
  }

  private fun enableNewEngine() {
    ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = true
  }

  @Test
  fun testCompletesPartialAddressFamily() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_IN${COMPLETION_POSITION}")

    val results = basicCompletionResultStrings
    assertContainsElements(results, "AF_INET", "AF_INET6")
  }

  @Test
  fun testCompletesSubsequentFamilyInList() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_UNIX AF_IN${COMPLETION_POSITION}")

    val results = basicCompletionResultStrings
    assertContainsElements(results, "AF_INET", "AF_INET6")
  }

  @Test
  fun testFlagOffOffersNoGrammarCompletions() {
    // Gating guarantee: with the experimental engine OFF (the default), grammar completion must not
    // fire. A GrammarOptionValue has no legacy autocomplete options, so the same partial token that
    // the flag-on path completes to AF_INET/AF_INET6 yields nothing grammar-derived here.
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_IN${COMPLETION_POSITION}")

    assertDoesntContain(basicCompletionResultStrings, "AF_INET", "AF_INET6")
  }
}
