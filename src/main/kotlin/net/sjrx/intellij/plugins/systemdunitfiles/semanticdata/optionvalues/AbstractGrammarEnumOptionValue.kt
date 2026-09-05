package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Enum-style validator backed by the parser-combinator engine instead of a plain set-membership check.
 *
 * Same "just give me the valid choices" API as [AbstractEnumOptionValue], but by routing through the
 * grammar engine each migrated enum gets precise error highlighting and replace-with-valid-choice
 * quick-fixes for free. The curated choice list is still returned for autocomplete, so completion does
 * not regress (a bare [grammar.GrammarOptionValue] would suggest nothing).
 *
 * Migration plan: move validators in [EnumOptionValues] onto this base one merge request at a time.
 * This class is intentionally dormant until the first subclass exists — nothing constructs it yet, so
 * [buildGrammar] is not invoked and the existing suite is unaffected.
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
