package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class TimerFileType private constructor() : AbstractUnitFileType(UnitFileLanguage.INSTANCE) {
  override fun getName(): String {
    return "Timer unit configuration for systemd"
  }

  override fun getDescription(): String {
    return displayName
  }

  override fun getDefaultExtension(): String {
    return "timer"
  }

  override fun getIcon(): Icon? {
    return UnitFileIcon.TIMER
  }

  override fun getDisplayName(): @Nls String {
    return "Timer unit configuration (systemd)"
  }

  companion object {
    val INSTANCE = TimerFileType()
  }
}
