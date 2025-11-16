package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMacvlanBroadcastQueueSizeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACVLAN]
            BroadcastMulticastQueueLength=0
            BroadcastMulticastQueueLength=1
            BroadcastMulticastQueueLength=100
            BroadcastMulticastQueueLength=1000
            BroadcastMulticastQueueLength=4294967294
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
            BroadcastMulticastQueueLength=-1
            BroadcastMulticastQueueLength=4294967295
            BroadcastMulticastQueueLength=5000000000
            BroadcastMulticastQueueLength=abc
            BroadcastMulticastQueueLength=12.5
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
