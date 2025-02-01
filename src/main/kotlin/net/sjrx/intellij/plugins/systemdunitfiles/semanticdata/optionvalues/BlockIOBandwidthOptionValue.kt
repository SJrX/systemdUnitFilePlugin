package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class BlockIOBandwidthOptionValue() : GrammarOptionValue("config_parse_blockio_bandwidth", GRAMMAR) {

  companion object {
    val GRAMMAR = SequenceCombinator(OneOrMore(SequenceCombinator(DEVICE, BYTES)), EOF())

    val validators = mapOf(
      Validator("config_parse_blockio_bandwidth", "0") to BlockIOBandwidthOptionValue()
    )
  }
}

