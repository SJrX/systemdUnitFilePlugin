package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class ServiceFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Service unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "service"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.SERVICE
  }

  override fun getDisplayName(): @Nls String {
    return "Service unit configuration (systemd)"
  }

  companion object {
    @JvmField
    val INSTANCE = ServiceFileType()
  }
}
