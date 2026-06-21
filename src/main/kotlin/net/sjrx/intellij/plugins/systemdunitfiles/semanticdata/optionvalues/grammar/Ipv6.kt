package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * IPv6 canonicalization to RFC 5952 §4 (#363):
 *  - lowercase hex,
 *  - drop leading zeros in each 16-bit group,
 *  - compress the longest run of all-zero groups to "::" (leftmost on a tie, only if the run is 2+),
 *  - never compress a single zero group.
 *
 * Hand-rolled and dependency-free. The address is parsed to eight groups, then re-formatted. The
 * mixed IPv4-tail notation (RFC 5952 §5, e.g. ::ffff:1.2.3.4) is intentionally out of scope for now —
 * [canonicalizeIpv6] returns null for addresses containing a dotted IPv4 part, so they're left alone.
 */
fun canonicalizeIpv6(address: String): String? {
  if (address.isEmpty() || '.' in address) return null
  val groups = parseGroups(address) ?: return null
  return format(groups)
}

private fun parseGroups(address: String): IntArray? {
  val doubleColon = address.indexOf("::")
  val groups: List<Int>
  if (doubleColon >= 0) {
    if (address.indexOf("::", doubleColon + 1) >= 0) return null // at most one "::"
    val head = address.substring(0, doubleColon).split(":").filter { it.isNotEmpty() }
    val tail = address.substring(doubleColon + 2).split(":").filter { it.isNotEmpty() }
    val missing = 8 - head.size - tail.size
    if (missing < 1) return null // "::" must stand for at least one zero group
    groups = (head + List(missing) { "0" } + tail).map { parseHextet(it) ?: return null }
  } else {
    val parts = address.split(":")
    if (parts.size != 8) return null
    groups = parts.map { parseHextet(it) ?: return null }
  }
  return groups.toIntArray()
}

private fun parseHextet(s: String): Int? {
  if (s.isEmpty() || s.length > 4) return null
  val value = s.toIntOrNull(16) ?: return null
  return if (value in 0..0xFFFF) value else null
}

private fun format(groups: IntArray): String {
  // Longest run of consecutive zero groups (leftmost on ties); only compressible if length >= 2.
  var runStart = -1
  var runLen = 0
  var i = 0
  while (i < groups.size) {
    if (groups[i] == 0) {
      var j = i
      while (j < groups.size && groups[j] == 0) j++
      if (j - i > runLen) {
        runLen = j - i
        runStart = i
      }
      i = j
    } else {
      i++
    }
  }
  if (runLen < 2) runStart = -1

  val sb = StringBuilder()
  i = 0
  while (i < groups.size) {
    if (i == runStart) {
      sb.append("::")
      i += runLen
      continue
    }
    if (sb.isNotEmpty() && !sb.endsWith("::")) sb.append(":")
    sb.append(Integer.toHexString(groups[i]))
    i++
  }
  return sb.toString()
}
