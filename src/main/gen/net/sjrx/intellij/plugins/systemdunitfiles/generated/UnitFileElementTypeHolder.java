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
  IElementType SECTION_GROUPS = new UnitFileElementType("SECTION_GROUPS");

  IElementType COMMENT = new UnitFileTokenType("COMMENT");
  IElementType COMPLETED_VALUE = new UnitFileTokenType("COMPLETED_VALUE");
  IElementType CONTINUING_VALUE = new UnitFileTokenType("CONTINUING_VALUE");
  IElementType CRLF = new UnitFileTokenType("CRLF");
  IElementType KEY = new UnitFileTokenType("KEY");
  IElementType SECTION = new UnitFileTokenType("SECTION");
  IElementType SEPARATOR = new UnitFileTokenType("SEPARATOR");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == PROPERTY) {
        return new UnitFilePropertyImpl(node);
      }
      else if (type == SECTION_GROUPS) {
        return new UnitFileSectionGroupsImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
