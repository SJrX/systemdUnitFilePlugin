package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardPrivateKeyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuard]
            PrivateKey=EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong=
            PrivateKey=Eor9QBsIoUG6C3ZKsKdqm4Vkt0n2N7qpSh2LzphQeho=
            PrivateKey=@network.wireguard.private
            PrivateKey=EEGlnEPYJV__kbvvIqxKkQwOiS-UENyPncC4bF46ong=
            PrivateKey=EEGlnEPYJV//kbvvIqxK kQwOiS+UENyPncC4bF46ong=
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
            [WireGuard]
            PrivateKey=EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong
            PrivateKey=EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46onh=
            PrivateKey=short=
            PrivateKey=@bad/name
            PrivateKey=EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong==
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
