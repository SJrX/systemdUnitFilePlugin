package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PreferValidAlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator



/**
 * FailureAction=, SuccessAction=, JobTimeoutAction=, StartLimitAction= and friends.
 *
 * Every name in systemd's
 * [emergency_action_table](https://github.com/systemd/systemd/blob/0f0682b773/src/core/emergency-action.c#L15-L32),
 * looked up case-sensitively by
 * [parse_emergency_action](https://github.com/systemd/systemd/blob/0f0682b773/src/core/emergency-action.c#L245-L263).
 * In the user manager everything after `exit-force` is refused with EOPNOTSUPP, but the plugin cannot tell the
 * scope of a unit file, so it accepts the full system-manager table.
 */
class EmergencyActionOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of(
      "none", "reboot", "reboot-force", "reboot-immediate", "poweroff", "poweroff-force", "poweroff-immediate", "exit", "exit-force",
      "soft-reboot", "soft-reboot-force", "kexec", "kexec-force", "halt", "halt-force", "halt-immediate",
    )
    const val VALIDATOR_NAME = "config_parse_emergency_action"
  }
}

class KillModeOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME), ValueWarnings {

  /**
   * KillMode=none is accepted with a warning that it is unsafe and deprecated.
   * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L871-L876
   */
  override fun valueWarnings(key: String, value: String): List<ValueWarning> {
    if (value != "none") return emptyList()
    return listOf(
      ValueWarning(
        0, value.length,
        "$key=none is unsafe and deprecated: systemd stops managing the service's processes, so they keep running after " +
          "the service is stopped or restarted. Use mixed or control-group instead."
      )
    )
  }

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("control-group", "process", "mixed", "none")
    const val VALIDATOR_NAME = "config_parse_kill_mode"
  }
}

class ManagedOOMModeOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of("auto", "kill")
    const val VALIDATOR_NAME = "config_parse_managed_oom_mode"
  }
}

class RestartOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("no", "on-success", "on-failure", "on-abnormal", "on-watchdog", "on-abort", "always")
    const val VALIDATOR_NAME = "config_parse_service_restart"
  }
}

class ServiceTypeOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("simple", "forking", "oneshot", "dbus", "notify", "notify-reload", "idle", "exec")
    const val VALIDATOR_NAME = "config_parse_service_type"
  }
}

class ProtectProcOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME) {
  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("noaccess", "invisible", "ptraceable", "default")
    const val VALIDATOR_NAME = "config_parse_protect_proc"

  }
}
class ProcSubsetOptionValue : AbstractGrammarEnumOptionValue(validOptions, VALIDATOR_NAME) {
  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("all", "pid")
    const val VALIDATOR_NAME = "config_parse_proc_subset"

  }
}

class KeyRingModeOptionValue: AbstractGrammarEnumOptionValue(ImmutableSet.of("inherit", "private", "shared"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_keyring_mode"
  }
}



class PersonalityOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("arm64","arm64-be","arm","arm-be","alpha","arc","arc-be","cris","x86-64","x86","ia64","loongarch64","m68k","mips64-le","mips64","mips-le","mips","nios2","parisc64","parisc","ppc64-le","ppc64","ppc","ppc-le","riscv32","riscv64","s390x","s390","sh64","sh","sparc64","sparc","tilegx"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_personality"
  }
}

/**
 * sched_policy_from_string(): a name from sched_policy_table, or (string-table fallback) any safe_atou() number up
 * to INT_MAX.
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/process-util.c#L2317-L2326
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L65-L80
 */
class CpuSchedulingPolicyOptionValue : SimpleGrammarOptionValues(
  VALIDATOR_NAME,
  SequenceCombinator(
    AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("other", "batch", "idle", "fifo", "ext", "rr", ignoreCase = false),
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = Int.MAX_VALUE.toLong(),
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    ),
    EOF(),
  ),
) {
  override fun getAutoCompleteOptions(project: Project): Set<String> = ImmutableSet.of("other", "batch", "idle", "fifo", "ext", "rr")

  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_cpu_sched_policy"
  }
}

