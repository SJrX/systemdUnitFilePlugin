package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat
import org.junit.Test

/**
 * End-to-end: a MAC address not in the preferred format is flagged and the quick fix rewrites it
 * (flag-gated, and off when the preference is LEAVE_ALONE).
 *
 * Expects the problem description to contain "preferred format" and the quick fix name to start with
 * "Convert MAC address to".
 */
class MacAddressFormatInspectionTest : AbstractUnitFileTest() {

  override fun tearDown() {
    try {
      val state = ExperimentalSettings.getInstance(project).state
      state.useGrammarParseEngine = false
      state.preferredMacFormat = PreferredMacFormat.COLON_LOWER
    } finally {
      super.tearDown()
    }
  }

  private fun configure(format: PreferredMacFormat = PreferredMacFormat.COLON_LOWER, engine: Boolean = true) {
    val state = ExperimentalSettings.getInstance(project).state
    state.useGrammarParseEngine = engine
    state.preferredMacFormat = format
  }

  private fun hasFormatWarning() = myFixture.doHighlighting().any {
    it.severity == HighlightSeverity.WEAK_WARNING && it.description?.contains("preferred format") == true
  }

  @Test
  fun testNonPreferredIsFlagged() {
    configure()
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00-1A-2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertTrue(hasFormatWarning())
  }

  @Test
  fun testAlreadyPreferredIsNotFlagged() {
    configure()
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00:1a:2b:3c:4d:5e\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertFalse(hasFormatWarning())
  }

  @Test
  fun testOtherPreferenceFlagsSystemdDefault() {
    configure(PreferredMacFormat.DOT_LOWER)
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00:1a:2b:3c:4d:5e\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertTrue(hasFormatWarning())
  }

  @Test
  fun testNotFlaggedWhenLeaveAlone() {
    configure(PreferredMacFormat.LEAVE_ALONE)
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00-1A-2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertFalse(hasFormatWarning())
  }

  @Test
  fun testNotFlaggedWhenEngineOff() {
    configure(engine = false)
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00-1A-2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertFalse(hasFormatWarning())
  }

  // --- Hardware-address fields (Match=, Link=): 6-byte MACs are flagged, other lengths and IPs are not.

  @Test
  fun testMacInHardwareAddressFieldIsFlagged() {
    configure()
    setupFileInEditor("f.network", "[Match]\nMACAddress=00-1A-2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertTrue(hasFormatWarning())
  }

  @Test
  fun testNonMacHardwareAddressesAreNotFlagged() {
    configure()
    // 4-byte and 20-byte (Infiniband) raw addresses, plus an IPv6 literal: none is a 6-byte MAC.
    setupFileInEditor(
      "f.network",
      "[Match]\nMACAddress=01-02-03-04 " +
        "00-01-02-03-04-05-06-07-08-09-0A-0B-0C-0D-0E-0F-10-11-12-13 2001:db8::1\n",
    )
    enableInspection(MacAddressFormatInspection::class.java)
    assertFalse(hasFormatWarning())
  }

  @Test
  fun testQuickFixRewritesMacWithinHardwareAddressList() {
    configure()
    setupFileInEditor("f.network", "[Match]\nMACAddress=2001:db8::1 00-1A-${COMPLETION_POSITION}2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert MAC address to"))
    myFixture.checkResult("[Match]\nMACAddress=2001:db8::1 00:1a:2b:3c:4d:5e\n")
  }

  @Test
  fun testQuickFixRewritesToPreferred() {
    configure()
    setupFileInEditor("f.link", "[SR-IOV]\nMACAddress=00-1A-${COMPLETION_POSITION}2B-3C-4D-5E\n")
    enableInspection(MacAddressFormatInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert MAC address to"))
    myFixture.checkResult("[SR-IOV]\nMACAddress=00:1a:2b:3c:4d:5e\n")
  }

  @Test
  fun testQuickFixRewritesOnlyTheTargetInAList() {
    // Offset handling: SourceMACAddress= is a whitespace-separated MAC list; only the second entry
    // (where the caret is) should be rewritten.
    configure()
    setupFileInEditor(
      "f.netdev",
      "[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 AA-BB-${COMPLETION_POSITION}CC-DD-EE-FF\n",
    )
    enableInspection(MacAddressFormatInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert MAC address to"))
    myFixture.checkResult("[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 aa:bb:cc:dd:ee:ff\n")
  }

  // --- Regressions from the PR #576 review.

  @Test
  fun testFixAllRewritesEveryMacInAList() {
    // Each fix lengthens the value, so later fixes in the same value must not use stale offsets.
    configure()
    setupFileInEditor("f.netdev", "[MACVLAN]\nSourceMACAddress=1-2-3-4-5-${COMPLETION_POSITION}6 A-B-C-D-E-F 00:11:22:33:44:55\n")
    enableInspection(MacAddressFormatInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Fix all 'MAC address not in preferred format' problems in file"))
    myFixture.checkResult("[MACVLAN]\nSourceMACAddress=01:02:03:04:05:06 0a:0b:0c:0d:0e:0f 00:11:22:33:44:55\n")
  }

  @Test
  fun testQuickFixOnContinuationLineKeepsLayout() {
    // valueText joins the lines, so its offsets must be mapped back onto the raw text.
    configure()
    setupFileInEditor(
      "f.netdev",
      "[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 \\\n    AA-BB-${COMPLETION_POSITION}CC-DD-EE-FF\n",
    )
    enableInspection(MacAddressFormatInspection::class.java)
    myFixture.doHighlighting()

    myFixture.launchAction(myFixture.findSingleIntention("Convert MAC address to"))
    myFixture.checkResult("[MACVLAN]\nSourceMACAddress=00:11:22:33:44:55 \\\n    aa:bb:cc:dd:ee:ff\n")
  }

  @Test
  fun testWakeOnLanPasswordIsNotTreatedAsAMac() {
    // A SecureOn password is spelled like a MAC but isn't an address.
    configure()
    setupFileInEditor("f.link", "[Link]\nWakeOnLanPassword=00-11-22-33-44-55\n")
    enableInspection(MacAddressFormatInspection::class.java)
    assertFalse(hasFormatWarning())
  }
}
