// This is a generated file. Not intended for manual editing.
package net.sjrx.intellij.plugins.systemdunitfiles.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class UnitFileVisitor extends PsiElementVisitor {

  public void visitProperty(@NotNull UnitFileProperty o) {
    visitPsiElement(o);
  }

  public void visitSectionGroups(@NotNull UnitFileSectionGroups o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull UnitFileValue o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
