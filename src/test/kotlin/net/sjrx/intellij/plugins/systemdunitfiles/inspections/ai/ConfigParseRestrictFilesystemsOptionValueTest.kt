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
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictFileSystems=~proc,sysfs,foo
            RestrictFileSystems=ext4 @foo @basic-api
            RestrictFileSystems=~proc foo @bar @basic-api
            RestrictFileSystems=EXT4
            RestrictFileSystems=ext5
            RestrictFileSystems=@basic_api
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(6, myFixture.doHighlighting())
    }
}
