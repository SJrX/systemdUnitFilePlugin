package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecInputDataOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      StandardInputData=V2XigLJyZSBubyBzdHJhbmdlcnMgdG8gbG92ZQpZb3Uga25vdyB0aGUgcnVsZXMgYW5kIHNvIGRv \
        IEkKQSBmdWxsIGNvbW1pdG1lbnQncyB3aGF0IEnigLJtIHRoaW5raW5nIG9mCllvdSB3b3VsZG4n
      StandardInputData=aGVsbG8=
      StandardInputData=aGVsbA==
      StandardInputData=aGVs
      StandardInputData=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      StandardInputData=hello
      StandardInputData=aGVsbG8
      StandardInputData=a!b=
      StandardInputData====
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }
}
