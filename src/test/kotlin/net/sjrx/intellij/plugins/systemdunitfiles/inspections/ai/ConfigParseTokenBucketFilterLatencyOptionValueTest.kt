package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTokenBucketFilterLatencyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [TokenBucketFilter]
            LatencySec=10s
            LatencySec=100ms
            LatencySec=1m
            LatencySec=1h
            LatencySec=500us
            LatencySec=42
            LatencySec=1.5s
            LatencySec=infinity
            LatencySec=2hour
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [TokenBucketFilter]
            LatencySec=invalid
            LatencySec=-10
            LatencySec=10x
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(3, myFixture.doHighlighting())
    }
}
