package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpSocketPriorityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            SocketPriority=0
            SocketPriority=6
            SocketPriority=-1
            SocketPriority=2147483647
            SocketPriority=-2147483648
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [DHCPv4]
            SocketPriority=<error descr="Invalid value supplied for option.">forever</error>
            SocketPriority=<error descr="Invalid value supplied for option.">infinity</error>
            SocketPriority=<error descr="Invalid value supplied for option.">abc</error>
            SocketPriority=<error descr="Invalid value supplied for option.">1.5</error>
            SocketPriority=<error descr="Invalid value supplied for option.">2147483648</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
