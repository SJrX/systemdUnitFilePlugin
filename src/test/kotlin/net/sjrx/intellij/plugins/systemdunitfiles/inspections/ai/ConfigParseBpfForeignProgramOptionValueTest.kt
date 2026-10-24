package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBpfForeignProgramOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first two are the systemd.resource-control(5) examples.
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFProgram=egress:/sys/fs/bpf/egress-hook
            BPFProgram=bind6:/sys/fs/bpf/sock-addr-hook
            BPFProgram=ingress:/sys/fs/bpf/ingress
            BPFProgram=sock_create:/sys/fs/bpf/a
            BPFProgram=sock_ops:/sys/fs/bpf/a
            BPFProgram=device:/sys/fs/bpf/a
            BPFProgram=bind4:/sys/fs/bpf/a
            BPFProgram=connect4:/sys/fs/bpf/a
            BPFProgram=connect6:/sys/fs/bpf/a
            BPFProgram=post_bind4:/sys/fs/bpf/a
            BPFProgram=post_bind6:/sys/fs/bpf/a
            BPFProgram=sendmsg4:/sys/fs/bpf/a
            BPFProgram=sendmsg6:/sys/fs/bpf/a
            BPFProgram=sysctl:/sys/fs/bpf/a
            BPFProgram=recvmsg4:/sys/fs/bpf/a
            BPFProgram=recvmsg6:/sys/fs/bpf/a
            BPFProgram=getsockopt:/sys/fs/bpf/a
            BPFProgram=setsockopt:/sys/fs/bpf/a
            BPFProgram=egress:/sys/fs/bpf/%N-egress
            BPFProgram=egress:%t/bpf/prog
            BPFProgram=egress::/sys/fs/bpf/double-colon
            BPFProgram=egress:/sys/fs/bpf/with:colon
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
            BPFProgram=egress:/sys/fs/bpf/egress-hook
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            BPFProgram=ingress:/sys/fs/bpf/x
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            BPFProgram=device:/sys/fs/bpf/x
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val slice = """
            [Slice]
            BPFProgram=sock_create:/sys/fs/bpf/x
        """.trimIndent()
        setupFileInEditor("file.slice", slice)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFProgram=/usr/lib/systemd/bpf/custom.bpf
            BPFProgram=egress
            BPFProgram=egress:
            BPFProgram=xdp:/sys/fs/bpf/x
            BPFProgram=Egress:/sys/fs/bpf/x
            BPFProgram=egress:sys/fs/bpf/x
            BPFProgram=egress:/sys/fs/bpf/../x
            BPFProgram=cgroup_inet_egress:/sys/fs/bpf/x
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(8, myFixture.doHighlighting())
    }
}
