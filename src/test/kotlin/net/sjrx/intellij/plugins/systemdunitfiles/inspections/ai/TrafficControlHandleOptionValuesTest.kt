package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** Traffic-control Handle= / Parent= / ClassId= in .network [QDisc] and [*Class] sections. */
class TrafficControlHandleOptionValuesTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Handles are hexadecimal uint16 (so `10` is 0x10). Parent= takes root/clsact/ingress or a
        // major:minor handle in a [QDisc], but only root or a handle in a [*Class].
        // language="unit file (systemd)"
        val file = """
            [QDisc]
            Handle=3a
            Handle=0002
            Handle=ffff
            Handle=10
            Parent=root
            Parent=clsact
            Parent=ingress
            Parent=2:3c

            [NetworkEmulator]
            Handle=0037
            Parent=35:0

            [HierarchyTokenBucketClass]
            Parent=root
            Parent=1:1
            ClassId=1:10
            ClassId=abcd:1
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Handle=gg: not hex. Handle=1:2: a handle number takes no colon. Parent=clsact in a [*Class]:
        // clsact/ingress are qdisc-only, not accepted by config_parse_tclass_parent.
        // language="unit file (systemd)"
        val file = """
            [QDisc]
            Handle=gg
            Handle=1:2
            Parent=nonsense

            [HierarchyTokenBucketClass]
            Parent=clsact
            ClassId=root
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
