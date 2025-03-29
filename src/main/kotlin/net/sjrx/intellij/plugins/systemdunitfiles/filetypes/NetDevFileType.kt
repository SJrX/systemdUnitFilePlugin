package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class NetDevFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Virtual Network Device Configuration (systemd-networkd)"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "netdev"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.NETDEV
  }

  override fun getDisplayName(): @Nls String {
    return "Virtual Network Device Configuration (systemd-networkd)"
  }

  companion object {
    val INSTANCE = NetDevFileType()
  }
}
