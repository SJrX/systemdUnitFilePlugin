package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAddressFamiliesOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictAddressFamilies=none
            RestrictAddressFamilies=AF_INET
            RestrictAddressFamilies=AF_INET AF_INET6
            RestrictAddressFamilies=AF_UNIX AF_NETLINK
            RestrictAddressFamilies=~AF_PACKET
            RestrictAddressFamilies=~AF_INET AF_INET6
            RestrictAddressFamilies=AF_BRIDGE AF_X25 AF_AX25
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidEnumeratedFamilies() {
        // Names that the previous loose RegexTerminal happened to accept but that we now want to
        // keep accepting: an alias (AF_LOCAL), a mixed-case real name (AF_DECnet), the newest
        // families, and a long whitespace-separated list including the inversion prefix.
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictAddressFamilies=AF_LOCAL
            RestrictAddressFamilies=AF_DECnet
            RestrictAddressFamilies=AF_VSOCK AF_XDP AF_MCTP
            RestrictAddressFamilies=~AF_UNIX AF_INET AF_INET6 AF_NETLINK AF_PACKET
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
            RestrictAddressFamilies=<error descr="Invalid value">inet</error>
            RestrictAddressFamilies=<error descr="Invalid value">AF_inet</error>
            RestrictAddressFamilies=<error descr="Invalid value">AF_INET, AF_INET6</error>
            RestrictAddressFamilies=<error descr="Invalid value">~ AF_PACKET</error>
            RestrictAddressFamilies=<error descr="Invalid value">NONE</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }

    @Test
    fun testUnknownFamiliesAreNowRejected() {
        // Regression guard for the grammar fix: the old RegexTerminal("AF_[A-Z0-9_]+") accepted
        // any AF_-prefixed token, so these passed validation incorrectly. With the enumerated
        // family set they must be flagged (syntactically well-formed, semantically invalid).
        // Raw values (no <error> markup) so the guard does not depend on markup stripping; each
        // invalid property contributes exactly one highlight.
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictAddressFamilies=AF_BOGUS
            RestrictAddressFamilies=AF_INETZ
            RestrictAddressFamilies=AF_INET AF_MADEUP
            RestrictAddressFamilies=AF_DECNET
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
