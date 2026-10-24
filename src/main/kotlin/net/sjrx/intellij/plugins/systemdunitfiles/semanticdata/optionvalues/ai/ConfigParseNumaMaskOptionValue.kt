package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtouNumber

/*
 * [Service]/[Socket]/[Mount]/[Swap] NUMAMask=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#NUMAMask=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1572-L1608   config_parse_numa_mask (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3790-L3819   config_parse_unit_cpu_set (unit_full_printf, then config_parse_cpu_set)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L220          config_parse_cpu_set (words split on WHITESPACE "," with EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305            parse_range (N or N-M, '-' not coalesced)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429            safe_atou_full (base 0, plus mangle_base 0b/0o at L403)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L17           CPU_SET_MAX_NCPU 8192 (cpu_set_add_range refuses end >= it)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-36-NUMAPOLICY.sh#L166
 */

// 0b/0o spellings of a value below CPU_SET_MAX_NCPU (8192 = 2^13 = 0o20000).
private val NODE_INDEX = safeAtouNumber(8192, """0[bB]0*[01]{1,13}|0[oO]0*(?:1[0-7]{4}|[0-7]{1,4})""")

private val NODE_RANGE = SequenceCombinator(NODE_INDEX, ZeroOrOne(SequenceCombinator(HYPHEN, NODE_INDEX)))

private val DOUBLE_QUOTE = LiteralChoiceTerminal("\"")
private val SINGLE_QUOTE = LiteralChoiceTerminal("'")

// unit_full_printf() runs over the whole value before it is split, so a word with a specifier in it
// could expand to anything; such words are accepted unchecked. Tried first: it needs a '%', which a
// plain range never contains, so it cannot shadow one.
private val SPECIFIER_WORD = RegexTerminal("""[^\s,]*%[^\s,]*""", """[^\s,]*%[^\s,]*""")

private val NODE_WORD = AlternativeCombinator(
  SPECIFIER_WORD,
  SequenceCombinator(DOUBLE_QUOTE, NODE_RANGE, DOUBLE_QUOTE),
  SequenceCombinator(SINGLE_QUOTE, NODE_RANGE, SINGLE_QUOTE),
  NODE_RANGE,
)

// WHITESPACE and ',' are both separators and are coalesced, so leading/trailing/doubled ones are fine.
private val NODE_SEPARATOR = RegexTerminal("""[\s,]+""", """[\s,]+""")

/**
 * Validator for `NUMAMask=` (config_parse_numa_mask, ltype 0).
 *
 * Either exactly `all` (compared with streq before anything else) or a CPU-set style list: node
 * indexes `N` or ranges `N-M`, separated by whitespace and/or commas. Each number is read by
 * safe_atou(), so `0x10`, `010` (octal), `0b11` and `0o7` are all legal spellings, and must be below
 * CPU_SET_MAX_NCPU (8192). systemd does not check against the machine's actual NUMA node count at
 * parse time.
 *
 * Not checked: a reversed range such as `3-1` (systemd logs "Invalid CPU range" and skips that word —
 * the grammar framework cannot compare two numbers); and words containing a `%` specifier, which are
 * accepted unchecked.
 */
class ConfigParseNumaMaskOptionValue : SimpleGrammarOptionValues(
  "config_parse_numa_mask",
  AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("all"), EOF()),
    SequenceCombinator(
      ZeroOrOne(NODE_SEPARATOR),
      NODE_WORD,
      ZeroOrMore(SequenceCombinator(NODE_SEPARATOR, NODE_WORD)),
      ZeroOrOne(NODE_SEPARATOR),
      EOF(),
    ),
  )
)
