package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseId128OptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN and src/libsystemd/sd-id128/test-id128.c.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            MachineID=f28f129b51874b1280a89421ec4b4ad4
            MachineID=deadbeef-dead-dead-beef-111111111111
            MachineID=F28F129B51874B1280A89421EC4B4AD4
            MachineID=urn:uuid:01020304-0506-0708-090a-0b0c0d0e0f10
            MachineID=URN:UUID:01020304-0506-0708-090a-0b0c0d0e0f10
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // All-zero IDs, urn: without dashes, misplaced/missing dash, too long, too short (test-id128.c), garbage.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            MachineID=00000000000000000000000000000000
            MachineID=00000000-0000-0000-0000-000000000000
            MachineID=urn:uuid:0102030405060708090a0b0c0d0e0f10
            MachineID=01020304-0506-0708-090a0b0c0d0e0f10
            MachineID=01020304-0506-0708-090a-0b0c0d0e0f101
            MachineID=f28f129b51874b1280a89421ec4b4ad
            MachineID=xyz
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
