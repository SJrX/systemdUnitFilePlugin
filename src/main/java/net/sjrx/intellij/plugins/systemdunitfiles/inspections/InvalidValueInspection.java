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

public class InvalidValueInspection extends LocalInspectionTool {
  
  private SemanticDataRepository sdr = SemanticDataRepository.getInstance();
  
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    
    ArrayList<ProblemDescriptor> problems = new ArrayList<>();
    
    SemanticDataRepository sdr = SemanticDataRepository.getInstance();
    
    Collection<UnitFileSectionType> sections = PsiTreeUtil.collectElementsOfType(file, UnitFileSectionType.class);
    
    for (UnitFileSectionType section : sections) {
      
      
      Collection<UnitFilePropertyType> keyAndValuePropertiesInSection =
        PsiTreeUtil.collectElementsOfType(section, UnitFilePropertyType.class);
      
      for (final UnitFilePropertyType keyAndValueProperty : keyAndValuePropertiesInSection) {
  
        if (!sdr.getOptionValidator(section.getSectionName(), keyAndValueProperty.getValue())
               .isValidValue(keyAndValueProperty.getValue())) {
          problems.add(manager.createProblemDescriptor(keyAndValueProperty.getValueNode().getPsi(), "Invalid value", true,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
        }
        
      }
    }
    
    return problems.toArray(new ProblemDescriptor[0]);
  }
  
  
}
