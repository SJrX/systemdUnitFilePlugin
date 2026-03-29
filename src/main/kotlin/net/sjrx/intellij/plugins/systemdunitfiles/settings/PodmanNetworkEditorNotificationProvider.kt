package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import java.util.function.Function
import javax.swing.JComponent

class PodmanNetworkEditorNotificationProvider : EditorNotificationProvider {

  override fun collectNotificationData(
    project: Project,
    file: VirtualFile
  ): Function<in FileEditor, out JComponent?>? {
    if (!file.name.endsWith(".network")) return null

    val settings = PodmanQuadletSettings.getInstance(project)
    if (settings.state.notificationDismissed) return null

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
    if (!shouldSuggestPodmanSupport(psiFile)) return null

    return Function { fileEditor ->
      val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
      panel.text = "This file appears to be a Podman Quadlet network file."
      panel.createActionLabel("Enable Podman Quadlet support (experimental)") {
        settings.state.enabled = true
        EditorNotifications.getInstance(project).updateAllNotifications()
        DaemonCodeAnalyzer.getInstance(project).restart()
      }
      panel.createActionLabel("Dismiss") {
        settings.state.notificationDismissed = true
        EditorNotifications.getInstance(project).updateAllNotifications()
      }
      panel
    }
  }
}
