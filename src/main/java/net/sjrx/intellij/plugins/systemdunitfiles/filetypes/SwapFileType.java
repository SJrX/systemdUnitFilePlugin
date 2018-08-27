package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class SwapFileType extends LanguageFileType {
  public static final SwapFileType INSTANCE = new SwapFileType();

  private SwapFileType() {
    super(UnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Swap unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".swap\" encodes information about a swap device or file for memory"
           + " paging controlled and supervised by systemd.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "swap";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return UnitFileIcon.FILE;
  }
}
