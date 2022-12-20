package net.sjrx.intellij.plugins.systemdunitfiles.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import java.util.stream.Collectors

/**
 * Completion Contributor for keywords in a section.
 */
class UnitFileKeyCompletionContributor : CompletionContributor() {
  /**
   * Default constructor.
   */
  init {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(UnitFileElementTypeHolder.KEY).withLanguage(UnitFileLanguage.INSTANCE),
           object : CompletionProvider<CompletionParameters>() {
             override fun addCompletions(parameters: CompletionParameters,
                                         context: ProcessingContext,
                                         resultSet: CompletionResultSet) {
               var section = PsiTreeUtil.getParentOfType(parameters.originalPosition, UnitFileSectionGroups::class.java)
               if (section == null) section = PsiTreeUtil.getParentOfType(parameters.position, UnitFileSectionGroups::class.java)
               if (section == null) return
               val sectionName = section.sectionName
               val definedKeys = section.propertyList.stream().map { obj: UnitFileProperty -> obj.key }.collect(Collectors.toSet())
               val sdr = SemanticDataRepository.instance
               for (keyword in sdr.getDocumentedKeywordsInSection(sectionName)) {
                 if (definedKeys.contains(keyword)) continue
                 val deprecated = sdr.isDeprecated(sectionName, keyword)
                 val builder = LookupElementBuilder
                   .create(keyword)
                   .withInsertHandler(KeyInsertHandler())
                   .withPresentableText(keyword)
                   .withStrikeoutness(deprecated)
                   .withIcon(UnitFileIcon.FILE)
                 resultSet.addElement(builder)
               }
             }
           }
    )
  }

  private class KeyInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
      val element = context.file.findElementAt(context.startOffset) ?: return
      if (element.node.elementType === UnitFileElementTypeHolder.KEY) {
        val next = PsiTreeUtil.nextVisibleLeaf(element)
        if (next != null && next.node.elementType === UnitFileElementTypeHolder.SEPARATOR) {
          return
        }
      }
      if (StringUtil.containsChar(" =", context.completionChar)) {
        context.setAddCompletionChar(false)
      }
      EditorModificationUtil.insertStringAtCaret(context.editor, "=")
    }
  }
}
