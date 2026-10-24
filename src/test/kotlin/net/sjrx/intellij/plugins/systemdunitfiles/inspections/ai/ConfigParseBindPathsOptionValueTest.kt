package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBindPathsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidServiceValues() {
    // Values from systemd's test-load-fragment.c, test/test-execute/exec-bindpaths.service and the corpus.
    // language="unit file (systemd)"
    val file = """
      [Service]
      BindPaths=/tmp:/tmp/test-exec-bindpaths
      BindPaths="/tmp/test-exec-bindpaths source:/tmp/test-exec-bindpaths destination"   /tmp/test-exec-bindpaths\:source:/tmp/test-exec-bindpaths\:destination
      BindReadOnlyPaths=  /usr/bin  /usr/lib     /lib64
      BindReadOnlyPaths=-/usr/lib -/usr/lib64 /usr/bin /usr/sbin
      BindPaths=/etc /home:/mnt:norbind -/foo/bar/baz:/usr:rbind
      BindPaths=%f:/tmp/scrub:norbind
      BindPaths=%t/foo:%h/bar
      BindPaths=/a:/b:
      BindPaths=/tmp/markers/
      BindPaths=/a/./b//c:/d
      BindPaths=/a\x20b:/c\sd
      BindReadOnlyPaths=/dev/log:/var/lib/haproxy/dev/log
      BindReadOnlyPaths='/srv/a b':/srv/c
      BindPaths=
      BindPaths=/a\"b":/c
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidInOtherUnitTypes() {
    // language="unit file (systemd)"
    val socket = """
      [Socket]
      BindPaths=/run/foo:/run/bar
      BindReadOnlyPaths=-/etc/resolv.conf
    """.trimIndent()
    setupFileInEditor("file.socket", socket)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())

    // language="unit file (systemd)"
    val mount = """
      [Mount]
      BindPaths=/run/foo:/run/bar:rbind
      BindReadOnlyPaths=/etc/machine-id
    """.trimIndent()
    setupFileInEditor("file.mount", mount)
    assertSize(0, myFixture.doHighlighting())

    // language="unit file (systemd)"
    val swap = """
      [Swap]
      BindPaths=/run/foo
      BindReadOnlyPaths=/run/dbus/system_bus_socket
    """.trimIndent()
    setupFileInEditor("file.swap", swap)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      BindPaths=relative/path
      BindPaths=/a:relative
      BindPaths=/a:
      BindPaths=/a::rbind
      BindPaths=/a:/b:ro
      BindPaths=/a:/b:rbind:extra
      BindPaths=/a/../b
      BindPaths=/a:-/b
      BindPaths=/a\qb
      BindReadOnlyPaths=/usr/bin foo
      BindReadOnlyPaths=/usr/bin /a:/b:recursive
      BindPaths=/a\"
      BindPaths=/a\ud800
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(13, highlights)
  }
}
