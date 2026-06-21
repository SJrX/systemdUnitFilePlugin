package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.ReplaceInvalidLiteralChoiceQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.OptionValueInformation
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

open class GrammarOptionValue(
  override val validatorName: String,
  val combinator: Combinator

) : OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    throw IllegalStateException("This should not be called")
  }

  /**
   * Generates problem descriptors based on the value.
   *
   * @param property - the Psi Element we are examining.
   * @param holder - A problem holder that we should add to.
   */
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val value = property.valueText ?: return

    if (ExperimentalSettings.getInstance(property.project).state.useGrammarParseEngine) {
      generateProblemDescriptorsViaParse(property, value, holder)
      return
    }

    val syntaticMatch = combinator.SyntacticMatch(value, 0)

    try {

      if (syntaticMatch.matchResult == -1) {

        // We couldn't match the syntax, and we don't have tokens
        // If we matched up to a specific point, we can highlight the rest.
        // If we matched to the end, it's unclear what to highlight (sometimes it can be the last char, maybe the whole thing), so we will match everything.
        val tr = if (syntaticMatch.longestMatch < value.length) {
          TextRange(syntaticMatch.longestMatch, value.length)
        } else {
          TextRange(0, value.length)
        }

        holder.registerProblem(property.valueNode.psi, "${property.key}'s value does not match the expected format. Possible reasons include unrecognized characters or premature end of input.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, tr)
        return
      }

      val semanticMatch = combinator.SemanticMatch(value, 0)

      if (semanticMatch.matchResult == -1) {

        if (semanticMatch.tokens.size < syntaticMatch.tokens.size) {
          // We couldn't fully understand everything, but syntactically recognized, let's highlight the first token as the problem.


          // Get the token from the semanticMatch tokens, where the match.longestMatch points to the token

          var tokenLength = 0
          var tokenIndex = 0
          for (tokens in syntaticMatch.tokens) {
            tokenLength += tokens.length
            if (tokenLength > semanticMatch.longestMatch) {
              break
            }
            tokenIndex++
          }

          val problemToken = syntaticMatch.tokens[tokenIndex]

          val problemTerminal = syntaticMatch.terminals[tokenIndex]


          val prefixLength = semanticMatch.longestMatch

          val tr = TextRange(prefixLength, prefixLength + problemToken.length)

          val quickFixes = mutableListOf<LocalQuickFix>()

          if (problemTerminal is LiteralChoiceTerminal) {
            for (choice in problemTerminal.choices) {
              quickFixes.add(ReplaceInvalidLiteralChoiceQuickFix(prefixLength, problemToken, choice))
            }
          } else if (problemTerminal is FlexibleLiteralChoiceTerminal) {
            for (choice in problemTerminal.choices) {
              quickFixes.add(ReplaceInvalidLiteralChoiceQuickFix(prefixLength, problemToken, choice))
            }
          }

          holder.registerProblem(property.valueNode.psi, "${property.key}'s value is correctly formatted but seems invalid.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, tr, *quickFixes.toTypedArray())
        } else {
          holder.registerProblem(property.valueNode.psi, "${property.key}'s value is correctly formatted but seems invalid.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }



        return
      }

    } catch (e: RuntimeException) {
      LOG.error("Error while processing ${property.key} with value ${value}", e)
      return holder.registerProblem(property.valueNode.psi, "Internal error, please report an bug to the systemd plugin. Include the Key and Value used.", ProblemHighlightType.ERROR)
    }

    return


  }

  /**
   * Experimental path (#467): validate via the list-of-successes engine and map the [ParseOutcome]
   * onto the same problem descriptors the SyntacticMatch/SemanticMatch path produces. Gated behind
   * [ExperimentalSettings.useGrammarParseEngine] so the original engine remains the default.
   */
  private fun generateProblemDescriptorsViaParse(property: UnitFilePropertyType, value: String, holder: ProblemsHolder) {
    val outcome = try {
      combinator.validate(value) { ProgressManager.checkCanceled() }
    } catch (e: ProcessCanceledException) {
      throw e
    } catch (e: RuntimeException) {
      LOG.error("Error while processing ${property.key} with value $value", e)
      holder.registerProblem(property.valueNode.psi, "Internal error, please report an bug to the systemd plugin. Include the Key and Value used.", ProblemHighlightType.ERROR)
      return
    }

    when (outcome) {
      is ParseOutcome.Valid -> return

      is ParseOutcome.SyntaxError -> {
        // Highlight from where parsing got stuck to the end (or everything if it reached the end).
        val tr = if (outcome.furthest < value.length) {
          TextRange(outcome.furthest, value.length)
        } else {
          TextRange(0, value.length)
        }
        holder.registerProblem(property.valueNode.psi, "${property.key}'s value does not match the expected format. Possible reasons include unrecognized characters or premature end of input.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, tr)
      }

      is ParseOutcome.SemanticError -> {
        // Well-formed but invalid: highlight the offending token, and offer literal replacements.
        val bad = outcome.badToken
        val tr = TextRange(bad.start, bad.end)

        val quickFixes = mutableListOf<LocalQuickFix>()
        val choices = when (val terminal = bad.terminal) {
          is LiteralChoiceTerminal -> terminal.choices
          is FlexibleLiteralChoiceTerminal -> terminal.choices
          else -> emptyArray()
        }
        for (choice in choices) {
          quickFixes.add(ReplaceInvalidLiteralChoiceQuickFix(bad.start, bad.text, choice))
        }

        holder.registerProblem(property.valueNode.psi, "${property.key}'s value is correctly formatted but seems invalid.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, tr, *quickFixes.toTypedArray())
      }
    }
  }

  companion object {
    private val LOG = Logger.getInstance(SemanticDataRepository::class.java)
  }
}
