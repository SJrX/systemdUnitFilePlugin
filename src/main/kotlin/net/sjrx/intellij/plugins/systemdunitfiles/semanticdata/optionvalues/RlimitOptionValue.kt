package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.util.Optional

class RlimitOptionValue(grammar : Combinator) : GrammarOptionValue("config_parse_rlimit", grammar) {
  companion object {

    val GENERIC_SEQ =
      AlternativeCombinator(
        OptionalWhitespacePrefix(FlexibleLiteralChoiceTerminal("infinity")),
        OptionalWhitespacePrefix(IntegerTerminal(0, Int.MAX_VALUE)))

    val BYTE_SEQ = AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("infinity"),
      SequenceCombinator(
        OptionalWhitespacePrefix(IntegerTerminal(0, Int.MAX_VALUE)),
        OptionalWhitespacePrefix(LiteralChoiceTerminal("K", "M", "G", "T", "P", "E"))
      ),
      OptionalWhitespacePrefix(IntegerTerminal(0, Int.MAX_VALUE)))

    val TIME_SEQ = AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("infinity"),
      OneOrMore(
        SequenceCombinator(
          OptionalWhitespacePrefix(IntegerTerminal(0, Int.MAX_VALUE)),
          OptionalWhitespacePrefix(FlexibleLiteralChoiceTerminal("usec", "us", "μs", "msec", "ms", "seconds", "second", "sec", "s", "minutes", "minute", "min", "m", "hours", "hour", "hr", "h", "days", "day", "d", "weeks", "week", "w", "months", "month", "M", "years", "year", "y"))
        )
      ),
      OptionalWhitespacePrefix(IntegerTerminal(0, Int.MAX_VALUE))
    )

    val NICE_SEQ = AlternativeCombinator(
      SequenceCombinator(LiteralChoiceTerminal("+", "-"), IntegerTerminal(0, 21)),
      OptionalWhitespacePrefix(SequenceCombinator(IntegerTerminal(0, 41))),
    )

    val COLON = LiteralChoiceTerminal(":")

    val BYTE_RLIMIT = SequenceCombinator(AlternativeCombinator(SequenceCombinator(BYTE_SEQ, COLON, BYTE_SEQ), BYTE_SEQ), EOF())
    val TIME_RLIMIT = SequenceCombinator(AlternativeCombinator(SequenceCombinator(TIME_SEQ, COLON, TIME_SEQ), TIME_SEQ), EOF())
    val GENERIC_RLIMIT = SequenceCombinator(AlternativeCombinator(SequenceCombinator(GENERIC_SEQ, COLON, GENERIC_SEQ), GENERIC_SEQ), EOF())
    val NICE_RLIMIT = SequenceCombinator(AlternativeCombinator(SequenceCombinator(NICE_SEQ, COLON, NICE_SEQ), NICE_SEQ), EOF())


    val validators = mapOf(
      //        Exec.LimitCPU,                config_parse_rlimit,         RLIMIT_CPU,               offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_CPU") to RlimitOptionValue(TIME_RLIMIT),
      //        Exec.LimitFSIZE,              config_parse_rlimit,         RLIMIT_FSIZE,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_FSIZE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitDATA,               config_parse_rlimit,         RLIMIT_DATA,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_DATA") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitSTACK,              config_parse_rlimit,         RLIMIT_STACK,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_STACK") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitCORE,               config_parse_rlimit,         RLIMIT_CORE,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_CORE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitRSS,                config_parse_rlimit,         RLIMIT_RSS,               offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RSS") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNOFILE,             config_parse_rlimit,         RLIMIT_NOFILE,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NOFILE") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitAS,                 config_parse_rlimit,         RLIMIT_AS,                offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_AS") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNPROC,              config_parse_rlimit,         RLIMIT_NPROC,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NPROC") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitMEMLOCK,            config_parse_rlimit,         RLIMIT_MEMLOCK,           offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_MEMLOCK") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitLOCKS,              config_parse_rlimit,         RLIMIT_LOCKS,             offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_LOCKS") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitSIGPENDING,         config_parse_rlimit,         RLIMIT_SIGPENDING,        offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_SIGPENDING") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitMSGQUEUE,           config_parse_rlimit,         RLIMIT_MSGQUEUE,          offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_MSGQUEUE") to RlimitOptionValue(BYTE_RLIMIT),
      //        Exec.LimitNICE,               config_parse_rlimit,         RLIMIT_NICE,              offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_NICE") to RlimitOptionValue(NICE_RLIMIT),
      //        Exec.LimitRTPRIO,             config_parse_rlimit,         RLIMIT_RTPRIO,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RTPRIO") to RlimitOptionValue(GENERIC_RLIMIT),
      //        Exec.LimitRTTIME,             config_parse_rlimit,         RLIMIT_RTTIME,            offsetof(Settings, rlimit)
      Validator("config_parse_rlimit", "RLIMIT_RTTIME") to RlimitOptionValue(TIME_RLIMIT),
    )
  }
}
