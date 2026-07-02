package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.Ipv6CanonicalFormInspection
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/**
 * A hardware-address field (Match=, Link=) accepts IPv6 literals, and those literals carry the
 * SemanticTag.IPV6 from the shared IPV6_ADDR combinator — so the RFC 5952 canonicalization inspection
 * and its quick-fix compose with the new MAC validators (#501). A strict 6-byte MAC field, which does
 * not accept IP literals, must never be touched.
 */
class MacAddressIpv6QuickFixTest : AbstractUnitFileTest() {

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
  fun testNonCanonicalIpv6InMatchIsFlagged() {
    enableNewEngine()
    setupFileInEditor("f.network", "[Match]\nMACAddress=2001:DB8::1\n")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertTrue(hasCanonicalWarning())
  }

  @Test
  fun testPlainMacIsNotFlagged() {
    enableNewEngine()
    setupFileInEditor("f.network", "[Match]\nMACAddress=00:11:22:33:44:55\n")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertFalse(hasCanonicalWarning())
  }

  @Test
  fun testStrictMacFieldNeverColorsAnIpv6() {
    // SR-IOV.MACAddress is a strict 6-byte MAC: an IPv6 literal is invalid there and, crucially, is
    // NOT a labeled IPv6 span, so the canonicalization inspection must not fire.
    enableNewEngine()
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=2001:DB8::1\n")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    assertFalse(hasCanonicalWarning())
  }

  @Test
  fun testQuickFixRewritesSingleIpv6() {
    enableNewEngine()
    setupFileInEditor("f.network", "[Link]\nMACAddress=2001:D${COMPLETION_POSITION}B8::1\n")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert to canonical IPv6"))
    myFixture.checkResult("[Link]\nMACAddress=2001:db8::1\n")
  }

  @Test
  fun testQuickFixRewritesOnlyTheIpv6WithinAList() {
    // Offset handling: the IPv6 sits after a MAC in a Match= list; only it should be rewritten.
    enableNewEngine()
    setupFileInEditor("f.network", "[Match]\nMACAddress=00:11:22:33:44:55 2001:D${COMPLETION_POSITION}B8::1\n")
    enableInspection(Ipv6CanonicalFormInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert to canonical IPv6"))
    myFixture.checkResult("[Match]\nMACAddress=00:11:22:33:44:55 2001:db8::1\n")
  }
}