package net.sjrx.intellij.plugins.systemdunitfiles.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import java.util.function.Supplier
import java.util.stream.Collectors

class UnitFileValueCompletionContributor : CompletionContributor() {
  /**
   * Default constructor.
   */
  init {
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
           object : CompletionProvider<CompletionParameters>() {
             override fun addCompletions(parameters: CompletionParameters,
                                         context: ProcessingContext,
                                         resultSet: CompletionResultSet) {
               val position = parameters.position
               val property = PsiTreeUtil.getParentOfType(position, UnitFileProperty::class.java)
               val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java)
               if (property == null || section == null) {
                 return
               }
               val sectionName = section.sectionName
               val keyName = property.key
               val sdr = SemanticDataRepository.getInstance()
               val validator = sdr.getOptionValidator(sectionName, keyName)
               resultSet.addAllElements(
                 validator.getAutoCompleteOptions(property.project)
                   .stream()
                   .map { lookupString: String? -> LookupElementBuilder.create(lookupString!!) }
                   .collect(Collectors.toCollection(
                     Supplier { HashSet() }))
               )
             }
           }
    )
  }
}
