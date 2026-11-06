package net.sjrx.intellij.plugins.systemdunitfiles.intentions

/**
 * Rewrites a MAC address from its current spelling [original] to the user's preferred [replacement],
 * e.g. `00-1A-2B-3C-4D-5E` -> `00:1a:2b:3c:4d:5e`.
 */
class NormalizeMacFormatQuickFix(original: String, replacement: String) : ReplaceValueSpanQuickFix(
  original,
  replacement,
  name = "Convert MAC address to '$replacement'",
  family = "Convert MAC address to preferred format",
)
