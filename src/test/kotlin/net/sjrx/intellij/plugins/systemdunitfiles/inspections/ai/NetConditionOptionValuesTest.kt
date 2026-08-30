package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** The .network/.netdev/.link [Match] condition keys — config_parse_net_condition. */
class NetConditionOptionValuesTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Match]
            Host=myhost
            Host=!myhost
            Virtualization=kvm
            Virtualization=!container
            Virtualization=no
            Architecture=x86-64
            Architecture=!arm64
            Firmware=uefi
            Firmware=device-tree-compatible(some-board)
            Credential=mycred
            KernelVersion=>=5.4
            Version=6.1
            KernelCommandLine=quiet
            MachineTag=production
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
            [Match]
            Virtualization=bogus
            Architecture=nonsense
            Firmware=bios
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
