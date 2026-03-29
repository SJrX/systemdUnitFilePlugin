package net.sjrx.intellij.plugins.systemdunitfiles.podman

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.UnknownKeyInSectionInspection
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PodmanQuadletSettings

class PodmanNetworkInspectionTest : AbstractUnitFileTest() {

  override fun setUp() {
    super.setUp()
    PodmanQuadletSettings.getInstance(project).state.enabled = true
  }

  override fun tearDown() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false
    super.tearDown()
  }

  fun testPodmanNetworkKeysDoNotTriggerUnknownKeyInspection() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
        Gateway=192.168.1.1
        Driver=bridge
        DisableDNS=true
        Internal=false
        NetworkName=mynet
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(UnknownKeyInSectionInspection::class.java)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.WARNING || it.type == HighlightInfoType.ERROR }

    assertEmpty(highlights)
  }

  fun testBooleanValidationWorksForPodmanKeys() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=10.0.0.0/8
        DisableDNS=notabool
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.WARNING }

    assertSize(1, highlights)
    assertStringContains("notabool", highlights[0]!!.description)
  }

  fun testValidBooleanValuePassesValidation() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=10.0.0.0/8
        DisableDNS=true
        Internal=false
        IPv6=yes
        NetworkDeleteOnStop=no
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.WARNING }

    assertEmpty(highlights)
  }

  fun testPodmanKeysAreUnknownWhenFeatureDisabled() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false

    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(UnknownKeyInSectionInspection::class.java)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.WARNING }

    // Subnet should be unknown in systemd-networkd's Network section
    assertSize(1, highlights)
  }
}
