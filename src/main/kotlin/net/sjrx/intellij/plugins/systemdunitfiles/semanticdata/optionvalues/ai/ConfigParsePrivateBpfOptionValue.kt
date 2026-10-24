package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * PrivateBPF= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#PrivateBPF=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L150   config_parse_private_bpf (0), via DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4185-L4190   private_bpf_table / DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32   string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23   parse_boolean (STRCASE_IN_SET)
 */

/**
 * Validator for `PrivateBPF=`.
 *
 * private_bpf_from_string() tries parse_boolean() first and then the table, whose only entries are
 * `no` and `yes` — both already boolean spellings. So the accepted set is exactly parse_boolean's:
 * `1 yes y true t on` / `0 no n false f off`, compared case-insensitively (STRCASE_IN_SET), so `Yes`
 * and `TRUE` are fine. That is the shared [BOOLEAN]; there are no non-boolean names left to put in front
 * of it, so no PreferValidAlternativeCombinator is needed. (Were the table to grow a name such as
 * `auto`, it would be matched case-sensitively by streq, unlike the boolean spellings.)
 *
 * Note: DEFINE_CONFIG_PARSE_ENUM has no empty-string branch, so systemd actually logs `PrivateBPF=` as
 * a parse error; the plugin never hands empty values to a validator, so that is not flagged.
 *
 * Covers: Service.PrivateBPF, Socket.PrivateBPF, Mount.PrivateBPF, Swap.PrivateBPF.
 */
class ConfigParsePrivateBpfOptionValue : SimpleGrammarOptionValues(
    "config_parse_private_bpf",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
