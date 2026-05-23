package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDelegateOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            Delegate=yes
            Delegate=no
            Delegate=true
            Delegate=false
            Delegate=cpu
            Delegate=cpu memory io
            Delegate=cpu cpuacct cpuset io blkio memory devices pids
            Delegate=bpf-firewall bpf-devices bpf-foreign
            Delegate=bpf-socket-bind bpf-restrict-network-interfaces bpf-bind-network-interface
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            Delegate=<error descr="Invalid value">maybe</error>
            Delegate=<error descr="Invalid value">cpu unknown</error>
            Delegate=<error descr="Invalid value">CPU</error>
            Delegate=<error descr="Invalid value">cpu,memory</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
