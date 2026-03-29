package net.sjrx.intellij.plugins.systemdunitfiles.podman

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PodmanQuadletSettings

class PodmanNetworkCompletionTest : AbstractUnitFileTest() {

  override fun setUp() {
    super.setUp()
    PodmanQuadletSettings.getInstance(project).state.enabled = true
  }

  override fun tearDown() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false
    super.tearDown()
  }

  fun testCompletionInNetworkSectionIncludesPodmanKeys() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=10.0.0.0/8
        $COMPLETION_POSITION
    """.trimIndent()
    setupFileInEditor("file.network", file)

    val completions = basicCompletionResultStrings

    assertContainsElements(completions, "Gateway", "Driver", "DisableDNS", "NetworkName")
  }

  fun testCompletionInNetworkSectionIncludesBooleanKeys() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=10.0.0.0/8
        D$COMPLETION_POSITION
    """.trimIndent()
    setupFileInEditor("file.network", file)

    val completions = basicCompletionResultStrings

    assertContainsElements(completions, "DisableDNS", "DNS", "Driver")
  }

  fun testCompletionInUnitSectionWorksForPodmanNetwork() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=10.0.0.0/8
        [Unit]
        D$COMPLETION_POSITION
    """.trimIndent()
    setupFileInEditor("file.network", file)

    val completions = basicCompletionResultStrings

    assertContainsElements(completions, "Description", "Documentation", "DefaultDependencies")
  }

  fun testSectionCompletionIncludesPodmanSections() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
        [$COMPLETION_POSITION
    """.trimIndent()
    setupFileInEditor("file.network", file)

    val completions = basicCompletionResultStrings

    assertContainsElements(completions, "Network", "Unit", "Install", "Service")
    // Should not include systemd-networkd-only sections
    assertDoesntContain(completions, "Match", "Route", "Address", "DHCPv4")
  }

  fun testCompletionWithoutFeatureEnabledUsesSystemdNetworkd() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false

    // language="unit file (systemd)"
    val file = """
        [Network]
        $COMPLETION_POSITION
    """.trimIndent()
    setupFileInEditor("file.network", file)

    val completions = basicCompletionResultStrings

    // With podman disabled, this should use systemd-networkd which does not have Subnet
    assertDoesntContain(completions, "Subnet")
  }
}
