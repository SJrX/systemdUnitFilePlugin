package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.TC_HANDLE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.TC_HANDLE_NUMBER

/*
 * Traffic-control Handle= / Parent= / ClassId= for the [QDisc] and [*Class] sections of .network files.
 *
 * parsers https://github.com/systemd/systemd/blob/048970650c/src/network/tc/qdisc.c   config_parse_qdisc_handle, config_parse_qdisc_parent
 *         https://github.com/systemd/systemd/blob/048970650c/src/network/tc/tclass.c   config_parse_tclass_parent, config_parse_tclass_classid
 *         https://github.com/systemd/systemd/blob/048970650c/src/network/tc/tc-util.c  parse_handle
 * keys    systemd-build/build/networkd-network-gperf.gperf  ([QDisc]/[<qdisc>]/[<class>] Handle=/Parent=/ClassId=)
 *
 * Each parser's ltype (QDISC_KIND_* / TCLASS_KIND_*) is passed only to qdisc_new_static / tclass_new_static
 * to allocate the right struct — it has no effect on how the value is parsed. So each of these registers
 * once under the `*` ltype wildcard and covers every kind.
 *
 * The man page is silent on Handle= and documents Parent= for one section only as "clsact or ingress",
 * which is narrower than the parser: `Parent=root` (54 occurrences in the reference corpus) and the
 * `major:minor` form are both accepted here, following the C source.
 */

/** `Handle=` in a [QDisc]/qdisc section — config_parse_qdisc_handle: a single hex uint16. */
class ConfigParseQdiscHandleOptionValue : SimpleGrammarOptionValues(
    "config_parse_qdisc_handle",
    SequenceCombinator(TC_HANDLE_NUMBER, EOF())
)

/** `Parent=` in a [QDisc]/qdisc section — config_parse_qdisc_parent: `root`, `clsact`, `ingress`, or a handle. */
class ConfigParseQdiscParentOptionValue : SimpleGrammarOptionValues(
    "config_parse_qdisc_parent",
    SequenceCombinator(
        AlternativeCombinator(FlexibleLiteralChoiceTerminal("root", "clsact", "ingress"), TC_HANDLE),
        EOF()
    )
)

/** `Parent=` in a [*Class] section — config_parse_tclass_parent: `root` or a handle (no clsact/ingress). */
class ConfigParseTclassParentOptionValue : SimpleGrammarOptionValues(
    "config_parse_tclass_parent",
    SequenceCombinator(
        AlternativeCombinator(FlexibleLiteralChoiceTerminal("root"), TC_HANDLE),
        EOF()
    )
)

/** `ClassId=` in a [*Class] section — config_parse_tclass_classid: a handle. */
class ConfigParseTclassClassidOptionValue : SimpleGrammarOptionValues(
    "config_parse_tclass_classid",
    SequenceCombinator(TC_HANDLE, EOF())
)
