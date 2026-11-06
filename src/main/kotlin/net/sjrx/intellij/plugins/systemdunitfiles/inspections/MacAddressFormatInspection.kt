package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.NormalizeMacFormatQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SemanticTag
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.formatMac
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat

/**
 * Suggests rewriting a 6-byte MAC address (a span tagged [SemanticTag.MAC_ADDRESS]) to the user's
 * [PreferredMacFormat], behind the grammar flag. Off when the preference is [PreferredMacFormat.LEAVE_ALONE].
 */
class MacAddressFormatInspection : TaggedSpanRewriteInspection() {

  override val tag = SemanticTag.MAC_ADDRESS

  override fun rewriter(settings: ExperimentalSettings.State): ((String) -> String?)? {
    val format = settings.preferredMacFormat
    if (format == PreferredMacFormat.LEAVE_ALONE) return null
    return { formatMac(it, format) }
  }

  override fun message(replacement: String) = "MAC address is not in preferred format, use: '$replacement'"

  override fun quickFix(original: String, replacement: String): LocalQuickFix = NormalizeMacFormatQuickFix(original, replacement)
}
