package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePrivateUsersOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Exec]
            PrivateUsers=yes
            PrivateUsers=no
            PrivateUsers=true
            PrivateUsers=false
            PrivateUsers=pick
            PrivateUsers=identity
            PrivateUsers=1000
            PrivateUsers=0
            PrivateUsers=1000:65536
            PrivateUsers=100000:1000000
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Exec]
            PrivateUsers=<error descr="Invalid value">maybe</error>
            PrivateUsers=<error descr="Invalid value">Pick</error>
            PrivateUsers=<error descr="Invalid value">-1</error>
            PrivateUsers=<error descr="Invalid value">1000:</error>
            PrivateUsers=<error descr="Invalid value">:1000</error>
            PrivateUsers=<error descr="Invalid value">1000 65536</error>
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
