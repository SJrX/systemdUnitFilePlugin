package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnitFileTokenType extends IElementType {
  public UnitFileTokenType(@NotNull String debugName) {
    super(debugName, SystemdUnitFileLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "UnitFileTokenType{" + super.toString() + "}";
  }
}
