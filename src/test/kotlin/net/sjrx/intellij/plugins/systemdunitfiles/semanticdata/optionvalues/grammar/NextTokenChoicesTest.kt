package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseAddressFamiliesOptionValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the grammar-completion FIRST-set computation (no IntelliJ fixture needed). */
class NextTokenChoicesTest {

  private val addressFamilies = ConfigParseAddressFamiliesOptionValue().combinator

  @Test
  fun testEmptyValueOffersStartTokens() {
    val choices = addressFamilies.nextTokenChoices("")
    assertTrue(choices.contains("none"))
    assertTrue(choices.contains("~"))
    assertTrue(choices.contains("AF_INET"))
  }

  @Test
  fun testAfterInversionOffersFamiliesNotNone() {
    // After "~" the grammar expects an address family, not "none" or another "~".
    val choices = addressFamilies.nextTokenChoices("~")
    assertTrue(choices.contains("AF_INET"))
    assertFalse(choices.contains("none"))
    assertFalse(choices.contains("~"))
  }

  @Test
  fun testAfterFamilyAndSpaceOffersAnotherFamily() {
    // Context awareness: after a family and a separator, another family is expected.
    assertTrue(addressFamilies.nextTokenChoices("AF_INET ").contains("AF_INET6"))
  }
}
