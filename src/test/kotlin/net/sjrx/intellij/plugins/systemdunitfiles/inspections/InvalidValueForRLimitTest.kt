package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueForRLimitTest : AbstractUnitFileTest() {

  fun testNoWarningWhenCPULimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCPU=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenCPULimitIsInfinityIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCPU=infinity
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }



  fun testWarningWhenInvalidCPULimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCPU=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitCPU\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenCPULimitSpecifiedInNSpawnFile() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Exec]
            LimitCPU=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }


  fun testWarningWhenInvalidCPULimitSpecifiedInNSpawnFile() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Exec]
            LimitCPU=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitCPU\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenCPULimitSpecifiedInComplexFormat() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCPU=6m
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenCPULimitSpecifiedInComplexFormatAndAdditionalWhitespace() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Exec]
            LimitCPU= 1 day 6 m 10 s
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenCPUSoftAndHardLimitSpecifiedInComplexFormatAndAdditionalWhitespace() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Exec]
            LimitCPU= 1 day 6 m 10 s: 2 years
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenCPULimitSpecifiedInComplexFormatAndMiscGarbageIsPresent() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Exec]
            LimitCPU= 1 day 12 ns 2 years
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitCPU\'s value does not match the expected format", info!!.description)
  }


  fun testWarningWhenInvalidCPULimitSpecifiedInComplexFormat() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCPU=-11m
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitCPU\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenFSIZELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitFSIZE=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenFSIZELimitIsInfinityIsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitFSIZE=infinity
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenFSIZELimitSpecifiedWithWhiteSpaceUnitsAndSoftAndHardLimits() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitFSIZE=1 K: 2 G
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidFSIZELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitFSIZE=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitFSIZE\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenDATALimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitDATA=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidDATALimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitDATA=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitDATA\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenSTACKLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitSTACK=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidSTACKLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitSTACK=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitSTACK\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenCORELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCORE=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidCORELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitCORE=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitCORE\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenRSSLIMITSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRSS=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidRSSLIMITSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRSS=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitRSS\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenNOFILELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNOFILE=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenNOFILELimitWithWhitespaceSoftAndHardAndInfinityIsSpecified() {
    // rlimit_parse_u64() compares the field with "infinity" exactly, and the field after ':' is not trimmed, so
    // " infinity" is refused ("Failed to parse LimitNOFILE=1: infinity"). This test used to assert the opposite (#568).
    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/rlimit-util.c#L79-L102
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNOFILE=1: infinity
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testNoWarningWhenNOFILELimitWithWhitespaceSoftAndHardIsSpecified() {
    // safe_atou64() skips leading whitespace, so " 2" is fine.
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNOFILE=1: 2
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenASLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitAS=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidNOFILELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNOFILE=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitNOFILE's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-11", info.text)
  }

  fun testNoWarningWhenNPROCLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNPROC=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidNPROCLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNPROC=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitNPROC's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-11", info.text)
  }

  fun testNoWarningWhenMEMLOCKLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitMEMLOCK=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidMEMLOCKLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitMEMLOCK=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitMEMLOCK\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenLOCKSLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitLOCKS=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidLOCKSLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitLOCKS=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitLOCKS's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-11", info.text)
  }

  fun testNoWarningWhenSIGPENDINGLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitSIGPENDING=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidSIGPENDINGLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitSIGPENDING=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitSIGPENDING's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-11", info.text)
  }


  fun testNoWarningWhenMSGQUEUELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitMSGQUEUE=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidMSGQUEUELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitMSGQUEUE=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitMSGQUEUE\'s value does not match the expected format", info!!.description)
  }

  fun testNoWarningWhenNICELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNICE=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidNICELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitNICE=-21
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitNICE's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("21", info.text)
  }

  fun testNoWarningWhenRTPRIOLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRTPRIO=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidRTPRIOLimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRTPRIO=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("LimitRTPRIO's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-11", info.text)
  }

  fun testNoWarningWhenRTTIMELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRTTIME=1
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidRTTIMELimitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
            [Service]
            LimitRTTIME=-11
            """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    // The time and size fields no longer read a leading \'-\' as a number, so this is now a format error (#568).
    assertStringContains("LimitRTTIME\'s value does not match the expected format", info!!.description)
  }
}
