package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.OptionValueInformation;
import org.jetbrains.annotations.NotNull;

public class InvalidValueInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    PsiFile file = holder.getFile();
    if (!(file instanceof UnitFile) || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      return PsiElementVisitor.EMPTY_VISITOR;
    }
    return new MyVisitor(holder);
  }

  private static class MyVisitor extends UnitFileVisitor {
    @NotNull
    private final ProblemsHolder holder;

    public MyVisitor(@NotNull ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitPropertyType(@NotNull UnitFilePropertyType property) {
      UnitFileSectionGroups section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups.class);
      if (section == null) {
        return;
      }

      String value = property.getValueText();
      if (value == null) {
        return;
      }
      String key = property.getKey();
      OptionValueInformation ovi = SemanticDataRepository.getInstance().getOptionValidator(section.getSectionName(), key);
      ovi.generateProblemDescriptors(property, holder);
    }
  }
}
