package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpStaticLeaseAddressOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidIPv4Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServerStaticLease]
            Address=192.168.1.100
            Address=10.0.0.1
            Address=172.16.0.254
            Address=1.2.3.4
            Address=255.255.255.255
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [DHCPServerStaticLease]
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">256.1.2.3</error>
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">192.168.1</error>
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">192.168.1.1.1</error>
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">not-an-ip</error>
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">2001:db8::1</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testInvalidSpecialValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServerStaticLease]
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">any</error>
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">localhost</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }

    @Test
    fun testInvalidWithPrefixLength() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServerStaticLease]
            Address=<error descr="Option 'Address' expects a value matching the grammar: IPV4_ADDR">192.168.1.1/24</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
