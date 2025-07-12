package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidSectionHeaderNameAnnotatorTest : AbstractUnitFileTest() {
  fun testThatInvalidSectionNamesAreAnnotated() {
    // Fixture Setup
    val file = """
      [Serv${'\t'}ice]
      Requires=Hello Good Sir
      """.trimIndent()
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ILLEGAL_SECTION_NAME, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Serv\tice]", highlightElement!!.text)
  }

  fun testThatInvalidSectionNameWithLeftBraceAreAnnotated() {
    // Fixture Setup
    val file = """
           [Serv[ice]
           Requires=Hello Good Sir
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ILLEGAL_SECTION_NAME, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Serv[ice]", highlightElement!!.text)
  }

  fun testThatValidSectionNamesAreNotAnnotated() {
    // Fixture Setup
    val file = """
           [Service]
           Requires=Hello Good Sir
           """.trimIndent()
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testUnterminatedSectionName() {
    // Fixture Setup
    val file = """
           [Service
           Foo=Bar
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ILLEGAL_SECTION_NAME, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Service", highlightElement!!.text)
  }

  fun testAutoMountSectionInAutoMountFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Automount]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.automount", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testADeviceFileWithNoOtherOptionsHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.device", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }


  fun testMountSectionInMountFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Mount]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.mount", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testPathSectionInPathFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Path]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.path", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }


  fun testServiceSectionInServiceFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Service]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.service", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }


  fun testSliceSectionInSliceFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Slice]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.slice", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testSocketSectionInSocketFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Socket]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.socket", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testSwapSectionInSwapFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Swap]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.swap", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testATargetFileWithNoOtherOptionsHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.target", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }



  fun testTimerSectionInTimerFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Timer]
    
    [Install]
    
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.timer", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testAllSectionsInServiceFileHasABunchOfWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Automount]
    [Mount]
    [Path]
    [Service]
    [Slice]
    [Socket]
    [Swap]
    [Timer]
    [Install]
    [Unit]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.service", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(7, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Automount is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Mount is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Path is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Slice is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Socket is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Swap is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Timer is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")

  }


  fun testNSpawnSectionsInNSpawnFileHasNoWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Exec]
    
    [Files]
    
    [Network]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.nspawn", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(0, highlights)
  }

  fun testNSpawnSectionsInServiceFileHasWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Exec]
    
    [Files]
    
    [Network]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.service", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(3, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Exec is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Files is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
    assertContainsElements(highlightTexts, "The section Network is not allowed in Service files, only the following are allowed: [Unit, Install, Service]")
  }

  fun testServiceSectionsInNSpawnFileHasWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Unit]
    
    [Install]
    
    [Service]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.nspawn", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(3, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Unit is not allowed in Nspawn files, only the following are allowed: [Exec, Files, Network]")
    assertContainsElements(highlightTexts, "The section Install is not allowed in Nspawn files, only the following are allowed: [Exec, Files, Network]")
    assertContainsElements(highlightTexts, "The section Service is not allowed in Nspawn files, only the following are allowed: [Exec, Files, Network]")
  }

  fun testServiceSectionsInLinkFileHasWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Unit]
    
    [Install]
    
    [Service]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.link", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(3, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Unit is not allowed in Link files, only the following are allowed: [Link, Match, SR-IOV]")
    assertContainsElements(highlightTexts, "The section Install is not allowed in Link files, only the following are allowed: [Link, Match, SR-IOV]")
    assertContainsElements(highlightTexts, "The section Service is not allowed in Link files, only the following are allowed: [Link, Match, SR-IOV]")
  }

  fun testServiceSectionsInNetDevFileHasWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Unit]
    
    [Install]
    
    [Service]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.netdev", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(3, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Unit is not allowed in Netdev files, only the following are allowed: [BareUDP, BatmanAdvanced, Bond, Bridge, FooOverUDP, GENEVE, HSR, IPoIB, IPVLAN, IPVTAP, L2TP, L2TPSession, MACsec, MACsecReceiveAssociation, MACsecReceiveChannel, MACsecTransmitAssociation, MACVLAN, MACVTAP, Match, NetDev, Peer, Tap, Tun, Tunnel, VLAN, VRF, VXCAN, VXLAN, WireGuard, WireGuardPeer, WLAN, Xfrm]")
    assertContainsElements(highlightTexts, "The section Install is not allowed in Netdev files, only the following are allowed: [BareUDP, BatmanAdvanced, Bond, Bridge, FooOverUDP, GENEVE, HSR, IPoIB, IPVLAN, IPVTAP, L2TP, L2TPSession, MACsec, MACsecReceiveAssociation, MACsecReceiveChannel, MACsecTransmitAssociation, MACVLAN, MACVTAP, Match, NetDev, Peer, Tap, Tun, Tunnel, VLAN, VRF, VXCAN, VXLAN, WireGuard, WireGuardPeer, WLAN, Xfrm]")
    assertContainsElements(highlightTexts, "The section Service is not allowed in Netdev files, only the following are allowed: [BareUDP, BatmanAdvanced, Bond, Bridge, FooOverUDP, GENEVE, HSR, IPoIB, IPVLAN, IPVTAP, L2TP, L2TPSession, MACsec, MACsecReceiveAssociation, MACsecReceiveChannel, MACsecTransmitAssociation, MACVLAN, MACVTAP, Match, NetDev, Peer, Tap, Tun, Tunnel, VLAN, VRF, VXCAN, VXLAN, WireGuard, WireGuardPeer, WLAN, Xfrm]")
  }

  fun testServiceSectionsInNetworkFileHasWarnings() {
    /*
     * Fixture Setup
     */
    // language="unit file (systemd)"
    val file = """
    [Unit]
    
    [Install]
    
    [Service]
    """.trimIndent()

    /*
     * Exercise SUT
     */
    setupFileInEditor("file.network", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(3, highlights)

    val highlightTexts = highlights.map { it.description }

    assertContainsElements(highlightTexts, "The section Unit is not allowed in Network files, only the following are allowed: [Address, BandMultiQueueing, BFIFO, Bridge, BridgeFDB, BridgeMDB, BridgeVLAN, CAKE, CAN, ClassfulMultiQueueing, ControlledDelay, DeficitRoundRobinScheduler, DeficitRoundRobinSchedulerClass, DHCPPrefixDelegation, DHCPServer, DHCPServerStaticLease, DHCPv4, DHCPv6, EnhancedTransmissionSelection, FairQueueing, FairQueueingControlledDelay, FlowQueuePIE, GenericRandomEarlyDetection, HeavyHitterFilter, HierarchyTokenBucket, HierarchyTokenBucketClass, IPoIB, IPv6AcceptRA, IPv6AddressLabel, IPv6PREF64Prefix, IPv6Prefix, IPv6RoutePrefix, IPv6SendRA, Link, LLDP, Match, Neighbor, Network, NetworkEmulator, NextHop, PFIFO, PFIFOFast, PFIFOHeadDrop, PIE, QDisc, QuickFairQueueing, QuickFairQueueingClass, Route, RoutingPolicyRule, StochasticFairBlue, StochasticFairnessQueueing, TokenBucketFilter, TrivialLinkEqualizer]")
    assertContainsElements(highlightTexts, "The section Install is not allowed in Network files, only the following are allowed: [Address, BandMultiQueueing, BFIFO, Bridge, BridgeFDB, BridgeMDB, BridgeVLAN, CAKE, CAN, ClassfulMultiQueueing, ControlledDelay, DeficitRoundRobinScheduler, DeficitRoundRobinSchedulerClass, DHCPPrefixDelegation, DHCPServer, DHCPServerStaticLease, DHCPv4, DHCPv6, EnhancedTransmissionSelection, FairQueueing, FairQueueingControlledDelay, FlowQueuePIE, GenericRandomEarlyDetection, HeavyHitterFilter, HierarchyTokenBucket, HierarchyTokenBucketClass, IPoIB, IPv6AcceptRA, IPv6AddressLabel, IPv6PREF64Prefix, IPv6Prefix, IPv6RoutePrefix, IPv6SendRA, Link, LLDP, Match, Neighbor, Network, NetworkEmulator, NextHop, PFIFO, PFIFOFast, PFIFOHeadDrop, PIE, QDisc, QuickFairQueueing, QuickFairQueueingClass, Route, RoutingPolicyRule, StochasticFairBlue, StochasticFairnessQueueing, TokenBucketFilter, TrivialLinkEqualizer]")
    assertContainsElements(highlightTexts, "The section Service is not allowed in Network files, only the following are allowed: [Address, BandMultiQueueing, BFIFO, Bridge, BridgeFDB, BridgeMDB, BridgeVLAN, CAKE, CAN, ClassfulMultiQueueing, ControlledDelay, DeficitRoundRobinScheduler, DeficitRoundRobinSchedulerClass, DHCPPrefixDelegation, DHCPServer, DHCPServerStaticLease, DHCPv4, DHCPv6, EnhancedTransmissionSelection, FairQueueing, FairQueueingControlledDelay, FlowQueuePIE, GenericRandomEarlyDetection, HeavyHitterFilter, HierarchyTokenBucket, HierarchyTokenBucketClass, IPoIB, IPv6AcceptRA, IPv6AddressLabel, IPv6PREF64Prefix, IPv6Prefix, IPv6RoutePrefix, IPv6SendRA, Link, LLDP, Match, Neighbor, Network, NetworkEmulator, NextHop, PFIFO, PFIFOFast, PFIFOHeadDrop, PIE, QDisc, QuickFairQueueing, QuickFairQueueingClass, Route, RoutingPolicyRule, StochasticFairBlue, StochasticFairnessQueueing, TokenBucketFilter, TrivialLinkEqualizer]")
  }


}

