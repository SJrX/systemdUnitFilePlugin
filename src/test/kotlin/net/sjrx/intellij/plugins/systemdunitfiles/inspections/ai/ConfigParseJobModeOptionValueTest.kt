package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseJobModeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            OnSuccessJobMode=fail
            OnFailureJobMode=replace
            OnSuccessJobMode=replace-irreversibly
            OnFailureJobMode=isolate
            OnSuccessJobMode=flush
            OnFailureJobMode=ignore-dependencies
            OnSuccessJobMode=ignore-requirements
            OnFailureJobMode=triggering
            OnSuccessJobMode=restart-dependencies
            OnFailureJobMode=lenient
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
            [Unit]
            OnSuccessJobMode=<error descr="Invalid value">unknown</error>
            OnFailureJobMode=<error descr="Invalid value">Replace</error>
            OnSuccessJobMode=<error descr="Invalid value">fail isolate</error>
            OnFailureJobMode=<error descr="Invalid value">replace_irreversibly</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
