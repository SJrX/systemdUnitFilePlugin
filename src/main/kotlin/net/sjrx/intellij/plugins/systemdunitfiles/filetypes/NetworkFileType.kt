package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class NetworkFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Network Configuration (systemd-networkd)"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "network"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.NETWORK
  }

  override fun getDisplayName(): @Nls String {
    return "Network Configuration (systemd-networkd)"
  }

  companion object {
    val INSTANCE = NetworkFileType()
  }
}
