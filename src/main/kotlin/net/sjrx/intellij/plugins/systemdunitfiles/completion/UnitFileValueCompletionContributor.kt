package net.sjrx.intellij.plugins.systemdunitfiles.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.nextTokenChoices
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
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
               val sdr = SemanticDataRepository.instance
               val fileClass = section.containingFile.fileClass()

               val validator = sdr.getOptionValidator(fileClass, sectionName, keyName)

               if (ExperimentalSettings.getInstance(property.project).state.useGrammarParseEngine &&
                 validator is GrammarOptionValue
               ) {
                 addGrammarCompletions(parameters, validator, property, resultSet)
                 return
               }

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

  /**
   * Experimental grammar-based completion (#467 / #343): suggest the literal/choice tokens the
   * grammar could accept at the caret. We read the value text up to the caret, find the tightest
   * split where the grammar expects an enumerable token whose choices match what's already typed,
   * and set that as the prefix so the platform filters correctly (otherwise a partial token like
   * "~AF_IN" would be the prefix and match nothing).
   */
  private fun addGrammarCompletions(
    parameters: CompletionParameters,
    validator: GrammarOptionValue,
    property: UnitFileProperty,
    resultSet: CompletionResultSet,
  ) {
    val valueStart = property.valueNode?.psi?.textRange?.startOffset ?: return
    val caret = parameters.offset
    if (caret < valueStart) return
    val pre = parameters.position.containingFile.text.substring(valueStart, caret)
    val combinator = validator.combinator

    // Case 1 — completing a partial token. Find the token start: the longest non-empty trailing
    // word for which the grammar expects an enumerable choice that STRICTLY extends it (i.e. there's
    // more to type). This beats just advancing, so "h" completes to "home" rather than offering "="
    // (the lenient terminal would otherwise treat "h" as a finished identifier).
    for (split in 0 until pre.length) {
      ProgressManager.checkCanceled()
      val word = pre.substring(split)
      val choices = combinator.nextTokenChoices(pre.substring(0, split))
      if (choices.any { it.length > word.length && it.startsWith(word) }) {
        resultSet.withPrefixMatcher(word).addAllElements(choices.map { LookupElementBuilder.create(it) })
        return
      }
    }

    // Case 2 — at a fresh token boundary (e.g. empty value, or after a complete token like "~" or
    // "root="). Offer whatever can come next, matched against an empty prefix.
    ProgressManager.checkCanceled()
    val choices = combinator.nextTokenChoices(pre)
    if (choices.isNotEmpty()) {
      resultSet.withPrefixMatcher("").addAllElements(choices.map { LookupElementBuilder.create(it) })
    }
  }
}
