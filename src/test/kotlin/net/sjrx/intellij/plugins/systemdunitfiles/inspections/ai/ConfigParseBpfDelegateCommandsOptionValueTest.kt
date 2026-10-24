package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBpfDelegateCommandsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first value is from systemd's test/units/TEST-07-PID1.private-bpf.sh (L49-L52).
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateCommands=BPFObjPin,BPFBtfLoad,BPFMapFreeze,BPFLinkDetach
            BPFDelegateCommands=BPFProgLoad
            BPFDelegateCommands=any
            BPFDelegateCommands=BPFMapCreate,BPFMapLookupElem,BPFMapUpdateElem
            BPFDelegateCommands=BPFMapCreate
            BPFDelegateCommands=BPFProgAssocStructOps
            BPFDelegateCommands=BPFProgLoad,BPFMapCreate,,BPFLinkCreate,
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
            BPFDelegateCommands=any
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            BPFDelegateCommands=BPFMapCreate
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            BPFDelegateCommands=BPFProgAssocStructOps,BPFMapCreate
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateCommands=BPF_PROG_LOAD
            BPFDelegateCommands=BPFProgload
            BPFDelegateCommands=BPFProgLoad, BPFMapCreate
            BPFDelegateCommands=BPFProgLoad BPFMapCreate
            BPFDelegateCommands=any,BPFProgLoad
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(5, myFixture.doHighlighting())
    }
}
