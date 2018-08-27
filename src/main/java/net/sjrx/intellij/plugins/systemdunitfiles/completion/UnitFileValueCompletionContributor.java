package net.sjrx.intellij.plugins.systemdunitfiles.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFileSectionGroupsImpl;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import org.jetbrains.annotations.NotNull;

public class UnitFileValueCompletionContributor extends CompletionContributor {
  
  private SemanticDataRepository sdr = SemanticDataRepository.getInstance();
  
  /**
   * Default constructor.
   */
  public UnitFileValueCompletionContributor() {
    extend(CompletionType.BASIC,
      PlatformPatterns.psiElement(UnitFileElementTypeHolder.VALUE).withLanguage(UnitFileLanguage.INSTANCE),
      new CompletionProvider<CompletionParameters>() {
        
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      ProcessingContext context,
                                      @NotNull CompletionResultSet resultSet) {
          if (parameters.getPosition().getParent() == null) {
            return;
          }
          
          if ((parameters.getPosition().getParent().getParent() == null)
              || (!(parameters.getPosition().getParent().getParent() instanceof UnitFileSectionGroupsImpl))) {
            return;
          }

          String sectionName = ((UnitFileSectionGroupsImpl) parameters.getPosition().getParent().getParent()).getSectionName();
          String keyName = ((UnitFilePropertyType) parameters.getPosition().getParent()).getKey();
  
          for (String value : sdr.getOptionValidator(sectionName, keyName).getAutoCompleteOptions()) {
            LookupElementBuilder builder =
              LookupElementBuilder.create(value)
                .withIcon(UnitFileIcon.FILE).appendTailText("(" + keyName + " value)", true);
    
            resultSet.addElement(builder);
    
          }
        }
      }
    );
  }
  
  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    super.fillCompletionVariants(parameters, result);
  }
}
