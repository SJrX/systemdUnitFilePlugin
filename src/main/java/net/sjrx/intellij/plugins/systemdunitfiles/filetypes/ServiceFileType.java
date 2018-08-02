package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;

import javax.swing.Icon;

import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServiceFileType extends LanguageFileType {
  public static final ServiceFileType INSTANCE = new ServiceFileType();

  private ServiceFileType() {
    super(SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "systemd service file";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Service unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "service";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }
}
