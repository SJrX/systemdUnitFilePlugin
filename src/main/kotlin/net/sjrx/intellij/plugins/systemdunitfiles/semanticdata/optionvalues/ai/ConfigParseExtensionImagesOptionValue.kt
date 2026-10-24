package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EXEC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EXTENSION_IMAGE_SOURCE_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.imageMountOptionsTail
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] ExtensionImages=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#ExtensionImages=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5457-L5592   config_parse_extension_images (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (UNQUOTE|RETAIN_ESCAPE words, then ':' with CUNESCAPE|UNESCAPE_SEPARATORS, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253             unit_path_printf (source specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34          path_simplify_and_warn (PATH_CHECK_ABSOLUTE|PATH_CHECK_NON_API_VFS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/mountpoint-util.c#L721        path_below_api_vfs (/dev, /sys, /proc)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/gpt.c#L174                   partition_designator_from_string
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-50-DISSECT.mountfsd.sh#L135
 */

/**
 * Validator for `ExtensionImages=` (config_parse_extension_images, ltype 0).
 *
 * A whitespace-separated list of `[-]SOURCE[:OPTIONS | :PARTITION:OPTIONS…]` words. SOURCE is absolute
 * after specifier expansion, free of `..`, and — unlike MountImages= — may not be below /dev, /proc or
 * /sys (PATH_CHECK_NON_API_VFS, no DEV_OK). The option tail is the same as MountImages=: a single
 * root-partition option string, or `PARTITION:OPTIONS` pairs. ':' separators are coalesced; a ':' in
 * the path is written `\:`.
 *
 * Lenient corners: quoted runs are opaque, `%` specifiers and the mount options are not checked.
 */
class ConfigParseExtensionImagesOptionValue : SimpleGrammarOptionValues(
  "config_parse_extension_images",
  whitespaceList(
    SequenceCombinator(
      ZeroOrOne(EXEC_COALESCED_COLONS),
      EXTENSION_IMAGE_SOURCE_FIELD,
      imageMountOptionsTail(),
    )
  )
)
