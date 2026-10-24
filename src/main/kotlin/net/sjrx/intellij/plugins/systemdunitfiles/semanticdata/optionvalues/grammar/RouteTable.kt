package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Route tables as systemd-networkd reads them, pinned to systemd 0f0682b773 (the plugin's last_commit_hash).
 *
 * Every route-table setting hands its whole (stripped) value to manager_get_route_table_from_string():
 * [Route] Table=, [RoutingPolicyRule] Table=, [VRF] Table=, [WireGuard] RouteTable=, [WireGuardPeer] RouteTable=,
 * and RouteTable= in [DHCPv4], [DHCPv6] and [IPv6AcceptRA]. That function tries, in order
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L435-L464):
 *
 *  1. the predefined names `default`, `main`, `local`, compared case-sensitively
 *     (https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L427-L433);
 *  2. the names an administrator defined with RouteTable=NAME:NUMBER in networkd.conf;
 *  3. safe_atou32() on the whole value, refusing 0 with -ERANGE.
 *
 * THE LENIENCY. Step 2 depends on networkd.conf, which the plugin cannot see, so any value that could be such a
 * name is accepted. config_parse_route_table_names() decides what a name may be
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L496-L616):
 *  - each word comes from extract_first_word(…, NULL, 0) (#L526). With flags 0 a backslash takes the next
 *    character literally: https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L142-L144
 *    sets the flag and https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L105-L106 copies
 *    the character. So `my\ table:100` defines the name `my table`, and a name may contain whitespace;
 *  - the name is the part before the first `:` (#L537-L544), so it never contains `:`;
 *  - it must not be empty (#L546-L550) or consist only of the digits 0-9 (#L551-L555);
 *  - it must not be a predefined name (#L556-L561), but those are accepted by step 1 anyway.
 * So every non-empty value without a `:` that is not all digits is a possible name. That includes spellings that
 * look numeric but are not all digits, such as `0x0`, `-0`, `+5` and `0b 1`, and capitalised forms such as `Main`.
 * Without a matching networkd.conf entry systemd rejects most of these (sd-check, which loads no networkd.conf,
 * reports `Table=0x0`, `Table=-0` and `Table=mytable`). Flagging "a name that is not defined anywhere we can see"
 * would be stricter than the parser and belongs in a separate, weaker inspection.
 *
 * What is flagged, as systemd does whatever networkd.conf says: a value containing `:`, and an all-digit value that
 * safe_atou32 refuses (`0`, `00`, `08`, `4294967296`, `040000000000`). All of these were confirmed with sd-check on
 * [Route] Table=, [RoutingPolicyRule] Table=, [DHCPv4] RouteTable= and [VRF] Table=.
 *
 * Settings that check something else first must add that branch themselves. The WireGuard keys, for example, take
 * an empty value or a false boolean to mean "off" before they look up a table (netdev/wireguard.c).
 */

/** The predefined table names (route_table_table), case-sensitive. */
val ROUTE_TABLE_PREDEFINED_NAME = LiteralChoiceTerminal("default", "main", "local")

/** A table number: safe_atou32() on the whole value, with 0 refused by manager_get_route_table_from_string(). */
val ROUTE_TABLE_NUMBER = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = emptySet(),
  minInclusive = 1L,
  maxInclusive = 4_294_967_295L,
  acceptsWhitespace = true,
  acceptsMinus = true,
)

/**
 * A word that networkd.conf's RouteTable= could have defined as a table name. It is non-empty, has no `:`, and is
 * not made up only of digits (see the file comment for the C rules). The shape is exact, so an all-digit value is
 * left to [ROUTE_TABLE_NUMBER] and its errors are located the way safe_atou32 would locate them.
 */
val ROUTE_TABLE_USER_DEFINED_NAME = RegexTerminal("""(?![0-9]+(?::|\z))[^:]+""", """(?![0-9]+(?::|\z))[^:]+""")

/**
 * A complete route-table value, as manager_get_route_table_from_string() reads it. This includes the end of input,
 * so use it as the whole grammar of the value or as one alternative of it. Each branch carries its own [EOF], so
 * the classic first-match engine can still fall back from the number branch to the name branch on `1abc`.
 */
val ROUTE_TABLE_FROM_STRING = AlternativeCombinator(
  SequenceCombinator(ROUTE_TABLE_PREDEFINED_NAME, EOF()),
  SequenceCombinator(ROUTE_TABLE_NUMBER, EOF()),
  SequenceCombinator(ROUTE_TABLE_USER_DEFINED_NAME, EOF()),
)
