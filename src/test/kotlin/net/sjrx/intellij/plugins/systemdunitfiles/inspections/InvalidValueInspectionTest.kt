package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionTest : AbstractUnitFileTest() {
  fun testAllValidBooleanValuesDoNotThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           TTYReset=1
           TTYReset=yes
           TTYReset=y
           TTYReset=true
           TTYReset=t
           TTYReset=on
           TTYReset=0
           TTYReset=no
           TTYReset=n
           TTYReset=false
           TTYReset=f
           TTYReset=off
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalBooleanValueTriggersInspection() {
    // Fixture Setup
    val file = """
           [Service]
           TTYReset=Most Def
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Most Def", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("must be one of", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("off", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("Most Def", highlightElement!!.text)
  }

  fun testValidDocumentationValuesOnDistinctLinesDoNotThrowException() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           Documentation=https://www.google.com
           Documentation=file:/tmp/test.txt
           Documentation=info:gcc#G++_and_GCC
           Documentation=man:test(7)
           
           """.trimIndent()
    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testValidDocumentationValuesOnSingleLineDoNotThrowException() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com https://www.google.com file:/tmp/test.txt info:gcc#G++_and_GCC man:test(7)
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testInvalidDocumentationValuesOnDistinctLinesTriggersInspection() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           Documentation=https://www.google.com
           Documentation=file:/tmp/test.txt
           Documentation=ftp://www.google.com/rules.txt
           Documentation=info:gcc#G++_and_GCC
           Documentation=man:test(7)
           
           """.trimIndent()
    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("ftp://www.google.com/rules.txt", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("a space separated list", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("ftp://www.google.com/rules.txt", highlightElement!!.text)
  }

  fun testInvalidDocumentationValuesOnSingleLineTriggersInspection() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com https://www.google.com file:/tmp/test.txt ftp://www.google.com/rules.txt info:gcc#G++_and_GCC man:test(7)
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("ftp://www.google.com/rules.txt", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("a space separated list", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    AbstractUnitFileTest.Companion.assertStringContains("ftp://www.google.com/rules.txt", highlightElement!!.text)
  }

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
    AbstractUnitFileTest.Companion.assertStringContains("sigkill", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("match one of", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("control-group", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("process", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("mixed", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("none", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("sigkill", highlightElement!!.text)
  }

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
    AbstractUnitFileTest.Companion.assertStringContains("yes", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("match one of", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("no", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on-success", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on-failure", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on-abnormal", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on-watchdog", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("on-abort", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("always", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("yes", highlightElement!!.text)
  }

  fun testValidModeStringOptionsDoNotTriggerInspection() {
    // Fixture Setup
    val file = """
           [Service]
           UMask=0000
           UMask=0111
           UMask=111
           UMask=0777
           UMask=4777
           UMask=2777
           UMask=666
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalModeStringOptionsTriggersInspection() {
    // Fixture Setup
    val file = """
           [Service]
           UMask=rwxrwxrwx
           UMask=u=rwx,g=rwx,o=rwx
           UMask=0A548
           UMask=0897
           UMask=71777
           UMask=71
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(6, highlights)
    for (info in highlights) {
      AbstractUnitFileTest.Companion.assertStringContains("Value is expected to be a 3 or 4 digit octal number not:", info!!.description)
      TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
      val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
      TestCase.assertNotNull(highlightElement)
      AbstractUnitFileTest.Companion.assertStringContains(highlightElement!!.text.trim { it <= ' ' }, info.description)
    }
  }

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
    AbstractUnitFileTest.Companion.assertStringContains("remote", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("match one of", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("simple", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("forking", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("oneshot", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("dbus", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("notify", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("idle", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("exec", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("remote", highlightElement!!.text)
  }

  fun testIllegalServiceTypeValueTriggersInspectionWithoutNewLine() {
    // Fixture Setup
    val file = """
           [Service]
           Type=remote
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("remote", info!!.description)
    AbstractUnitFileTest.Companion.assertStringContains("match one of", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("simple", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("forking", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("oneshot", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("dbus", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("notify", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("idle", info.description)
    AbstractUnitFileTest.Companion.assertStringContains("exec", info.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("remote", highlightElement!!.text)
  }

  fun testInvalidNameInAfterTriggersWarning() {
    // Fixture Setup
    val file = """
           [Unit]
           After=mysql
           
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid unit name detected,", info!!.description)
  }

  fun testInvalidUnitTypeInWantsTriggersWarning() {
    // Fixture Setup
    val file = """
           [Unit]
           Wants=mysql.foo
           
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Unit type foo", info!!.description)
    TestCase.assertEquals("foo", info.text)
  }

  fun testMultipleWarningsAreGeneratedFromInvalidTypesAndNamesWarning() {
    // Fixture Setup
    val file = ("[Unit]\n"
      + "Wants=mysql postgres.service mongo.db  neo4j.graph  cassandra    \n"
      + "Wants=\n"
      + "Wants=    redis.cache  networking.target \n")


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(5, highlights)
    val mysqlErrorMissingType = highlights[0]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid unit name detected", mysqlErrorMissingType!!.description)
    TestCase.assertEquals("mysql", mysqlErrorMissingType.text)
    val mongoInvalidType = highlights[1]
    AbstractUnitFileTest.Companion.assertStringContains("Unit type db", mongoInvalidType!!.description)
    TestCase.assertEquals("db", mongoInvalidType.text)
    val neo4jInvalidType = highlights[2]
    AbstractUnitFileTest.Companion.assertStringContains("Unit type graph", neo4jInvalidType!!.description)
    TestCase.assertEquals("graph", neo4jInvalidType.text)
    val cassandraMissingType = highlights[3]
    AbstractUnitFileTest.Companion.assertStringContains("Invalid unit name detected", cassandraMissingType!!.description)
    TestCase.assertEquals("cassandra", cassandraMissingType.text)
    val redisInvalidType = highlights[4]
    AbstractUnitFileTest.Companion.assertStringContains("Unit type cache", redisInvalidType!!.description)
    TestCase.assertEquals("cache", redisInvalidType.text)
  }

}
