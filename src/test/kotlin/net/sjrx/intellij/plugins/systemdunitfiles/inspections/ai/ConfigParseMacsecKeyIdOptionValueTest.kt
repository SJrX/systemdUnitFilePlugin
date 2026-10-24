package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMacsecKeyIdOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            KeyId=01
            KeyId=0203
            KeyId=0203040506070809
            KeyId=ABCDEF
            KeyId=00112233445566778899aabbccddeeff
            KeyId=01 02 03
            [MACsecReceiveAssociation]
            KeyId=020304
            KeyId=ff
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [MACsecTransmitAssociation]
            KeyId=1
            KeyId=255
            KeyId=0
            KeyId=0x01
            KeyId=00112233445566778899aabbccddeeff00
            [MACsecReceiveAssociation]
            KeyId=zz
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
