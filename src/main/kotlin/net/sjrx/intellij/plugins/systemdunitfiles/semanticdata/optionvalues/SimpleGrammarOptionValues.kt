package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger



open class SimpleGrammarOptionValues(validatorName: String, grammar: Combinator) : GrammarOptionValue(validatorName, grammar) {


  companion object {

    val Capabilities = FlexibleLiteralChoiceTerminal(
      "CAP_AUDIT_CONTROL",
      "CAP_AUDIT_READ",
      "CAP_AUDIT_WRITE",
      "CAP_BLOCK_SUSPEND",
      "CAP_BPF",
      "CAP_CHECKPOINT_RESTORE",
      "CAP_CHOWN",
      "CAP_DAC_OVERRIDE",
      "CAP_DAC_READ_SEARCH",
      "CAP_FOWNER",
      "CAP_FSETID",
      "CAP_IPC_LOCK",
      "CAP_IPC_OWNER",
      "CAP_KILL",
      "CAP_LEASE",
      "CAP_LINUX_IMMUTABLE",
      "CAP_MAC_ADMIN",
      "CAP_MAC_OVERRIDE",
      "CAP_MKNOD",
      "CAP_NET_ADMIN",
      "CAP_NET_BIND_SERVICE",
      "CAP_NET_BROADCAST",
      "CAP_NET_RAW",
      "CAP_PERFMON",
      "CAP_SETGID",
      "CAP_SETFCAP",
      "CAP_SETPCAP",
      "CAP_SETUID",
      "CAP_SYS_ADMIN",
      "CAP_SYS_BOOT",
      "CAP_SYS_CHROOT",
      "CAP_SYS_MODULE",
      "CAP_SYS_NICE",
      "CAP_SYS_PACCT",
      "CAP_SYS_PTRACE",
      "CAP_SYS_RAWIO",
      "CAP_SYS_RESOURCE",
      "CAP_SYS_TIME",
      "CAP_SYS_TTY_CONFIG",
      "CAP_SYSLOG",
      "CAP_WAKE_ALARM"
    )

    val validators = mapOf(
      Validator("config_parse_ip_port", "0") to SimpleGrammarOptionValues("config_parse_ip_port", SequenceCombinator(IntegerTerminal(0, 65536 ), EOF())),
      Validator("config_parse_coalesce_u32", "0") to SimpleGrammarOptionValues("config_parse_coalesce_u32",
                                                                               SequenceCombinator(
                                                                                 AlternativeCombinator(
                                                                                   // Handles hex
                                                                                   SequenceCombinator(LiteralChoiceTerminal("0x"), OneOrMore(RegexTerminal("[0-9a-zA-Z]{1,8}", "[0-9a-fA-F]{1,8}"))),
                                                                                      // Handles oct and dec formats (although not up to 32 bits for octal)
                                                                                    IntegerTerminal(0, 4_294_967_296),
                                                                                      ),
                                                                                 EOF())),

      // config_parse_capability_set() → parse_capability_set(): an optional leading '~', then words
      // (extract_first_word with EXTRACT_UNQUOTE|EXTRACT_RELAX). capability_set_from_string() only logs unknown
      // names at debug level and skips them, so systemd's parser accepts any word; the known names are offered for
      // completion. https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1866-L1897
      // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/capability-list.c#L160-L186
      Validator("config_parse_capability_set", "0") to SimpleGrammarOptionValues("config_parse_capability_set",
                                                                                 SequenceCombinator(
                                                                                   ZeroOrOne(LiteralChoiceTerminal("~")),
                                                                                   ZeroOrOne(WhitespaceTerminal()),
                                                                                   ZeroOrOne(
                                                                                     SequenceCombinator(
                                                                                       AlternativeCombinator(RegexTerminal("\\S+", "\\S+"), Capabilities),
                                                                                       ZeroOrMore(
                                                                                         SequenceCombinator(
                                                                                           WhitespaceTerminal(),
                                                                                           AlternativeCombinator(RegexTerminal("\\S+", "\\S+"), Capabilities),
                                                                                         )
                                                                                       ),
                                                                                       ZeroOrOne(WhitespaceTerminal()),
                                                                                     )
                                                                                   ),
                                                                                 EOF())),

      // config_parse_exec_quota(): "off", a parse_permyriad() percentage (at most 100%), or a parse_size() byte count
      // (base 1024, the full 64-bit range).
      // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4668-L4708
      Validator("config_parse_exec_quota", "0") to SimpleGrammarOptionValues("config_parse_exec_quota",
                                                                               SequenceCombinator(
                                                                                 PreferValidAlternativeCombinator(
                                                                                   LiteralChoiceTerminal("off"),
                                                                                   TC_PERCENT,
                                                                                   parseSize(base = 1024, minInclusive = BigInteger.ZERO, maxInclusive = SafeAtoTerminal.UINT64_MAX, allowInnerWhitespace = true),
                                                                                 ),
                                                                                 EOF()
                                                                               ))

    )

  }
}

