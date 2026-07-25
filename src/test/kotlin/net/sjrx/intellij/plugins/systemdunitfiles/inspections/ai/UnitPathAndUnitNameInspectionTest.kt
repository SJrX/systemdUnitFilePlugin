package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

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
    // One path per assignment — the value is never split on whitespace.
    assertRejected("f.path", "[Path]\nPathExists=/tmp/a /tmp/b\n")
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
