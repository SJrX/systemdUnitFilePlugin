// This is a generated file. Not intended for manual editing.
package net.sjrx.intellij.plugins.systemdunitfiles.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.*;

public class UnitFileSectionGroupsImpl extends ASTWrapperPsiElement implements UnitFileSectionGroups {

  public UnitFileSectionGroupsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull UnitFileVisitor visitor) {
    visitor.visitSectionGroups(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof UnitFileVisitor) accept((UnitFileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<UnitFileProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, UnitFileProperty.class);
  }

}
