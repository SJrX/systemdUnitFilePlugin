package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*

class SemanticDataRepositoryTest : AbstractUnitFileTest() {

  /**
   * Verifies that every registered validator references a function name that actually
   * exists in one of the gperf files. Catches stale validators after upstream renames.
   *
   * Validators kept for backward compatibility with older systemd versions should be
   * added to the allowlist below with a comment explaining why.
   */
  fun testAllRegisteredValidatorsExistInGperfFiles() {
    val sdr = SemanticDataRepository.instance

    // Validators intentionally kept for backward compatibility with older systemd versions
    val legacyAllowlist = setOf(
      // Renamed to config_parse_unit_cpu_set in systemd; old name kept for older versions
      Validator("config_parse_allowed_cpuset", "0"),
      // Deprecated cgroup v1 options removed from gperf but still valid on older systemd
      Validator("config_parse_cpu_shares", "0"),
      Validator("config_parse_blockio_weight", "0"),
      Validator("config_parse_blockio_bandwidth", "0"),
    )

    // Collect all Validator instances referenced in the gperf files
    val gperfValidators = mutableSetOf<Validator>()
    for (fileClass in FileClass.entries) {
      for (section in sdr.getSectionNamesForSectionAndKey(fileClass)) {
        for (key in sdr.getAllowedKeywordsInSectionFromValidators(fileClass, section)) {
          gperfValidators.add(sdr.getValidatorForSectionAndKey(fileClass, section, key))
        }
      }
    }

    // Check all registered validators (not just AI-generated ones)
    val registeredValidators = sdr.getValidatorMap()
    val staleValidators = registeredValidators.keys.filter { validator ->
      // Skip the NullOptionValue sentinel and wildcard validators — these are synthetic
      // and intentionally don't correspond to gperf entries
      validator.validatorName != "NULL"
        && validator.validatorArgument != "*"
        && validator !in gperfValidators
        && validator !in legacyAllowlist
    }

    assertTrue(
      "Registered validators reference function names not found in any gperf file: " +
        staleValidators.joinToString(", "),
      staleValidators.isEmpty()
    )
  }
  fun testInteresting() {
    val sdr = SemanticDataRepository.instance
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Socket", "SendSIGKILL"), BooleanOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Unit", "Documentation"), DocumentationOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Mount", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Socket", "DirectoryMode"), ModeStringOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE , "Unit", "XXXX"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "XXXX", "Yes"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "Restart"), RestartOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "Type"), ServiceTypeOptionValue::class.java)
  }

  fun testDeclaredUnderKeywordDiffers() {
    val sdr = SemanticDataRepository.instance

    for (fileClass in FileClass.entries) {
      for (section in sdr.getSectionNamesForFile(fileClass.name)) {
        val data = sdr.getKeyValuePairsForSectionFromDocumentation(fileClass, section)
        for ((key, value) in data) {
          val declaredUnderKeyword = value.declaredUnderKeyword
          if (declaredUnderKeyword != null && declaredUnderKeyword != key) {
            println("Mismatch: $section.$key: $declaredUnderKeyword")
          }
        }
      }
    }

  }

  fun testAllRequiredOptionsExists () {

    // Fixture Setup

    val sdr = SemanticDataRepository.instance

    val files = listOf(
      "file.automount",
      "file.device",
      "file.mount",
      "file.path",
      "file.service",
      "file.slice",
      "file.socket",
      "file.swap",
      "file.target",
      "file.timer",
    )


    // Execute SUT & Verification


    for (file in files) {
      val keys = sdr.getRequiredKeys(file)

      for (key in keys) {
        val sectionAndKey = key.split('.')
        val validKeys = sdr.getAllowedKeywordsInSectionFromValidators(FileClass.UNIT_FILE, sectionAndKey[0])

        assertContainsElements(validKeys, sectionAndKey[1])
      }
    }
  }
}
