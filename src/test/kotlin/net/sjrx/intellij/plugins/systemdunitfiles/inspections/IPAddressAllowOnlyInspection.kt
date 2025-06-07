package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class IPAddressAllowOnlyInspectionTest: AbstractUnitFileTest() {

  fun testServiceThrowsWarningWhenIPAddressAllowIsUsedByNoIPAddressDeny() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=test
      
      [Service]
      IPAddressAllow=192.168.0.0/24

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(IPAddressAllowOnlyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testServiceThrowsNoWarningWhenIPAddressAllowIsUsedByIPAddressDeny() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=test
      
      [Service]
      IPAddressAllow=192.168.0.0/24
      IPAddressDeny=any

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(IPAddressAllowOnlyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
