package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class TimerFileType extends LanguageFileType {
  public static final TimerFileType INSTANCE = new TimerFileType();

  private TimerFileType() {
    super(UnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Timer unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".timer\" encodes information about a timer controlled and supervised by "
           + "systemd, for timer-based activation.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "timer";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return UnitFileIcon.FILE;
  }
}
