package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMountImagesOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      MountImages=/tmp/app0.raw:/var/tmp:noatime,nosuid
      MountImages=/tmp/app0.raw:/var/tmp:nosuid
      MountImages=-/srv/foo.raw:/srv/foo
      MountImages=/srv/foo.raw:/srv/foo:root:ro,nosuid:usr:nodev
      MountImages=/srv/foo.raw:/srv/foo:root:ro:nosuid
      MountImages=/srv/foo.raw:/srv/foo:root:
      MountImages=/srv/foo.raw::/srv/foo
      MountImages=/dev/sda1:/mnt/disk
      MountImages=%S/img.raw:%t/img
      MountImages=/srv/a\:b.raw:/srv/c /srv/d.raw:/srv/e:ro
      MountImages=/srv/foo.raw:/srv/foo:rootcontext=x
      MountImages=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      MountImages=/srv/foo.raw
      MountImages=/srv/foo.raw:
      MountImages=relative.raw:/srv/foo
      MountImages=/srv/foo.raw:relative
      MountImages=/srv/foo.raw:/proc/foo
      MountImages=/sys/foo.raw:/srv/foo
      MountImages=/srv/foo.raw:/srv/foo:bogus:ro
      MountImages=/srv/foo.raw:/srv/../foo
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
