package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ThrowableRunnable
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.TimeHelper.USEC_PER_MSEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.TimeHelper.USEC_PER_SEC
import org.junit.Test


class TimeHelperTest : AbstractUnitFileTest() {

  @Test
  fun testThatInfinityParses() {
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs("infinity"))
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs(" infinity"))
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs("infinity "))
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs("infinity "))
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs("infinity "))
    assertEquals(ULong.MAX_VALUE, TimeHelper.parseSecs("infinity "))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("Infinity"))
  }

  fun testReferenceValuesFromCCode() {
    assertEquals(1 * USEC_PER_SEC, TimeHelper.parseSecs("1").toLong())
    assertEquals(1 * USEC_PER_SEC, TimeHelper.parseSecs("1s").toLong())
    assertEquals(100 * USEC_PER_MSEC, TimeHelper.parseSecs("100ms").toLong())
    assertEquals(5 * 60 * USEC_PER_SEC + 20 * USEC_PER_SEC, TimeHelper.parseSecs("5min 20s").toLong())

    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("-1"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("10foo"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("garbage"))
  }

  fun testOtherIllegalValues() {
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("-0"))

    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("-12.34.56"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("0.-0"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.+1"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.sec"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.hoge"))
  }

  fun testOtherValues() {
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("-0"))

    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("-12.34.56"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("0.-0"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.+1"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.sec"))
    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("3.hoge"))
  }

//  fun testOverflows() {
//
////    assertEquals(584541UL * USEC_PER_YEAR.toULong(), TimeHelper.parseSecs("584541 years").toULong())
////    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("584542 years"))
//
//
//    UsefulTestCase.assertThrows(IllegalArgumentException::class.java, GetRunnable("584541 years 366 days"))
//
//
//  }


}

fun GetRunnable(input: String): ThrowableRunnable<IllegalArgumentException> {

  return ThrowableRunnable {
    TimeHelper.parseSecs(input)
  }
}
