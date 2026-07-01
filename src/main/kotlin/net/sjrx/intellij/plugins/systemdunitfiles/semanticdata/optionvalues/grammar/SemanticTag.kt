package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/**
 * A semantic identity a grammar can attach to a [Labeled] span, independent of its coloring [Role].
 *
 * Where [Role] answers "what colour?", a tag answers "what *is* this span?". It lets a feature act on
 * a span because the grammar *declared* what it is, rather than re-sniffing the raw text and hoping no
 * other labeled span happens to look the same. Currently only IPv6 canonicalization keys off it (see
 * Ipv6CanonicalFormInspection); add members as other features need structural identity.
 */
enum class SemanticTag {
  /** A whole IPv6 address (possibly with an IPv4 tail), as matched by `IPV6_ADDR`. */
  IPV6,
}
