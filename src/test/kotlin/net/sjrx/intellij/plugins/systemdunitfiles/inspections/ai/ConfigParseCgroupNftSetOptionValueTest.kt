package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCgroupNftSetOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first two are from systemd's test/units/TEST-07-PID1.nft.sh and the man page example.
        // language="unit file (systemd)"
        val file = """
            [Service]
            NFTSet=cgroup:inet:sd_test:c user:inet:sd_test:u group:inet:sd_test:g
            NFTSet=cgroup:inet:filter:my_service user:inet:filter:serviceuser
            NFTSet=cgroup:ip:t:s
            NFTSet=user:ip6:my.table:set/v6
            NFTSet=group:arp:a:b group:bridge:a:b group:netdev:a:b
            NFTSet="cgroup:inet:filter:quoted"
            NFTSet=cgroup::inet:t:s
            NFTSet=:cgroup:inet:t:s
            NFTSet=cgroup:inet:t:s:
            NFTSet=::cgroup:inet:t:s:: user:inet:t:u:
            NFTSet="cgroup:inet:t:s:"
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testValidAcrossUnitTypes() {
        // language="unit file (systemd)"
        val socket = """
            [Socket]
            NFTSet=cgroup:inet:sd_test:c
        """.trimIndent()
        setupFileInEditor("file.socket", socket)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val mount = """
            [Mount]
            NFTSet=user:inet:sd_test:u
        """.trimIndent()
        setupFileInEditor("file.mount", mount)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val swap = """
            [Swap]
            NFTSet=group:inet:sd_test:g
        """.trimIndent()
        setupFileInEditor("file.swap", swap)
        assertSize(0, myFixture.doHighlighting())

        // language="unit file (systemd)"
        val slice = """
            [Slice]
            NFTSet=cgroup:inet:sd_test:c
        """.trimIndent()
        setupFileInEditor("file.slice", slice)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            NFTSet=address:inet:sd_test:addr4
            NFTSet=cgroup:ipv4:t:s
            NFTSet=cgroup:inet:1table:s
            NFTSet=cgroup:inet:t
            NFTSet=cgroup:inet:t:s:extra
            NFTSet=inet:filter:my_service
            NFTSet=cgroup:inet:t:s,user:inet:t:u
            NFTSet=cgroup:inet:t-x:s
            NFTSet=CGROUP:inet:t:s
            NFTSet=cgroup:inet:t:
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(10, myFixture.doHighlighting())
    }
}
