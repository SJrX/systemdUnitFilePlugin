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
    fun testRuntimeOnlyRejectionsAreAccepted() {
        // sd-check at 0f0682b773 accepts every one of these (one value per probe file). The unit-file parser
        // stores any word that does not unquote to `@group` unchecked; unknown names are only logged and
        // skipped when the unit starts ("bpf-restrict-fs: Invalid filesystem name '…', ignoring.",
        // src/core/bpf-restrict-fs.c#L117-L121). Issue #565 tracks a separate warning for them. The kf6
        // autotest values ("invalid", "temporary", "@auxiliary-api btrfs  nfs @temporary unknown") are here.
        // A `~` word after the first position is the literal name `~proc` / `~@foo`, and quotes and
        // backslash escapes are removed anywhere in a word (EXTRACT_UNQUOTE).
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictFileSystems=~proc,sysfs,foo
            RestrictFileSystems=EXT4
            RestrictFileSystems=ext5
            RestrictFileSystems=invalid
            RestrictFileSystems=temporary
            RestrictFileSystems=@auxiliary-api btrfs  nfs @temporary unknown
            RestrictFileSystems=ext4foo nfs4x foo.bar!
            RestrictFileSystems=ext4 ~proc
            RestrictFileSystems=ext4 ~@foo
            RestrictFileSystems=ext4 ~
            RestrictFileSystems=~ ~@bogus
            RestrictFileSystems=~~proc
            RestrictFileSystems="~@bogus"
            RestrictFileSystems=\~proc
            RestrictFileSystems=ext"4"
            RestrictFileSystems=ext4""
            RestrictFileSystems=""
            RestrictFileSystems="a\"b"
            RestrictFileSystems='a\'b'
            RestrictFileSystems="@basic-api" '@network'
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // "@foo" and "@bar" are TEST-07's deliberately unknown groups. The parser refuses unknown groups
        // ("bpf-restrict-fs: Unknown filesystem group, ignoring: …", sd-check at 0f0682b773), also when
        // the word only unquotes to `@…` ("@foo", ""@foo, \@foo) and when the `~` marker comes first.
        // An unterminated quote is "Trailing garbage in RestrictFileSystems".
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictFileSystems=ext4 @foo @basic-api
            RestrictFileSystems=~proc foo @bar @basic-api
            RestrictFileSystems=@basic_api
            RestrictFileSystems=@unknowngroup
            RestrictFileSystems=@
            RestrictFileSystems=@Basic-api
            RestrictFileSystems=~@bogus
            RestrictFileSystems="@foo"
            RestrictFileSystems=""@foo
            RestrictFileSystems=\@foo
            RestrictFileSystems="@basic-api"x
            RestrictFileSystems=ext"4
            RestrictFileSystems=ext4 "unterminated
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(13, myFixture.doHighlighting())
    }
}
