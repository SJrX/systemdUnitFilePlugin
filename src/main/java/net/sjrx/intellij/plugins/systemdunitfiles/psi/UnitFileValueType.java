package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import org.jetbrains.annotations.NotNull;

public interface UnitFileValueType extends PsiElement {

  TokenSet valueTypes = TokenSet.create(UnitFileElementTypeHolder.COMPLETED_VALUE, UnitFileElementTypeHolder.CONTINUING_VALUE);

  /**
   * Retrieves the logical value for this node.
   * <p/>
   * This will go over all the value containing nodes and skip the comments. It may not actually match systemd exactly right now, but that
   * probably doesn't matter at the moment.
   *
   * @return retrieves the value of this value.
   */
  @NotNull
  default String getValue() {
    StringBuilder sb = null;
    PsiElement psiChild = getFirstChild();
    while (psiChild != null) {
      if (valueTypes.contains(psiChild.getNode().getElementType())) {
        if (sb == null) sb = new StringBuilder();
        sb.append(psiChild.getText().trim().replaceFirst("\\\\$", " "));
      }
      psiChild = psiChild.getNextSibling();
    }

    if (sb == null) return "";

    // Transform \\n (trailing line to a space), as per: https://www.freedesktop.org/software/systemd/man/systemd.syntax.html#
    return sb.toString().trim();
  }
}
