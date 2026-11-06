package net.sjrx.intellij.plugins.systemdunitfiles.settings

/**
 * Which spelling the user prefers for 6-byte MAC addresses. Drives
 * [net.sjrx.intellij.plugins.systemdunitfiles.inspections.MacAddressFormatInspection], which suggests
 * rewriting any MAC not already in this form.
 *
 * systemd's `parse_ether_addr()` accepts colon, hyphen and dot (Cisco) separators in any case, with
 * leading zeros optional per field. The default is [COLON_LOWER], the form systemd itself prints
 * (`ether_addr_to_string()`). [LEAVE_ALONE] disables the suggestion entirely.
 */
enum class PreferredMacFormat(val displayName: String) {
  LEAVE_ALONE("Leave as written (no suggestions)"),
  COLON_LOWER("00:1a:2b:3c:4d:5e (systemd default)"),
  COLON_UPPER("00:1A:2B:3C:4D:5E"),
  HYPHEN_UPPER("00-1A-2B-3C-4D-5E (Windows style)"),
  DOT_LOWER("001a.2b3c.4d5e (Cisco style)"),
  ;

  // Shown as-is in the settings combo box.
  override fun toString(): String = displayName
}
