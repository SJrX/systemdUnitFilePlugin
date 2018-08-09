package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnitFilePropertyType extends ASTWrapperPsiElement {

  public UnitFilePropertyType(@NotNull ASTNode node) {
    super(node);
  }

  /**
   * Returns the key for the property.
   *
   * @return the key for this property
   */
  public String getKey() {
    ASTNode keyNode = this.getNode().findChildByType(UnitFileElementTypeHolder.KEY);
    if (keyNode != null) {
      // IMPORTANT: Convert embedded escaped spaces to simple spaces
      return keyNode.getText().replaceAll("\\\\ ", " ");
    } else {
      throw new IllegalStateException("Every instance of " + this.getClass() + "should have a key node underneath");
    }
  }

  /**
   *
   * @return
   */
  public TextRange getKeyTextRange() {

    ASTNode keyNode = this.getNode().findChildByType(UnitFileElementTypeHolder.KEY);
    Objects.requireNonNull(keyNode);
    return keyNode.getTextRange().shiftLeft(keyNode.getStartOffset());
  }

  /**
   * Returns the value for the property.
   *
   * @return the value for this property
   */
  public String getValue() {
    ASTNode valueNode = this.getNode().findChildByType(UnitFileElementTypeHolder.VALUE);
    if (valueNode != null) {
      return valueNode.getText();
    } else {
      throw new IllegalStateException("Every instance of " + this.getClass() + "should have a value node underneath");
    }
  }

}
