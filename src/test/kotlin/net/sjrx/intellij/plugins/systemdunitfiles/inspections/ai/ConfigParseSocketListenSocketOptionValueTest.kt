package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseSocketListenSocketOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
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
    // `0` and `65536` are ports out of range for ListenStream= (they would be netlink family numbers,
    // which only ListenNetlink= takes); all 23 confirmed rejected by sd-check at 0f0682b773.
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
    // The last three are refused by config_parse_socket_listen ("Address family not supported"), checked
    // with sd-check at 0f0682b773. The inspection dispatches on the key; the key-agnostic union grammar
    // (`combinator`) accepts them, which is why a harness that only reads `combinator` sees them as valid.
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
  fun testSafeAtoSpellings() {
    // The numbers go through the shared SafeAtoTerminal. Every value here was checked one per file with
    // sd-check at 0f0682b773: the first 15 are accepted, the last 5 rejected. Ports are parse_ip_port
    // (SAFE_ATO_REFUSE_LEADING_WHITESPACE, then 0 refused); vsock CID/port and the netlink group are plain
    // safe_atou() on the rest of the text; the netlink family is the first " \t\n\r"-separated word.
    // The plugin's lexer does not take a raw \v, so those spellings are in testVerticalTabSpellings.
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenStream=0b+1
      ListenStream=0b 1
      ListenStream=0b +1
      ListenStream=0o 7
      ListenStream=1.2.3.4:0b 1
      ListenStream=[::1]:0b+1
      ListenStream=1.2.3.4:0b+1%%lo
      ListenStream=vsock:2: 80
      ListenStream=vsock: 2:80
      ListenStream=vsock:2:0b 1
      ListenStream=vsock:0b+1:80
      ListenNetlink=route 0b 1
      ListenNetlink=route 0b+1
      ListenNetlink=0b+1 5
      ListenNetlink=route  5
      ListenStream=0b-0
      ListenStream=1.2.3.4: 80
      ListenStream=+0b1
      ListenStream=vsock:2:0b -1
      ListenNetlink=route 0b -1
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }

  @Test
  fun testVerticalTabSpellings() {
    // strtoul skips a leading \v, but neither SAFE_ATO_REFUSE_LEADING_WHITESPACE nor extract_first_word
    // treat it as whitespace. sd-check at 0f0682b773, one value per file: the first three are accepted,
    // `route\v5` is rejected (one word, not a netlink family).
    val validator = ConfigParseSocketListenSocketOptionValue()
    val stream = validator.combinatorForKey("ListenStream")
    val netlink = validator.combinatorForKey("ListenNetlink")
    for (engine in listOf(GrammarEngineVerdict::classic, GrammarEngineVerdict::parseEngine)) {
      assertEquals(GrammarEngineVerdict.VALID, engine(stream, "\u000B80"))
      assertEquals(GrammarEngineVerdict.VALID, engine(stream, "1.2.3.4:\u000B80"))
      assertEquals(GrammarEngineVerdict.VALID, engine(netlink, "route \u000B5"))
      assertEquals(GrammarEngineVerdict.Kind.SYNTAX, engine(netlink, "route\u000B5").kind)
    }
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
