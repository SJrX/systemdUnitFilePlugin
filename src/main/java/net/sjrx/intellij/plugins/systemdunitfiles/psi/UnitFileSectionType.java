package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class UnitFileSectionType extends ASTWrapperPsiElement {

  public UnitFileSectionType(@NotNull ASTNode node) {
    super(node);
  }

  /**
   * Returns the section name of a section in a file.
   *
   * @return the section name
   */
  public String getSectionName() {
    String sectionNameWithBrackets = getNode().getText();

    return sectionNameWithBrackets.substring(1, sectionNameWithBrackets.length() - 1);
  }

}
