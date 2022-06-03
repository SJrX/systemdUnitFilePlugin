package net.sjrx.intellij.plugins.systemdunitfiles.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.OptionValueInformation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.stream.Collectors;

public class UnitFileValueCompletionContributor extends CompletionContributor {
  
  
  /**
   * Default constructor.
   */
  public UnitFileValueCompletionContributor() {
    /*
     * We only look at completed values here because I haven't found a case where we have any auto completion on values with white space
     * which would be compatible with CONTINUING_VALUES. The only case that comes to mind that is close is Documentation= could support
     * auto completing the prefix http:// https://, etc... and that could support a case like:
     *
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
          PsiElement position = parameters.getPosition();
          UnitFileProperty property = PsiTreeUtil.getParentOfType(position, UnitFileProperty.class);
          UnitFileSectionGroups section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups.class);

          if ((property == null) || (section == null)) {
            return;
          }

          String sectionName = section.getSectionName();
          String keyName = property.getKey();
          
          SemanticDataRepository sdr = SemanticDataRepository.getInstance();
          OptionValueInformation validator = sdr.getOptionValidator(sectionName, keyName);

          resultSet.addAllElements(
            validator.getAutoCompleteOptions(property.getProject())
              .stream()
              .map(LookupElementBuilder::create)
              .collect(Collectors.toCollection(HashSet::new))
          );
        }
      }
    );
  }
}
