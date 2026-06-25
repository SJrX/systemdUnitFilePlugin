package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings
import org.junit.Test

/**
 * End-to-end check that, with the experimental flag on, the InvalidValue inspection is driven by the
 * new list-of-successes engine (GrammarOptionValue.validate) and reproduces the expected behaviour.
 *
 * We only spot-check here rather than re-running the whole inspection suite under both engines — the
 * engine itself is covered by ParseTest; this just proves the wiring and the ParseOutcome -> problem
 * descriptor mapping.
 */
class GrammarParseEngineInspectionTest : AbstractUnitFileTest() {

  private fun enableNewEngine() {
    ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = true
  }

  // The light-test project is shared across test classes, so the opt-in flag must not leak into
  // other tests (which assume the original engine).
  override fun tearDown() {
    try {
      ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = false
    } finally {
      super.tearDown()
    }
  }

  @Test
  fun testValidAddressFamiliesUnderNewEngine() {
    enableNewEngine()
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictAddressFamilies=none
            RestrictAddressFamilies=AF_INET AF_INET6
            RestrictAddressFamilies=~AF_UNIX AF_NETLINK
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidAddressFamiliesUnderNewEngine() {
    enableNewEngine()
    // Three malformed lists, each ill-formed against the grammar's shape -> one highlight each:
    // a stray comma, a name without the AF_ prefix, and a lowercase tail the AF_* regex rejects.
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictAddressFamilies=AF_INET, AF_INET6
            RestrictAddressFamilies=inet
            RestrictAddressFamilies=AF_inet
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(3, myFixture.doHighlighting())
  }

  @Test
  fun testFlagOffStillUsesOriginalEngine() {
    // Sanity: with the flag left off, the same valid input is accepted (the default path runs).
    // language="unit file (systemd)"
    val file = """
            [Service]
            RestrictAddressFamilies=AF_INET AF_INET6
        """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(0, myFixture.doHighlighting())
  }
}
