package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Bridge] DefaultPVID= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#DefaultPVID=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/vlan-util.c#L44-L67   config_parse_default_port_vlanid (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/vlan-util.c#L8-L23   parse_vlanid (safe_atou16, vlanid_is_valid)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/vlan-util.h#L7-L13   VLANID_MAX (4094)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-bridge.netdev#L13   DefaultPVID=9
 */

/**
 * Validator for `[Bridge] DefaultPVID=` (.netdev).
 *
 * C function: config_parse_default_port_vlanid. Either `none` (no PVID) or a VLAN id read by
 * parse_vlanid (safe_atou16, so hex, octal and `0b`/`0o` work) and checked by vlanid_is_valid, i.e. 0…4094.
 *
 * The man page says "an integer in the range 1…4094", but vlanid_is_valid only checks `id <= 4094`, so
 * `0` is accepted (and stored as 0, the same as `none`). This follows the C. An invalid value is logged
 * and ignored by systemd.
 *
 * There is no isempty() branch, so an empty `DefaultPVID=` is a parse error in systemd (logged and ignored), not
 * a reset. The plugin never runs a grammar on an empty value, so it is not flagged here.
 */
class ConfigParseDefaultPortVlanidOptionValue : SimpleGrammarOptionValues(
    "config_parse_default_port_vlanid",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("none"),
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 4094L,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
        ),
        EOF()
    )
)
