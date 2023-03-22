package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class SliceFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Slice unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "slice"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.SLICE
  }

  override fun getDisplayName(): @Nls String {
    return "Slice unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = SliceFileType()
  }
}
