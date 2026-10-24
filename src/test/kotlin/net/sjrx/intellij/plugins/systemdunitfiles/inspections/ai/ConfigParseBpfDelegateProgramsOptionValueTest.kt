package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBpfDelegateProgramsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first value is from systemd's test/units/TEST-07-PID1.private-bpf.sh (L49-L52).
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegatePrograms=BPFProgTypeTracepoint,BPFProgTypeXdp,BPFProgTypeTracing
            BPFDelegatePrograms=BPFProgTypeKprobe
            BPFDelegatePrograms=any
            BPFDelegatePrograms=BPFProgTypeUnspec,BPFProgTypeSocketFilter,BPFProgTypeKprobe
            BPFDelegatePrograms=BPFProgTypeUnspec
            BPFDelegatePrograms=BPFProgTypeLwtSeg6local
            BPFDelegatePrograms=BPFProgTypeNetfilter
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidAcrossUnitTypes() {
        // language="unit file (systemd)"
        val socket = """
            [Socket]
            BPFDelegatePrograms=any
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            BPFDelegatePrograms=BPFProgTypeUnspec
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            BPFDelegatePrograms=BPFProgTypeNetfilter,BPFProgTypeUnspec
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegatePrograms=BPFProgTypeXDP
            BPFDelegatePrograms=BPFProgTypeXdp BPFProgTypeTracing
            BPFDelegatePrograms=BPFProgTypeLwtSeg6Local
            BPFDelegatePrograms=tracepoint
            BPFDelegatePrograms=BPFProgTypeSyscall
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(5, myFixture.doHighlighting())
    }
}
