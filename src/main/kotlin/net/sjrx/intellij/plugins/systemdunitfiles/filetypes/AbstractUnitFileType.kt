package net.sjrx.intellij.plugins.systemdunitfiles.filetypes

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.annotations.Nls

abstract class AbstractUnitFileType : LanguageFileType {
  protected constructor(language: Language) : super(language)
  protected constructor(language: Language, secondary: Boolean) : super(language, secondary)

  abstract override fun getDisplayName(): @Nls String
}
