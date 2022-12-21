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

  fun testTimerHasWarningIfNoRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Timer]
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.timer", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }

  fun testTimerHasNoWarningIfRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Timer]
      OnBootSec=5
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.timer", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testSwapHasWarningIfNoRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Swap]
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.swap", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }

  fun testSwapHasNoWarningIfRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Swap]
      What=/file.swp
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.swap", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testMountHasWarningIfNoRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Mount]
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.mount", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }

  fun testMountHasNoWarningIfRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Mount]
      What=/file.swp
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.mount", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testSocketHasWarningIfNoRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Socket]
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.socket", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }

  fun testSocketHasNoWarningIfRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Socket]
      ListenFIFO=true
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.socket", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  fun testAutomountHasWarningIfNoRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Automount]
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.automount", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)

  }

  fun testAutomountHasNoWarningIfRequiredOptionExists() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
      # SPDX-License-Identifier: LGPL-2.1-or-later
      [Unit]
      Description=Some Path
      
      [Automount]
      Where=/file.swp
    """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.automount", file)
    enableInspection(MissingRequiredKeyInspection::class.java)

    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }
}
