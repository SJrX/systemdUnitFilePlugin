package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] TCPRetransmissionTimeoutSec=. */
class ConfigParseRouteMetricTcpRtoOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            TCPRetransmissionTimeoutSec=300s
            TCPRetransmissionTimeoutSec=200ms
            TCPRetransmissionTimeoutSec=1
            TCPRetransmissionTimeoutSec=1us
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Route]
            TCPRetransmissionTimeoutSec=0
            TCPRetransmissionTimeoutSec=infinity
            TCPRetransmissionTimeoutSec=50d
            TCPRetransmissionTimeoutSec=fast
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
