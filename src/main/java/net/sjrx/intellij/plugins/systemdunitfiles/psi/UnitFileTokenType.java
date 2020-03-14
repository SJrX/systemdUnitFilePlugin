package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.psi.tree.IElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import org.jetbrains.annotations.NotNull;

public class UnitFileTokenType extends IElementType {
  public UnitFileTokenType(@NotNull String debugName) {
    super(debugName, UnitFileLanguage.INSTANCE);
  }
}
