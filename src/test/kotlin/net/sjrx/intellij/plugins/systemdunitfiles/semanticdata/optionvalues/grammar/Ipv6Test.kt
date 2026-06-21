package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for RFC 5952 IPv6 canonicalization. */
class Ipv6Test {

  @Test
  fun testCanonicalizes() {
    assertEquals("2001:db8::1", canonicalizeIpv6("2001:0db8:0000:0000:0000:0000:0000:0001"))
    assertEquals("2001:db8::1", canonicalizeIpv6("2001:DB8::1"))           // lowercase
    assertEquals("fe80::1", canonicalizeIpv6("FE80:0:0:0:0:0:0:1"))         // compress + lowercase
    assertEquals("::", canonicalizeIpv6("0:0:0:0:0:0:0:0"))                 // all zeros
    assertEquals("::1", canonicalizeIpv6("0:0:0:0:0:0:0:1"))
    assertEquals("1:2:3:4:5:6:7:8", canonicalizeIpv6("1:2:3:4:5:6:7:8"))    // nothing to compress
  }

  @Test
  fun testAlreadyCanonicalIsIdempotent() {
    for (a in listOf("2001:db8::1", "::1", "::", "fe80::1", "1:2:3:4:5:6:7:8")) {
      assertEquals(a, canonicalizeIpv6(a))
    }
  }

  @Test
  fun testLongestRunIsCompressed() {
    // Two zero runs: compress the longer one (the second).
    assertEquals("1:0:0:1::1", canonicalizeIpv6("1:0:0:1:0:0:0:1"))
    // A single zero group must NOT be compressed.
    assertEquals("1:2:3:4:5:6:0:8", canonicalizeIpv6("1:2:3:4:5:6:0:8"))
  }

  @Test
  fun testTieCompressesLeftmostRun() {
    assertEquals("1::1:0:0:1:1", canonicalizeIpv6("1:0:0:1:0:0:1:1"))
  }

  @Test
  fun testNonIpv6ReturnsNull() {
    assertNull(canonicalizeIpv6("1.2.3.4"))           // IPv4
    assertNull(canonicalizeIpv6("::ffff:1.2.3.4"))    // embedded IPv4 (out of scope for now)
    assertNull(canonicalizeIpv6("8080"))              // integer
    assertNull(canonicalizeIpv6("hello"))
    assertNull(canonicalizeIpv6(""))
  }
}
