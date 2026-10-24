package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import java.math.BigDecimal
import java.math.BigInteger

/**
 * MemoryMin=, MemoryLow=, MemoryHigh=, MemoryMax=, MemorySwapMax=, MemoryZSwapMax= and their Startup variants:
 * config_parse_memory_limit(), https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3821-L3898
 *
 * `infinity`, a parse_permyriad() value (at most 100%), or a parse_size() value with base 1024. The result must be
 * below UINT64_MAX, and it may only be 0 (also "0%") for MemorySwapMax=, MemoryZSwapMax=, MemoryLow=, MemoryMin= and
 * the Startup variants of the first three.
 */
class MemoryLimitOptionValue() : OptionValueInformation {


  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val value = property.valueText ?: return
    val errorMessage = getErrorMessage(value) ?: zeroErrorMessage(property.key, value)
    if (errorMessage != null) {
      holder.registerProblem(property.valueNode.psi, errorMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }
  }

  /** The error for a limit of 0 on a key that does not allow it, or null. Expects a value [getErrorMessage] accepts. */
  private fun zeroErrorMessage(key: String, value: String): String? {
    if (key in ZERO_ALLOWED_KEYS || value.trim() == "infinity") {
      return null
    }
    val permyriad = parsePermyriad(value.trim())
    val isZero = if (permyriad != null) permyriad == 0 else parseSize1024(value.trim()) == BigInteger.ZERO
    return if (isZero) "Memory limit '$value' out of range: $key= must be above 0." else null
  }

  override fun getErrorMessage(value: String): String? {
    val trimmed = value.trim()

    if (trimmed == "infinity") {
      return null
    }

    // parse_permyriad() comes first; only when it fails is the value a parse_size() byte count, which must stay
    // below UINT64_MAX.
    if (parsePermyriad(trimmed) != null) {
      return null
    }

    val bytes = parseSize1024(trimmed)
    if (bytes != null && bytes < UINT64_MAX) {
      return null
    }

    return "Invalid memory limit: '${value}'. Takes a memory size in bytes. Can be suffixed with K, M, G, T, P, E to specify Kilo/Mega/Giga/Tera/Peta/Exabytes respectively. Alternatively a percentage (with up to 2 decimal points), permille or permyriad may be specified, or the special value 'infinity'."
  }

  override val validatorName: String
    get() = VALIDATOR_NAME



  companion object {
    const val VALIDATOR_NAME = "config_parse_memory_limit"
    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to MemoryLimitOptionValue())

    private val UINT64_MAX: BigInteger = BigInteger.ONE.shiftLeft(64) - BigInteger.ONE

    private const val WHITESPACE = " \t\n\r"

    // The base-1024 table of parse_size(), in table order; "" (a bare number) comes last.
    private val IEC_FACTORS: List<Pair<String, BigInteger>> = listOf(
      "E" to BigInteger.valueOf(1024).pow(6),
      "P" to BigInteger.valueOf(1024).pow(5),
      "T" to BigInteger.valueOf(1024).pow(4),
      "G" to BigInteger.valueOf(1024).pow(3),
      "M" to BigInteger.valueOf(1024).pow(2),
      "K" to BigInteger.valueOf(1024),
      "B" to BigInteger.ONE,
      "" to BigInteger.ONE,
    )

    /**
     * parse_size(t, 1024, …), https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283:
     * one or more `number[.fraction][suffix]` elements whose suffixes strictly descend through the table. Returns
     * the byte count, or null for -EINVAL / -ERANGE.
     */
    private fun parseSize1024(t: String): BigInteger? {
      val n = t.length
      var p = 0
      var total = BigInteger.ZERO
      var startPos = 0
      do {
        while (p < n && t[p] in WHITESPACE) p++
        // strtoull(p, &e, 10): an optional sign, then digits. A '-' is ERANGE.
        var q = p
        if (q < n && (t[q] == '+' || t[q] == '-')) q++
        val digitsStart = q
        while (q < n && t[q] in '0'..'9') q++
        if (q == digitsStart) return null
        if (t[p] == '-') return null
        val l = BigInteger(t.substring(digitsStart, q))
        if (l > UINT64_MAX) return null

        var e = q
        var frac = BigDecimal.ZERO
        if (e < n && t[e] == '.') {
          e++
          val fracStart = e
          while (e < n && t[e] in '0'..'9') e++
          if (e > fracStart) frac = BigDecimal("0." + t.substring(fracStart, e))
        }
        while (e < n && t[e] in WHITESPACE) e++

        var i = startPos
        while (i < IEC_FACTORS.size && !t.startsWith(IEC_FACTORS[i].first, e)) i++
        if (i >= IEC_FACTORS.size) return null
        val factor = IEC_FACTORS[i].second

        val roundUp = if (frac.signum() > 0) BigInteger.ONE else BigInteger.ZERO
        if (l + roundUp > UINT64_MAX / factor) return null
        val tmp = l * factor + frac.multiply(BigDecimal(factor)).toBigInteger()
        if (tmp > UINT64_MAX - total) return null
        total += tmp

        p = e + IEC_FACTORS[i].first.length
        startPos = i + 1
      } while (p < n)
      return total
    }

    // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3853-L3863
    private val ZERO_ALLOWED_KEYS = setOf(
      "MemorySwapMax", "StartupMemorySwapMax", "MemoryZSwapMax", "StartupMemoryZSwapMax", "MemoryLow", "StartupMemoryLow", "MemoryMin",
    )

    // safe_atoi(): leading whitespace, an optional sign, base 0 (so 0x10 and 010 are numbers).
    private val PERMYRIAD_NUMBER = SafeAtoTerminal(
      signed = true,
      base = 0,
      flags = emptySet(),
      minInclusive = SafeAtoTerminal.INT32_MIN,
      maxInclusive = SafeAtoTerminal.INT32_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    )

    /**
     * parse_permyriad(): `N‱`, `N[.d]‰` or `N[.d[d]]%`, at most 10000 permyriad. Returns the permyriad value, or
     * null for -EINVAL / -ERANGE.
     * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/percent-util.c#L7-L157
     */
    private fun parsePermyriad(p: String): Int? {
      val (body, scale, maxDecimals) = when {
        p.endsWith("‱") -> Triple(p.removeSuffix("‱"), 1, 0)
        p.endsWith("‰") -> Triple(p.removeSuffix("‰"), 10, 1)
        p.endsWith("%") -> Triple(p.removeSuffix("%"), 100, 2)
        else -> return null
      }
      val dot = if (maxDecimals > 0) body.indexOf('.') else -1
      val whole = if (dot >= 0) body.substring(0, dot) else body
      val decimals = if (dot >= 0) body.substring(dot + 1) else ""
      if (dot >= 0 && (decimals.isEmpty() || decimals.length > maxDecimals || decimals.any { it !in '0'..'9' })) {
        return null
      }
      val v = PERMYRIAD_NUMBER.valueOf(whole) ?: return null
      if (v.signum() < 0) {
        return null
      }
      val fraction = if (decimals.isEmpty()) 0 else decimals.padEnd(maxDecimals, '0').toInt()
      val result = v * BigInteger.valueOf(scale.toLong()) + BigInteger.valueOf(fraction.toLong())
      return if (result > BigInteger.valueOf(10000)) null else result.toInt()
    }


  }
}
