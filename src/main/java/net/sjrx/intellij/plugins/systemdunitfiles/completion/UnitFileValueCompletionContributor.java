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
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFilePropertyImpl;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFileSectionGroupsImpl;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import org.jetbrains.annotations.NotNull;

public class UnitFileValueCompletionContributor extends CompletionContributor {
  
  private SemanticDataRepository sdr = SemanticDataRepository.getInstance();
  
  /**
   * Default constructor.
   */
  public UnitFileValueCompletionContributor() {
    /*
     * We only look at completed values here because I haven't found a case where we have any auto completion on values with white space
     * which would be compatible with CONTINUING_VALUES. The only case that comes to mind that is close is Documentation= could support
     * auto completing the prefix http:// https://, etc... and that could support a case like:
     *
     * Documentation=htt<COMPLETE_HERE>    \
     *  man:hello
     *
     * But that isn't implemented currently.
     */
    extend(CompletionType.BASIC,
      PlatformPatterns.psiElement(UnitFileElementTypeHolder.COMPLETED_VALUE).withLanguage(UnitFileLanguage.INSTANCE),
      new CompletionProvider<CompletionParameters>() {
  
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet resultSet) {
          
          if ((parameters.getPosition().getParent() == null) || (parameters.getPosition().getParent().getParent() == null)) {
            return;
          }
  
  
          if (parameters.getPosition().getParent().getParent() instanceof UnitFileProperty) {
            UnitFilePropertyImpl ufp = (UnitFilePropertyImpl) parameters.getPosition().getParent().getParent();
            UnitFileSectionGroupsImpl ufsg = (UnitFileSectionGroupsImpl) ufp.getParent();
            
            String sectionName = ufsg.getSectionName();
            
            String keyName = ufp.getKey();
      
            for (String value : sdr.getOptionValidator(sectionName, keyName).getAutoCompleteOptions()) {
              LookupElementBuilder builder =
                LookupElementBuilder.create(value)
                  .withIcon(UnitFileIcon.FILE).appendTailText("(" + keyName + " value)", true);
        
              resultSet.addElement(builder);
            }
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
