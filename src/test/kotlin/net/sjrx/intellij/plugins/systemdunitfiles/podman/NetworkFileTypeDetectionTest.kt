package net.sjrx.intellij.plugins.systemdunitfiles.podman

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.FileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PodmanQuadletSettings

class NetworkFileTypeDetectionTest : AbstractUnitFileTest() {

  override fun setUp() {
    super.setUp()
    PodmanQuadletSettings.getInstance(project).state.enabled = true
  }

  override fun tearDown() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false
    super.tearDown()
  }

  fun testDetectsPodmanNetworkBySubnetKey() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
        Gateway=192.168.1.1
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    assertEquals(FileClass.PODMAN_NETWORK, psiFile.fileClass())
  }

  fun testDetectsPodmanNetworkByNetworkNameKey() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        NetworkName=mynet
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    assertEquals(FileClass.PODMAN_NETWORK, psiFile.fileClass())
  }

  fun testDetectsSystemdNetworkdByMatchSection() {
    // language="unit file (systemd)"
    val file = """
        [Match]
        Name=eth0
        [Network]
        DHCP=yes
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    assertEquals(FileClass.NETWORK, psiFile.fileClass())
  }

  fun testDetectsSystemdNetworkdByRouteSection() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        DHCP=yes
        [Route]
        Gateway=10.0.0.1
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    assertEquals(FileClass.NETWORK, psiFile.fileClass())
  }

  fun testDefaultsToSystemdNetworkdForAmbiguousFile() {
    // language="unit file (systemd)"
    val file = """
        [Network]
        DNS=8.8.8.8
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    // DNS exists in both, so default to systemd-networkd
    assertEquals(FileClass.NETWORK, psiFile.fileClass())
  }

  fun testDisabledSettingReturnsSystemdNetworkd() {
    PodmanQuadletSettings.getInstance(project).state.enabled = false

    // language="unit file (systemd)"
    val file = """
        [Network]
        Subnet=192.168.1.0/24
    """.trimIndent()

    val psiFile = setupFileInEditor("file.network", file)

    // Even with podman content, disabled means systemd-networkd
    assertEquals(FileClass.NETWORK, psiFile.fileClass())
  }
}
