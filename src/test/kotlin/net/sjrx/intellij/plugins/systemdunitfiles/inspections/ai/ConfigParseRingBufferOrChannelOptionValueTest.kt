package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRingBufferOrChannelOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            RxMiniBufferSize=max
            RxMiniBufferSize=1
            RxMiniBufferSize=100
            RxMiniBufferSize=4294967295
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
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
            [Link]
            RxMiniBufferSize=<error descr="Invalid value">0</error>
            RxMiniBufferSize=<error descr="Invalid value">-1</error>
            RxMiniBufferSize=<error descr="Invalid value">4294967296</error>
            RxMiniBufferSize=<error descr="Invalid value">Max</error>
            RxMiniBufferSize=<error descr="Invalid value">MAX</error>
            RxMiniBufferSize=<error descr="Invalid value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }
}
