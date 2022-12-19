package net.sjrx.intellij.plugins.systemdunitfiles.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository

class UnitFileDocumentationProvider : AbstractDocumentationProvider() {
  override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String? {
    // Hrm not sure what this does
    return "Hello, is it me you're looking for"
  }

  override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
    if (element.node == null) return null
    val elementType = element.node.elementType
    if (elementType == UnitFileElementTypeHolder.KEY) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return null
      val sectionName = section.sectionName
      val keyName = element.node.text
      val sdr: SemanticDataRepository = SemanticDataRepository.instance
      val keyComment = sdr.getDocumentationContentForKeyInSection(sectionName, keyName)
      if (keyComment != null) {
        return DocumentationMarkup.DEFINITION_START + keyName + DocumentationMarkup.DEFINITION_END + DocumentationMarkup.CONTENT_START + keyComment + DocumentationMarkup.CONTENT_END
      }
    } else if (elementType == UnitFileElementTypeHolder.SECTION) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return null
      val sectionName = section.sectionName
      val sdr: SemanticDataRepository = SemanticDataRepository.instance
      val sectionComment = sdr.getDocumentationContentForSection(sectionName)
      if (sectionComment != null) {
        return DocumentationMarkup.DEFINITION_START + sectionName + DocumentationMarkup.DEFINITION_END + DocumentationMarkup.CONTENT_START + sectionComment + DocumentationMarkup.CONTENT_END
      }
    } else return if (elementType == UnitFileElementTypeHolder.SEPARATOR) {
      generateDoc(PsiTreeUtil.skipWhitespacesBackward(element)!!, originalElement)
    } else {
      null
    }
    return null
  }

  override fun getUrlFor(element: PsiElement, originalElement: PsiElement): List<String>? {
    if (element.node == null) return null
    val elementType = element.node.elementType
    if (elementType == UnitFileElementTypeHolder.KEY) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return null
      val sectionName = section.sectionName
      val keyName = element.text
      val sdr: SemanticDataRepository = SemanticDataRepository.instance
      val url = sdr.getKeywordDocumentationUrl(sectionName, keyName)
      if (url != null) {
        return listOf(url)
      }
      val keyNameToPointTo = sdr.getKeywordLocationInDocumentation(sectionName, keyName)
      val filename = sdr.getKeywordFileLocationInDocumentation(sectionName, keyName)
      if (keyNameToPointTo != null && filename != null) {
        return listOf(
          "https://www.freedesktop.org/software/systemd/man/" + filename.replaceFirst(".xml$".toRegex(), ".html") + "#"
            + keyNameToPointTo + "="
        )
      }
    } else if (elementType == UnitFileElementTypeHolder.SEPARATOR) {
      return getUrlFor(PsiTreeUtil.skipWhitespacesBackward(element)!!, originalElement)
    } else if (elementType == UnitFileElementTypeHolder.SECTION) {
      val section = PsiTreeUtil.getParentOfType(element, UnitFileSectionType::class.java) ?: return null
      val sectionName = section.sectionName
      val sdr: SemanticDataRepository = SemanticDataRepository.instance
      val sectionUrl = sdr.getUrlForSectionName(sectionName)
      if (sectionUrl != null) {
        return listOf(sectionUrl)
      }
    }
    return null
  }

  override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement): PsiElement? {
    return null
  }

  override fun getCustomDocumentationElement(
    editor: Editor,
    file: PsiFile,
    contextElement: PsiElement?, offset: Int
  ): PsiElement? {
    if (contextElement == null) {
      // If no context element just return null
      return null
    }
    return if (contextElement.parent != null && contextElement.parent.parent != null
      && contextElement.parent.parent is UnitFilePropertyType
    ) {
      (contextElement.parent.parent as UnitFilePropertyType).keyNode.psi
    } else contextElement
  }
}
