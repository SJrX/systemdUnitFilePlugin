package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSrIovBooleanOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [SR-IOV]
            MACSpoofCheck=yes
            QueryReceiveSideScaling=no
            Trust=true
            MACSpoofCheck=false
            QueryReceiveSideScaling=on
            Trust=off
            MACSpoofCheck=1
            QueryReceiveSideScaling=0
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [SR-IOV]
            MACSpoofCheck=<error descr="Invalid value">maybe</error>
            Trust=<error descr="Invalid value">2</error>
            QueryReceiveSideScaling=<error descr="Invalid value">enabled</error>
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
