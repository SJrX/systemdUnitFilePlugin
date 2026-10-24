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
    fun testUnknownSyscallNamesAreAcceptedDeliberately() {
        // systemd warns "System call invalid is not known, ignoring." for these (sd-check at 0f0682b773),
        // but whether a name resolves depends on libseccomp and the native architecture, so only the
        // shape of a syscall name is checked. Documented leniency.
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallFilter=invalid
            SystemCallFilter=madeupcall
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
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
            SystemCallFilter=~uname:0777 uname:0b11 uname:0o17 uname:+5 uname:-0 uname:+0x10 uname:0b+1 uname:0o+7 uname:0B-0
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
        // "@system-service ~@mount": nspawn only honours `~` as the first character, so the second word goes
        // into the allow list verbatim as "~@mount". sd-check is silent (nspawn's parser stores words
        // unchecked), but at container start seccomp_add_syscall_filter_item() can never resolve it and
        // drops it with only a debug message, so the entry is a guaranteed no-op. Flagged on purpose.
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
            SystemCallFilter=~uname:0b-1
            SystemCallFilter=~uname:+0b1
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(14, highlights)
    }
}
