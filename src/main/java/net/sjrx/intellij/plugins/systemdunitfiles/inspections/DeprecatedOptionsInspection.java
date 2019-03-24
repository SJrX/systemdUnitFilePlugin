package net.sjrx.intellij.plugins.systemdunitfiles.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DeprecatedOptionsInspection extends LocalInspectionTool {
  
  private static final SemanticDataRepository sdr = SemanticDataRepository.getInstance();
  private static final Pattern DEPRECATED_COMMENT_REGEX = Pattern.compile("setting.+deprecated. Use (.+) instead.", Pattern.DOTALL);
  /**
   * Stores a mapping of section -> key -> string for deprecated text message, if the value is empty it is not deprecated.
   */
  private Map<String, Map<String, String>> deprecatedKeyAndValueToText = new HashMap<>();
  
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    ArrayList<ProblemDescriptor> problems = new ArrayList<>();
    
    SemanticDataRepository sdr = SemanticDataRepository.getInstance();
    
    Collection<UnitFileSectionType> sections = PsiTreeUtil.collectElementsOfType(file, UnitFileSectionType.class);
    
    for (UnitFileSectionType section : sections) {
      
      
      Collection<UnitFilePropertyType> keyAndValuePropertiesInSection =
        PsiTreeUtil.collectElementsOfType(section, UnitFilePropertyType.class);
      
      for (final UnitFilePropertyType keyAndValueProperty : keyAndValuePropertiesInSection) {
        if (sdr.isDeprecated(section.getSectionName(), keyAndValueProperty.getKey())) {
          String text = sdr.getDocumentationContentForKeyInSection(section.getSectionName(), keyAndValueProperty.getKey());
          problems.add(manager.createProblemDescriptor(keyAndValueProperty.getKeyNode().getPsi(), text.replaceAll("</?var>",""), true,
            ProblemHighlightType.LIKE_DEPRECATED, isOnTheFly));
        }
      }
    }
    
    return problems.toArray(new ProblemDescriptor[0]);
  }
}
