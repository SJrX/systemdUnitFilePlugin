package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class TargetFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Target unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "target"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.FILE
  }

  override fun getDisplayName(): @Nls String {
    return "Target unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = TargetFileType()
  }
}
