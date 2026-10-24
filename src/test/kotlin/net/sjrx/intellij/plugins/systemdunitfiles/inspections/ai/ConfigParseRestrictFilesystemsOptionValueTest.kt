package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRestrictFilesystemsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first six are the man page examples and TEST-07-PID1.exec-context.sh values.
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictFileSystems=ext4 tmpfs
            RestrictFileSystems=ext2 ext4
            RestrictFileSystems=~ext4 tmpfs
            RestrictFileSystems=~tmpfs,ext4
            RestrictFileSystems=~@basic-api
            RestrictFileSystems=~proc devtmpfs sysfs
            RestrictFileSystems=~
            RestrictFileSystems=@common-block @network @known
            RestrictFileSystems=btrfs_test_fs balloon-kvm ppc-cmm cgroup2 cgroup nfs4 nfs
            RestrictFileSystems="ext4" 'xfs'
            RestrictFileSystems=~ fuse, fuseblk,
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
            RestrictFileSystems=@temporary
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            RestrictFileSystems=~@privileged-api
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            RestrictFileSystems=@security @application
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // "foo", "@foo" and "@bar" are TEST-07's deliberately unknown names (logged and ignored).
        // Unknown groups are refused while parsing. Unknown plain names ("foo", "EXT4", "ext5", and the kf6
        // autotest's "invalid"/"temporary"/"unknown") pass the unit-file parser, so sd-check is silent, but
        // in a BPF_FRAMEWORK build the unit logs "bpf-restrict-fs: Invalid filesystem name '…', ignoring."
        // when it starts (src/core/bpf-restrict-fs.c#L117-L121). The name table is compiled in, so they are
        // flagged on purpose.
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictFileSystems=~proc,sysfs,foo
            RestrictFileSystems=ext4 @foo @basic-api
            RestrictFileSystems=~proc foo @bar @basic-api
            RestrictFileSystems=EXT4
            RestrictFileSystems=ext5
            RestrictFileSystems=@basic_api
            RestrictFileSystems=invalid
            RestrictFileSystems=temporary
            RestrictFileSystems=@auxiliary-api btrfs  nfs @temporary unknown
            RestrictFileSystems=@unknowngroup
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(10, myFixture.doHighlighting())
    }
}
