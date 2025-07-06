import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class OptionValueAnnotationTest : AbstractUnitFileTest() {
  fun testIPAddressIsHighlightedCorrectly() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      IPAddressAllow=127.0.0.1
      Type=oneshot
      ExecStart=/usr/bin/cowsay
    """.trimIndent()


    setupFileInEditor("file.service", file)
    val highlights = myFixture.doHighlighting()

    /*
     * Verification
     */
    assertSize(1, highlights)
  }
}
