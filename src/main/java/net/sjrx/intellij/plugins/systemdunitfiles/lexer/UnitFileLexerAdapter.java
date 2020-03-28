package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.FlexAdapter;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileLexer;

public class UnitFileLexerAdapter extends FlexAdapter {

  public UnitFileLexerAdapter() {
    super(new UnitFileLexer(null));
  }

}
