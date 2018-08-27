package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType;
import org.jetbrains.annotations.NotNull;

public class UnitFile extends PsiFileBase {

  public UnitFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, UnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return ServiceFileType.INSTANCE;
  }

  public String toString() {
    return "unit configuration file (systemd)";
  }
}
