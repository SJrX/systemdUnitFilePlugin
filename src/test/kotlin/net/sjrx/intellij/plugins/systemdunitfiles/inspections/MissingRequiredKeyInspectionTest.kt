package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class MissingRequiredKeyInspectionTest : AbstractUnitFileTest() {

  fun testServiceThrowsWarningWhenNoneOfRequiredOptionsArePresent() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Daughter Service

      [Service]
      Type=oneshot
      CPUAccounting=true

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(2, highlights)
  }

  fun testServiceHasNoWarningsARequiredOptionInUnitSectionPresent() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Daughter Service
      SuccessAction=foo

      [Service]
      Type=oneshot
      CPUAccounting=true

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testServiceHasNoWarningsARequiredOptionInServiceSectionPresent() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Daughter Service
     

      [Service]
      Type=oneshot
      ExecStart=foo
      CPUAccounting=true

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testServiceHasNoWarningsAllRequiredOptionsArePresent() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Daughter Service
      SuccessAction=foo
     

      [Service]
      Type=oneshot
      ExecStart=foo
      ExecStop=bar
      CPUAccounting=true

    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testPathHasWarningIfNoPathOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Path]
      MakeDirectory=true
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.path", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }
}
