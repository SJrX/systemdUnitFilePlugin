package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * SocketBindAllow= / SocketBindDeny=.
 *
 * [config_parse_cgroup_socket_bind](https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5940-L5983)
 * hands the value to
 * [parse_socket_bind_item](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L222-L290),
 * which splits it on `:` (an empty field is an error) and tries, in this order and each at most once,
 * [parse_af_token](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L147-L164)
 * (`ipv4`/`ipv6`, case-sensitive), parse_ip_protocol_token (`tcp`/`udp` in any case) and
 * [parse_ip_ports_token](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L187-L213):
 * `any`, or a port or port range read by
 * [parse_ip_port_range](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L736-L760)
 * with port 0 allowed. Each bound goes through safe_atou
 * ([parse_range](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340)), so leading
 * whitespace, `0x50` and `+80` are all fine, and `ipv4:tcp: 12345` is accepted. Anything left over is an error.
 *
 * parse_ip_port_range also refuses a range whose upper bound is below the lower one (`5-1`); that is checked on
 * the parsed numbers once the grammar has accepted the value. Not modelled: backslash escapes inside a field.
 */
class CgroupSocketBindOptionValue() : GrammarOptionValue("config_parse_cgroup_socket_bind", GRAMMAR) {

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val before = holder.resultCount
    super.generateProblemDescriptors(property, holder)
    if (holder.resultCount != before) {
      return
    }

    // The grammar accepted the value, so the ports (if any) are the last `:` field, and a `-` in it splits the
    // range: https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L753-L754
    val ports = (property.valueText ?: return).substringAfterLast(':')
    if (!ports.contains('-')) {
      return
    }
    val low = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = false)
      .valueOf(ports.substringBefore('-'))
    val high = SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = true)
      .valueOf(ports.substringAfter('-'))
    if (low != null && high != null && high < low) {
      holder.registerProblem(
        property.valueNode.psi,
        "${property.key}'s port range ends below where it starts ($low-$high)",
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
      )
    }
  }

  companion object {
    // Order matters for the classic engine only, which falls back to the first branch that matches leniently:
    // ports first (the lenient match of `ipv4` would take the first four digits of a port), then the address
    // family (its lenient match takes a whole bad word such as `icmp`, which the 3-letter `tcp` would cut short).
    val GRAMMAR = SequenceCombinator(
      PreferValidAlternativeCombinator(
        // ports only
        SequenceCombinator(
          SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = false),
          ZeroOrOne(
            SequenceCombinator(
              LiteralChoiceTerminal("-"),
              SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = true)
            )
          )
        ),
        LiteralChoiceTerminal("any"),
        // address family, then optionally protocol and/or ports
        SequenceCombinator(
          FlexibleLiteralChoiceTerminal("ipv4", "ipv6"),
          ZeroOrOne(
            SequenceCombinator(
              LiteralChoiceTerminal(":"),
              PreferValidAlternativeCombinator(
                SequenceCombinator(
                  SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = false),
                  ZeroOrOne(
                    SequenceCombinator(
                      LiteralChoiceTerminal("-"),
                      SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = true)
                    )
                  )
                ),
                LiteralChoiceTerminal("any"),
                SequenceCombinator(
                  FlexibleLiteralChoiceTerminal("tcp", "udp", ignoreCase = true),
                  ZeroOrOne(
                    SequenceCombinator(
                      LiteralChoiceTerminal(":"),
                      PreferValidAlternativeCombinator(
                        SequenceCombinator(
                          SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = false),
                          ZeroOrOne(
                            SequenceCombinator(
                              LiteralChoiceTerminal("-"),
                              SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = true)
                            )
                          )
                        ),
                        LiteralChoiceTerminal("any")
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        // protocol, then optionally ports
        SequenceCombinator(
          FlexibleLiteralChoiceTerminal("tcp", "udp", ignoreCase = true),
          ZeroOrOne(
            SequenceCombinator(
              LiteralChoiceTerminal(":"),
              PreferValidAlternativeCombinator(
                SequenceCombinator(
                  SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = false),
                  ZeroOrOne(
                    SequenceCombinator(
                      LiteralChoiceTerminal("-"),
                      SafeAtoTerminal(signed = false, base = 0, flags = emptySet(), minInclusive = 0L, maxInclusive = 65535L, acceptsWhitespace = true, acceptsMinus = true)
                    )
                  )
                ),
                LiteralChoiceTerminal("any")
              )
            )
          )
        )
      ),
      EOF())

    val validators = mapOf(
      Validator("config_parse_cgroup_socket_bind", "0") to CgroupSocketBindOptionValue()
    )
  }
}
