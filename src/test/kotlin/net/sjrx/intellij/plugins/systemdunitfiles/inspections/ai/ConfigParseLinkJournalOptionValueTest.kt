package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLinkJournalOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // parse_link_journal's names plus parse_boolean (case-insensitive).
        // language="unit file (systemd)"
        val file = """
            [Exec]
            LinkJournal=no
            LinkJournal=auto
            LinkJournal=guest
            LinkJournal=host
            LinkJournal=try-guest
            LinkJournal=try-host
            LinkJournal=yes
            LinkJournal=false
            LinkJournal=0
            LinkJournal=On
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown mode; the names are compared with streq().
        // language="unit file (systemd)"
        val file = """
            [Exec]
            LinkJournal=try-auto
            LinkJournal=Guest
            LinkJournal=persistent
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
