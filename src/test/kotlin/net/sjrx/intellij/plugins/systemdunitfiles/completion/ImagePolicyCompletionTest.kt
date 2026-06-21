package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/**
 * Grammar completion for the more complex RootImagePolicy= grammar (#467 / #343), behind the flag.
 * Exercises a fresh boundary (empty), completing a partial partition identifier, and the position
 * after "partition=" where policy flags are expected.
 */
class ImagePolicyCompletionTest : AbstractUnitFileTest() {

  override fun tearDown() {
    try {
      ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = false
    } finally {
      super.tearDown()
    }
  }

  private fun enableNewEngine() {
    ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = true
  }

  @Test
  fun testEmptyValueOffersPartitionsAndDefault() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRootImagePolicy=${COMPLETION_POSITION}")
    assertContainsElements(basicCompletionResultStrings, "root", "usr", "swap", "+")
  }

  @Test
  fun testPartialPartitionIdentifierIsCompleted() {
    // Regression for the bug: typing "r" used to offer "=" (the lenient terminal treated "r" as a
    // finished identifier); it should complete the identifier instead.
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRootImagePolicy=r${COMPLETION_POSITION}")
    assertContainsElements(basicCompletionResultStrings, "root", "root-verity", "root-verity-sig")
  }

  @Test
  fun testAfterPartitionEqualsOffersPolicyFlags() {
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRootImagePolicy=root=${COMPLETION_POSITION}")
    assertContainsElements(basicCompletionResultStrings, "verity", "signed", "encrypted")
  }

  @Test
  fun testAcceptingPartitionChainsTheEqualsSeparator() {
    // Accepting a partition designator auto-inserts the forced "=" (then re-opens completion),
    // so the user goes straight to choosing a policy flag.
    enableNewEngine()
    setupFileInEditor("file.service", "[Service]\nRootImagePolicy=hom${COMPLETION_POSITION}")
    myFixture.completeBasic() // single match "home" -> auto-inserted -> handler appends "="
    myFixture.checkResult("[Service]\nRootImagePolicy=home=${COMPLETION_POSITION}")
  }
}
