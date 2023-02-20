package net.sjrx.intellij.plugins.systemdunitfiles.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon

class NewUnitFileAction : CreateFileFromTemplateAction(
  "systemd Unit File",
  "Creates a new systemd Unit File",
  UnitFileIcon.FILE) {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {

    builder.setTitle("New Systemd Unit File")
      .addKind("Service",
               UnitFileIcon.FILE,
               "service unit configuration.service"
      )
      .addKind("Socket",
               UnitFileIcon.FILE,
               "example.socket"
      )
      .addKind("Device",
               UnitFileIcon.FILE,
               "example.device")
      .addKind("Mount",
               UnitFileIcon.FILE,
               "example.mount")
      .addKind("Automount",
               UnitFileIcon.FILE,
               "example.automount")
      .addKind("Swap",
               UnitFileIcon.FILE,
               "example.swap")
      .addKind("Path",
               UnitFileIcon.FILE,
               "example.path")
      .addKind("Target",
               UnitFileIcon.FILE,
               "example.target")
      .addKind("Timer",
               UnitFileIcon.FILE,
               "example.timer")
      .addKind("Slice",
               UnitFileIcon.FILE,
               "example.slice")





  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
    return "New Systemd Unit File"
  }
}
