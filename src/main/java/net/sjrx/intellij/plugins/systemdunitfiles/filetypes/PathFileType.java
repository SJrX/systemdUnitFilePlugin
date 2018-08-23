package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class PathFileType extends LanguageFileType {
  public static final PathFileType INSTANCE = new PathFileType();

  private PathFileType() {
    super(SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Path unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".path\" encodes information about a path monitored by systemd,"
           + " for path-based activation.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "path";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }
}
