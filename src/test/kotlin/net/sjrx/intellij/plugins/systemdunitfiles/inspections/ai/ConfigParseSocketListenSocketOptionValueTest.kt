package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketListenSocketOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidStreamAndDatagramAddresses() {
    // From TEST(socket_address_parse) (src/test/test-socket-netlink.c) and the Debian corpus.
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenStream=65535
      ListenStream=8888
      ListenStream=0x50
      ListenStream=[2001:0db8:0000:85a3:0000:0000:ac1f:8001]:8888
      ListenStream=[::1]:8888
      ListenStream=[::]:111
      ListenStream=[::1]:1234%%lo
      ListenStream=[fe80::1]:80%%eth0
      ListenStream=[fe80::1]:80%%2
      ListenStream=192.168.1.254:8888
      ListenStream=0.0.0.0:80
      ListenStream=127.0.0.1:1234%%lo
      ListenStream=/foo/bar
      ListenStream=/run/foo bar.sock
      ListenStream=@abstract
      ListenStream=@/org/freedesktop/systemd1/logger
      ListenStream=%t/pulse/native
      ListenStream=%t/foot-%i.sock
      ListenStream=%C/sigstore/gitsign/cache.sock
      ListenStream=%i
      ListenStream=vsock:2:1234
      ListenStream=vsock::1234
      ListenStream=vsock:host:22
      ListenStream=vsock:-1:22
      ListenStream=vsock-stream:any:22
      ListenStream=vsock:-0:5
      ListenDatagram=69
      ListenDatagram=[::]:53
      ListenDatagram=127.0.0.1:1153
      ListenDatagram=@fcm_clif
      ListenDatagram=/path/to/socket
      ListenDatagram=vsock-dgram:3:4000
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidStreamAndDatagramAddresses() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenStream=junk
      ListenStream=192.168.1.1
      ListenStream=.168.1.1
      ListenStream=989.168.1.1:80
      ListenStream=192.168.1.1:65536
      ListenStream=192.168.1.1:0
      ListenStream=0
      ListenStream=65536
      ListenStream=[::1]
      ListenStream=[::1]8888
      ListenStream=::1
      ListenStream=[::1]:0
      ListenStream=[a:b:1]:8888
      ListenStream=[::1]%%lo:1234
      ListenStream=[::1]:1234%%lo%%lo
      ListenStream=/
      ListenStream=@
      ListenStream=vsock:2:1234x
      ListenStream=vsock:2x:1234
      ListenStream=vsock:2
      ListenStream=[fe80::1]:80%eth0
      ListenDatagram=80/udp
      ListenDatagram=localhost:53
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(23, highlights)
  }

  @Test
  fun testUnixSocketPathLength() {
    // sockaddr_un_set_path: 107 bytes is the longest path that fits with its NUL terminator. The limit
    // applies after patch_var_run() has rewritten /var/run/ to /run/, so /var/run/ paths get 4 more.
    val ok = "/" + "a".repeat(106)
    val tooLong = "/" + "a".repeat(107)
    val file = """
      [Socket]
      ListenStream=$ok
      ListenStream=@${"a".repeat(106)}
      ListenStream=$tooLong
      ListenStream=/var/run/${"a".repeat(102)}
      ListenStream=/var/run/${"a".repeat(103)}
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(2, highlights)
  }

  @Test
  fun testSequentialPacketIsUnixOnly() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenSequentialPacket=/run/foo.sock
      ListenSequentialPacket=@namespace
      ListenSequentialPacket=%t/foo.sock
      ListenSequentialPacket=1.2.3.4:1234
      ListenSequentialPacket=1234
      ListenSequentialPacket=vsock-seqpacket:2:1234
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(3, highlights)
  }

  @Test
  fun testNetlink() {
    // From TEST(socket_address_parse_netlink) and the corpus.
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenNetlink=route
      ListenNetlink=route 10
      ListenNetlink=kobject-uevent 1
      ListenNetlink=kobject-uevent	10
      ListenNetlink=audit 1
      ListenNetlink=route 1361
      ListenNetlink=rdma 4
      ListenNetlink=16
      ListenNetlink=generic 0x10
      ListenNetlink=route -0
      ListenNetlink=junk
      ListenNetlink=route ten
      ListenNetlink=route 1 2
      ListenNetlink=/run/foo.sock
      ListenNetlink=route -1
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }

  @Test
  fun testEmptyValueResets() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenStream=
      ListenNetlink=
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }
}
