package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/**
 * Validator for `[Unit] ConditionVirtualization=` / `AssertVirtualization=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_VIRTUALIZATION; the parameter is
 * checked by condition_test_virtualization (src/shared/condition.c), which accepts, in order:
 * `private-users`, any boolean parse_boolean() understands, the categories `vm` and `container`, and
 * finally any id in virtualization_table (src/basic/virt.c).
 *
 * The names are folded into one terminal rather than an alternation so that the whole value is one
 * token — that keeps error localization and completion pointing at the value itself.
 */
class ConfigParseUnitConditionVirtualizationOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(VIRTUALIZATION)
) {
    companion object {
        private val VIRTUALIZATION = FlexibleLiteralChoiceTerminal(
            // parse_boolean()
            "1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off",
            // categories, plus the userns special case
            "vm", "container", "private-users",
            // virtualization_table — VMs
            "none",
            "kvm",
            "amazon",
            "qemu",
            "bochs",
            "xen",
            "uml",
            "vmware",
            "oracle",
            "microsoft",
            "zvm",
            "parallels",
            "bhyve",
            "qnx",
            "acrn",
            "powervm",
            "apple",
            "sre",
            "google",
            "vm-other",
            // virtualization_table — containers
            "systemd-nspawn",
            "lxc-libvirt",
            "lxc",
            "openvz",
            "docker",
            "podman",
            "rkt",
            "wsl",
            "proot",
            "pouch",
            "container-other",
        )
    }
}