/**
 * NUMAPolicy=: every name in
 * [mpol_table](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/numa-util.c#L253-L261), parsed by
 * [DEFINE_CONFIG_PARSE_ENUM_WITH_DEFAULT](https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L174).
 */
class NumaPolicyOptionValue : AbstractGrammarEnumOptionValue(
  ImmutableSet.of("default", "preferred", "bind", "interleave", "local", "preferred-many", "weighted-interleave"),
  VALIDATOR_NAME
) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_numa_policy"
  }
}

/**
 * ioprio_class_from_string(): a name from ioprio_class_table, or (string-table fallback) any safe_atou() number up
 * to IOPRIO_NR_CLASSES-1 (7).
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ioprio-util.c#L24-L31
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L65-L80
 */
class IOSchedulingClassOptionValue : SimpleGrammarOptionValues(
  VALIDATOR_NAME,
  SequenceCombinator(
    AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("none", "realtime", "best-effort", "idle", ignoreCase = false),
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = 7L,
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    ),
    EOF(),
  ),
) {
  override fun getAutoCompleteOptions(project: Project): Set<String> = ImmutableSet.of("none", "realtime", "best-effort", "idle")

  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_io_class"
  }
}

class LogLevelOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("emerg", "alert", "crit", "err", "warning", "notice", "info", "debug"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_log_level"
  }
}

class SysLogFacilityOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("kern","user", "mail", "daemon", "auth", "syslog", "lpr", "news", "uucp", "cron", "authpriv", "ftp", "local0", "local1", "local2", "local3", "local4", "local5", "local6", "local7"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_log_facility"
  }
}

class UtmpModeOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("init", "login", "user"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_utmp_mode"
  }
}

class DevicePolicyOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("auto", "closed", "strict"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_device_policy"
  }
}

class ManagedOOMPreferenceOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("none", "avoid", "omit"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_managed_oom_preference"
  }
}

class ServiceExitTypeOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("main", "cgroup"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_service_exit_type"
  }
}

class ServiceTimeoutFailureModeOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("terminate", "abort", "kill"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_service_timeout_failure_mode"
  }
}

class NotifyAccessOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("none", "main", "exec", "all"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_notify_access"
  }
}

class OOMPolicyOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("continue", "stop", "kill"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_oom_policy"
  }
}

class SocketTimestampingOptionValue : AbstractGrammarEnumOptionValue(ImmutableSet.of("off", "us", "usec", "µs", "ns", "nsec"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_socket_timestamping"
  }
}

/**
 * [Socket] SocketProtocol=.
 *
 * [parse_socket_protocol](https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L87-L97) runs
 * [parse_ip_protocol_full](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ip-protocol-list.c#L36-L70)
 * (a protocol name in any case, or a safe_atoi number that has a name) and then keeps only IPPROTO_UDPLITE (136),
 * IPPROTO_SCTP (132) and IPPROTO_MPTCP (262). So `mptcp`, `SCTP`, `136` and `0x84` are all accepted.
 */
class SocketProtocolOptionValue : SimpleGrammarOptionValues(
  VALIDATOR_NAME,
  SequenceCombinator(
    PreferValidAlternativeCombinator(
      FlexibleLiteralChoiceTerminal("udplite", "sctp", "mptcp", ignoreCase = true),
      SafeAtoTerminal(signed = true, base = 0, flags = emptySet(), minInclusive = 136L, maxInclusive = 136L, acceptsWhitespace = true, acceptsMinus = true),
      SafeAtoTerminal(signed = true, base = 0, flags = emptySet(), minInclusive = 132L, maxInclusive = 132L, acceptsWhitespace = true, acceptsMinus = true),
      SafeAtoTerminal(signed = true, base = 0, flags = emptySet(), minInclusive = 262L, maxInclusive = 262L, acceptsWhitespace = true, acceptsMinus = true),
    ),
    EOF()
  )
) {
  override fun getAutoCompleteOptions(project: Project): Set<String> = setOf("udplite", "sctp", "mptcp")

  companion object {
    const val VALIDATOR_NAME = "config_parse_socket_protocol"
  }
}

