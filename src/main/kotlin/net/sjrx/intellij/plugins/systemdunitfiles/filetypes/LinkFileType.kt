package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class LinkFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Network Device Configuration (systemd-networkd)"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "link"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.LINK
  }

  override fun getDisplayName(): @Nls String {
    return "Network Device Configuration (systemd-networkd)"
  }

  companion object {
    val INSTANCE = LinkFileType()
  }
}
