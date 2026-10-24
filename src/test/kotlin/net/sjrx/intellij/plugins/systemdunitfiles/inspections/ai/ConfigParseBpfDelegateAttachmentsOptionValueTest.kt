package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBpfDelegateAttachmentsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first value is from systemd's test/units/TEST-07-PID1.private-bpf.sh (L49-L52).
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateAttachments=BPFFlowDissector,BPFCgroupSysctl,BPFNetfilter
            BPFDelegateAttachments=BPFCgroupInetIngress
            BPFDelegateAttachments=any
            BPFDelegateAttachments=BPFCgroupInetIngress,BPFCgroupInetEgress,BPFCgroupInetSockCreate
            BPFDelegateAttachments=BPFTraceFsessionMulti
            BPFDelegateAttachments=BPFLircMode2
            BPFDelegateAttachments=BPFCgroupInet4Bind,BPFCgroupInet6Bind
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
            BPFDelegateAttachments=any
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            BPFDelegateAttachments=BPFCgroupInetIngress
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            BPFDelegateAttachments=BPFTraceFsessionMulti,BPFCgroupInetIngress
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BPFDelegateAttachments=BPFCgroupInetSkbIngress
            BPFDelegateAttachments=BPFFlowDissector, BPFNetfilter
            BPFDelegateAttachments=BPF_NETFILTER
            BPFDelegateAttachments=ingress
            BPFDelegateAttachments=BPFTcxIngress BPFTcxEgress
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(5, myFixture.doHighlighting())
    }
}
