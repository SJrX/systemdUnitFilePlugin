package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import org.jetbrains.annotations.NotNull;

public interface UnitFilePropertyType extends PsiElement {

  /**
   * Returns the key for the property.
   *
   * @return the key for this property
   */
  @NotNull
  default String getKey() {
    ASTNode keyNode = this.getKeyNode();

    return keyNode.getText().replaceAll("\\\\ ", " ");
  }

  /**
   * Returns the key node for the property.
   *
   * @return the key node for this property
   */
  @NotNull
  default ASTNode getKeyNode() {
    ASTNode keyNode = this.getNode().findChildByType(UnitFileElementTypeHolder.KEY);
    if (keyNode != null) {
      return keyNode;
    } else {
      throw new IllegalStateException("Every instance of " + this.getClass() + " should have a key node underneath");
    }
  }

  /**
   * Returns the value for the property.
   *
   * @return the value for this property
   */
  default ASTNode getValueNode() {
    // Probably need to fix this as well
    return this.getNode().findChildByType(UnitFileElementTypeHolder.VALUE);
  }

  /**
   * Returns the value for the property.
   *
   * @return the value for this property
   */
  default String getValueText() {
    if (getValueNode() != null) {
      return ((UnitFileValueType) getValueNode().getPsi()).getValue();
    } else {
      return null;
    }
  }
}
