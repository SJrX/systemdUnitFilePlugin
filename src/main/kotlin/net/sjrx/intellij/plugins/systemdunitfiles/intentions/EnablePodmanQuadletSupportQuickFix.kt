package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.EditorNotifications
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PodmanQuadletSettings

private fun enablePodmanSupport(project: Project) {
  PodmanQuadletSettings.getInstance(project).state.enabled = true
  EditorNotifications.getInstance(project).updateAllNotifications()
  DaemonCodeAnalyzer.getInstance(project).restart()
}

/**
 * LocalQuickFix for use with inspections (ProblemsHolder.registerProblem).
 */
class EnablePodmanQuadletSupportQuickFix : LocalQuickFix {

  override fun getFamilyName(): String = "Enable Podman Quadlet support (experimental)"

  override fun startInWriteAction(): Boolean = false

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    enablePodmanSupport(project)
  }
}

/**
 * IntentionAction for use with annotators (AnnotationBuilder.withFix).
 */
class EnablePodmanQuadletSupportIntention : IntentionAction {

  override fun getFamilyName(): String = "Enable Podman Quadlet support (experimental)"

  override fun getText(): String = familyName

  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

  override fun startInWriteAction(): Boolean = false

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
    enablePodmanSupport(project)
  }
}
