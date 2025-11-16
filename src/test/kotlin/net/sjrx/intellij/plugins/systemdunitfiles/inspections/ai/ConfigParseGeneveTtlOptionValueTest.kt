package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGeneveTtlOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [GENEVE]
            TTL=inherit
            TTL=0
            TTL=1
            TTL=64
            TTL=128
            TTL=255
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
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
            [GENEVE]
            TTL=<error descr="Invalid option value">Inherit</error>
            TTL=<error descr="Invalid option value">INHERIT</error>
            TTL=<error descr="Invalid option value">256</error>
            TTL=<error descr="Invalid option value">-1</error>
            TTL=<error descr="Invalid option value">1000</error>
            TTL=<error descr="Invalid option value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }
}
