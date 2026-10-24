package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Socket] MessageQueueMaxMessages=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#MessageQueueMaxMessages=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L960   config_parse_long (DEFINE_PARSER(long, long, safe_atoli))
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.h#L114   safe_atoli (safe_atolli where long is 64-bit)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L560   safe_atolli (strtoll base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403   mangle_base (0b / 0o prefixes)
 * linux  https://man7.org/linux/man-pages/man7/mq_overview.7.html
 * linux  https://man7.org/linux/man-pages/man3/strtol.3.html
 */

/**
 * Validator for config_parse_long, used only by `[Socket] MessageQueueMaxMessages=`.
 *
 * A signed `long` as safe_atoli() reads it ([SafeAtoTerminal], signed, base 0): optional leading
 * whitespace and sign, then decimal, `0x` hex, leading-zero octal, or a `0b`/`0o` prefix — after which
 * strtoll starts afresh, so `0b-1` and `0b -1` are -1 (sd-check accepts both) — anything from LONG_MIN
 * to LONG_MAX on a 64-bit host.
 * The setting itself only means something when positive (socket.c tests `mq_maxmsg > 0`), but the
 * parser accepts any long and so does this.
 *
 * The C parser has no empty-value case (safe_atoli("") fails and the line is ignored with a warning);
 * the plugin does not validate empty assignments, so that is not reported.
 */
class ConfigParseLongOptionValue : SimpleGrammarOptionValues(
  "config_parse_long",
  SequenceCombinator(
    SafeAtoTerminal(
      signed = true,
      base = 0,
      flags = emptySet(),
      minInclusive = SafeAtoTerminal.INT64_MIN,
      maxInclusive = SafeAtoTerminal.INT64_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    ),
    EOF(),
  )
)
