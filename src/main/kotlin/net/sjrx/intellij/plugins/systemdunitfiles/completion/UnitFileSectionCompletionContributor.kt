package net.sjrx.intellij.plugins.systemdunitfiles.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileLanguage
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository

class UnitFileSectionCompletionContributor() : CompletionContributor() {
  init {
    extend(
      CompletionType.BASIC,
      PlatformPatterns.psiElement(UnitFileElementTypeHolder.SECTION).withLanguage(UnitFileLanguage.INSTANCE),
      object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters,
                                      context: ProcessingContext,
                                      resultSet: CompletionResultSet) {
          val extension = parameters.position.containingFile.name.substringAfterLast(".", "")

          val completeSections = SemanticDataRepository.instance.getAllowedSectionsInFile(parameters.position.containingFile.name)

          resultSet.addAllElements(
            completeSections.map {
              LookupElementBuilder.create(it).withIcon(UnitFileIcon.FILE).withInsertHandler(KeyInsertHandler())
            }
          )
        }
      }
    )
  }

  private class KeyInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
      EditorModificationUtil.insertStringAtCaret(context.editor, "]")
    }
  }
}
