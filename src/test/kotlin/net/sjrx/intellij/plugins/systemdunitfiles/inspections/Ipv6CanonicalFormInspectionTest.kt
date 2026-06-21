package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/** End-to-end: a non-canonical IPv6 address is flagged and the quick-fix rewrites it (flag-gated). */
class Ipv6CanonicalFormInspectionTest : AbstractUnitFileTest() {

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

  private fun hasCanonicalWarning() = myFixture.doHighlighting().any {
    it.severity == HighlightSeverity.WEAK_WARNING && it.description?.contains("canonical form") == true
  }

  @Test
  fun testNonCanonicalIsFlagged() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nIPAddressAllow=2001:DB8::1")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertTrue(hasCanonicalWarning())
  }

  @Test
  fun testAlreadyCanonicalIsNotFlagged() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nIPAddressAllow=2001:db8::1")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertFalse(hasCanonicalWarning())
  }

  @Test
  fun testNotFlaggedWhenFlagOff() {
    setupFileInEditor("file.service", "[Service]\nIPAddressAllow=2001:DB8::1")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertFalse(hasCanonicalWarning())
  }

  @Test
  fun testQuickFixRewritesToCanonical() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nIPAddressAllow=2001:D${COMPLETION_POSITION}B8::1")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    myFixture.doHighlighting()

    val fix = myFixture.findSingleIntention("Convert to canonical IPv6")
    myFixture.launchAction(fix)

    myFixture.checkResult("[Service]\nIPAddressAllow=2001:db8::1")
  }
}
