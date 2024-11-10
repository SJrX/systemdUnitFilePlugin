package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

class ImagePolicyOptionValue : GrammarOptionValue(validatorName, IMAGE_POLICY_COMBINATOR) {

  companion object {
    val validatorName = "config_parse_image_policy"

    // Image Polcies
    // https://www.freedesktop.org/software/systemd/man/latest/systemd.image-policy.html
    val IMAGE_POLICY_SEPARATOR = LiteralChoiceTerminal(":")
    val PARTITION_IDENTIFIER = FlexibleLiteralChoiceTerminal( "root", "usr", "home", "srv", "esp", "xbootldr", "swap", "root-verity", "root-verity-sig", "usr-verity", "usr-verity-sig", "tmp", "var")
    val PARTITION_POLICY_FLAG = FlexibleLiteralChoiceTerminal("unprotected", "verity", "signed", "encrypted", "unused", "absent", "read-only-off", "read-only-on", "growfs-off", "growfs-on")
    val PARTITION_POLICY_FLAG_SEPARATOR = LiteralChoiceTerminal("+")
    val PARTITION_IDENTIFIER_FLAG_SEPARATOR = LiteralChoiceTerminal("=")


    val PARTITION_POLICY_FLAGS = SequenceCombinator(PARTITION_POLICY_FLAG, ZeroOrMore(SequenceCombinator(PARTITION_POLICY_FLAG_SEPARATOR, PARTITION_POLICY_FLAG)))


    val SINGLE_EXPLICIT_IMAGE_POLICY = SequenceCombinator(PARTITION_IDENTIFIER, PARTITION_IDENTIFIER_FLAG_SEPARATOR, PARTITION_POLICY_FLAGS)
    val DEFAULT_IMAGE_POLICY = SequenceCombinator(PARTITION_POLICY_FLAG_SEPARATOR, PARTITION_POLICY_FLAGS)
    var IMAGE_POLICY= AlternativeCombinator(SINGLE_EXPLICIT_IMAGE_POLICY, DEFAULT_IMAGE_POLICY)
    val IMAGE_POLICY_COMBINATOR = SequenceCombinator(IMAGE_POLICY, ZeroOrMore(SequenceCombinator(IMAGE_POLICY_SEPARATOR, IMAGE_POLICY)), EOF())

    val validators = mapOf(
      Validator(validatorName, "0") to ImagePolicyOptionValue()
    )
  }

}
