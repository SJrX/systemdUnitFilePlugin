package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpTosOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidNamedValues() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=low-delay
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidNamedValueThroughput() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=throughput
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidNamedValueReliability() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=reliability
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidNamedValueLowCost() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=low-cost
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidIntegerLowerBound() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=0
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidIntegerUpperBound() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=255
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidIntegerMidRange() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=42
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidWord() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=invalid_value
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(1, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidIntegerOutOfRange() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=300
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(1, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidIntegerNegative() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            IPTOS=-1
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(1, myFixture.doHighlighting())
    }
}
