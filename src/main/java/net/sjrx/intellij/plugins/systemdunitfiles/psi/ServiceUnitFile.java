package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType;
import org.jetbrains.annotations.NotNull;

public class ServiceUnitFile extends PsiFileBase {

  public ServiceUnitFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, SystemdUnitFileLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return ServiceFileType.INSTANCE;
  }

  public String toString() {
    return "systemd service unit configuration";
  }
}
