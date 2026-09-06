package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import java.util.function.Function
import javax.swing.JComponent

/**
 * Release 1 rollout banner (GitHub #467).
 *
 * Shown at the top of a systemd unit file once the new grammar engine has become the default, so
 * active users know the validation engine changed and have a one-click way out if it misbehaves.
 * Because [EditorNotificationProvider]s are evaluated lazily (only when a matching file is actually
 * opened), users who never touch a unit file never see it.
 *
 * Template to copy from: [PodmanNetworkEditorNotificationProvider] — same shape (gate on settings,
 * detect the file, return a panel with action labels).
 */
class NewGrammarEngineEditorNotificationProvider : EditorNotificationProvider {

  override fun collectNotificationData(
    project: Project,
    file: VirtualFile
  ): Function<in FileEditor, out JComponent?>? {

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

    if (psiFile.language != UnitFileLanguage.INSTANCE) {
      return null
    }

    val state = ExperimentalSettings.getInstance(project).state
    if (!state.useGrammarParseEngine) {
      return null
    }

    if (state.newEngineBannerDismissed) {
      return null
    }

    return Function { fileEditor ->
      val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)

      panel.text = "Unit File Support (systemd) use a new experimental engine for validating systemd values. Having problems, switch back and report it."
      panel.createActionLabel("Dismiss") {
        state.newEngineBannerDismissed = true
        EditorNotifications.getInstance(project).updateAllNotifications()
        DaemonCodeAnalyzer.getInstance(project).restart()
      }

      panel.createActionLabel("Switch back") {
        state.useGrammarParseEngine = false
        EditorNotifications.getInstance(project).updateAllNotifications()
        DaemonCodeAnalyzer.getInstance(project).restart()
      }
      panel

    }
  }
}
