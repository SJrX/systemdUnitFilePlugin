package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ANY_CONDITION_ARGUMENT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_ARCHITECTURE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_CREDENTIAL
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_FIRMWARE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_VIRTUALIZATION
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.netCondition

/*
 * The [Match] Condition keys of .network / .netdev / .link files — config_parse_net_condition.
 *
 * parser https://github.com/systemd/systemd/blob/048970650c/src/shared/net-condition.c  config_parse_net_condition
 * checks https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c      condition_test_net (table_net)
 * keys   systemd-build/build/{networkd-network,netdev,link-config}-gperf.gperf  (the Match.* keys)
 *
 * config_parse_net_condition strips a single optional leading `!` (negate) and stores the rest, which is
 * then checked at runtime by exactly the same condition_test_* functions as the matching [Unit]
 * Condition*=/Assert*= settings — condition_test_net dispatches all eight shared types to the same leaf
 * checks. So each value grammar below is the shared parameter grammar from grammar/Combinators.kt,
 * wrapped by [netCondition] (which models the `!`-only prefix) instead of [conditionString] (which also
 * handles the `|` trigger the [Unit] parser accepts).
 *
 * Keys: Match.Host, Match.Virtualization, Match.KernelCommandLine, Match.KernelVersion, Match.Version,
 * Match.Credential, Match.Architecture, Match.Firmware, Match.MachineTag.
 */

/** `[Match] Host=` — a hostname, machine/boot/product ID, or fnmatch glob (any non-empty string). */
class ConfigParseNetConditionHostOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(ANY_CONDITION_ARGUMENT)
)

/** `[Match] Virtualization=` — condition_test_virtualization. */
class ConfigParseNetConditionVirtualizationOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(CONDITION_VIRTUALIZATION)
)

/** `[Match] KernelCommandLine=` — a bare option or `name=value` (stored verbatim, any non-empty string). */
class ConfigParseNetConditionKernelCommandLineOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(ANY_CONDITION_ARGUMENT)
)

/** `[Match] KernelVersion=` / `[Match] Version=` — a version comparison list; a bare token is a glob, so
 *  any non-empty string is valid (condition_test_version). */
class ConfigParseNetConditionVersionOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(ANY_CONDITION_ARGUMENT)
)

/** `[Match] Credential=` — credential_name_valid. */
class ConfigParseNetConditionCredentialOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(CONDITION_CREDENTIAL)
)

/** `[Match] Architecture=` — `native` or an architecture_table name. */
class ConfigParseNetConditionArchitectureOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(CONDITION_ARCHITECTURE)
)

/** `[Match] Firmware=` — uefi / device-tree / device-tree-compatible(...) / smbios-field(...). */
class ConfigParseNetConditionFirmwareOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(CONDITION_FIRMWARE)
)

/** `[Match] MachineTag=` — an fnmatch glob against /etc/machine-info TAGS (any non-empty string). */
class ConfigParseNetConditionMachineTagOptionValue : SimpleGrammarOptionValues(
    "config_parse_net_condition", netCondition(ANY_CONDITION_ARGUMENT)
)
