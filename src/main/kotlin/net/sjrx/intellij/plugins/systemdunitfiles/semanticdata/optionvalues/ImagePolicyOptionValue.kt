package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * RootImagePolicy=, MountImagePolicy=, ExtensionImagePolicy=: config_parse_image_policy() →
 * image_policy_from_string(…, graceful = true).
 *
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L776-L810
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L341-L465
 *
 * The value is "-", "*", "~", or ':'-separated `designator=flags` rules (an empty designator sets the default
 * flags), with the flags '+'-separated. In graceful mode an unknown partition designator or policy flag is only
 * logged at debug level and skipped, so systemd's parser accepts any word in either place, including an empty flag
 * after a trailing '+' (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L237-L269).
 * The known names are kept in the grammar for completion. What systemd does refuse is a rule without '=' and a
 * designator given twice (architecture-specific designator aliases are not resolved for that check).
 */
class ImagePolicyOptionValue : GrammarOptionValue(validatorName, IMAGE_POLICY_COMBINATOR) {

  companion object {
    val validatorName = "config_parse_image_policy"

    // Image Polcies
    // https://www.freedesktop.org/software/systemd/man/latest/systemd.image-policy.html
    val IMAGE_POLICY_SEPARATOR = LiteralChoiceTerminal(":")
    // partition_designator_table, https://github.com/systemd/systemd/blob/0f0682b773/src/shared/gpt.c#L156-L170
    val PARTITION_IDENTIFIER = FlexibleLiteralChoiceTerminal( "root", "usr", "home", "srv", "esp", "xbootldr", "swap", "root-verity", "root-verity-sig", "usr-verity", "usr-verity-sig", "tmp", "var")
    // policy_flag_from_string_one() and policy_flag_from_fstype(),
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L180-L235
    val PARTITION_POLICY_FLAG = FlexibleLiteralChoiceTerminal(
      "unprotected", "verity", "signed", "encrypted", "encryptedwithintegrity", "unused", "absent", "open", "ignore",
      "read-only-off", "read-only-on", "growfs-off", "growfs-on",
      "btrfs", "erofs", "ext4", "f2fs", "squashfs", "vfat", "xfs",
    )
    val PARTITION_POLICY_FLAG_SEPARATOR = LiteralChoiceTerminal("+")
    val PARTITION_IDENTIFIER_FLAG_SEPARATOR = LiteralChoiceTerminal("=")

    // Any word is accepted (graceful mode); the regex comes first so that the legacy engine, which takes the first
    // alternative that matches, never strands part of an unknown word.
    val PARTITION_POLICY_FLAGS = SequenceCombinator(
      AlternativeCombinator(RegexTerminal("[^+:]*", "[^+:]*"), PARTITION_POLICY_FLAG),
      ZeroOrMore(
        SequenceCombinator(
          PARTITION_POLICY_FLAG_SEPARATOR,
          AlternativeCombinator(RegexTerminal("[^+:]*", "[^+:]*"), PARTITION_POLICY_FLAG),
        )
      ),
    )

    val SINGLE_EXPLICIT_IMAGE_POLICY = SequenceCombinator(
      AlternativeCombinator(RegexTerminal("[^=:]*", "[^=:]*"), PARTITION_IDENTIFIER),
      PARTITION_IDENTIFIER_FLAG_SEPARATOR,
      PARTITION_POLICY_FLAGS,
    )

    val IMAGE_POLICY_COMBINATOR = SequenceCombinator(
      AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("-", "*", "~"), EOF()),
        // A designator given twice, or two default rules (empty designator), is -ENOTUNIQ.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L413-L432
        ValueChecked(
          SequenceCombinator(SINGLE_EXPLICIT_IMAGE_POLICY, ZeroOrMore(SequenceCombinator(IMAGE_POLICY_SEPARATOR, SINGLE_EXPLICIT_IMAGE_POLICY)))
        ) { text -> noDuplicateDesignators(text) },
      ),
      EOF(),
    )

    // partition_designator_table, https://github.com/systemd/systemd/blob/0f0682b773/src/shared/gpt.c#L156-L170
    private val KNOWN_DESIGNATORS = setOf(
      "root", "usr", "home", "srv", "esp", "xbootldr", "swap", "root-verity", "root-verity-sig", "usr-verity", "usr-verity-sig", "tmp", "var",
    )

    /**
     * False if a rule repeats a designator from partition_designator_table, or the default rule (empty designator),
     * as image_policy_from_string() checks it after strstrip(). Unknown designators are skipped by graceful parsing,
     * and architecture-specific aliases (root-x86-64, ...) depend on the build, so neither is counted.
     */
    private fun noDuplicateDesignators(text: String): Boolean {
      val seen = mutableSetOf<String>()
      for (rule in text.split(':')) {
        val designator = rule.substringBefore('=').trim(' ', '\t', '\n', '\r')
        if (designator.isNotEmpty() && designator !in KNOWN_DESIGNATORS) {
          continue
        }
        if (!seen.add(designator)) {
          return false
        }
      }
      return true
    }

    val validators = mapOf(
      Validator(validatorName, "0") to ImagePolicyOptionValue()
    )
  }

}
