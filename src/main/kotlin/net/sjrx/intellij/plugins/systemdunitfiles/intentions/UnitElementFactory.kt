package net.sjrx.intellij.plugins.systemdunitfiles.intentions

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType

object UnitElementFactory {

  fun createFile(project: Project, text: String) : UnitFile {
    return PsiFileFactory.getInstance(project).createFileFromText("dummy.service", ServiceFileType.INSTANCE, text) as UnitFile
  }

  fun createProperty(project: Project, name: String, value: String = ""): UnitFilePropertyType  {
    val file = createFile(project, "${name}=${value}")
    return file.firstChild as UnitFilePropertyType
  }

}
