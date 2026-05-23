package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSyscallArchsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallArchitectures=native
            SystemCallArchitectures=x86-64
            SystemCallArchitectures=native x86 x86-64
            SystemCallArchitectures=arm arm64
            SystemCallArchitectures=mips64-le-n32
            SystemCallArchitectures=ppc64-le riscv64 s390x
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
            SystemCallArchitectures=<error descr="Invalid value">amd64</error>
            SystemCallArchitectures=<error descr="Invalid value">x86_64</error>
            SystemCallArchitectures=<error descr="Invalid value">aarch64</error>
            SystemCallArchitectures=<error descr="Invalid value">native,x86</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
