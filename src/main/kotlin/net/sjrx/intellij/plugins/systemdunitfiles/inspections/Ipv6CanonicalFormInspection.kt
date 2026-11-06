package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.CanonicalizeIpv6QuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SemanticTag
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.canonicalizeIpv6
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Suggests rewriting an IPv6 address (a span tagged [SemanticTag.IPV6]) to its RFC 5952 canonical form
 * (#363), behind the grammar flag. Addresses [canonicalizeIpv6] can't handle (e.g. an IPv4 tail) are
 * left alone.
 */
class Ipv6CanonicalFormInspection : TaggedSpanRewriteInspection() {

  override val tag = SemanticTag.IPV6

  override fun rewriter(settings: ExperimentalSettings.State): ((String) -> String?) = ::canonicalizeIpv6

  override fun message(replacement: String) = "IPv6 address is not in canonical form (RFC 5952); use '$replacement'"

  override fun quickFix(original: String, replacement: String): LocalQuickFix = CanonicalizeIpv6QuickFix(original, replacement)
}
