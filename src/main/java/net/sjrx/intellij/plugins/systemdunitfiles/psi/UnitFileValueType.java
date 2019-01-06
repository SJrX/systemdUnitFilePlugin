package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitFileValueType extends ASTWrapperPsiElement {
  
  private static Set<IElementType> valueTypes;
  
  static {
    Set<IElementType> valueTypes = new HashSet<>();
    
    valueTypes.add(UnitFileElementTypeHolder.COMPLETED_VALUE);
    valueTypes.add(UnitFileElementTypeHolder.CONTINUING_VALUE);
    
    UnitFileValueType.valueTypes = Collections.unmodifiableSet(valueTypes);
  }
  
  
  public UnitFileValueType(@NotNull ASTNode node) {
    super(node);
  }
  
  public ASTNode getValueNode() {
    return this.getNode();
  }
  
  
  /**
   * Retrieves the logical value for this node.
   * <p/>
   * This will go over all the value containing nodes and skip the comments. It may not actually match systemd exactly right now, but that
   * probably doesn't matter at the moment.
   *
   * @return retrieves the value of this value.
   */
  public String getValue() {
    
    String value = getAllChildrenForRealzies()
      .filter(child -> valueTypes.contains(child.getNode().getElementType()))
       .map(PsiElement::getText)
       .collect(Collectors.joining());
    
    // Transform \\n (trailing line to a space), as per: https://www.freedesktop.org/software/systemd/man/systemd.syntax.html#
    return value.replaceAll("\\\n", " ").trim();
  }
  
  /**
   * Returns all the children of the node as a stream.
   *
   * @see com.intellij.psi.PsiElement#getChildren() - doesn't return leaf nodes in some cases I don't know why.
   *
   */
  private Stream<PsiElement> getAllChildrenForRealzies() {
    
    PsiElement child = this.getFirstChild();
    if (child == null) {
      return Stream.empty();
    }
  
    List<PsiElement> elements = new ArrayList<>();
    
    do {
      elements.add(child);
    }  while (child != getLastChild());
    
    return elements.stream();
  }
  
}
