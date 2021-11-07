package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class SliceFileType extends AbstractUnitFileType {
  public static final SliceFileType INSTANCE = new SliceFileType();

  private SliceFileType() {
    super(UnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Slice unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return getDisplayName();
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "slice";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return UnitFileIcon.FILE;
  }
  
  @Nls
  @Override
  public @NotNull String getDisplayName() {
    return "Slice unit configuration (systemd)";
  }
}
