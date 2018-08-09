package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class UnknownKeyInSectionInspection extends LocalInspectionTool {

  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {

    ArrayList<ProblemDescriptor> problems = new ArrayList<>();


    Collection<UnitFileSectionGroups> sections = PsiTreeUtil.collectElementsOfType(file, UnitFileSectionGroups.class);

    for (UnitFileSectionGroups section : sections) {
      Collection<UnitFilePropertyType> elements = PsiTreeUtil.collectElementsOfType(section, UnitFilePropertyType.class);

      for (final UnitFilePropertyType e : elements) {

        final PsiElement thisElement = e;

        if (e.getKey().startsWith("D")) {
          problems.add(
            new ProblemDescriptorBase(e, e, "This type is unknown", new LocalQuickFix[0], ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false,  e.getKeyTextRange(),
                                      true, isOnTheFly));
        }
      }
    }






    /*
    file.accept(new PsiRecursiveElementWalkingVisitor() {
      @Override
      public void visitElement(PsiElement element) {

        super.visitElement(element);

      }
    }
    );
    */

    return problems.toArray(new ProblemDescriptor[0]);
  }
}
