package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMacsecKeyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30c16
            Key=0123456789ABCDEF0123456789ABCDEF
            Key=ffffffffffffffffffffffffffffffff
            Key=00000000000000000000000000000000
            Key=aAbBcCdDeEfF00112233445566778899
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
            [MACsecTransmitAssociation]
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30c1
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30c167
            Key=notahexstring123456789012345678
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30c1g
            Key=short
            Key=toolongkeytoolongkeytoolongkeytoolongkey
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testInvalidTooShort() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            Key=abc123
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidTooLong() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30c16ff
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidNonHexCharacters() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            Key=dffafc8d7b9a43d5b9a3dfbbf6a30cXX
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
