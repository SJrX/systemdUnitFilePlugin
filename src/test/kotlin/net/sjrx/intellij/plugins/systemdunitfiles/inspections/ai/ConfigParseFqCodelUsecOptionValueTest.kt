package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFqCodelUsecOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [FairQueueingControlledDelay]
      TargetSec=10ms
      IntervalSec=10ms
      CEThresholdSec=10ms
      TargetSec=200ms
      IntervalSec=200ms
      CEThresholdSec=200ms
      TargetSec=1min30s
      IntervalSec=1min30s
      CEThresholdSec=1min30s
      TargetSec=5s 500ms
      IntervalSec=5s 500ms
      CEThresholdSec=5s 500ms
      TargetSec=5s500ms
      IntervalSec=5s500ms
      CEThresholdSec=5s500ms
      TargetSec=.5s
      IntervalSec=.5s
      CEThresholdSec=.5s
      TargetSec=+3.1s
      IntervalSec=+3.1s
      CEThresholdSec=+3.1s
      TargetSec=3.1s.2
      IntervalSec=3.1s.2
      CEThresholdSec=3.1s.2
      TargetSec=3.1 .2
      IntervalSec=3.1 .2
      CEThresholdSec=3.1 .2
      TargetSec=1.5h
      IntervalSec=1.5h
      CEThresholdSec=1.5h
      TargetSec=23us
      IntervalSec=23us
      CEThresholdSec=23us
      TargetSec=23µs
      IntervalSec=23µs
      CEThresholdSec=23µs
      TargetSec=2 weeks
      IntervalSec=2 weeks
      CEThresholdSec=2 weeks
      TargetSec=100
      IntervalSec=100
      CEThresholdSec=100
      # Largest finite values parse_time() accepts (total < USEC_INFINITY, each integer <= LLONG_MAX).
      TargetSec=9223372036854775807us 9223372036854775807us
      IntervalSec=9223372036854775807us 9223372036854775807us
      CEThresholdSec=9223372036854775807us 9223372036854775807us
      TargetSec=18446744073708s
      IntervalSec=18446744073708s
      CEThresholdSec=18446744073708s
      TargetSec=infinity
      IntervalSec=infinity
      CEThresholdSec=infinity
      TargetSec=
      IntervalSec=
      CEThresholdSec=
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
      [FairQueueingControlledDelay]
      TargetSec=5.
      IntervalSec=5.
      CEThresholdSec=5.
      TargetSec=-5s
      IntervalSec=-5s
      CEThresholdSec=-5s
      TargetSec=3.s
      IntervalSec=3.s
      CEThresholdSec=3.s
      TargetSec=12.34.56
      IntervalSec=12.34.56
      CEThresholdSec=12.34.56
      TargetSec=5mins
      IntervalSec=5mins
      CEThresholdSec=5mins
      TargetSec=10 foo
      IntervalSec=10 foo
      CEThresholdSec=10 foo
      TargetSec=infinity 5s
      IntervalSec=infinity 5s
      CEThresholdSec=infinity 5s
      TargetSec=Infinity
      IntervalSec=Infinity
      CEThresholdSec=Infinity
      # parse_time() ERANGE: integer above LLONG_MAX, or total reaching USEC_INFINITY.
      TargetSec=9223372036854775808us
      IntervalSec=9223372036854775808us
      CEThresholdSec=9223372036854775808us
      TargetSec=18446744073709551615us
      IntervalSec=18446744073709551615us
      CEThresholdSec=18446744073709551615us
      TargetSec=9223372036854775807us 9223372036854775807us 1us
      IntervalSec=9223372036854775807us 9223372036854775807us 1us
      CEThresholdSec=9223372036854775807us 9223372036854775807us 1us
      TargetSec=18446744073709s
      IntervalSec=18446744073709s
      CEThresholdSec=18446744073709s
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(36, highlights)
  }

}
