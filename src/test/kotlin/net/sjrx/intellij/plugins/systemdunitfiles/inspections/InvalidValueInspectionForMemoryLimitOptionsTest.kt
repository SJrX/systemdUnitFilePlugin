package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForMemoryLimitOptionsTest : AbstractUnitFileTest() {

  fun testNoWarningWhenSpecify1GigabyteLiteral() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=1073741824
           MemoryLow=10248576K
           MemoryMin=1024 M
           MemoryMax=1 G
           MemorySwapMax=0.001 T
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithVariousPercentageValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=0%
           MemoryLow=1%
           MemoryMin=50   %  
           MemoryMax=50.0%
           MemorySwapMax=100%
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWithVariousInfinityValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=infinity
           MemoryLow=   infinity      
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWithInfinityInCaps() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=INFINITY      
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testWarningWithMoreThanTwoPercentagePointsInCaps() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=40.054%      
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testWarningWithMoreThanTwoPercentageDigits() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=40.054%      
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testWarningWithZeroPercentagePointsButDecimal() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           MemoryHigh=40.%      
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testWarningWithLowercaseUnits() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
            MemoryLow=10248576k
           MemoryMin=1024 m
           MemoryMax=1 g
           MemorySwapMax=0.001 t
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(4, highlights)
  }

  fun testWarningWithBytesSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
            MemoryLow=10248576KB
           MemoryMin=1024 MB
           MemoryMax=1 GB
           MemorySwapMax=0.001 TiB
           
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(4, highlights)
  }
}
