package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.IEC_SIZE

/*
 * [Socket] MessageQueueMessageSize=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#MessageQueueMessageSize=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1004-L1032   config_parse_iec_size_long (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size (base 1024)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L111   parse_size cases (1024 block)
 */

/**
 * Validator for config_parse_iec_size_long, used only by `[Socket] MessageQueueMessageSize=`.
 *
 * A parse_size(…, 1024) byte size: `8192`, `8K`, `1.5M`, `3. K`, `4 M 11.5K`, `3.5G3B` — see
 * [IEC_SIZE]. Suffixes are upper-case B/K/M/G/T/P/E only. systemd additionally requires the result to
 * fit in a long and the suffixes of a multi-part size to be strictly descending; neither is checked
 * here.
 *
 * The C parser has no empty-value case; the plugin does not validate empty assignments.
 */
class ConfigParseIecSizeLongOptionValue : SimpleGrammarOptionValues(
  "config_parse_iec_size_long",
  IEC_SIZE
)
