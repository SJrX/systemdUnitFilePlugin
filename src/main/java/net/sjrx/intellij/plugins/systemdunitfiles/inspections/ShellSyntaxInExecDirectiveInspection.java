package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFile;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ExecOptionValue;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.OptionValueInformation;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellSyntaxInExecDirectiveInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    PsiFile file = holder.getFile();
    if (!(file instanceof UnitFile) || !file.getLanguage().isKindOf(UnitFileLanguage.INSTANCE)) {
      return PsiElementVisitor.EMPTY_VISITOR;
    }
    return new MyVisitor(holder);
  }

  enum LexerTypes {
    WHITESPACE("\\s+"),
    STRING_LITERAL("((?<![\\\\])['\"])((?:.(?!(?<![\\\\])\\1))*.?)\\1"),
    NONSPECIAL_CHARACTERS("[^<>|&\\s]+"),
    PIPE("\\|"),
    OUTPUT_REDIRECTS(">{1,2}"),
    INPUT_REDIRECTS("<{1,2}"),
    BACKGROUND("&"),
    SINGLE_CHARACTER(".");
    Pattern pattern;
    
    LexerTypes(String regexp) {
      this.pattern = Pattern.compile("^" + regexp);
    }
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
      
      if (!(ovi instanceof ExecOptionValue)) {
        return;
      }

      StringBuilder completedString = new StringBuilder();
      
      InspectionManager im = holder.getManager();
      
      while (completedString.length() < value.length()) {
        for (LexerTypes lexerType : LexerTypes.values()) {
          String searchString = value.substring(completedString.length());
          Matcher m = lexerType.pattern.matcher(searchString);
          if (m.find()) {
            switch (lexerType) {
              case PIPE:
                ProblemDescriptor pd = im.createProblemDescriptor(property.getValueNode().getPsi(),
                  TextRange.create(completedString.length(), m.group(0).length() + completedString.length()),
                  "systemd does not support piping of output in " + property.getKey() + ". It is recommended you wrap this command in /bin/sh -c",
                  ProblemHighlightType.WARNING,
                  false);
                holder.registerProblem(pd);
                break;
              case INPUT_REDIRECTS:
                pd = im.createProblemDescriptor(property.getValueNode().getPsi(),
                  TextRange.create(completedString.length(), m.group(0).length() + completedString.length()),
                  "systemd does not input redirects in " + property.getKey() + ". It is recommended you wrap this command in /bin/sh -c",
                  ProblemHighlightType.WARNING,
                  false);
                holder.registerProblem(pd);
                break;
              case OUTPUT_REDIRECTS:
                pd = im.createProblemDescriptor(property.getValueNode().getPsi(),
                  TextRange.create(completedString.length(), m.group(0).length() + completedString.length()),
                  "systemd does not redirecting output in " + property.getKey() + ". It is recommended you wrap this command in /bin/sh -c ",
                  ProblemHighlightType.WARNING,
                  false);
                holder.registerProblem(pd);
                break;
              case BACKGROUND:
                pd = im.createProblemDescriptor(property.getValueNode().getPsi(),
                  TextRange.create(completedString.length(), m.group(0).length() + completedString.length()),
                  "systemd does not backgrounding in " + property.getKey() + ". It is recommended you wrap this command in /bin/sh -c ",
                  ProblemHighlightType.WARNING,
                  false);
                holder.registerProblem(pd);
                break;
              default:
                break;
            }
            completedString.append(m.group(0));
            break;
          }
  
         
        }
      }
    }
  }
}
