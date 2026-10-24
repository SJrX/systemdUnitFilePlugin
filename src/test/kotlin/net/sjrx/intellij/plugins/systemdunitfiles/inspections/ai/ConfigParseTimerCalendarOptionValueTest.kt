package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTimerCalendarOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValuesFromSystemdTests() {
    // Every input of TEST(calendar_spec_one) in src/test/test-calendarspec.c, plus the man page examples.
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnCalendar=Sat,Thu,Mon-Wed,Sat-Sun
      OnCalendar=Sat,Thu,Mon..Wed,Sat..Sun
      OnCalendar=Mon,Sun 12-*-* 2,1:23
      OnCalendar=Wed *-1
      OnCalendar=Wed-Wed,Wed *-1
      OnCalendar=Wed..Wed,Wed *-1
      OnCalendar=Wed, 17:48
      OnCalendar=Wednesday,
      OnCalendar=Wed-Sat,Tue 12-10-15 1:2:3
      OnCalendar=Wed..Sat,Tue 12-10-15 1:2:3
      OnCalendar=*-*-7 0:0:0
      OnCalendar=10-15
      OnCalendar=monday *-12-* 17:00
      OnCalendar=Mon,Fri *-*-3,1,2 *:30:45
      OnCalendar=12,14,13,12:20,10,30
      OnCalendar=mon,fri *-1/2-1,3 *:30:45
      OnCalendar=03-05 08:05:40
      OnCalendar=08:05:40
      OnCalendar=05:40
      OnCalendar=Sat,Sun 12-05 08:05:40
      OnCalendar=Sat,Sun 08:05:40
      OnCalendar=2003-03-05 05:40
      OnCalendar=2003-03-05
      OnCalendar=03-05
      OnCalendar=hourly
      OnCalendar=daily
      OnCalendar=monthly
      OnCalendar=weekly
      OnCalendar=minutely
      OnCalendar=quarterly
      OnCalendar=semi-annually
      OnCalendar=annually
      OnCalendar=*:2/3
      OnCalendar=2015-10-25 01:00:00 uTc
      OnCalendar=2015-10-25 01:00:00 Asia/Vladivostok
      OnCalendar=weekly Pacific/Auckland
      OnCalendar=2016-03-27 03:17:00.4200005
      OnCalendar=2016-03-27 03:17:00/0.42
      OnCalendar=9..11,13:00,30
      OnCalendar=1..3-1..3 1..3:1..3
      OnCalendar=00:00:1.125..2.125
      OnCalendar=00:00:1.0..3.8
      OnCalendar=00:00:01..03
      OnCalendar=00:00:01/2,02..03
      OnCalendar=*:4,30:0..3
      OnCalendar=*:4,30:0/1
      OnCalendar=*:4,30:0/1,3,5
      OnCalendar=*-*~1 Utc
      OnCalendar=*-*~* 00:00:00
      OnCalendar=Monday
      OnCalendar=Monday *-*-*
      OnCalendar=*-*-*
      OnCalendar=*:*:*
      OnCalendar=*:*
      OnCalendar=12:*
      OnCalendar=*:30
      OnCalendar=93..00-*-*
      OnCalendar=00..07-*-*
      OnCalendar=*:20..39/5
      OnCalendar=00:00:20..40/1
      OnCalendar=*~03/1,03..05
      OnCalendar=@1493187147
      OnCalendar=@1493187147 UTC
      OnCalendar=@0
      OnCalendar=*:05..05
      OnCalendar=*:05..10/6
      OnCalendar=Thu,Fri 2012-*-1,5 11:12:13
      OnCalendar=12..14:10,20,30
      OnCalendar=05:40:23.4200004/3.1700005
      OnCalendar=2003-02..04-05
      OnCalendar=daily UTC
      OnCalendar=*-02~03
      OnCalendar=Mon *-05~07/1
      OnCalendar=Mon 2017-05~01..07 UTC
      OnCalendar=Sun *-*-* 01:00:00 Europe/Dublin
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidValuesFromCorpus() {
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnCalendar=Daily
      OnCalendar=*-*-* 6:00
      OnCalendar=*:0/5
      OnCalendar=*-*-* 00,12:00:00
      OnCalendar=Sun *-*-1..7 0:45:00
      OnCalendar=Mon..Fri *-*-* 06:30:00
      OnCalendar=Mon,Tue,Wed,Thu,Fri,Sat
      OnCalendar=Mon *-*-* 6:15:0
      OnCalendar=*-*-* 16,17,18,19,20,21,22,23,00,01,02,03,04,05,06:00:00
      OnCalendar=*-*-* *:00/10:00
      OnCalendar=Fri 2012-11-23 11:12:13
      OnCalendar=daily utc
      OnCalendar=*-*-* %i:00
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // Rejections from TEST(calendar_spec_from_string) whose cause is a shape or a single-field bound,
    // plus a few typos seen in the wild.
    // language="unit file (systemd)"
    val file = """
      [Timer]
      OnCalendar=test
      OnCalendar=7
      OnCalendar=121212:1:2
      OnCalendar=2000-03-05.23 00:00:00
      OnCalendar=2000-03-05 00:00.1:00
      OnCalendar=2016~11-22
      OnCalendar=Monday.. 12:00
      OnCalendar=Monday..
      OnCalendar=-00:+00/-5
      OnCalendar=00:+00/-5
      OnCalendar=2016- 11- 24 12: 30: 00
      OnCalendar=*~29
      OnCalendar=*~16..31
      OnCalendar=00:00/60
      OnCalendar=00:00:2300
      OnCalendar=00:00:18446744073709551615
      OnCalendar=@88588582097858858
      OnCalendar=*:4,30:*,5
      OnCalendar=*:4,30:5,*
      OnCalendar=Mo *-12-* 17:00
      OnCalendar=dialy
      OnCalendar=*-*-* 24:00
      OnCalendar=*-13-01
      OnCalendar=1969-01-01
      OnCalendar=%e
    """.trimIndent()

    setupFileInEditor("file.timer", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(25, highlights)
  }
}
