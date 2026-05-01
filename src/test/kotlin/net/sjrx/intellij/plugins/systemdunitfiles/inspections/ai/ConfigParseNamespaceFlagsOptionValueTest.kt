package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNamespaceFlagsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidSingleNamespaceTypes() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=cgroup
            RestrictNamespaces=ipc
            RestrictNamespaces=net
            RestrictNamespaces=mnt
            RestrictNamespaces=pid
            RestrictNamespaces=user
            RestrictNamespaces=uts
            RestrictNamespaces=time
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testValidWhitespaceSeparatedList() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=cgroup ipc
            RestrictNamespaces=mnt uts ipc
            RestrictNamespaces=cgroup ipc net mnt pid user uts time
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testValidInvertedList() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=~cgroup
            RestrictNamespaces=~cgroup net
            RestrictNamespaces=~mnt uts ipc
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testValidBooleans() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=yes
            RestrictNamespaces=no
            RestrictNamespaces=true
            RestrictNamespaces=false
            RestrictNamespaces=on
            RestrictNamespaces=off
            RestrictNamespaces=1
            RestrictNamespaces=0
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testValidDelegateNamespaces() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            DelegateNamespaces=mnt
            DelegateNamespaces=~user
            DelegateNamespaces=yes
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=invalid_value_1
            RestrictNamespaces=foo bar
            RestrictNamespaces=cgroup,ipc
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidTrailingTilde() {
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictNamespaces=cgroup~
            RestrictNamespaces=cgroup ~ipc
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(2, myFixture.doHighlighting())
  }
}
