package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBindOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN and systemd-nspawn(1): empty source (temporary dir), relative source, `+` prefix,
        // escaped colons, and the bind options parse_mount_bind_options knows.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Bind=/var/lib/foo
            BindReadOnly=/tmp/ephemeral-config
            Bind=+/sysroot/ostree/deploy/os/var:/var
            Bind=/tmp/dir:/foo
            Bind=/tmp/dir:/also-foo:noidmap,norbind
            BindReadOnly=/etc/ld.so.cache:/etc/ld.so.cache
            Bind=/tmp/owneridmap/bind:/home/testuser:owneridmap
            BindReadOnly=/tmp/rootidmap/bind:/mnt:rootidmap
            Bind=:/scratch
            Bind=relative/dir:/mnt
            Bind=/path\:with\:colons:/mnt
            Bind=/a:/b:
            Bind=/a:/b:rbind,,idmap,
            Bind=/my dir:/mnt/my dir
            Bind=+/foo
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // A lone path must be absolute; empty or relative destination; `+` must be followed by an absolute path;
        // unknown bind options.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Bind=relative
            Bind=/a:
            Bind=/a:relative
            Bind=+relative:/b
            Bind=/a:/b:bogus
            Bind=/a:/b:rbind:x
            BindReadOnly=/a:/b:ro
            Bind=+:/b
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(8, highlights)
    }
}
