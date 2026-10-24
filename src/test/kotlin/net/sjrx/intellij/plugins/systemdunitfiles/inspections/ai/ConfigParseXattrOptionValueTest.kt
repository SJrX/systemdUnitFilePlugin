package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseXattrOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // The first three are from systemd's test/units/TEST-74-AUX-UTILS.socket-xattr.sh and the
        // systemd.socket(5) Varlink example.
        // language="unit file (systemd)"
        val file = """
            [Socket]
            XAttrEntryPoint=user.varlink=entrypoint
            XAttrListen=user.varlink=listen
            XAttrAccept=user.varlink=server
            XAttrEntryPoint=user.unit=%n
            XAttrListen=user.%N.tag=a=b=c
            XAttrAccept=user.empty=
            XAttrAccept=user.percent=100%%
            XAttrAccept=user.literal=50%-off
            XAttrAccept=user.trailing=5%
            XAttrAccept=user.spaces=hello world
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            XAttrEntryPoint=security.selinux=foo
            XAttrListen=trusted.x=y
            XAttrAccept=user.varlink
            XAttrEntryPoint=User.varlink=entrypoint
            XAttrListen=user.x=%z
            XAttrAccept=user.%9=y
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(6, myFixture.doHighlighting())
    }
}
