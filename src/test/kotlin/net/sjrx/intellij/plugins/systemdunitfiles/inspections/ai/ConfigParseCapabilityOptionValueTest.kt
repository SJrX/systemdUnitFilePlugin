package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCapabilityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Values from systemd's TEST-13-NSPAWN plus the C edges: `all`, any-case names (gperf --ignore-case),
        // and numeric capabilities read by safe_atoi (base 0: hex, octal, 0b/0o, sign) up to CAP_LIMIT (62).
        // language="unit file (systemd)"
        val file = """
            [Exec]
            Capability=CAP_BLOCK_SUSPEND CAP_BPF CAP_CHOWN
            DropCapability=CAP_AUDIT_CONTROL CAP_AUDIT_WRITE
            AmbientCapability=CAP_BPF CAP_CHOWN
            Capability=all
            DropCapability=all
            Capability=cap_net_admin Cap_Sys_Admin
            Capability=12 0x15 062 0 +3 0b101 0o7
            DropCapability=CAP_NET_ADMIN     CAP_SYS_TIME
            Capability=62
            AmbientCapability=CAP_CHECKPOINT_RESTORE CAP_WAKE_ALARM
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown name; comma is not a separator; above CAP_LIMIT; `all` is case-sensitive; negative; one bad word in a list.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            Capability=CAP_BOGUS
            DropCapability=CAP_NET_ADMIN,CAP_SYS_ADMIN
            AmbientCapability=63
            Capability=ALL
            Capability=-1
            DropCapability=CAP_NET_ADMIN CAP_NOPE
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
