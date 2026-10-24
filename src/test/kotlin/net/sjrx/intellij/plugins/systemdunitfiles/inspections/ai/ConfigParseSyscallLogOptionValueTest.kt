package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSyscallLogOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // "~ some_name  another_name" (kf6 autotest): systemd warns that both names are unknown, but syscall
        // names are resolved by libseccomp per architecture, so only their shape is checked (deliberate).
        // "@system-service", "read write open" and "ptrace,reboot swapoff" are from systemd's
        // test/units/TEST-*.sh and the man page.
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallLog=@system-service
            SystemCallLog=read write open
            SystemCallLog=ptrace,reboot swapoff
            SystemCallLog=~@sync @mount
            SystemCallLog=~ read, write
            SystemCallLog=~
            SystemCallLog=_llseek,
            SystemCallLog=~ some_name  another_name
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidAcrossUnitTypes() {
        // language="unit file (systemd)"
        val socket = """
            [Socket]
            SystemCallLog=@network-io
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            SystemCallLog=~@mount
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            SystemCallLog=swapon swapoff
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallLog=@not-a-group
            SystemCallLog=uname:EPERM
            SystemCallLog=~uname:EPERM
            SystemCallLog=read Write
            SystemCallLog=~~read
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(5, myFixture.doHighlighting())
    }
}
