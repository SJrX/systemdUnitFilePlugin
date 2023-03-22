package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class AutomountFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Automount unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "automount"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.AUTOMOUNT
  }

  override fun getDisplayName(): @Nls String {
    return "Automount unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = AutomountFileType()
  }
}
