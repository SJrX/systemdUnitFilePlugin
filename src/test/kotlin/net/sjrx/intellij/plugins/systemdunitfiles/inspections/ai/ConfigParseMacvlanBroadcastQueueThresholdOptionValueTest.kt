package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMacvlanBroadcastQueueThresholdOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACVLAN]
            BroadcastQueueThreshold=no
            BroadcastQueueThreshold=0
            BroadcastQueueThreshold=1
            BroadcastQueueThreshold=100
            BroadcastQueueThreshold=2147483647
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACVLAN]
            BroadcastQueueThreshold=-1
            BroadcastQueueThreshold=2147483648
            BroadcastQueueThreshold=yes
            BroadcastQueueThreshold=invalid
            BroadcastQueueThreshold=3000000000
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
