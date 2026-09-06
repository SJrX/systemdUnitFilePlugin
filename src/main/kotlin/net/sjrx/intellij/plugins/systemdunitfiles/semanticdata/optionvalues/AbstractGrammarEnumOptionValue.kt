package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator


/**
 * Enum-style validator backed by the parser-combinator engine instead of a plain set-membership check.
 *
 * Routing through the grammar engine gives each enum precise error highlighting and
 * replace-with-valid-choice quick-fixes. The curated choice list is still returned for
 * autocomplete, so completion does not regress (a bare [grammar.GrammarOptionValue] would
 * suggest nothing).
 */
abstract class AbstractGrammarEnumOptionValue(
  private val validOptions: Set<String>,
  validatorName: String,
) : SimpleGrammarOptionValues(validatorName, buildGrammar(validOptions)) {

  override fun getAutoCompleteOptions(project: Project): Set<String> = validOptions

  override fun invalidValueMessage(key: String, badValue: String): String {
    return "$key's value '$badValue' does not match one of the expected values: $validOptions"
  }

  companion object {
    /**
     * Turn the set of valid enum spellings into the grammar the engine validates against.
     */
    private fun buildGrammar(validOptions: Set<String>): Combinator {
      return SequenceCombinator(FlexibleLiteralChoiceTerminal(choices = validOptions.toTypedArray(), ignoreCase = false), EOF())
    }
  }
}
