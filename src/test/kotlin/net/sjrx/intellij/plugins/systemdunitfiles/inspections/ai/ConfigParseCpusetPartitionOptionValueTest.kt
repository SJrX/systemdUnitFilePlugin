package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCpusetPartitionOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            CPUSetPartition=member
            CPUSetPartition=root
            CPUSetPartition=isolated
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
            CPUSetPartition=<error descr="Invalid value">none</error>
            CPUSetPartition=<error descr="Invalid value">Member</error>
            CPUSetPartition=<error descr="Invalid value">root isolated</error>
            CPUSetPartition=<error descr="Invalid value">123</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
