package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForUIntOptionValue : AbstractUnitFileTest() {

  fun testWeakWarningWhenNegativeIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [VXLAN]
           # uint32
           VNI=-1
                                 
           [Bridge]
           # uint16
           Priority=-2
           
           [HSR]
           # uint8
           Supervision=-3
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(3, highlights)
    var info = highlights[0]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-1", info.text)

    info = highlights[1]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-2", info.text)

    info = highlights[2]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("-3", info.text)
  }

  fun testWeakWarningWhenTooBigAnIntegerSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [VXLAN]
           # uint32
           VNI=9999999999
                                 
           [Bridge]
           # uint16
           Priority=65536
           
           [HSR]
           # uint8
           Supervision=256
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(3, highlights)
    var info = highlights[0]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("9999999999", info.text)

    info = highlights[1]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("65536", info.text)

    info = highlights[2]
    assertStringContains("is correctly formatted but seems invalid", info!!.description)
    TestCase.assertEquals("256", info.text)
  }

  fun testNoWeakWarningOnBoundaryConditions() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
     [VXLAN]
     # uint32
     VNI=0
     VNI=1
     VNI=4294967295
     VNI=4294967294
     VNI=2294967294
     VNI=2094967294
     VNI=65536
     VNI=65534
                           
     [Bridge]
     # uint16
     Priority=0
     Priority=1
     Priority=65535
     
     [HSR]
     # uint8
     Supervision=0
     Supervision=1
     Supervision=255
     """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
