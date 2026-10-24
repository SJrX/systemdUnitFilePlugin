package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVlanQosMapsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [VLAN]
            EgressQOSMaps=21-7 45-5
            IngressQOSMaps=1-1
            EgressQOSMaps=5
            IngressQOSMaps=0x10-0x3 "7-2" '8-1'
            EgressQOSMaps=4294967295-7
            IngressQOSMaps=1-7  2-6
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
            [VLAN]
            EgressQOSMaps=21:7
            IngressQOSMaps=1-
            EgressQOSMaps=-5
            IngressQOSMaps=1-2-3
            EgressQOSMaps=4294967296-1
            IngressQOSMaps=a-b
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
