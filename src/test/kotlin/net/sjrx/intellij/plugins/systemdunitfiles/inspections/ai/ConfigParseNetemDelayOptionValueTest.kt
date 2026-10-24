package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNetemDelayOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [NetworkEmulator]
      DelaySec=10ms
      DelayJitterSec=10ms
      DelaySec=200ms
      DelayJitterSec=200ms
      DelaySec=1min30s
      DelayJitterSec=1min30s
      DelaySec=5s 500ms
      DelayJitterSec=5s 500ms
      DelaySec=5s500ms
      DelayJitterSec=5s500ms
      DelaySec=.5s
      DelayJitterSec=.5s
      DelaySec=+3.1s
      DelayJitterSec=+3.1s
      DelaySec=3.1s.2
      DelayJitterSec=3.1s.2
      DelaySec=3.1 .2
      DelayJitterSec=3.1 .2
      DelaySec=1.5h
      DelayJitterSec=1.5h
      DelaySec=23us
      DelayJitterSec=23us
      DelaySec=23µs
      DelayJitterSec=23µs
      DelaySec=2 weeks
      DelayJitterSec=2 weeks
      DelaySec=100
      DelayJitterSec=100
      DelaySec=infinity
      DelayJitterSec=infinity
      DelaySec=
      DelayJitterSec=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [NetworkEmulator]
      DelaySec=5.
      DelayJitterSec=5.
      DelaySec=-5s
      DelayJitterSec=-5s
      DelaySec=3.s
      DelayJitterSec=3.s
      DelaySec=12.34.56
      DelayJitterSec=12.34.56
      DelaySec=5mins
      DelayJitterSec=5mins
      DelaySec=10 foo
      DelayJitterSec=10 foo
      DelaySec=infinity 5s
      DelayJitterSec=infinity 5s
      DelaySec=Infinity
      DelayJitterSec=Infinity
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(16, highlights)
  }

}
