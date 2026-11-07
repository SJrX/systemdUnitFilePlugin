package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat

/**
 * Keeps the MAC address as written and makes its [format] the preferred one instead, so the file stops
 * being flagged. Changes the project setting only; the file is not edited.
 */
class SetPreferredMacFormatQuickFix(private val format: PreferredMacFormat) : LocalQuickFix {

  override fun getName(): String = "Set preferred MAC address format to ${format.displayName}"

  override fun getFamilyName(): String = "Set preferred MAC address format"

  override fun startInWriteAction(): Boolean = false

  // The default preview would run applyFix, which changes the real setting.
  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo =
    IntentionPreviewInfo.Html("Changes <b>Settings | Tools | systemd Unit Files | Preferred MAC address format</b> to ${format.displayName}.")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    ExperimentalSettings.getInstance(project).state.preferredMacFormat = format
    DaemonCodeAnalyzer.getInstance(project).restart()
  }
}
