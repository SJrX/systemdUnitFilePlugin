package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.psi.tree.IElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class UnitFileElementType extends IElementType {

  public UnitFileElementType(@NotNull @NonNls String debugName) {
    super(debugName, UnitFileLanguage.INSTANCE);
  }
}
