package net.sjrx.intellij.plugins.systemdunitfiles.podman

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.UnknownKeyInSectionInspection
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PodmanQuadletSettings

class PodmanQuickFixTest : AbstractUnitFileTest() {

  override fun setUp() {
    super.setUp()
    PodmanQuadletSettings.getInstance(project).state.enabled = false
  }

  override fun tearDown() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false
    super.tearDown()
  }

  fun testUnknownKeyInspectionOffersEnablePodmanQuickFix() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(UnknownKeyInSectionInspection::class.java)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.WARNING }

    assertSize(1, highlights)
    assertContainsQuickfix(highlights[0], "Enable Podman Quadlet support (experimental)")
  }

  fun testInvalidSectionAnnotationOffersEnablePodmanQuickFix() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
        [Install]
        WantedBy=default.target
    """.trimIndent()

    setupFileInEditor("file.network", file)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.ERROR && it.description?.contains("not allowed") == true }

    assertSize(1, highlights)
    assertContainsQuickfix(highlights[0], "Enable Podman Quadlet support (experimental)")
  }

  fun testNoQuickFixWhenFileDoesNotLookLikePodman() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        DHCP=yes
        [Install]
        WantedBy=default.target
    """.trimIndent()

    setupFileInEditor("file.network", file)

    val highlights = myFixture.doHighlighting()
      .filter { it.type == HighlightInfoType.ERROR && it.description?.contains("not allowed") == true }

    // Install is not allowed in systemd-networkd .network files, but since the file
    // doesn't look like podman (no podman-only keys), there should be no podman quick fix
    assertSize(1, highlights)
    val quickFixes = highlights[0].quickFixActionRanges
    assertTrue("Expected no quick fixes for non-podman file", quickFixes == null || quickFixes.isEmpty())
  }
}
