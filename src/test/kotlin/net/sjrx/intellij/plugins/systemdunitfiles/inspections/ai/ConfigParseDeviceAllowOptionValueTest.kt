package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDeviceAllowOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            DeviceAllow=/dev/sda1
            DeviceAllow=/dev/sda1 r
            DeviceAllow=/dev/sda1 rw
            DeviceAllow=/dev/sda1 rwm
            DeviceAllow=/dev/dri/card0 rw
            DeviceAllow=block-loop
            DeviceAllow=block-loop rw
            DeviceAllow=char-rtc r
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            DeviceAllow=relative-path
            DeviceAllow=/dev/sda1 x
            DeviceAllow=/dev/sda1 rwx
            DeviceAllow=block-loop xyz
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(4, highlights)
    }
}
