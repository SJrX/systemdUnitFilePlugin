package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class TargetFileType extends LanguageFileType {
  public static final TargetFileType INSTANCE = new TargetFileType();

  private TargetFileType() {
    super(UnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Target unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".target\" encodes information about a target unit of systemd, which is used "
           + "for grouping units and as well-known synchronization points during start-up.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "target";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return UnitFileIcon.FILE;
  }
}
