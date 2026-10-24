package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EXEC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.MOUNT_IMAGE_DESTINATION_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.MOUNT_IMAGE_SOURCE_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.imageMountOptionsTail
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] MountImages=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#MountImages=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5307-L5455   config_parse_mount_images (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (UNQUOTE|RETAIN_ESCAPE words, then ':' with CUNESCAPE|UNESCAPE_SEPARATORS, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253             unit_path_printf (source/destination specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34          path_simplify_and_warn (PATH_CHECK_ABSOLUTE|PATH_CHECK_NON_API_VFS_DEV_OK)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/mountpoint-util.c#L721        path_below_api_vfs (/dev, /sys, /proc)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/gpt.c#L174                   partition_designator_from_string
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-50-DISSECT.mountfsd.sh#L161
 */

/**
 * Validator for `MountImages=` (config_parse_mount_images, ltype 0).
 *
 * A whitespace-separated list of `[-]SOURCE:DESTINATION[:OPTIONS | :PARTITION:OPTIONS…]` words:
 *  - SOURCE (optionally `-`-prefixed) and DESTINATION are both required, absolute after specifier
 *    expansion, free of `..`, and not below /proc or /sys (/dev is allowed: DEV_OK);
 *  - then either a single mount-option string for the root partition, or `PARTITION:OPTIONS` pairs
 *    (an optional lone trailing option string still applies to root). A pair whose partition name is
 *    unknown is logged and skipped.
 *  - ':' separators are coalesced, so `a::b` equals `a:b`; a ':' inside a path is written `\:`.
 *
 * Lenient corners: quoted runs are opaque, `%` specifiers and the mount options themselves are not
 * checked.
 */
class ConfigParseMountImagesOptionValue : SimpleGrammarOptionValues(
  "config_parse_mount_images",
  whitespaceList(
    SequenceCombinator(
      ZeroOrOne(EXEC_COALESCED_COLONS),
      MOUNT_IMAGE_SOURCE_FIELD,
      EXEC_COALESCED_COLONS,
      MOUNT_IMAGE_DESTINATION_FIELD,
      imageMountOptionsTail(),
    )
  )
)
