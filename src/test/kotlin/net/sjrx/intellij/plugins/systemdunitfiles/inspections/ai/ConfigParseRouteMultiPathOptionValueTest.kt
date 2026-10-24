package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] MultiPathRoute=. */
class ConfigParseRouteMultiPathOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            MultiPathRoute=10.0.0.1@eth0 20
            MultiPathRoute=192.168.1.1 50
            MultiPathRoute=@wg0 15
            MultiPathRoute=2001:db8::1@eth0
            MultiPathRoute=2001:1234:5:9fff:ff:ff:ff:ff@dummy98 5
            MultiPathRoute=@1 15
            MultiPathRoute=@999999 10
            MultiPathRoute=10.0.0.1@5
            MultiPathRoute=fe80::1
            MultiPathRoute=@eth0 256
            MultiPathRoute=@eth0 1
            MultiPathRoute=@eth0 0b 1
            MultiPathRoute=@+0b1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects the weight +0b1 and the empty device in `@ 5`; it accepts the
    // weight 0b 1 and the device name +0b1 (parse_ifindex refuses it, ifname_valid accepts it).
    // language="unit file (systemd)"
    val file = """
            [Route]
            MultiPathRoute=@eth0 0
            MultiPathRoute=@eth0 257
            MultiPathRoute=@
            MultiPathRoute=@0
            MultiPathRoute=10.0.0.1/24@eth0
            MultiPathRoute=@eth0 10 20
            MultiPathRoute=eth0
            MultiPathRoute=@eth0 +0b1
            MultiPathRoute=@ 5
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(9, myFixture.doHighlighting())
  }
}
