package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugLexerAdapter extends LexerBase {

  private static final Logger LOG = Logger.getInstance(DebugLexerAdapter.class);
  private final UnitFileLexerAdapter ufla = new UnitFileLexerAdapter();

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    LOG.info("start(" + buffer + "," + startOffset + "," + endOffset + "," + initialState  + ") ==> void");
    ufla.start(buffer, startOffset, endOffset, initialState);
  }

  @Override
  public int getState() {

    int state =  ufla.getState();

    LOG.info("getState() ==> " + state);

    return state;
  }

  @Nullable
  @Override
  public IElementType getTokenType() {

    IElementType ooType =  ufla.getTokenType();

    LOG.info("getTokenType() ==> " + ooType);

    return ooType;
  }

  @Override
  public int getTokenStart() {
    int tokenStart = ufla.getTokenStart();

    LOG.info("getTokenStart() ==> " + tokenStart);

    return tokenStart;
  }

  @Override
  public int getTokenEnd() {
    int tokenEnd =  ufla.getTokenEnd();

    LOG.info("getTokenEnd() ==> " + tokenEnd);

    return tokenEnd;
  }

  @Override
  public void advance() {
    ufla.advance();

    LOG.info("advance() ==> void");
  }

  @NotNull
  @Override
  public CharSequence getBufferSequence() {
    CharSequence cs = ufla.getBufferSequence();

    LOG.info("getBufferSequence() ==> " + cs);

    return cs;
  }

  @Override
  public int getBufferEnd() {

    int be = ufla.getBufferEnd();

    LOG.info("getBufferEnd() ==> " + be);

    return be;
  }
}
