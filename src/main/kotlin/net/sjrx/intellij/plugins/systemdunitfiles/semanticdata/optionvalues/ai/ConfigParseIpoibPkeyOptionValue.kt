package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [IPoIB] PartitionKey= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#PartitionKey=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/ipoib.c#L100-L140   config_parse_ipoib_pkey (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L599-L613   safe_atou16_full (base 0)
 */

/**
 * Validator for `[IPoIB] PartitionKey=` (.netdev).
 *
 * C function: config_parse_ipoib_pkey. A uint16 read with safe_atou16() (so `0x8001` works, which is
 * how partition keys are usually written) in 1…0xffff, except 0x8000 (the full-membership bit with an
 * empty key). Spelled as two ranges either side of 0x8000. An empty assignment unsets it; an invalid
 * value is logged and ignored by systemd.
 */
class ConfigParseIpoibPkeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipoib_pkey",
    SequenceCombinator(
        AlternativeCombinator(
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 1L,
                maxInclusive = 0x7fffL,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0x8001L,
                maxInclusive = 0xffffL,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
        ),
        EOF()
    )
)
