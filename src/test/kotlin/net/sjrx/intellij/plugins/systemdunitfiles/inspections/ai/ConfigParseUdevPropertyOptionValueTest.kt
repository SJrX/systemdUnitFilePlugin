package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUdevPropertyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            Property=ID_NET_MANAGED_BY=io.systemd.Network
            Property=ID_NET_NAME_MAC=*
            Property=FOO=bar BAZ=qux
            Property=FOO=
            Property="FOO=bar baz"
            Property=FOO="bar baz"
            Property='FOO=bar baz' X=1
            Property=HOST_%H=1
            Property=_X1=a=b
            Property=DEVPATHX=1
            Property=TAGS_EXTRA=1
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            Property=FOO
            Property=1FOO=bar
            Property=INTERFACE=eth0
            Property=SYNTH_ARG_X=1
            Property=FOO-BAR=1
            Property=ACTION=add
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
