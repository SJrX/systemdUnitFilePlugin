package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import java.util.*

class SemanticDataDocumentationCompletionTest : AbstractUnitFileTest() {
  fun testAllOptions() {
    val sdr = SemanticDataRepository.instance
    val doc: MutableSet<String> = TreeSet()

    for (fileClass in FileClass.entries) {
      for (sectionName in sdr.getSectionNamesForFile(fileClass.fileClass)) {
        for (keyName in sdr.getDocumentedKeywordsInSection(fileClass, sectionName)) {
          doc.add("${fileClass.fileClass}.$sectionName.$keyName")
        }
      }
    }
    val code: MutableSet<String> = TreeSet()

    for (fileClass in FileClass.entries) {
      for (sectionName in sdr.getSectionNamesForFile(fileClass.fileClass)) {
        for (keyName in sdr.getAllowedKeywordsInSectionFromValidators(fileClass, sectionName)) {
          code.add("${fileClass.fileClass}.$sectionName.$keyName")
        }
      }
    }

    println(doc.size) //0
    println(code.size) //1131
    val codeButNotDoc: MutableSet<String> = TreeSet(code)
    val docButNotCode: MutableSet<String> = TreeSet(doc)
    codeButNotDoc.removeAll(doc)
    docButNotCode.removeAll(code)

    System.err.println("***** (Code but not Doc) *****")
    assertEmpty("Expected that everything in the code was in the documentation, but we are missing the following:", codeButNotDoc)


    System.err.println("***** (Doc but not code) *****")
    System.err.flush()

    assertEmpty("Expected that everything in the documentation was in the code, but we have documentation for the following unknown thingies: ", docButNotCode)

  }
}
