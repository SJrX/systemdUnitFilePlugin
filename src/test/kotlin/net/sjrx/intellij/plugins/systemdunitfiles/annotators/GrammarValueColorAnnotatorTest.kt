package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/**
 * End-to-end grammar value coloring (#467 / #342), behind the experimental flag. We assert that the
 * expected spans get an INFORMATION-level coloring annotation (checked by the colored substring).
 */
class GrammarValueColorAnnotatorTest : AbstractUnitFileTest() {

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

  private fun colored(highlights: List<HighlightInfo>, text: String) =
    highlights.any { it.severity == HighlightSeverity.INFORMATION && it.text == text }

  @Test
  fun testSocketBindValueIsColoredByRole() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nSocketBindAllow=ipv4:tcp:8080")

    val highlights = myFixture.doHighlighting()
    assertTrue(colored(highlights, "ipv4")) // ENUM
    assertTrue(colored(highlights, ":"))    // OPERATOR
    assertTrue(colored(highlights, "8080")) // LITERAL
  }

  @Test
  fun testIpAddressIsColoredAsOneLiteralSpan() {
    enableNewEngine()
    setupFileInEditor("file.network", "[Network]\nGateway=192.168.1.1")

    // The whole address is one colored span (thanks to Labeled), not per-octet.
    assertTrue(colored(myFixture.doHighlighting(), "192.168.1.1"))
  }

  @Test
  fun testNoValueColoringWhenFlagOff() {
    setupFileInEditor("file.service", "[Service]\nSocketBindAllow=ipv4:tcp:8080")

    assertFalse(colored(myFixture.doHighlighting(), "8080"))
  }
}
