package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseImportCredentialOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      ImportCredential=fsck.*
      ImportCredential=test.creds.*
      ImportCredential=test.creds.*:renamed.creds.
      ImportCredential=test.creds.first:renamed.creds.first
      ImportCredential=brummbaer
      ImportCredential=*
      ImportCredential=x*
      ImportCredential=my cred
      ImportCredential=foo:
      ImportCredential=%N.*
      ImportCredential=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      ImportCredential=.
      ImportCredential=..
      ImportCredential=foo/bar
      ImportCredential=?
      ImportCredential=*a
      ImportCredential=a?
      ImportCredential=a[abc]
      ImportCredential=foo:bar:baz
      ImportCredential=foo:..
      ImportCredential=foo:a/b
      ImportCredential=:foo
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(11, highlights)
  }
}
