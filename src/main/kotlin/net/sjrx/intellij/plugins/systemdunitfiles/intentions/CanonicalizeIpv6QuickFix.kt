package net.sjrx.intellij.plugins.systemdunitfiles.intentions

/**
 * Replaces the IPv6 address [original] with its RFC 5952 [canonical] form.
 */
class CanonicalizeIpv6QuickFix(original: String, canonical: String) : ReplaceValueSpanQuickFix(
  original,
  canonical,
  name = "Convert to canonical IPv6 '$canonical'",
  family = "Convert to canonical IPv6 (RFC 5952)",
)
