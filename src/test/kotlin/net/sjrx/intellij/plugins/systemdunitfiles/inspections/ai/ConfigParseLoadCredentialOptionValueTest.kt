package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLoadCredentialOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            LoadCredential=mycred:/etc/mycred
            LoadCredentialEncrypted=other:/run/creds/other
            LoadCredential=justid
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // A '/' in the id and a ".." id are rejected by credential_name_valid.
        // language=unit file (systemd)
        val file = """
            [Service]
            LoadCredential=bad/id:/secret
            LoadCredentialEncrypted=..:/secret
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
