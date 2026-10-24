package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NSEC_DURATION_FINITE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] RxCoalesceCQENSec= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#RxCoalesceCQENSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ethtool-util.c#L1370-L1415   config_parse_coalesce_sec (ltype "true", i.e. nsec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1374-L1466   parse_nsec
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L141   TEST(parse_nsec)
 */

/**
 * Validator for `[Link] RxCoalesceCQENSec=` (.link) — the one key that registers config_parse_coalesce_sec
 * with ltype `true`, which switches the parser from parse_sec() to parse_nsec().
 *
 * parse_nsec reads one or more `number[unit]` elements, a bare number meaning nanoseconds (`500`,
 * `1us`, `2.5`, `1s500ms`, `.7`). See [NSEC_DURATION_FINITE].
 *
 * `infinity` parses, but config_parse_coalesce_sec then refuses any result above UINT32_MAX ("Too large
 * … value"), and NSEC_INFINITY is UINT64_MAX — so `infinity` is never a usable value here and is
 * rejected. The same UINT32_MAX ceiling (about 4.29 seconds in nanoseconds) applies to every finite value
 * too, but that compares the computed sum and is not modelled. An invalid value is logged and ignored.
 */
class ConfigParseCoalesceNsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_coalesce_sec",
    SequenceCombinator(NSEC_DURATION_FINITE, EOF())
)
