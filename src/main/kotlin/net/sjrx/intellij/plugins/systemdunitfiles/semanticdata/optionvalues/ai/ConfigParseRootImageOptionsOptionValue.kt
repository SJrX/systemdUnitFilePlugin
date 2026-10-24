package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EXEC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RETAINED_FREE_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.partitionDesignator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] RootImageOptions=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#RootImageOptions=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1648-L1713   config_parse_root_image_options (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/strv.c#L481-L531              strv_split_colon_pairs (UNQUOTE|RETAIN_ESCAPE words, then ':' with CUNESCAPE|UNESCAPE_SEPARATORS; >2 fields is -EINVAL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/gpt.c#L156-L183               partition_designator_table / partition_designator_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L249             unit_full_printf (options)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-gpt.c#L136-L142           partition designator shorthands accepted/rejected
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-50-DISSECT.mountfsd.sh#L155-L156
 */

/**
 * Validator for `RootImageOptions=` (config_parse_root_image_options, ltype 0).
 *
 * A whitespace-separated list of `[PARTITION:]OPTIONS` words. A word with two fields names a partition
 * designator first; a word with one field is the root partition's options. A third field makes
 * strv_split_colon_pairs() fail with -EINVAL, which discards the whole assignment.
 *
 * The man page lists only root, usr, home, srv, esp, xbootldr, tmp and var; the C table also accepts
 * swap, root-verity, usr-verity, root-verity-sig, usr-verity-sig and the -vty/-vsig shorthands, so this
 * follows the C. The ':' separators are coalesced (no EXTRACT_DONT_COALESCE_SEPARATORS), so `root::ro`
 * and a trailing ':' are accepted. The mount options themselves are free-form (handed to mount(8) at
 * runtime) and only their escapes are checked.
 */
class ConfigParseRootImageOptionsOptionValue : SimpleGrammarOptionValues(
  "config_parse_root_image_options",
  whitespaceList(
    SequenceCombinator(
      ZeroOrOne(EXEC_COALESCED_COLONS),
      AlternativeCombinator(
        SequenceCombinator(partitionDesignator(), EXEC_COALESCED_COLONS, RETAINED_FREE_FIELD),
        RETAINED_FREE_FIELD,
      ),
      ZeroOrOne(EXEC_COALESCED_COLONS),
    )
  )
)
