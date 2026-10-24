package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.math.BigDecimal
import java.math.BigInteger

class MemoryLimitOptionValue() : OptionValueInformation {


  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    // parse_size(rvalue, 1024) below UINT64_MAX,
    // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3838-L3862
    val bytes = parseSize1024(value.trim())
    if (bytes != null && bytes < UINT64_MAX) {
      return null
    }

    if (INFINITY.matches(value)) {
      return null
    }

    if (VALID_PERCENTAGE.matches(value)) {
      return null
    }

    return "Invalid memory limit: '${value}'. Takes a memory size in bytes. Can be suffixed with K, M, G, T, P, E to specify Kilo/Mega/Giga/Tera/Peta/Exabytes respectively. Alternatively a percentage (with up to 2 decimal points) may be specified, or the special value 'infinity'."
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
    val VALID_PERCENTAGE = """^\s*(100|([0-9]?[0-9]))(\.[0-9]{1,2})?\s*%\s*$""".toRegex()
    val INFINITY = """^\s*infinity\s*$""".toRegex()


  }
}
