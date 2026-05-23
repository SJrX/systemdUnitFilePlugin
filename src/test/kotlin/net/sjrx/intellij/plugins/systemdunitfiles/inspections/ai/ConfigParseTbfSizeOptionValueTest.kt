package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTbfSizeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [TokenBucketFilter]
            BurstBytes=1024
            LimitBytes=64K
            MTUBytes=1500
            MPUBytes=64
            BurstBytes=1M
            LimitBytes=1G
            MTUBytes=2T
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
            [TokenBucketFilter]
            BurstBytes=<error descr="Invalid value">abc</error>
            LimitBytes=<error descr="Invalid value">-1</error>
            MTUBytes=<error descr="Invalid value">10X</error>
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
