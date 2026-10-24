package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * [Link] Advertise= and [EnergyEfficientEthernet] LinkMode= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#Advertise=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ethtool-util.c#L1125-L1177   config_parse_advertise (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ethtool-util.c#L162-L168   ethtool_link_mode_bit_table (DEFINE_STRING_TABLE_LOOKUP)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ethtool-link-mode.py   (generates the table from enum ethtool_link_mode_bit_indices)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/ethtool.h#L1966-L2109   enum ethtool_link_mode_bit_indices
 * linux  https://man7.org/linux/man-pages/man8/ethtool.8.html
 */

/**
 * The link-mode names ethtool-link-mode.py derives from the vendored kernel header: each
 * `ETHTOOL_LINK_MODE_<name>_BIT` lower-cased with `_` turned into `-`, and `autoneg` renamed
 * `autonegotiation`. 125 names at 0f0682b773.
 */
private val LINK_MODE = FlexibleLiteralChoiceTerminal(
    "10baset-half", "10baset-full", "100baset-half", "100baset-full", "1000baset-half",
    "1000baset-full", "autonegotiation", "tp", "aui", "mii", "fibre", "bnc", "10000baset-full",
    "pause", "asym-pause", "2500basex-full", "backplane", "1000basekx-full", "10000basekx4-full",
    "10000basekr-full", "10000baser-fec", "20000basemld2-full", "20000basekr2-full",
    "40000basekr4-full", "40000basecr4-full", "40000basesr4-full", "40000baselr4-full",
    "56000basekr4-full", "56000basecr4-full", "56000basesr4-full", "56000baselr4-full",
    "25000basecr-full", "25000basekr-full", "25000basesr-full", "50000basecr2-full",
    "50000basekr2-full", "100000basekr4-full", "100000basesr4-full", "100000basecr4-full",
    "100000baselr4-er4-full", "50000basesr2-full", "1000basex-full", "10000basecr-full",
    "10000basesr-full", "10000baselr-full", "10000baselrm-full", "10000baseer-full",
    "2500baset-full", "5000baset-full", "fec-none", "fec-rs", "fec-baser", "50000basekr-full",
    "50000basesr-full", "50000basecr-full", "50000baselr-er-fr-full", "50000basedr-full",
    "100000basekr2-full", "100000basesr2-full", "100000basecr2-full", "100000baselr2-er2-fr2-full",
    "100000basedr2-full", "200000basekr4-full", "200000basesr4-full", "200000baselr4-er4-fr4-full",
    "200000basedr4-full", "200000basecr4-full", "100baset1-full", "1000baset1-full",
    "400000basekr8-full", "400000basesr8-full", "400000baselr8-er8-fr8-full", "400000basedr8-full",
    "400000basecr8-full", "fec-llrs", "100000basekr-full", "100000basesr-full",
    "100000baselr-er-fr-full", "100000basecr-full", "100000basedr-full", "200000basekr2-full",
    "200000basesr2-full", "200000baselr2-er2-fr2-full", "200000basedr2-full", "200000basecr2-full",
    "400000basekr4-full", "400000basesr4-full", "400000baselr4-er4-fr4-full", "400000basedr4-full",
    "400000basecr4-full", "100basefx-half", "100basefx-full", "10baset1l-full", "800000basecr8-full",
    "800000basekr8-full", "800000basedr8-full", "800000basedr8-2-full", "800000basesr8-full",
    "800000basevr8-full", "10baset1s-full", "10baset1s-half", "10baset1s-p2mp-half",
    "10baset1brr-full", "200000basecr-full", "200000basekr-full", "200000basedr-full",
    "200000basedr-2-full", "200000basesr-full", "200000basevr-full", "400000basecr2-full",
    "400000basekr2-full", "400000basedr2-full", "400000basedr2-2-full", "400000basesr2-full",
    "400000basevr2-full", "800000basecr4-full", "800000basekr4-full", "800000basedr4-full",
    "800000basedr4-2-full", "800000basesr4-full", "800000basevr4-full", "1600000basecr8-full",
    "1600000basekr8-full", "1600000basedr8-full", "1600000basedr8-2-full"
)

/**
 * Validator for `[Link] Advertise=` and `[EnergyEfficientEthernet] LinkMode=` (.link).
 *
 * C function: config_parse_advertise. A whitespace-separated list (extract_first_word, no unquoting) of
 * ethtool link-mode names, each looked up exactly (case-sensitive) in ethtool_link_mode_bit_table —
 * speed/duplex modes like `1000baset-full` as well as the non-speed bits (`autonegotiation`, `pause`,
 * `asym-pause`, `tp`, `fibre`, `fec-rs`, …).
 *
 * The table is generated at build time from the kernel's enum ethtool_link_mode_bit_indices in
 * systemd's vendored copy of linux/ethtool.h (src/include/uapi, which meson puts first on the include
 * path). That is wider than the ethtool-link-mode.xml table in the plugin's generated man data, which
 * lists 102 names; this follows the header the C is built against. An unknown name is logged and
 * skipped by udev; an empty assignment clears the list.
 */
class ConfigParseAdvertiseOptionValue : SimpleGrammarOptionValues(
    "config_parse_advertise",
    SequenceCombinator(LINK_MODE, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), LINK_MODE)), EOF())
)
