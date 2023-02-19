package net.sjrx.intellij.plugins.systemdunitfiles.inspections



import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForEmergencyActionOptionValueTest : AbstractUnitFileTest() {

  fun testNoWarningWhenNoneSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=none
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootImmediateSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot-immediate
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffImmediateSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff-immediate
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenExitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=exit
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenExitForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=exit-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

}


class InvalidValueInspectionForManagedOOMModeOptionsTest : AbstractUnitFileTest() {

  fun testNoWarningWhenAutoSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=auto
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenKillSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=kill
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForKillModeOptionValue : AbstractUnitFileTest() {

  fun testAllKillModeValuesDoNotThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           KillMode=control-group
           KillMode=process
           KillMode=mixed
           KillMode=none
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalKillModeValueTriggersInspection() {
    // Fixture Setup
    val file = """
           [Service]
           KillMode=sigkill
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("sigkill", info!!.description)
    assertStringContains("match one of", info.description)
    assertStringContains("control-group", info.description)
    assertStringContains("process", info.description)
    assertStringContains("mixed", info.description)
    assertStringContains("none", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("sigkill", highlightElement!!.text)
  }
}

class InvalidValueInspectionForRestartOptionValueTest : AbstractUnitFileTest() {

  fun testAllRestartOptionValuesDoNotThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           Restart=no
           Restart=always
           Restart=on-success
           Restart=on-failure
           Restart=on-abnormal
           Restart=on-abort
           Restart=on-watchdog
           Restart=on-watchdog
           """.trimIndent()


    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalRestartOptionValueTriggersInspection() {
    // Fixture Setup
    val file = """
           [Service]
           Restart=yes
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("yes", info!!.description)
    assertStringContains("match one of", info.description)
    assertStringContains("no", info.description)
    assertStringContains("on-success", info.description)
    assertStringContains("on-failure", info.description)
    assertStringContains("on-abnormal", info.description)
    assertStringContains("on-watchdog", info.description)
    assertStringContains("on-abort", info.description)
    assertStringContains("always", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("yes", highlightElement!!.text)
  }
}

class InvalidValueInspectionForServiceTypeOptionValues : AbstractUnitFileTest() {

  fun testAllServiceTypesValuesDoNotThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           Type=simple
           Type=forking
           Type=oneshot
           Type=dbus
           Type=notify
           Type=idle
           Type=exec
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalServiceTypeValueTriggersInspectionWithNewLine() {
    // Fixture Setup
    val file = """
           [Service]
           Type=remote
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("remote", info!!.description)
    assertStringContains("match one of", info.description)
    assertStringContains("simple", info.description)
    assertStringContains("forking", info.description)
    assertStringContains("oneshot", info.description)
    assertStringContains("dbus", info.description)
    assertStringContains("notify", info.description)
    assertStringContains("idle", info.description)
    assertStringContains("exec", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("remote", highlightElement!!.text)
  }

  fun testIllegalServiceTypeValueTriggersInspectionWithoutNewLine() {
    // Fixture Setup
    val file = """
           [Service]
           Type=remote""".trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("remote", info!!.description)
    assertStringContains("match one of", info.description)
    assertStringContains("simple", info.description)
    assertStringContains("forking", info.description)
    assertStringContains("oneshot", info.description)
    assertStringContains("dbus", info.description)
    assertStringContains("notify", info.description)
    assertStringContains("idle", info.description)
    assertStringContains("exec", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("remote", highlightElement!!.text)
  }
}

class InvalidValueInspectionForProtectProcOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ProtectProc=invisible
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ProtectProc=never!
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForProcSubsetOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ProcSubset=all
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ProcSubset=the null set
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForKeyRingModeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           KeyringMode=inherit
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           KeyringMode=public
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForPersonalityOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           Personality=arm64
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           Personality=amd64
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForCpuSchedulingModeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUSchedulingPolicy=other
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUSchedulingPolicy=Most Important First
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForNumaPolicyModeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           NUMAPolicy=default
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           NUMAPolicy=furthest-core
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForIOSchedulingClassOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOSchedulingClass=realtime
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           IOSchedulingClass=worst-case-scheduler
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForLogLevelOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SyslogLevel=emerg
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SyslogLevel=unimportant
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionFoSyslogFacilityOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SyslogFacility=kern
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SyslogFacility=/dev/null
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForUtmpModeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           UtmpMode=init
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           UtmpMode=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}


class InvalidValueInspectionForDevicePolicyOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           DevicePolicy=strict
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           DevicePolicy=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}


class InvalidValueInspectionForManagedOOMPreferenceOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMPreference=omit
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMPreference=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}


class InvalidValueInspectionForServiceExitTypeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ExitType=cgroup
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           ExitType=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForServiceTimeoutFailureModeOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           TimeoutStartFailureMode=kill
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           TimeoutStartFailureMode=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForNotifyAccessOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           NotifyAccess=none
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           NotifyAccess=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForOOMPolicyOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           OOMPolicy=kill
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           OOMPolicy=unknown
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForSocketTimestampingOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           Timestamping=µs
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           Timestamping=ms
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForSocketProtocolOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           SocketProtocol=udplite
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           Timestamping=udplight
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}

class InvalidValueInspectionForSocketBindOptionValue : AbstractUnitFileTest() {
  fun testNoWarningWhenValidValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           BindIPv6Only=ipv6-only
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Socket]
           BindIPv6Only=ipv4-only
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}







