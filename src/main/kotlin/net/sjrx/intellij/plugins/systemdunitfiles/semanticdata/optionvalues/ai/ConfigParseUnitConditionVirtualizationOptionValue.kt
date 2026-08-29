package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionVirtualization= / AssertVirtualization=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionVirtualization=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c   config_parse_unit_condition_string (CONDITION_VIRTUALIZATION)
 * check  https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/condition.c     condition_test_virtualization
 * values https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/virt.c           virtualization_table
 *
 * The check tries, in order: the literal "private-users", parse_boolean(), the categories "vm" and
 * "container", and finally an exact id from the table — so all four groups are one choice set here.
 */

/**
 * Validator for `[Unit] ConditionVirtualization=` / `AssertVirtualization=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_VIRTUALIZATION; the parameter is
 * checked by condition_test_virtualization (src/shared/condition.c), which accepts, in order:
 * `private-users`, any boolean parse_boolean() understands, the categories `vm` and `container`, and
 * finally any id in virtualization_table (src/basic/virt.c).
 *
 * The boolean is kept as its own [BOOLEAN] alternative rather than folded into the name list: to
 * systemd these really are two different branches, and a later formatting or completion pass can only
 * tell "this span is a boolean" from "this span is a virtualization id" if the grammar says so.
 *
 * [BOOLEAN] has to come second. It matches a prefix of the value, so on `none` — a real entry in
 * virtualization_table — it would otherwise match the leading `no` and strand `ne`, and under the
 * classic engine AlternativeCombinator never backtracks out of a branch that matched.
 */
class ConfigParseUnitConditionVirtualizationOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(AlternativeCombinator(VIRTUALIZATION, BOOLEAN))
) {
    companion object {
        private val VIRTUALIZATION = FlexibleLiteralChoiceTerminal(
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
