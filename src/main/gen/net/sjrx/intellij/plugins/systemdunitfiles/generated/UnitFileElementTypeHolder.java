// This is a generated file. Not intended for manual editing.
package net.sjrx.intellij.plugins.systemdunitfiles.generated;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileTokenType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.*;

public interface UnitFileElementTypeHolder {

  IElementType PROPERTY = new UnitFileElementType("PROPERTY");

  IElementType COMMENT = new UnitFileTokenType("COMMENT");
  IElementType CRLF = new UnitFileTokenType("CRLF");
  IElementType KEY = new UnitFileTokenType("KEY");
  IElementType SECTION = new UnitFileTokenType("SECTION");
  IElementType SEPARATOR = new UnitFileTokenType("SEPARATOR");
  IElementType VALUE = new UnitFileTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == PROPERTY) {
        return new UnitFilePropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