/**
 * [Socket] BindIPv6Only=.
 *
 * [socket_address_bind_ipv6_only_or_bool_from_string](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-label.c#L21-L39)
 * first tries parse_boolean (true means `ipv6-only`, false means `both`) and then the case-sensitive table
 * `default`, `both`, `ipv6-only`.
 */
class SocketBindOptionValue : SimpleGrammarOptionValues(
  VALIDATOR_NAME,
  SequenceCombinator(
    PreferValidAlternativeCombinator(
      FlexibleLiteralChoiceTerminal("default", "both", "ipv6-only"),
      BOOLEAN,
    ),
    EOF()
  )
) {
  override fun getAutoCompleteOptions(project: Project): Set<String> = setOf("default", "both", "ipv6-only")

  companion object {
    const val VALIDATOR_NAME = "config_parse_socket_bind"
  }
}

object EnumOptionValues {
  val validators =

    mapOf(
      Validator(ServiceTypeOptionValue.VALIDATOR_NAME) to ServiceTypeOptionValue(),
      Validator(ProtectProcOptionValue.VALIDATOR_NAME) to ProtectProcOptionValue(),
      Validator(ProcSubsetOptionValue.VALIDATOR_NAME) to ProcSubsetOptionValue(),
      Validator(RestartOptionValue.VALIDATOR_NAME) to RestartOptionValue(),
      Validator(ManagedOOMModeOptionValue.VALIDATOR_NAME) to ManagedOOMModeOptionValue(),
      Validator(KillModeOptionValue.VALIDATOR_NAME) to KillModeOptionValue(),
      Validator(EmergencyActionOptionValue.VALIDATOR_NAME) to EmergencyActionOptionValue(),
      Validator(KeyRingModeOptionValue.VALIDATOR_NAME) to KeyRingModeOptionValue(),
      Validator(PersonalityOptionValue.VALIDATOR_NAME) to PersonalityOptionValue(),
      Validator(CpuSchedulingPolicyOptionValue.VALIDATOR_NAME) to CpuSchedulingPolicyOptionValue(),
      Validator(NumaPolicyOptionValue.VALIDATOR_NAME) to NumaPolicyOptionValue(),
      Validator(IOSchedulingClassOptionValue.VALIDATOR_NAME) to IOSchedulingClassOptionValue(),
      Validator(LogLevelOptionValue.VALIDATOR_NAME) to LogLevelOptionValue(),
      Validator(SysLogFacilityOptionValue.VALIDATOR_NAME) to SysLogFacilityOptionValue(),
      Validator(UtmpModeOptionValue.VALIDATOR_NAME) to UtmpModeOptionValue(),
      Validator(DevicePolicyOptionValue.VALIDATOR_NAME) to DevicePolicyOptionValue(),
      Validator(ManagedOOMPreferenceOptionValue.VALIDATOR_NAME) to ManagedOOMPreferenceOptionValue(),
      Validator(ServiceExitTypeOptionValue.VALIDATOR_NAME) to ServiceExitTypeOptionValue(),
      Validator(ServiceTimeoutFailureModeOptionValue.VALIDATOR_NAME) to ServiceTimeoutFailureModeOptionValue(),
      Validator(NotifyAccessOptionValue.VALIDATOR_NAME) to NotifyAccessOptionValue(),
      Validator(OOMPolicyOptionValue.VALIDATOR_NAME) to OOMPolicyOptionValue(),
      Validator(SocketTimestampingOptionValue.VALIDATOR_NAME) to SocketTimestampingOptionValue(),
      Validator(SocketProtocolOptionValue.VALIDATOR_NAME) to SocketProtocolOptionValue(),
      Validator(SocketBindOptionValue.VALIDATOR_NAME) to SocketBindOptionValue(),
    )

}
