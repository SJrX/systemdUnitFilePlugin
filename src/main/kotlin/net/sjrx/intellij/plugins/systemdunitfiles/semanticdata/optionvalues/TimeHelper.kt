package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

/**
 * This class is adapted from time-util.h/c
 * https://github.com/systemd/systemd/blob/main/src/basic/time-util.h#L46
 * https://github.com/systemd/systemd/blob/main/src/basic/time-util.c#L1131
 */


data class Multiplier(val str : String, val usecs : Long)


object TimeHelper {
  val USEC_PER_SEC: Long = 1000000
  val USEC_PER_MSEC: Long = 1000
  val NSEC_PER_SEC: Long = 1000000000
  val NSEC_PER_MSEC: Long = 1000000
  val NSEC_PER_USEC: Long = 1000

  val USEC_PER_MINUTE: Long = 60 * USEC_PER_SEC
  val NSEC_PER_MINUTE: Long = 60 * NSEC_PER_SEC
  val USEC_PER_HOUR: Long = 60 * USEC_PER_MINUTE
  val NSEC_PER_HOUR: Long = 60 * NSEC_PER_MINUTE
  val USEC_PER_DAY: Long = 24 * USEC_PER_HOUR
  val NSEC_PER_DAY: Long = 24 * NSEC_PER_HOUR
  val USEC_PER_WEEK: Long = 7 * USEC_PER_DAY
  val NSEC_PER_WEEK: Long = 7 * NSEC_PER_DAY
  val USEC_PER_MONTH: Long = 2629800 * USEC_PER_SEC
  val NSEC_PER_MONTH: Long = 2629800 * NSEC_PER_SEC
  val USEC_PER_YEAR: Long = 31557600 * USEC_PER_SEC
  val NSEC_PER_YEAR: Long = 31557600 * NSEC_PER_SEC

  val multiplers = listOf(
    Multiplier( "seconds", USEC_PER_SEC    ),
    Multiplier( "second",  USEC_PER_SEC    ),
    Multiplier( "sec",     USEC_PER_SEC    ),
    Multiplier( "s",       USEC_PER_SEC    ),
    Multiplier( "minutes", USEC_PER_MINUTE ),
    Multiplier( "minute",  USEC_PER_MINUTE ),
    Multiplier( "min",     USEC_PER_MINUTE ),
    Multiplier( "months",  USEC_PER_MONTH  ),
    Multiplier( "month",   USEC_PER_MONTH  ),
    Multiplier( "M",       USEC_PER_MONTH  ),
    Multiplier( "msec",    USEC_PER_MSEC   ),
    Multiplier( "ms",      USEC_PER_MSEC   ),
    Multiplier( "m",       USEC_PER_MINUTE ),
    Multiplier( "hours",   USEC_PER_HOUR   ),
    Multiplier( "hour",    USEC_PER_HOUR   ),
    Multiplier( "hr",      USEC_PER_HOUR   ),
    Multiplier( "h",       USEC_PER_HOUR   ),
    Multiplier( "days",    USEC_PER_DAY    ),
    Multiplier( "day",     USEC_PER_DAY    ),
    Multiplier( "d",       USEC_PER_DAY    ),
    Multiplier( "weeks",   USEC_PER_WEEK   ),
    Multiplier( "week",    USEC_PER_WEEK   ),
    Multiplier( "w",       USEC_PER_WEEK   ),
    Multiplier( "years",   USEC_PER_YEAR   ),
    Multiplier( "year",    USEC_PER_YEAR   ),
    Multiplier( "y",       USEC_PER_YEAR   ),
    Multiplier( "usec",    1         ),
    Multiplier( "us",      1         ),
    Multiplier( "μs",      1         ), /* U+03bc (aka GREEK SMALL LETTER MU) */
    Multiplier( "µs",      1         ),
  )

  val WHITESPACE = """[ \t\n\r]""".toRegex()

  val NUMBER = """^\d*\.?\d+""".toRegex()

  fun parseSecs(time: String): ULong {
    return parseTime(time,  Multiplier( "sec",     USEC_PER_SEC    ));
  }

  fun parseTime(time: String, defaultMultiplier : Multiplier): ULong {

    var p = time.trim()

    var something = false

    var usec : ULong = 0u

    if (p == "infinity") {
      return ULong.MAX_VALUE
    }


    while(true) {
      p = p.trimStart()

      if (p.length == 0) {
        if (!something) {
          throw IllegalArgumentException("Could not parse $p in $time")
        } else {
          break
        }
      }

      if (p.startsWith('-')) {
        throw IllegalArgumentException("Negatives are not allowed in $time")
      }

      val result = NUMBER.find(p)

      val value = result?.groups?.get(0)?.value?.toDouble() ?: throw IllegalArgumentException("Could not parse leading number ($p) in $time")
      p = p.substring(result.groups.get(0)?.value!!.length).trim()

      var multiplier : Multiplier? = null
      for (m in multiplers) {
        if (p.startsWith(m.str)) {
          multiplier = m
          p = p.substring(m.str.length).trim()
          break
        }
      }

      if (multiplier == null) {
        multiplier = defaultMultiplier
      }
// There is something odd with the below that I don't care about right now.
// For some reason k is turned a signed 32-bit integer.

//      if (value >= (ULong.MAX_VALUE / multiplier.usecs.toULong()).toDouble()) {
//        // This checks that the single term won't cause an overflow.
//        throw IllegalArgumentException("Value is too big $p in $time")
//      }
//
       val k : ULong = (multiplier.usecs.toULong() * value.toULong())
//      if (k > ULong.MAX_VALUE - usec) {
//        // This checks that the addition of this value won't cause an overflow.
//        throw IllegalArgumentException("Total value is too $p in $time")
//      }

      usec += k

      something = true;

    }




    return usec
  }



}
