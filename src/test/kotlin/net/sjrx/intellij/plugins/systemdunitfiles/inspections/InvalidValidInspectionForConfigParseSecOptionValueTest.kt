package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValidInspectionForConfigParseSecOptionValueTest : AbstractUnitFileTest() {

  fun testWeakWarningWhenStringSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           StartLimitIntervalSec=abc
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Could not parse leading number", info!!.description)
    TestCase.assertEquals("abc", info.text)
  }

  fun testWeakWarningWhenNegativeSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           StartLimitIntervalSec=-5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Negatives are not allowed", info!!.description)
    TestCase.assertEquals("-5", info.text)
  }

  fun testWeakWarningWhenNegativeSpecifiedInMultipleTerms() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           StartLimitIntervalSec=1 day -5 secs
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Negatives are not allowed", info!!.description)
    TestCase.assertEquals("1 day -5 secs", info.text)
  }

  fun testNoWarningsWithVariousFormats() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           StartLimitIntervalSec=1 day
           
           [Service]
           RestartSec=542
           WatchdogSec=1 years
           RuntimeMaxSec=1 us 1 y 1d 1 w 1hr 1m 1 ms 
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }



}
