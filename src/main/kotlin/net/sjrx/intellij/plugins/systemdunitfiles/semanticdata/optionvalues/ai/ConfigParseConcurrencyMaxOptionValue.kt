package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtouNumber

/*
 * [Slice] ConcurrencyHardMax= / ConcurrencySoftMax= / ActivatingConcurrencyMax=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.slice.html#ConcurrencyHardMax=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.slice.html#ActivatingConcurrencyMax=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L6091-L6111   config_parse_concurrency_max (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L966           config_parse_unsigned → safe_atou
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429            safe_atou_full (base 0, plus mangle_base 0b/0o at L403)
 */

/**
 * Validator for the [Slice] concurrency limits (config_parse_concurrency_max, ltype 0):
 * `ConcurrencyHardMax=`, `ConcurrencySoftMax=` and `ActivatingConcurrencyMax=`.
 *
 * `infinity` (exact, streq), or an unsigned int as safe_atou() reads it — decimal, `0x` hex,
 * leading-zero octal, `0b`/`0o`, optionally `+`-signed (but not `+0b…`/`+0o…`, which mangle_base misses) —
 * up to UINT_MAX (4294967295). `0` is legal (it blocks all activation), and so is a negative zero such
 * as `-0` (strtoul accepts it and safe_atou only refuses a non-zero negative).
 */
class ConfigParseConcurrencyMaxOptionValue : SimpleGrammarOptionValues(
  "config_parse_concurrency_max",
  SequenceCombinator(
    AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("infinity"),
      safeAtouNumber(4_294_967_296L, """0[bB]0*[01]{1,32}|0[oO]0*(?:[0-3][0-7]{10}|[0-7]{1,10})""", allowNegativeZero = true),
    ),
    EOF(),
  )
)
