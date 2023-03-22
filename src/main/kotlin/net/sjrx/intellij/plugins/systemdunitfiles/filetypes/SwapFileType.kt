package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class SwapFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Swap unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "swap"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.SWAP
  }

  override fun getDisplayName(): @Nls String {
    return "Swap unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = SwapFileType()
  }
}
