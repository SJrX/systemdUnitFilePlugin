package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.bpfDelegateList

/*
 * BPFDelegateMaps= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#BPFDelegateMaps=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L179   config_parse_bpf_delegate_maps (0), via DEFINE_CONFIG_PARSE_PTR
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.h#L271   bpf_delegate_maps_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4221-L4253   bpf_delegate_from_string ("any", or a "," list; unknown names warned and ignored)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4195   DEFINE_STRING_TABLE_LOOKUP(bpf_delegate_map_type) — exact streq
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/generate-bpf-delegate-configs.py   builds bpf_delegate_map_type_table from enum bpf_map_type in src/include/uapi/linux/bpf.h
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.private-bpf.sh#L50   BPFDelegateMaps=BPFMapTypeArray,BPFMapTypeCpumap,BPFMapTypeRingbuf
 * linux  https://man7.org/linux/man-pages/man2/bpf.2.html
 */

/**
 * Validator for `BPFDelegateMaps=` (the bpffs `delegate_maps=` mount option; only effective with `PrivateBPF=yes`).
 *
 * Either `any` (the whole value) or a comma-separated list of the names below — see [bpfDelegateList]
 * for the splitting rules (commas only, no spaces; empty fields are skipped). The names are what
 * src/core/generate-bpf-delegate-configs.py makes of each `BPF_…` enumerator of `enum bpf_map_type` in the
 * uapi bpf.h systemd bundles at 0f0682b773 (`BPF_FOO_BAR` → `BPFFooBar`; aliases written as
 * `X = Y,` are skipped by its regex and therefore not accepted). Regenerate with
 *
 *     python3 src/core/generate-bpf-delegate-configs.py code src/include/uapi/linux/bpf.h
 *
 * An unknown name is logged ("Unknown BPF delegate option, ignoring") and dropped, so it is flagged.
 * The list tracks the bundled kernel header, so a newer systemd may know names this one does not.
 *
 * Covers: Service.BPFDelegateMaps, Socket.BPFDelegateMaps, Mount.BPFDelegateMaps, Swap.BPFDelegateMaps.
 *
 * The empty assignment (mask 0, i.e. nothing delegated) never reaches the grammar.
 */
class ConfigParseBpfDelegateMapsOptionValue : SimpleGrammarOptionValues(
    "config_parse_bpf_delegate_maps",
    bpfDelegateList(
        FlexibleLiteralChoiceTerminal(
            "BPFMapTypeUnspec", "BPFMapTypeHash", "BPFMapTypeArray", "BPFMapTypeProgArray", "BPFMapTypePerfEventArray",
            "BPFMapTypePercpuHash", "BPFMapTypePercpuArray", "BPFMapTypeStackTrace", "BPFMapTypeCgroupArray", "BPFMapTypeLruHash",
            "BPFMapTypeLruPercpuHash", "BPFMapTypeLpmTrie", "BPFMapTypeArrayOfMaps", "BPFMapTypeHashOfMaps", "BPFMapTypeDevmap",
            "BPFMapTypeSockmap", "BPFMapTypeCpumap", "BPFMapTypeXskmap", "BPFMapTypeSockhash", "BPFMapTypeCgroupStorageDeprecated",
            "BPFMapTypeReuseportSockarray", "BPFMapTypePercpuCgroupStorageDeprecated", "BPFMapTypeQueue", "BPFMapTypeStack", "BPFMapTypeSkStorage",
            "BPFMapTypeDevmapHash", "BPFMapTypeStructOps", "BPFMapTypeRingbuf", "BPFMapTypeInodeStorage", "BPFMapTypeTaskStorage",
            "BPFMapTypeBloomFilter", "BPFMapTypeUserRingbuf", "BPFMapTypeCgrpStorage", "BPFMapTypeArena", "BPFMapTypeInsnArray",
            "BPFMapTypeRhash",
        )
    )
)
