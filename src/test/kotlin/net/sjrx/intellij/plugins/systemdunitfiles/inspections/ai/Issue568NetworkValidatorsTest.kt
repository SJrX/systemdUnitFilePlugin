package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import com.intellij.lang.annotation.HighlightSeverity
import org.junit.Test

/**
 * Issue #568, network theme: networkd / netdev values whose plugin verdict disagreed with systemd's parser.
 * Every value below was checked with steve-local/systemd-tools/bin/sd-check at 0f0682b773, one value per
 * file: the "valid" ones produce no complaint (or, for Address=, only the "specified without prefix length"
 * advice), the "invalid" ones are refused by the parser.
 *
 * `[Xfrm] InterfaceId=0` passes the parser but is refused by xfrm_verify() ("Xfrm interface ID cannot be zero",
 * src/network/netdev/xfrm.c#L30-L39), which drops the netdev, so it is flagged too (#565).
 */
class Issue568NetworkValidatorsTest : AbstractUnitFileTest() {

  private fun assertHighlights(fileName: String, file: String, expected: Int) {
    setupFileInEditor(fileName, file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()
    assertSize(expected, highlights)
  }

  @Test
  fun testErspanHardwareIdValid() {
    // language="unit file (systemd)"
    val file = """
      [Tunnel]
      ERSPANHardwareId=0
      ERSPANHardwareId=63
      ERSPANHardwareId=0x1f
      ERSPANHardwareId=0x2f
      ERSPANHardwareId=0x3f
      ERSPANHardwareId=077
      ERSPANHardwareId=0o77
      ERSPANHardwareId=0b1
      ERSPANHardwareId=+5
      ERSPANHardwareId=-0
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testErspanHardwareIdInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Tunnel]
      ERSPANHardwareId=64
      ERSPANHardwareId=0x40
      ERSPANHardwareId=-1
      ERSPANHardwareId=abc
      ERSPANHardwareId=0x
    """.trimIndent()
    assertHighlights("file.netdev", file, 5)
  }

  @Test
  fun testXfrmInterfaceIdValid() {
    // language="unit file (systemd)"
    val file = """
      [Xfrm]
      InterfaceId=0x98
      InterfaceId=0x99
      InterfaceId=1
      InterfaceId=4294967295
      InterfaceId=0xffffffff
      InterfaceId=077
      InterfaceId=+7
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testXfrmInterfaceIdInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Xfrm]
      InterfaceId=4294967296
      InterfaceId=-1
      InterfaceId=0x100000000
      InterfaceId=08
      InterfaceId=abc
    """.trimIndent()
    assertHighlights("file.netdev", file, 5)
  }

  @Test
  fun testXfrmInterfaceIdZeroIsRefusedByVerify() {
    // xfrm_verify() refuses 0 and netdev_load_one() then drops the netdev (src/network/netdev/netdev.c#L1110-L1114).
    // language="unit file (systemd)"
    val file = """
      [Xfrm]
      InterfaceId=0
      InterfaceId=0x0
    """.trimIndent()
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting().filter { it.severity == HighlightSeverity.WARNING }
    assertSize(2, highlights)
    assertStringContains("InterfaceId must be 1…4294967295; systemd-networkd refuses 0", highlights[0].description)
  }

  @Test
  fun testUint32ZeroStillValidForOtherKeys() {
    // Only [Xfrm] InterfaceId= in a .netdev has the extra rule; other config_parse_uint32 keys keep 0.
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      PoolOffset=0
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testFooOverUdpProtocolValid() {
    // language="unit file (systemd)"
    val file = """
      [FooOverUDP]
      Protocol=GRE
      Protocol=ipip
      Protocol=IPIP
      Protocol=gre
      Protocol=tcp
      Protocol=Udp
      Protocol=3
      Protocol=255
      Protocol=0x11
      Protocol=0
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testFooOverUdpProtocolInvalid() {
    // language="unit file (systemd)"
    val file = """
      [FooOverUDP]
      Protocol=256
      Protocol=-1
      Protocol=smc
      Protocol=mptcp
      Protocol=foo
    """.trimIndent()
    assertHighlights("file.netdev", file, 5)
  }

  @Test
  fun testFooOverUdpPortValid() {
    // language="unit file (systemd)"
    val file = """
      [FooOverUDP]
      Port=1
      Port=65535
      Port=0x10
      Port=+5
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testFooOverUdpPortInvalid() {
    // language="unit file (systemd)"
    val file = """
      [FooOverUDP]
      Port=0
      Port=-0
      Port=65536
      Port=0x10000
      Port=abc
    """.trimIndent()
    assertHighlights("file.netdev", file, 5)
  }

  @Test
  fun testSixRdPrefixValid() {
    // language="unit file (systemd)"
    val file = """
      [Tunnel]
      IPv6RapidDeploymentPrefix=2001:db8::
      IPv6RapidDeploymentPrefix=2001:db8::1
      IPv6RapidDeploymentPrefix=2001:db8::/16
      IPv6RapidDeploymentPrefix=2001:db8::/0x10
      IPv6RapidDeploymentPrefix=2001:db8::/128
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testSixRdPrefixInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Tunnel]
      IPv6RapidDeploymentPrefix=2001:db8::/0
      IPv6RapidDeploymentPrefix=2001:db8::/129
      IPv6RapidDeploymentPrefix=1.2.3.4
      IPv6RapidDeploymentPrefix=2001:db8::/
    """.trimIndent()
    assertHighlights("file.netdev", file, 4)
  }

  @Test
  fun testPdPrefixHintValid() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      PrefixDelegationHint=2001:db8::
      PrefixDelegationHint=2001:db8::/56
      PrefixDelegationHint=2001:db8::/0b1
      PrefixDelegationHint=2001:db8::/128
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testPdPrefixHintInvalid() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv6]
      PrefixDelegationHint=2001:db8::/0
      PrefixDelegationHint=2001:db8::/129
      PrefixDelegationHint=1.2.3.4/24
    """.trimIndent()
    assertHighlights("file.network", file, 3)
  }

  @Test
  fun testIpv6PrefixPrefixValid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6Prefix]
      Prefix=2001:db8::
      Prefix=2001:db8::/0
      Prefix=2001:db8::/64
      Prefix=2001:db8::/0x40
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testIpv6PrefixPrefixInvalid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6Prefix]
      Prefix=2001:db8::/129
      Prefix=2001:db8::/abc
      Prefix=1.2.3.4/24
    """.trimIndent()
    assertHighlights("file.network", file, 3)
  }

  @Test
  fun testAddressScopeValid() {
    // language="unit file (systemd)"
    val file = """
      [Address]
      Scope=global
      Scope=site
      Scope=link
      Scope=host
      Scope=nowhere
      Scope=0
      Scope=200
      Scope=255
      Scope=0x10
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testAddressScopeInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Address]
      Scope=SITE
      Scope=Global
      Scope=256
      Scope=-1
      Scope=universe
    """.trimIndent()
    assertHighlights("file.network", file, 5)
  }

  @Test
  fun testBridgeFdbMaxLearnedValid() {
    // language="unit file (systemd)"
    val file = """
      [Bridge]
      FDBMaxLearned=0
      FDBMaxLearned=0x100
      FDBMaxLearned=4294967295
      FDBMaxLearned=0o400
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testBridgeFdbMaxLearnedInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Bridge]
      FDBMaxLearned=4294967296
      FDBMaxLearned=-1
      FDBMaxLearned=abc
    """.trimIndent()
    assertHighlights("file.netdev", file, 3)
  }

  @Test
  fun testCakeFirewallMarkValid() {
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      FirewallMark=1
      FirewallMark=0xff00
      FirewallMark=4294967295
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testCakeFirewallMarkInvalid() {
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      FirewallMark=0
      FirewallMark=4294967296
      FirewallMark=-1
      FirewallMark=abc
    """.trimIndent()
    assertHighlights("file.network", file, 4)
  }

  @Test
  fun testCodelPacketLimitValid() {
    // language="unit file (systemd)"
    val file = """
      [ControlledDelay]
      PacketLimit=0
      PacketLimit=4294967295
      PacketLimit=0x10
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testCodelPacketLimitInvalid() {
    // language="unit file (systemd)"
    val file = """
      [ControlledDelay]
      PacketLimit=4294967296
      PacketLimit=-1
    """.trimIndent()
    assertHighlights("file.network", file, 2)
  }

  @Test
  fun testPfifoHeadDropPacketLimitValid() {
    // language="unit file (systemd)"
    val file = """
      [PFIFOHeadDrop]
      PacketLimit=0
      PacketLimit=0x100
      PacketLimit=4294967295
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testPfifoHeadDropPacketLimitInvalid() {
    // language="unit file (systemd)"
    val file = """
      [PFIFOHeadDrop]
      PacketLimit=4294967296
      PacketLimit=-1
    """.trimIndent()
    assertHighlights("file.network", file, 2)
  }

  @Test
  fun testQfqWeightValid() {
    // language="unit file (systemd)"
    val file = """
      [QuickFairQueueingClass]
      Weight=1
      Weight=1023
      Weight=1024
      Weight=0x400
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testQfqWeightInvalid() {
    // language="unit file (systemd)"
    val file = """
      [QuickFairQueueingClass]
      Weight=0
      Weight=1025
      Weight=-1
    """.trimIndent()
    assertHighlights("file.network", file, 3)
  }

  @Test
  fun testFqQuantumBytesValid() {
    // language="unit file (systemd)"
    val file = """
      [FairQueueing]
      QuantumBytes=10.5M
      QuantumBytes=10.M
      QuantumBytes=0
      QuantumBytes=4294967295
      QuantumBytes=3G1023M
      QuantumBytes=1G 512M
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testFqQuantumBytesInvalid() {
    // language="unit file (systemd)"
    val file = """
      [FairQueueing]
      QuantumBytes=1T
      QuantumBytes=4G
      QuantumBytes=4294967296
      QuantumBytes=5k
      QuantumBytes=-1
    """.trimIndent()
    assertHighlights("file.network", file, 5)
  }

  @Test
  fun testCakeRttSecValid() {
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      RTTSec=100ms
      RTTSec=71min
      RTTSec=1us
      RTTSec=4294967295us
      RTTSec=1min 30s
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testCakeRttSecInvalid() {
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      RTTSec=2hour
      RTTSec=infinity
      RTTSec=0
      RTTSec=72min
      RTTSec=4294967296us
    """.trimIndent()
    assertHighlights("file.network", file, 5)
  }

  @Test
  fun testCanDataTimeQuantaNSecValid() {
    // language="unit file (systemd)"
    val file = """
      [CAN]
      DataTimeQuantaNSec=1
      DataTimeQuantaNSec=4294967295
      DataTimeQuantaNSec=4s
      DataTimeQuantaNSec=4.2s
      DataTimeQuantaNSec=1us
      DataTimeQuantaNSec=5ns
      DataTimeQuantaNSec=1ms 500us
      DataTimeQuantaNSec=1.5
      DataTimeQuantaNSec=12.34 .56
      DataTimeQuantaNSec=1s.5
      DataTimeQuantaNSec=1 2
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testCanDataTimeQuantaNSecInvalid() {
    // language="unit file (systemd)"
    val file = """
      [CAN]
      DataTimeQuantaNSec=infinity
      DataTimeQuantaNSec=0
      DataTimeQuantaNSec=4294967296
      DataTimeQuantaNSec=4.3s
      DataTimeQuantaNSec=5s
      DataTimeQuantaNSec=12.34.56
      DataTimeQuantaNSec=1+2
      DataTimeQuantaNSec=1.5+2
    """.trimIndent()
    assertHighlights("file.network", file, 8)
  }

  @Test
  fun testNetworkDnsValid() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=10.10.10.10#aaa.com 10.10.10.11:1111#bbb.com [1111:2222::3333]:1234#ccc.com
      DNS=1.2.3.4#a#b
      DNS=1.2.3.4:0x35
      DNS=[::1]:53
      DNS=::1#x
      DNS=1.2.3.4:+53
      DNS=::ffff:1.2.3.4
      DNS=1.2.3.4
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testNetworkDnsInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=1.2.3.4#
      DNS=1.2.3.4:0
      DNS=[::1]
      DNS=1.2.3.4:65536
      DNS=1.2.3.4 1.2.3.4:0
    """.trimIndent()
    assertHighlights("file.network", file, 5)
  }

  @Test
  fun testNetDevMacAddressValid() {
    // language="unit file (systemd)"
    val file = """
      [NetDev]
      MACAddress=none
      MACAddress=00:11:22:33:44:55
      MACAddress=0011.2233.4455
      MACAddress=00-11-22-33-44-55
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testNetDevMacAddressInvalid() {
    // language="unit file (systemd)"
    val file = """
      [NetDev]
      MACAddress=NONE
      MACAddress=None
      MACAddress=001122334455
      MACAddress=00:11:22:33:44
    """.trimIndent()
    assertHighlights("file.netdev", file, 4)
  }

  @Test
  fun testRoutePrefixPreferenceValid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6RoutePrefix]
      Preference=normal
      Preference=default
      Preference=medium
      Preference=high
      Preference=low
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testRoutePrefixPreferenceInvalid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6RoutePrefix]
      Preference=Normal
      Preference=HIGH
      Preference=highest
    """.trimIndent()
    assertHighlights("file.network", file, 3)
  }

  @Test
  fun testAdActorSystemValid() {
    // language="unit file (systemd)"
    val file = """
      [Bond]
      AdActorSystem=02:23:45:67:89:ab
      AdActorSystem=0223.4567.89ab
      AdActorSystem=2:2:3:4:5:6
      AdActorSystem=00:00:00:00:00:01
    """.trimIndent()
    assertHighlights("file.netdev", file, 0)
  }

  @Test
  fun testAdActorSystemInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Bond]
      AdActorSystem=01:23:45:67:89:ab
      AdActorSystem=00:00:00:00:00:00
      AdActorSystem=0123.4567.89ab
      AdActorSystem=123.4567.89ab
      AdActorSystem=03-00-00-00-00-00
    """.trimIndent()
    assertHighlights("file.netdev", file, 5)
  }

  @Test
  fun testRelayTargetValid() {
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      RelayTarget=1.2.3.4
      RelayTarget=10.0.0.1
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testRelayTargetInvalid() {
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      RelayTarget=0.0.0.0
      RelayTarget=::1
    """.trimIndent()
    assertHighlights("file.network", file, 2)
  }

  @Test
  fun testIpv6AcceptRaTokenValid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6AcceptRA]
      Token=static:::1
      Token=::1:0:0:0
      Token=prefixstable:2001:db8::
      Token=prefixstable:::
      Token=eui64
      Token=static:::1:0:0:0
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testIpv6AcceptRaTokenInvalid() {
    // language="unit file (systemd)"
    val file = """
      [IPv6AcceptRA]
      Token=static:::
      Token=::
      Token=static:2001:db8::
      Token=2001:db8::1:0:0:0:0
    """.trimIndent()
    assertHighlights("file.network", file, 4)
  }

  @Test
  fun testNetworkAddressValid() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      Address=2001:db8::1
      Address=0.0.0.0
      Address=::
      Address=1.2.3.4/0x10
      Address=2001:db8::1/0x40
      Address=1.2.3.4/ 8
      Address=2001:db8::1/0b 1
    """.trimIndent()
    assertHighlights("file.network", file, 0)
  }

  @Test
  fun testNetworkAddressInvalid() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      Address=1.2.3.4/33
      Address=2001:db8::1/129
      Address=2001:db8::1/-1
    """.trimIndent()
    assertHighlights("file.network", file, 3)
  }
}
