package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class MountFileType extends LanguageFileType {
  public static final MountFileType INSTANCE = new MountFileType();

  private MountFileType() {
    super(SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Mount unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".mount\" encodes information about a file system mount "
           + "point controlled and supervised by systemd.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "mount";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }
}
