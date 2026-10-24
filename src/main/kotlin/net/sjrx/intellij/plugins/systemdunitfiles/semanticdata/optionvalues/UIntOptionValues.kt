package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * The generic config_parse_uint32 / config_parse_uint16 / config_parse_uint8 table parsers.
 *
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L956-L966   DEFINE_PARSER(uint8/uint16/uint32, …, safe_atou8/16/32)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L583-L613   safe_atou8_full / safe_atou16_full
 *
 * Each is a safe_ato* call on the whole value (base 0), so `Xfrm.InterfaceId=0x98`, `077`, `0b101` and
 * `+5` are accepted (sd-check at 0f0682b773), and anything above the type maximum is -ERANGE. See
 * [SafeAtoTerminal] for the exact syntax.
 *
 * Lenient on purpose: `[Xfrm] InterfaceId=0` is accepted by the parser and only refused later by the
 * netdev verify step ("Xfrm interface ID cannot be zero"), which is not a parser rejection (#565).
 */
class UIntOptionValues(validatorName: String, grammar: Combinator) : GrammarOptionValue(validatorName, grammar) {

  companion object {

    val validators = mapOf(
      Validator("config_parse_uint32", "0") to UIntOptionValues(
        "config_parse_uint32",
        SequenceCombinator(
          SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 4_294_967_295L,
            acceptsWhitespace = true,
            acceptsMinus = true,
          ),
          EOF(),
        ),
      ),
      Validator("config_parse_uint16", "0") to UIntOptionValues(
        "config_parse_uint16",
        SequenceCombinator(
          SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 65_535L,
            acceptsWhitespace = true,
            acceptsMinus = true,
          ),
          EOF(),
        ),
      ),
      Validator("config_parse_uint8", "0") to UIntOptionValues(
        "config_parse_uint8",
        SequenceCombinator(
          SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 255L,
            acceptsWhitespace = true,
            acceptsMinus = true,
          ),
          EOF(),
        ),
      ),
    )
  }
}
