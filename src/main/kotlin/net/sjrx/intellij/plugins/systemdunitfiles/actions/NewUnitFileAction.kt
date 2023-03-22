package net.sjrx.intellij.plugins.systemdunitfiles.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon

class NewUnitFileAction : CreateFileFromTemplateAction(
  "Systemd Unit File",
  "Creates a new systemd Unit File",
  UnitFileIcon.FILE
) {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {

    builder.setTitle("New Systemd Unit File")
      .addKind("Service", UnitFileIcon.SERVICE, "example.service")
      .addKind("Mount", UnitFileIcon.MOUNT, "example.mount")
      .addKind("Automount", UnitFileIcon.AUTOMOUNT, "example.automount")
      .addKind("Target", UnitFileIcon.TARGET, "example.target")
      .addKind("Timer", UnitFileIcon.TIMER, "example.timer")
      .addKind("Swap", UnitFileIcon.SWAP, "example.swap")
  }


  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
    return "New Systemd Unit File"
  }
}
