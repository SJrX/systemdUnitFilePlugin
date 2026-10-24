package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.TimeHelper

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ethtool-util.c#L1370-L1415   config_parse_coalesce_sec
 */

/**
 * Validator for Link.{RxCoalesceSec, RxCoalesceIrqSec, TxCoalesceSec, TxCoalesceIrqSec,
 *                    StatisticsBlockCoalesceSec, RxCoalesceLowSec, TxCoalesceLowSec,
 *                    RxCoalesceHighSec, TxCoalesceHighSec, CoalescePacketRateSampleIntervalSec}.
 * C Function: config_parse_coalesce_sec(0)
 *
 * Calls parse_sec, which accepts a fractional or integer number with any of systemd's time-unit
 * suffixes, and compound forms like "1h 30s". The result (in microseconds) must then fit in a uint32:
 * anything above UINT32_MAX µs (about 71.6 minutes, so `infinity`, `2hour` and `4295s`) is refused
 * with "Too large … value". StatisticsBlockCoalesceSec= and CoalescePacketRateSampleIntervalSec= also
 * refuse 0 µs ("Invalid … value"). The value is parsed with TimeHelper, a port of parse_time(); the grammar
 * is only used to locate a syntax error.
 */
class ConfigParseCoalesceSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_coalesce_sec",
    SequenceCombinator(TIME_VALUE, EOF())
) {
    override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
        val value = property.valueText ?: return
        // parse_sec()'s own -ERANGE (e.g. `999999999999999999`) is as much "too large" as a value above UINT32_MAX.
        val usec = try {
            TimeHelper.parseSecs(value)
        } catch (e: TimeHelper.OutOfRangeException) {
            ULong.MAX_VALUE
        } catch (e: IllegalArgumentException) {
            null
        }

        when {
            usec == null -> {
                // The grammar locates the error; TimeHelper (a port of parse_time()) has the last word.
                val before = holder.resultCount
                super.generateProblemDescriptors(property, holder)
                if (holder.resultCount == before) {
                    holder.registerProblem(
                        property.valueNode.psi,
                        "${property.key}'s value is not a valid time span",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    )
                }
            }
            usec > UINT32_MAX -> holder.registerProblem(
                property.valueNode.psi,
                "${property.key}'s value is too large, it must be at most $UINT32_MAX µs (about 71 minutes)",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            usec < 1u && property.key in NON_ZERO_KEYS -> holder.registerProblem(
                property.valueNode.psi,
                "${property.key}'s value must be at least 1 µs",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
            // parse_sec() took it (`+5` and `.5` too, which the shared TIME_VALUE grammar does not know).
            else -> Unit
        }
    }

    private companion object {
        val UINT32_MAX: ULong = 4294967295u
        val NON_ZERO_KEYS = setOf("StatisticsBlockCoalesceSec", "CoalescePacketRateSampleIntervalSec")
    }
}
