package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDomainsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      Domains=example.com
      Domains=hogehoge ~foofoo
      Domains=one two three four five six seven eight nine ten
      Domains=~.
      Domains=~
      Domains=.
      Domains=*
      Domains=~*
      Domains=example.com.
      Domains=_srv.example.com
      Domains=foo\.bar
      Domains=foo\\\\bar
      Domains=foo\\.bar
      Domains=foo\\\.bar
      Domains=foo\\065bar
      Domains=localhost.example.com
      Domains=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      Domains=example..com
      Domains=.example.com
      Domains=example.com..
      Domains=ok.example xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.com
      Domains=localhost
      Domains=foo.localhost
      Domains=~localhost
      Domains=Foo.LocalHost.
      Domains=localhost.localdomain
      Domains=x.localhost.localdomain
      Domains=foo\\bar
      Domains=foo\\256bar
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
