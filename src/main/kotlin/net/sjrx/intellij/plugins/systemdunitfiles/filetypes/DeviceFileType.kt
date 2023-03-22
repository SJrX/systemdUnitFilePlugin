package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class DeviceFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Device unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "device"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.DEVICE
  }

  override fun getDisplayName(): @Nls String {
    return "Device unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = DeviceFileType()
  }
}
