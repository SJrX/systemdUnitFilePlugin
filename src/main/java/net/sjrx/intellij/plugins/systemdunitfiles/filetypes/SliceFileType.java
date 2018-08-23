package net.sjrx.intellij.plugins.systemdunitfiles.filetypes;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class SliceFileType extends LanguageFileType {
  public static final SliceFileType INSTANCE = new SliceFileType();

  private SliceFileType() {
    super(SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Slice unit configuration for systemd";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "A unit configuration file whose name ends in \".slice\" encodes information about a slice unit. A slice unit "
           + "is a concept for hierarchically managing resources of a group of processes. This management is performed by creating a "
           + "node in the Linux Control Group (cgroup) tree. Units that manage processes (primarily scope and service units) may be "
           + "assigned to a specific slice. For each slice, certain resource limits may be set that apply to all processes of all "
           + "units contained in that slice. Slices are organized hierarchically in a tree. The name of the slice encodes the location "
           + "in the tree. The name consists of a dash-separated series of names, which describes the path to the slice from the root "
           + "slice. The root slice is named -.slice. Example: foo-bar.slice is a slice that is located within foo.slice, which in "
           + "turn is located in the root slice -.slice.";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "slice";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }
}
