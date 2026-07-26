package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/*
 * Expectations here are derived from systemd's C parsers at a8e93919c3 (https://github.com/systemd/systemd/blob/a8e93919c3),
 * the commit systemd-build/build/last_commit_hash pins, and NOT from what happens to appear in
 * real-world unit files. Where a case is subtle the individual test says which routine decides it.
 *
 * Several of the rejection cases are lifted from systemd's own negative fixtures under
 * test/test-network/conf/ and from KDE's syntax-highlighting test input, both of which deliberately
 * contain malformed values.
 */

/**
 * Tests for the unit-file path and unit-name validators added in #509: `[Path]` watch settings,
 * `[Socket] Symlinks=`, `[Socket] Service=` and `[Service] Sockets=`.
 */
class UnitPathAndUnitNameInspectionTest : AbstractUnitFileTest() {

  private fun highlights(fileName: String, text: String): Int {
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  private fun assertAccepted(fileName: String, vararg lines: String) =
    assertEquals(lines.joinToString(), 0, highlights(fileName, lines.joinToString("\n") + "\n"))

  private fun assertRejected(fileName: String, text: String) =
    assertTrue(text, highlights(fileName, text) >= 1)

  @Test
  fun testPathSpecTakesOneAbsolutePath() {
    assertAccepted(
      "f.path",
      "[Path]",
      "PathExists=/run/systemd/ask-password",
      "PathExistsGlob=/tmp/test63-glob*",
      "PathChanged=/run/lvm/lvm-devices-import",
      "PathModified=/tmp/test-path_unit",
      "DirectoryNotEmpty=/tmp/test-path_makedirectory/",
      "PathExists=%t/spool",
    )
    // path_simplify_and_warn(..., PATH_CHECK_ABSOLUTE): a relative path is refused. openqa's shipped
    // openqa-minion-restart.path gets this wrong with an unsubstituted "installvendorlib/..." value.
    assertRejected("f.path", "[Path]\nPathChanged=installvendorlib/Minion/pg.sql\n")
    // config_parse_path_spec takes the whole rvalue, so a space is a path character rather than a
    // separator: this is one directory called "a /tmp/b" under /tmp, and systemd accepts it.
    assertAccepted("f.path", "[Path]", "PathExists=/tmp/a /tmp/b")
  }

  @Test
  fun testSocketSymlinksTakesAListOfAbsolutePaths() {
    assertAccepted(
      "f.socket",
      "[Socket]",
      "Symlinks=/run/varlink/registry/io.systemd.Resolve",
      "Symlinks=/run/systemd/userdb/io.systemd.NamespaceResource /run/varlink/registry",
      "Symlinks=%t/varlink/registry/io.systemd.Import",
    )
    assertRejected("f.socket", "[Socket]\nSymlinks=a b c d e\n")
    assertRejected("f.socket", "[Socket]\nSymlinks=/run/ok relative/no\n")
  }

  @Test
  fun testSocketSymlinksHonoursQuotingAndEscapes() {
    // Unlike the [Path] settings, this list IS split with extract_first_word(..., EXTRACT_UNQUOTE),
    // which honours quotes and drops backslashes — so a path with a space can survive splitting.
    assertAccepted(
      "f.socket",
      "[Socket]",
      "Symlinks=\"/run/my socket\"",
      "Symlinks=\"/run/my socket\" /run/other",
      "Symlinks='/run/my socket'",
      "Symlinks=/run/my\\ socket",
    )
    // Quoted or not, each entry still has to be absolute.
    assertRejected("f.socket", "[Socket]\nSymlinks=\"relative/no\"\n")
  }

  @Test
  fun testSocketServiceMustNameAService() {
    assertAccepted(
      "f.socket",
      "[Socket]",
      "Service=gpg-agent.service",
      "Service=systemd-journald@%i.service",
    )
    // config_parse_socket_service's only shape check is endswith(".service").
    assertRejected("f.socket", "[Socket]\nService=some.socket\n")
    assertRejected("f.socket", "[Socket]\nService=some.target\n")
    assertRejected("f.socket", "[Socket]\nService=some.invalid\n")
    // Not a list.
    assertRejected("f.socket", "[Socket]\nService=some.service other.service\n")
  }

  @Test
  fun testServiceSocketsMustNameSockets() {
    assertAccepted(
      "f.service",
      "[Service]",
      "Sockets=some.socket",
      "Sockets=udev-control.socket udev-kernel.socket",
      "Sockets=some.socket some@instance.socket",
    )
    assertRejected("f.service", "[Service]\nSockets=some.service\n")
    assertRejected("f.service", "[Service]\nSockets=some.service some.socket\n")
  }
}
