package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTimerMonotonicOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Spellings from systemd's TEST(parse_sec) (src/test/test-time-util.c) and the Debian corpus,
    // spread across all five keys that share config_parse_timer's parse_sec branch.
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnActiveSec=5s
      OnActiveSec=5s500ms
      OnActiveSec= 5s 500ms
      OnActiveSec=5.5s 0.5ms
      OnBootSec=.22s
      OnBootSec=.50y
      OnBootSec=2.5
      OnBootSec=.7
      OnBootSec=23us
      OnBootSec=23μs
      OnBootSec=23µs
      OnStartupSec=infinity
      OnStartupSec=+3.1s
      OnStartupSec=3.1s.2
      OnStartupSec=3.1 .2
      OnStartupSec=3.1 sec .2 sec
      OnUnitActiveSec=1h30min
      OnUnitActiveSec=5min 15s 100ms
      OnUnitActiveSec=300ms20s 5day
      OnUnitActiveSec=2 minutes
      OnUnitActiveSec=1 min
      OnUnitActiveSec=12 M
      OnUnitActiveSec=1y2M3w4d5h6m7s
      OnUnitInactiveSec=23434
      OnUnitInactiveSec=0
      OnUnitInactiveSec=15m
      OnUnitInactiveSec=1 seconds
      OnUnitInactiveSec=7 month
      OnUnitInactiveSec=7hr 2hr
      OnUnitInactiveSec=%i
      OnUnitInactiveSec=%im
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testEmptyValueResets() {
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnBootSec=
      OnUnitActiveSec=
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // Rejections from TEST(parse_sec), plus mis-pluralised units and an unknown specifier.
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnActiveSec=xyz
      OnActiveSec=5.
      OnActiveSec=.s
      OnBootSec=-5s
      OnBootSec=0.-0s
      OnBootSec=infinity .7
      OnStartupSec=.3 infinity
      OnStartupSec=3.+1s
      OnStartupSec=3. 1s
      OnUnitActiveSec=3.s
      OnUnitActiveSec=12.34.56
      OnUnitActiveSec=12..34
      OnUnitActiveSec=5mins
      OnUnitInactiveSec=2hs
      OnUnitInactiveSec=12foo 1 bar
      OnUnitInactiveSec=min 1 sec
      OnUnitInactiveSec=%e
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(17, highlights)
  }
}
