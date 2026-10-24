package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseLongOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import org.junit.Test

class ConfigParseLongOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // safe_atoli: signed long, base 0 plus the 0b/0o prefixes mangle_base adds. strtoll starts afresh
    // after the prefix, so a sign or whitespace may follow it (the last five confirmed accepted one per
    // file with sd-check at 0f0682b773).
    // language="unit file (systemd)"
    val file = """
      [Socket]
      MessageQueueMaxMessages=10
      MessageQueueMaxMessages=0
      MessageQueueMaxMessages=-5
      MessageQueueMaxMessages=+5
      MessageQueueMaxMessages=0x10
      MessageQueueMaxMessages=-0x10
      MessageQueueMaxMessages=010
      MessageQueueMaxMessages=0b101
      MessageQueueMaxMessages=0o17
      MessageQueueMaxMessages=9223372036854775807
      MessageQueueMaxMessages=-9223372036854775808
      MessageQueueMaxMessages=0b-1
      MessageQueueMaxMessages=0o-7
      MessageQueueMaxMessages=0b -1
      MessageQueueMaxMessages=0b+1
      MessageQueueMaxMessages=0b 1
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testVerticalTabSpellings() {
    // strtoll skips a leading \v (C isspace); sd-check at 0f0682b773 accepts both. The plugin's lexer does
    // not take a raw \v, so this checks the grammar directly on both engines.
    val grammar = ConfigParseLongOptionValue().combinator
    for (engine in listOf(GrammarEngineVerdict::classic, GrammarEngineVerdict::parseEngine)) {
      assertEquals(GrammarEngineVerdict.VALID, engine(grammar, "\u000B5"))
      assertEquals(GrammarEngineVerdict.VALID, engine(grammar, "\u000B-1"))
    }
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      MessageQueueMaxMessages=ten
      MessageQueueMaxMessages=08
      MessageQueueMaxMessages=0x
      MessageQueueMaxMessages=9223372036854775808
      MessageQueueMaxMessages=-0b1
      MessageQueueMaxMessages=10K
      MessageQueueMaxMessages=1.5
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
