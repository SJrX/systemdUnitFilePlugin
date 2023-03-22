package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class PathFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Path unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "path"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.PATH
  }

  override fun getDisplayName(): @Nls String {
    return "Path unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = PathFileType()
  }
}
