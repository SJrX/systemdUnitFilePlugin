package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class MountFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Mount unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "mount"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.FILE
  }

  override fun getDisplayName(): @Nls String {
    return "Mount unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = MountFileType()
  }
}
