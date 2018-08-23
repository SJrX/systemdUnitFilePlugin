package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class SocketFileType extends LanguageFileType {
  public static final SocketFileType INSTANCE = new SocketFileType();

  private SocketFileType() {
    super(SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Socket unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".socket\" encodes information about an IPC or network socket or a file "
           + "system FIFO controlled and supervised by systemd, for socket-based activation.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "socket";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }
}
