package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UNIT_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * BPFProgram= ([Service]/[Socket]/[Mount]/[Swap]/[Slice]/[Scope]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.resource-control.html#BPFProgram=type:program-path
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5878-L5938   config_parse_bpf_foreign_program (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (":", no flags; separators coalesce)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/bpf-program.c#L25-L46   bpf_cgroup_attach_type_table (exact streq lookup)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253   unit_path_printf (specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34   path_simplify_and_warn (PATH_CHECK_ABSOLUTE, normalized)
 * linux  https://man7.org/linux/man-pages/man2/bpf.2.html   (BPF_PROG_ATTACH attach types)
 */

/** bpf_cgroup_attach_type_table: the cgroup attach types, by their bpftool names. */
private val BPF_CGROUP_ATTACH_TYPE = FlexibleLiteralChoiceTerminal(
    "ingress", "egress", "sock_create", "sock_ops", "device", "bind4", "bind6", "connect4", "connect6",
    "post_bind4", "post_bind6", "sendmsg4", "sendmsg6", "sysctl", "recvmsg4", "recvmsg6", "getsockopt", "setsockopt",
)

/**
 * Validator for `BPFProgram=TYPE:PATH`.
 *
 * config_parse_bpf_foreign_program() splits off the first `:`-delimited word (extract_first_word with
 * `:` as the only separator; a run of colons coalesces, so `egress::/x` still leaves `/x`), requires
 * something after it (leading colons are skipped too, so `:egress:/x` is the type `egress`), and looks the word up in bpf_cgroup_attach_type_table — one of `ingress`,
 * `egress`, `sock_create`, `sock_ops`, `device`, `bind4`, `bind6`, `connect4`, `connect6`,
 * `post_bind4`, `post_bind6`, `sendmsg4`, `sendmsg6`, `sysctl`, `recvmsg4`, `recvmsg6`, `getsockopt`,
 * `setsockopt` (exact, case-sensitive).
 *
 * The rest is a path: specifiers are expanded (unit_path_printf), then path_simplify_and_warn()
 * requires it to be absolute and normalized (no `..` component). That is [UNIT_PATH]: it must start
 * with `/` or a specifier and contain no `..` component; the rest of the value belongs to the path, so
 * further colons are part of it. Every failure is "…, ignoring", so it is flagged.
 *
 * Not checked: that the path is on bpffs (the man page says it generally starts `/sys/fs/bpf/`, but the
 * parser does not require it — the attach simply fails at runtime).
 *
 * Covers: Service.BPFProgram, Socket.BPFProgram, Mount.BPFProgram, Swap.BPFProgram, Slice.BPFProgram,
 * Scope.BPFProgram.
 *
 * The empty assignment removes all programs and never reaches the grammar.
 */
class ConfigParseBpfForeignProgramOptionValue : SimpleGrammarOptionValues(
    "config_parse_bpf_foreign_program",
    SequenceCombinator(
        ZeroOrOne(RegexTerminal(":+", ":+")),
        BPF_CGROUP_ATTACH_TYPE,
        RegexTerminal(":+", ":+"),
        UNIT_PATH,
        EOF()
    )
)
