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
import java.util.regex.Matcher;
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
        String text = getDeprecatedOptionText(section, keyAndValueProperty);
        
        if (text != null) {
          problems.add(manager.createProblemDescriptor(keyAndValueProperty.getKeyNode().getPsi(), text, true,
            ProblemHighlightType.LIKE_DEPRECATED, isOnTheFly));
        }
      }
    }
    
    return problems.toArray(new ProblemDescriptor[0]);
  }
  
  private String getDeprecatedOptionText(UnitFileSectionType section, UnitFilePropertyType keyAndValueProperty) {
    
    
    String sectionName = section.getSectionName();
    String key = keyAndValueProperty.getKey();
    
    String deprecationText =
      deprecatedKeyAndValueToText.computeIfAbsent(sectionName, k -> new HashMap<>()).computeIfAbsent(key, keyName -> {
        
        // We store empty strings in our cache for values that don't matter
        String documentation = sdr.getDocumentationContentForKeyInSection(section.getSectionName(), keyAndValueProperty.getKey());
        
        if (documentation == null) {
          return "";
        }

        Matcher m = DEPRECATED_COMMENT_REGEX.matcher(documentation);
        
        if (m.find()) {
          String replacement = m.group(1);
          // Get rid of the var tags.
          return "This option is deprecated. Use " + replacement.replaceAll("</?var>","") + " instead.";
        } else {
          return "";
        }
      });
    
    
    if (deprecationText.isEmpty()) {
      return null;
    } else {
      return deprecationText;
    }
  }
}
