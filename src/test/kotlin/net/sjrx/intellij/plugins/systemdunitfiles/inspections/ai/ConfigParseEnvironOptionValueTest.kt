package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseEnvironOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // From systemd's test/test-execute/exec-environment*.service, exec-unsetenvironment.service and the corpus.
    // language="unit file (systemd)"
    val file = """
      [Service]
      Environment="VAR1=word1 word2" VAR2=word3 "VAR3=${'$'}word 5 6"
      Environment="VAR3=foobar"
      Environment=FOO=bar QUUX=waldo VAR3=value3 VAR4=value4 VAR5=value5 X%b=%U VAR6=value6
      Environment=TOP_SECRET=%d/very_top_secret
      Environment='SHM_MEMORY=64'
      Environment="TZ=:/etc/localtime"
      Environment=SHELL_PROMPT_PREFIX="pre-udev "
      Environment=PYTHONPATH='/path/to/mysite'
      Environment=USER= CONFIG= HOST= PORT= EXTRAOPTS=
      Environment=JAVA_OPTS=-Djava.awt.headless=true
      Environment=USER=${'$'}{SERVICE_USER} HOME=${'$'}{SERVICE_HOME}
      Environment="OPTIONS="
      Environment=_UNDERSCORE=1 lower_case=2 A1=3
      Environment=TAB=a\tb NL=x\ny HEX=\x41 OCT=\101 UNI=é SP=a\sb
      Environment="QUOTED=say \"hi\""
      Environment="FOO"=bar
      Environment=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidInOtherUnitTypes() {
    // language="unit file (systemd)"
    val socket = """
      [Socket]
      Environment=FOO=bar
    """.trimIndent()
    setupFileInEditor("file.socket", socket)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())

    // language="unit file (systemd)"
    val mount = """
      [Mount]
      Environment="A=b c"
    """.trimIndent()
    setupFileInEditor("file.mount", mount)
    assertSize(0, myFixture.doHighlighting())

    // language="unit file (systemd)"
    val swap = """
      [Swap]
      Environment=A=1 B=2
    """.trimIndent()
    setupFileInEditor("file.swap", swap)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      Environment=UNICORN_OPTS=-E production
      Environment=FOO
      Environment=1FOO=bar
      Environment==bar
      Environment=FOO-BAR=1
      Environment="FOO=unbalanced
      Environment=FOO=a\qb
      Environment="=bar"
      Environment="NOEQUALS"
      Environment=FOO.BAR=1
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(10, highlights)
  }
}
