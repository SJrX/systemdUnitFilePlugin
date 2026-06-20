package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIoDeviceWeightOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            IODeviceWeight=/dev/sda 100
            IODeviceWeight=/dev/sdb 10000
            IODeviceWeight=/dev/sdc 1
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Weight must be in [1, 10000]; a path with no weight is also invalid.
        // language=unit file (systemd)
        val file = """
            [Service]
            IODeviceWeight=/dev/sda 0
            IODeviceWeight=/dev/sda 20000
            IODeviceWeight=/dev/sda
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
