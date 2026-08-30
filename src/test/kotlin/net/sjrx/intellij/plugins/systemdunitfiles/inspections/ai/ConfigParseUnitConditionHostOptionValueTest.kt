package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionHostOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // A host condition is a hostname, a machine/boot/product ID, or an fnmatch glob: any non-empty
        // string is legitimate, so none of these are flagged.
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionHost=myhostname
            ConditionHost=web-*
            ConditionHost=4db8f6a1e2c04b3d9a7e5f10c3b2a1d0
            AssertHost=*.example.com
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
            ConditionHost=!myhost
            ConditionHost=|myhost
            ConditionHost=|!myhost
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
