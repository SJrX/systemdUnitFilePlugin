package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileLexer;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class UnitFileLexerAdapter extends FlexAdapter {

  public UnitFileLexerAdapter() {
    super(new UnitFileLexer((Reader) null));
  }

}
