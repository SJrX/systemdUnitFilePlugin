package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseRestrictAddressFamiliesOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.deprecatedTokens
import org.junit.Test

class ConfigParseRestrictAddressFamiliesOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN (--restrict-address-families=): with or without AF_, any case, comma or space separated,
        // quoted words, and a bare `~`.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            RestrictAddressFamilies=unix,inet
            RestrictAddressFamilies=unix inet
            RestrictAddressFamilies=AF_UNIX AF_INET
            RestrictAddressFamilies=~netlink
            RestrictAddressFamilies=none
            RestrictAddressFamilies=unix,inet,netlink
            RestrictAddressFamilies=unix inet AF_NETLINK
            RestrictAddressFamilies=~packet
            RestrictAddressFamilies=~
            RestrictAddressFamilies=AF_INET, AF_INET6
            RestrictAddressFamilies=af_inet6
            RestrictAddressFamilies=~ AF_UNIX
            RestrictAddressFamilies="AF_INET" 'inet6'
            RestrictAddressFamilies=AF_DECnet decnet
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown family, `~` only allowed at the start, `none` only allowed on its own.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            RestrictAddressFamilies=AF_BOGUS
            RestrictAddressFamilies=inet ~unix
            RestrictAddressFamilies=~none
            RestrictAddressFamilies=none inet
            RestrictAddressFamilies=AF-INET
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }

    @Test
    fun testRemovedFamiliesAreDeprecatedInEitherSpelling() {
        // af_from_name still resolves kernel-removed families, so they are valid but flagged as obsolete,
        // whether written with the AF_ prefix or without it and in any case.
        val grammar = ConfigParseRestrictAddressFamiliesOptionValue().combinator
        val deprecated = grammar.deprecatedTokens("inet,AF_DECnet irda")
        assertEquals(2, deprecated.size)
        assertTrue(deprecated[0].message.contains("AF_DECnet"))
        assertTrue(deprecated[1].message.contains("AF_IRDA"))
        assertTrue(grammar.deprecatedTokens("AF_INET inet6 unix").isEmpty())
    }
}
