package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

class GrammarEngineKeyAnnotatorTest : AbstractUnitFileTest() {

  // The light-test project is shared across classes, so don't leak the opt-in.
  override fun tearDown() {
    try {
      ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = false
    } finally {
      super.tearDown()
    }
  }

  private fun markedKey(highlights: List<com.intellij.codeInsight.daemon.impl.HighlightInfo>, key: String) =
    highlights.any { it.severity == HighlightSeverity.INFORMATION && it.text == key }

  @Test
  fun testGrammarBackedKeyIsMarkedWhenFlagOn() {
    ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = true
    // RestrictAddressFamilies is validated by a GrammarOptionValue; PrivateTmp's value is not.
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_INET\n")

    val highlights = myFixture.doHighlighting()
    assertTrue(markedKey(highlights, "RestrictAddressFamilies"))
  }

  @Test
  fun testNotMarkedWhenFlagOff() {
    setupFileInEditor("file.service", "[Service]\nRestrictAddressFamilies=AF_INET\n")

    val highlights = myFixture.doHighlighting()
    assertFalse(markedKey(highlights, "RestrictAddressFamilies"))
  }
}
