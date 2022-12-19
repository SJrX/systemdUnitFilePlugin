package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import java.util.*

class SemanticDataDocumentationCompletionTest : AbstractUnitFileTest() {
  fun testAllOptions() {
    val sdr = SemanticDataRepository.instance
    val doc: MutableSet<String> = TreeSet()
    for (sectionName in sdr.sectionNamesFromDocumentation) {
      for (keyName in sdr.getDocumentedKeywordsInSection(sectionName)) {
        doc.add("$sectionName.$keyName")
      }
    }
    val code: MutableSet<String> = TreeSet()
    for (sectionName in sdr.sectionNamesFromValidators) {
      for (keyName in sdr.getAllowedKeywordsInSectionFromValidators(sectionName)) {
        code.add("$sectionName.$keyName")
      }
    }
    println(doc.size)
    println(code.size)
    val codeButNotDoc: MutableSet<String> = TreeSet(code)
    val docButNotCode: MutableSet<String> = TreeSet(code)
    codeButNotDoc.removeAll(doc)
    docButNotCode.removeAll(code)
    System.err.println("***** (Code but not doc) *****")
    System.err.flush()
    assertEmpty("Expected that everything in the code was in the documentation, but we are missing the following:", codeButNotDoc)
    assertEmpty("expected that everything in the documentation was in the code, but we have documentation for the following unknown thingies: ", docButNotCode)
  }
}
