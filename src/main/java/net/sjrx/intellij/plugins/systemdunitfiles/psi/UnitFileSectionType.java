package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface UnitFileSectionType extends PsiElement {

  /**
   * Returns the section name of a section in a file.
   *
   * @return the section name
   */
  @NotNull
  default String getSectionName() {
    String text = getNode().getFirstChildNode().getText().trim();
    if (text.isEmpty()) return text;
    if (text.charAt(text.length() - 1) == ']') {
      return text.substring(1, text.length() - 1);
    } else {
      // unterminated section name
      return text.substring(1);
    }
  }

}
