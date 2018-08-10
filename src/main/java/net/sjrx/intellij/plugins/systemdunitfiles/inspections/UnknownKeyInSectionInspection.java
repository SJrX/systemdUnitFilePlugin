package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.SemanticDataRepository;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class UnknownKeyInSectionInspection extends LocalInspectionTool {

  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {

    ArrayList<ProblemDescriptor> problems = new ArrayList<>();

    /*
     * TODO change to Java Streams maybe?
     */

    SemanticDataRepository sdr = SemanticDataRepository.getInstance();

    Collection<UnitFileSectionType> sections = PsiTreeUtil.collectElementsOfType(file, UnitFileSectionType.class);

    for (UnitFileSectionType section : sections) {
      Collection<UnitFilePropertyType> elements = PsiTreeUtil.collectElementsOfType(section, UnitFilePropertyType.class);

      for (final UnitFilePropertyType e : elements) {

        if (!sdr.getAllowedKeywordsInSection(section.getSectionName()).contains(e.getKey())) {
          // TODO Figure out what highlight to use

          problems.add(
            new ProblemDescriptorBase(e, e, "This type is unknown", new LocalQuickFix[0], ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                      false, e.getKeyTextRange(), true, isOnTheFly));
        }
      }
    }

    return problems.toArray(new ProblemDescriptor[0]);
  }
}
