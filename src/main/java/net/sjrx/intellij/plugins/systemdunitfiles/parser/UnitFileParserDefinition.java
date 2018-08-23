package net.sjrx.intellij.plugins.systemdunitfiles.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileParser;
import net.sjrx.intellij.plugins.systemdunitfiles.lexer.DebugLexerAdapter;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.ServiceUnitFile;
import org.jetbrains.annotations.NotNull;

public class UnitFileParserDefinition implements ParserDefinition {

  private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

  private static final TokenSet COMMENTS = TokenSet.create(UnitFileElementTypeHolder.COMMENT);

  private static final IFileElementType FILE = new IFileElementType(SystemdUnitFileLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new DebugLexerAdapter();
  }

  @Override
  public PsiParser createParser(Project project) {
    return new UnitFileParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return UnitFileElementTypeHolder.Factory.createElement(node);
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new ServiceUnitFile(viewProvider);
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACES;
  }
}
