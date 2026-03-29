package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.psi.PsiFile
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.FileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.detectNetworkFileClass

/**
 * Checks whether a .network file looks like a Podman Quadlet file but the feature is currently disabled.
 * Used to conditionally offer quick fixes and notifications.
 */
fun shouldSuggestPodmanSupport(file: PsiFile): Boolean {
  if (!file.name.endsWith(".network")) return false
  val settings = PodmanQuadletSettings.getInstance(file.project)
  if (settings.state.enabled) return false
  return detectNetworkFileClass(file.originalFile) == FileClass.PODMAN_NETWORK
}
