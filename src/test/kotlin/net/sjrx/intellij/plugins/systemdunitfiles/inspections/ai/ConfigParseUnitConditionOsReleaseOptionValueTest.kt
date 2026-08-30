package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionOsReleaseOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionOSRelease=ID=fedora
            ConditionOSRelease=ID=some-id
            ConditionOSRelease=VERSION_ID>=30
            ConditionOSRelease=ID=fedora VARIANT_ID=workstation
            ConditionOSRelease=NAME${'$'}=Fedora*
            ConditionOSRelease="PRETTY_NAME=Some Thing"
            AssertOSRelease=ID!=debian
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidWithMarkers() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionOSRelease=!ID=fedora
            ConditionOSRelease=|ID=fedora
            ConditionOSRelease=|!VERSION_ID>=30
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionOSRelease=fedora
            ConditionOSRelease=123=x
            ConditionOSRelease=ID = fedora
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
