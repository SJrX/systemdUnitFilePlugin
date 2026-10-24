package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

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
 * `infinity` (exact, streq), or an unsigned int as safe_atou() reads it (see [SafeAtoTerminal]), up to
 * UINT_MAX (4294967295). The value is the whole (stripped) setting, so whitespace can still reach the parser:
 * `0b 1` and a leading `\v` are accepted, as are `0b+1`, `0b-0`, a negative zero such as `-0`, and `+`-signed
 * numbers (but not `+0b1`/`-0b0`, which mangle_base misses). `0` is legal (it blocks all activation).
 */
class ConfigParseConcurrencyMaxOptionValue : SimpleGrammarOptionValues(
  "config_parse_concurrency_max",
  SequenceCombinator(
    AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("infinity"),
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = 4_294_967_295L,
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    ),
    EOF(),
  )
)
