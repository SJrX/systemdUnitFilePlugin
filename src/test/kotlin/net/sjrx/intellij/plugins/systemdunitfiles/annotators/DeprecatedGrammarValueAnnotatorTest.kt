package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/** End-to-end: a kernel-removed address family gets a weak warning (behind the flag). */
class DeprecatedGrammarValueAnnotatorTest : AbstractUnitFileTest() {

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

  private fun weakWarned(highlights: List<HighlightInfo>, text: String) =
    highlights.any { it.severity == HighlightSeverity.WEAK_WARNING && it.text == text }

  @Test
  fun testRemovedFamilyIsWeakWarned() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_INET AF_DECnet")
    assertTrue(weakWarned(myFixture.doHighlighting(), "AF_DECnet"))
  }

  @Test
  fun testCurrentFamilyIsNotWarned() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_INET")
    assertFalse(weakWarned(myFixture.doHighlighting(), "AF_INET"))
  }

  @Test
  fun testNoWarningWhenFlagOff() {
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_DECnet")
    assertFalse(weakWarned(myFixture.doHighlighting(), "AF_DECnet"))
  }
}
