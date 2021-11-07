package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractUnitFileType extends LanguageFileType {

  protected AbstractUnitFileType(@NotNull Language language) {
    super(language);
  }
  
  protected AbstractUnitFileType(@NotNull Language language, boolean secondary) {
    super(language, secondary);
  }
  
  @Nls
  @Override
  public abstract @NotNull String getDisplayName();
  
}
