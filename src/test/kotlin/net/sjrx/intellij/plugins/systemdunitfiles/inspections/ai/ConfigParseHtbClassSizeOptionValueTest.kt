package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHtbClassSizeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [HierarchyTokenBucketClass]
            QuantumBytes=1500
            MTUBytes=1500
            OverheadBytes=64
            BufferBytes=8K
            CeilBufferBytes=16K
            BufferBytes=1M
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [HierarchyTokenBucketClass]
            QuantumBytes=<error descr="Invalid value">abc</error>
            MTUBytes=<error descr="Invalid value">-1</error>
            BufferBytes=<error descr="Invalid value">8X</error>
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
