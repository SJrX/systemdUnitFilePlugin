package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.google.common.collect.ImmutableSet
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator



class EmergencyActionOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of("none", "reboot", "reboot-force", "reboot-immediate", "poweroff", "poweroff-force", "poweroff-immediate", "exit", "exit-force")
    const val VALIDATOR_NAME = "config_parse_emergency_action"
  }
}

class KillModeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("control-group", "process", "mixed", "none")
    const val VALIDATOR_NAME = "config_parse_kill_mode"
  }
}

class ManagedOOMModeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME){

  companion object {
    private val validOptions : Set<String> = ImmutableSet.of("auto", "kill")
    const val VALIDATOR_NAME = "config_parse_managed_oom_mode"
  }
}

class RestartOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("no", "on-success", "on-failure", "on-abnormal", "on-watchdog", "on-abort", "always")
    const val VALIDATOR_NAME = "config_parse_service_restart"
  }
}

class ServiceTypeOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {

  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("simple", "forking", "oneshot", "dbus", "notify", "idle", "exec")
    const val VALIDATOR_NAME = "config_parse_service_type"
  }
}

class ProtectProcOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {
  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("noaccess", "invisible", "ptraceable", "default")
    const val VALIDATOR_NAME = "config_parse_protect_proc"

  }
}
class ProcSubsetOptionValue : AbstractEnumOptionValue(validOptions, VALIDATOR_NAME) {
  companion object {
    private val validOptions: Set<String> = ImmutableSet.of("all", "pid")
    const val VALIDATOR_NAME = "config_parse_proc_subset"

  }
}

class KeyRingModeOptionValue: AbstractEnumOptionValue(ImmutableSet.of("inherit", "private", "shared"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_keyring_mode"
  }
}



class PersonalityOptionValue : AbstractEnumOptionValue(ImmutableSet.of("arm64","arm64-be","arm","arm-be","alpha","arc","arc-be","cris","x86-64","x86","ia64","loongarch64","m68k","mips64-le","mips64","mips-le","mips","nios2","parisc64","parisc","ppc64-le","ppc64","ppc","ppc-le","riscv32","riscv64","s390x","s390","sh64","sh","sparc64","sparc","tilegx"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_personality"
  }
}

class CpuSchedulingPolicyOptionValue : AbstractEnumOptionValue(ImmutableSet.of("other", "batch", "idle", "fifo", "rr"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_cpu_sched_policy"
  }
}

class NumaPolicyOptionValue : AbstractEnumOptionValue(ImmutableSet.of("default", "bind", "interleave", "local"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_numa_policy"
  }
}

class IOSchedulingClassOptionValue : AbstractEnumOptionValue(ImmutableSet.of("realtime", "best-effort", "idle"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_io_class"
  }
}

class LogLevelOptionValue : AbstractEnumOptionValue(ImmutableSet.of("emerg", "alert", "crit", "err", "warning", "notice", "info", "debug"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_log_level"
  }
}

class SysLogFacilityOptionValue : AbstractEnumOptionValue(ImmutableSet.of("kern","user", "mail", "daemon", "auth", "syslog", "lpr", "news", "uucp", "cron", "authpriv", "ftp", "local0", "local1", "local2", "local3", "local4", "local5", "local6", "local7"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_log_facility"
  }
}

class UtmpModeOptionValue : AbstractEnumOptionValue(ImmutableSet.of("init", "login", "user"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_exec_utmp_mode"
  }
}

class DevicePolicyOptionValue : AbstractEnumOptionValue(ImmutableSet.of("auto", "closed", "strict"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_device_policy"
  }
}

class ManagedOOMPreferenceOptionValue : AbstractEnumOptionValue(ImmutableSet.of("none", "avoid", "omit"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_managed_oom_preference"
  }
}

class ServiceExitTypeOptionValue : AbstractEnumOptionValue(ImmutableSet.of("main", "cgroup"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_service_exit_type"
  }
}

class ServiceTimeoutFailureModeOptionValue : AbstractEnumOptionValue(ImmutableSet.of("terminate", "abort", "kill"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_service_timeout_failure_mode"
  }
}
class NotifyAccessOptionValue : AbstractEnumOptionValue(ImmutableSet.of("none", "main", "exec", "all"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_notify_access"
  }
}

class OOMPolicyOptionValue : AbstractEnumOptionValue(ImmutableSet.of("continue", "stop", "kill"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_oom_policy"
  }
}

class SocketTimestampingOptionValue : AbstractEnumOptionValue(ImmutableSet.of("off", "us", "usec", "µs", "ns", "nsec"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_socket_timestamping"
  }
}

class SocketProtocolOptionValue : AbstractEnumOptionValue(ImmutableSet.of("udplite", "sctp"), VALIDATOR_NAME) {
  companion object {
    const val VALIDATOR_NAME = "config_parse_socket_protocol"
  }
}

class SocketBindOptionValue : AbstractEnumOptionValue(ImmutableSet.of("default", "both", "ipv6-only"), VALIDATOR_NAME) {

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
