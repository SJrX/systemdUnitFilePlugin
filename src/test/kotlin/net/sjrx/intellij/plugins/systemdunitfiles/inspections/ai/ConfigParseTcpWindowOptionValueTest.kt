package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTcpWindowOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            InitialAdvertisedReceiveWindow=1
            InitialAdvertisedReceiveWindow=500
            InitialAdvertisedReceiveWindow=1023
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
            [DHCPv4]
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">0</error>
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">1024</error>
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">2000</error>
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">-1</error>
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">abc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
    
    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            InitialAdvertisedReceiveWindow=1
            InitialAdvertisedReceiveWindow=1023
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">0</error>
            InitialAdvertisedReceiveWindow=<error descr="Invalid value supplied for 'InitialAdvertisedReceiveWindow'">1024</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
