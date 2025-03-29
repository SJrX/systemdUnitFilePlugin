package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileSectionCompletionContributorTest : AbstractUnitFileTest() {

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInAutomount() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.automount", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Automount")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInDevice() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.device", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInTarget() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.target", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInMount() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.mount", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Mount")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInPath() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.path", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Path")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInService() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Service")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInSocket() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.socket", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Socket")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInSwap() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.swap", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Swap")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInTimer() { // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.timer", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Timer")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInNSpawn() {
    // Fixture Setup
    val file = """
           [Files]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.link", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Link", "Match", "SR-IOV")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInNetDev() { // Fixture Setup
    val file = """
           [Files]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.netdev", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(
      completions,
      "BareUDP",
      "BatmanAdvanced",
      "Bond",
      "Bridge",
      "FooOverUDP",
      "GENEVE",
      "IPoIB",
      "IPVLAN",
      "IPVTAP",
      "L2TP",
      "L2TPSession",
      "MACsec",
      "MACsecReceiveAssociation",
      "MACsecReceiveChannel",
      "MACsecTransmitAssociation",
      "MACVLAN",
      "MACVTAP",
      "Match",
      "NetDev",
      "Peer",
      "Tap",
      "Tun",
      "Tunnel",
      "VLAN",
      "VRF",
      "VXCAN",
      "VXLAN",
      "WireGuard",
      "WireGuardPeer",
      "WLAN",
      "Xfrm"
    )
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInNetwork() { // Fixture Setup
    val file = """
           [Files]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.network", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(
      completions,
      "Address",
      "BandMultiQueueing",
      "BFIFO",
      "Bridge",
      "BridgeFDB",
      "BridgeMDB",
      "BridgeVLAN",
      "CAKE",
      "CAN",
      "ClassfulMultiQueueing",
      "ControlledDelay",
      "DeficitRoundRobinScheduler",
      "DeficitRoundRobinSchedulerClass",
      "DHCPPrefixDelegation",
      "DHCPServer",
      "DHCPServerStaticLease",
      "DHCPv4",
      "DHCPv6",
      "EnhancedTransmissionSelection",
      "FairQueueing",
      "FairQueueingControlledDelay",
      "FlowQueuePIE",
      "GenericRandomEarlyDetection",
      "HeavyHitterFilter",
      "HierarchyTokenBucket",
      "HierarchyTokenBucketClass",
      "IPoIB",
      "IPv6AcceptRA",
      "IPv6AddressLabel",
      "IPv6PREF64Prefix",
      "IPv6Prefix",
      "IPv6RoutePrefix",
      "IPv6SendRA",
      "Link",
      "LLDP",
      "Match",
      "Neighbor",
      "Network",
      "NetworkEmulator",
      "NextHop",
      "PFIFO",
      "PFIFOFast",
      "PFIFOHeadDrop",
      "PIE",
      "QDisc",
      "QuickFairQueueing",
      "QuickFairQueueingClass",
      "Route",
      "RoutingPolicyRule",
      "StochasticFairBlue",
      "StochasticFairnessQueueing",
      "TokenBucketFilter",
      "TrivialLinkEqualizer"
    )
  }

  fun testCompletionOfNewSectionInUnknownFileTypeIsEmpty() { // Fixture Setup
    val file = """
           [Tester]
           Whatevs=$COMPLETION_POSITION

           """.trimIndent()
    myFixture.configureByText("file.mystery", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertSize(0, completions)
  }
}
