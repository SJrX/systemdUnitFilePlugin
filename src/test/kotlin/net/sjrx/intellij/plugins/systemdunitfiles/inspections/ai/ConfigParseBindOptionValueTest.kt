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
        // A lone path must be absolute; empty or relative destination; `+` must be followed by an absolute path.
        // These are rejected by bind_mount_parse() itself (sd-check: "Invalid bind mount specification").
        // language="unit file (systemd)"
        val file = """
            [Files]
            Bind=relative
            Bind=/a:
            Bind=/a:relative
            Bind=+relative:/b
            Bind=+:/b
            Bind=/a:relative:rbind
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }

    @Test
    fun testOptionsRejectedOnlyAtContainerStart() {
        // Accepted: bind_mount_parse() stores the option string verbatim, so the settings parser (sd-check /
        // fuzz-nspawn-settings at 0f0682b773) accepts all of these. parse_mount_bind_options() fails the container
        // start with "Invalid bind mount option" (`ro` is not an option, read-only is BindReadOnly=; `rbind:x` is one
        // word), but that is not a parse error. Issue #565 tracks a separate warning for them.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Bind=/a:/b:bogus
            Bind=/a:/b:rbind:x
            BindReadOnly=/a:/b:ro
            Bind=/a:/b:rbind,bogus
            Bind=/a:/b::
            Bind=/a:/b:has space
            Bind=+/a:/b:x:y:z
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
