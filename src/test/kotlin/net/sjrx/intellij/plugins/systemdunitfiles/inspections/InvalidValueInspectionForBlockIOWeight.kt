package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForBlockBlockIOWeight : AbstractUnitFileTest() {

  fun testNoWarningWhenTenSpecified() {
      // Fixture Setup
      // language="unit file (systemd)"
      val file = """
           [Service]
           BlockIOWeight=10
           """.trimIndent()


      // Execute SUT
      setupFileInEditor("file.service", file)
      enableInspection(InvalidValueInspection::class.java)
      val highlights = myFixture.doHighlighting()

      // Verification
      assertSize(0, highlights)
  }

  fun testNoWarningWhenOneThousandSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=1000
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWeakWarningWhenNineIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=9
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("BlockIOWeight's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("9", info.text)
  }


  fun testWeakWarningWhenZeroIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=0
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("BlockIOWeight's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("0", info.text)
  }

  fun testWeakWarningWhenNegativeTenIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=-10
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("BlockIOWeight's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-10", info.text)
  }


  fun testWeakWarningWhenNegativeHundredThousandIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=-100000
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("BlockIOWeight's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-100000", info.text)
  }


  fun testWeakWarningWhenOneThousandAndOneIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           BlockIOWeight=1001
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("BlockIOWeight's value is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("1001", info.text)
  }

}
