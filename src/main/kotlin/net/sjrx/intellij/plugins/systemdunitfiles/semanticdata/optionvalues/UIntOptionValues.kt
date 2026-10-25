package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.FileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.fileClass
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
 * `[Xfrm] InterfaceId=0` is accepted by the parser, but xfrm_verify() then refuses it ("Xfrm interface ID cannot
 * be zero", src/network/netdev/xfrm.c#L30-L39) and netdev_load_one() drops the whole .netdev
 * (src/network/netdev/netdev.c#L1110-L1114), so it is flagged for that one key (#565, [Uint32OptionValue]).
 */
open class UIntOptionValues(validatorName: String, grammar: Combinator) : GrammarOptionValue(validatorName, grammar) {

  companion object {

    val validators = mapOf(
      Validator("config_parse_uint32", "0") to Uint32OptionValue(),
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

/**
 * config_parse_uint32: 0…4294967295 for every key, except `[Xfrm] InterfaceId=` in a .netdev file, which must
 * also not be 0 (xfrm_verify(), see [UIntOptionValues]). The key check is explicit because the registry is keyed
 * only by (parser, ltype).
 */
private class Uint32OptionValue : UIntOptionValues("config_parse_uint32", uint32Grammar(minInclusive = 0L)) {
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java)?.sectionName
    if (section == "Xfrm" && property.key == "InterfaceId" && property.containingFile.fileClass() == FileClass.NETDEV) {
      XFRM_INTERFACE_ID.generateProblemDescriptors(property, holder)
    } else {
      super.generateProblemDescriptors(property, holder)
    }
  }

  private companion object {
    val XFRM_INTERFACE_ID = object : GrammarOptionValue("config_parse_uint32", uint32Grammar(minInclusive = 1L)) {
      override fun invalidValueMessage(key: String, badValue: String): String =
        "$key must be 1…4294967295; systemd-networkd refuses 0 (\"Xfrm interface ID cannot be zero\") and does not create this netdev."
    }
  }
}

private fun uint32Grammar(minInclusive: Long) = SequenceCombinator(
  SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = minInclusive,
    maxInclusive = 4_294_967_295L,
    acceptsWhitespace = true,
    acceptsMinus = true,
  ),
  EOF(),
)
