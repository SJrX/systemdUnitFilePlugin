package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSyscallFilterOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidAllowLists() {
        // Real values from the Debian corpus and systemd's own units/tests.
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallFilter=@system-service
            SystemCallFilter=@system-service @mount
            SystemCallFilter=@system-service @module @raw-io bpf
            SystemCallFilter=@default @file-system @basic-io @system-service
            SystemCallFilter=read write open execve
            SystemCallFilter=ptrace,reboot swapoff
            SystemCallFilter=read, write
            SystemCallFilter=_llseek _newselect epoll_wait s390_pci_mmio_read
            SystemCallFilter=@known
            SystemCallFilter=,read,
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidDenyListsAndErrno() {
        // ~uname:EILSEQ, :kill, :255 come from systemd's test units; :4095/:0 from test-seccomp.c.
        // language="unit file (systemd)"
        val file = """
            [Socket]
            SystemCallFilter=~@mount
            SystemCallFilter=~ @clock @debug @module @mount @raw-io @reboot @swap @cpu-emulation @obsolete
            SystemCallFilter=~@privileged @resources
            SystemCallFilter=~uname:EILSEQ
            SystemCallFilter=~uname:ENOENT uname:EILSEQ
            SystemCallFilter=~uname:kill
            SystemCallFilter=~uname:255
            SystemCallFilter=~@sync:4095
            SystemCallFilter=~uname:0
            SystemCallFilter=~uname:0x1F
            SystemCallFilter=~uname:eperm
            SystemCallFilter=~uname:0777 uname:0b11 uname:0o17 uname:+5 uname:-0 uname:+0x10
            SystemCallFilter=~@sync:EPERM,@mount:kill uname:E2BIG
            SystemCallFilter=~
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidOtherUnitTypesAndNspawn() {
        // language="unit file (systemd)"
        val mount = """
            [Mount]
            SystemCallFilter=@system-service
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            SystemCallFilter=~@swap
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val nspawn = """
            [Exec]
            SystemCallFilter=@system-service ~@mount
            SystemCallFilter=~@clock add_key
        """.trimIndent()
        setupFileInEditor("file.nspawn", nspawn)
        // "@system-service ~@mount": the second word would be the (unknown) name "~@mount" — flagged.
        assertSize(1, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallFilter=@bogus-group
            SystemCallFilter=~write open execve DONOTEXIST
            SystemCallFilter=uname:EILSEQ
            SystemCallFilter=~uname:
            SystemCallFilter=~:255
            SystemCallFilter=~uname:4096
            SystemCallFilter=~uname:-3
            SystemCallFilter=~uname:12.3
            SystemCallFilter=~uname:EINVALaaa
            SystemCallFilter=~uname:255:EILSEQ
            SystemCallFilter=~uname:KILL
            SystemCallFilter=@service
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(12, highlights)
    }
}
