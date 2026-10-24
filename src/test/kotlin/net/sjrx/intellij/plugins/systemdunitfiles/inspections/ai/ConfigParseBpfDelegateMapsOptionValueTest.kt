package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBpfDelegateMapsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first value is from systemd's test/units/TEST-07-PID1.private-bpf.sh (L49-L52).
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateMaps=BPFMapTypeArray,BPFMapTypeCpumap,BPFMapTypeRingbuf
            BPFDelegateMaps=BPFMapTypeHash
            BPFDelegateMaps=any
            BPFDelegateMaps=BPFMapTypeUnspec,BPFMapTypeHash,BPFMapTypeArray
            BPFDelegateMaps=BPFMapTypeUnspec
            BPFDelegateMaps=BPFMapTypeCgroupStorageDeprecated
            BPFDelegateMaps=BPFMapTypeRhash
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
            BPFDelegateMaps=any
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            BPFDelegateMaps=BPFMapTypeUnspec
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            BPFDelegateMaps=BPFMapTypeRhash,BPFMapTypeUnspec
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateMaps=BPFMapTypeCgroupStorage
            BPFDelegateMaps=BPFMapTypeArray, BPFMapTypeHash
            BPFDelegateMaps=BPF_MAP_TYPE_ARRAY
            BPFDelegateMaps=Array
            BPFDelegateMaps=Any
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(5, myFixture.doHighlighting())
    }
}
