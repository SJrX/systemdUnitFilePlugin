package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.bpfDelegateList

/*
 * BPFDelegateCommands= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#BPFDelegateCommands=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L178   config_parse_bpf_delegate_commands (0), via DEFINE_CONFIG_PARSE_PTR
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.h#L263   bpf_delegate_commands_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4221-L4253   bpf_delegate_from_string ("any", or a "," list; unknown names warned and ignored)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4194   DEFINE_STRING_TABLE_LOOKUP(bpf_delegate_cmd) — exact streq
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/generate-bpf-delegate-configs.py   builds bpf_delegate_cmd_table from enum bpf_cmd in src/include/uapi/linux/bpf.h
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.private-bpf.sh#L49   BPFDelegateCommands=BPFObjPin,BPFBtfLoad,BPFMapFreeze,BPFLinkDetach
 * linux  https://man7.org/linux/man-pages/man2/bpf.2.html
 */

/**
 * Validator for `BPFDelegateCommands=` (the bpffs `delegate_cmds=` mount option; only effective with `PrivateBPF=yes`).
 *
 * Either `any` (the whole value) or a comma-separated list of the names below — see [bpfDelegateList]
 * for the splitting rules (commas only, no spaces; empty fields are skipped). The names are what
 * src/core/generate-bpf-delegate-configs.py makes of each `BPF_…` enumerator of `enum bpf_cmd` in the
 * uapi bpf.h systemd bundles at 0f0682b773 (`BPF_FOO_BAR` → `BPFFooBar`; aliases written as
 * `X = Y,` are skipped by its regex and therefore not accepted). Regenerate with
 *
 *     python3 src/core/generate-bpf-delegate-configs.py code src/include/uapi/linux/bpf.h
 *
 * An unknown name is logged ("Unknown BPF delegate option, ignoring") and dropped, so it is flagged.
 * The list tracks the bundled kernel header, so a newer systemd may know names this one does not.
 *
 * Covers: Service.BPFDelegateCommands, Socket.BPFDelegateCommands, Mount.BPFDelegateCommands, Swap.BPFDelegateCommands.
 *
 * The empty assignment (mask 0, i.e. nothing delegated) never reaches the grammar.
 */
class ConfigParseBpfDelegateCommandsOptionValue : SimpleGrammarOptionValues(
    "config_parse_bpf_delegate_commands",
    bpfDelegateList(
        FlexibleLiteralChoiceTerminal(
            "BPFMapCreate", "BPFMapLookupElem", "BPFMapUpdateElem", "BPFMapDeleteElem", "BPFMapGetNextKey",
            "BPFProgLoad", "BPFObjPin", "BPFObjGet", "BPFProgAttach", "BPFProgDetach",
            "BPFProgTestRun", "BPFProgGetNextId", "BPFMapGetNextId", "BPFProgGetFdById", "BPFMapGetFdById",
            "BPFObjGetInfoByFd", "BPFProgQuery", "BPFRawTracepointOpen", "BPFBtfLoad", "BPFBtfGetFdById",
            "BPFTaskFdQuery", "BPFMapLookupAndDeleteElem", "BPFMapFreeze", "BPFBtfGetNextId", "BPFMapLookupBatch",
            "BPFMapLookupAndDeleteBatch", "BPFMapUpdateBatch", "BPFMapDeleteBatch", "BPFLinkCreate", "BPFLinkUpdate",
            "BPFLinkGetFdById", "BPFLinkGetNextId", "BPFEnableStats", "BPFIterCreate", "BPFLinkDetach",
            "BPFProgBindMap", "BPFTokenCreate", "BPFProgStreamReadByFd", "BPFProgAssocStructOps",
        )
    )
)
