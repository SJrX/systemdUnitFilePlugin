package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionTest : AbstractUnitFileTest() {
  fun testAllValidBooleanValuesDoNotThrowError() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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

  fun testAllValidTristateValuesDoNotThrowError() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SetLoginEnvironment=1
           SetLoginEnvironment=yes
           SetLoginEnvironment=y
           SetLoginEnvironment=true
           SetLoginEnvironment=t
           SetLoginEnvironment=on
           SetLoginEnvironment=0
           SetLoginEnvironment=no
           SetLoginEnvironment=n
           SetLoginEnvironment=false
           SetLoginEnvironment=f
           SetLoginEnvironment=off
           SetLoginEnvironment=
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testIllegalTristateValueTriggersInspection() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SetLoginEnvironment=Most Def
           
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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



  fun testValidModeStringOptionsDoNotTriggerInspection() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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


  fun testInvalidNameInAfterTriggersWarning() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
