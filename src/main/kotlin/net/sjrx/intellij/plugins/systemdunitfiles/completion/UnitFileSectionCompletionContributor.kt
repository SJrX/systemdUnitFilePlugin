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

          val completeSections = when(extension) {
            "automount" ->  listOf("Unit", "Install", "Automount")
            "device", "target" -> listOf("Unit", "Install")
            "mount" ->  listOf("Unit", "Install", "Mount")
            "path" -> listOf("Unit", "Install", "Path")
            "service" -> listOf("Unit", "Install", "Service")
            "socket" -> listOf("Unit", "Install", "Socket")
            "swap" ->  listOf("Unit", "Install", "Swap")
            "timer" -> listOf("Unit", "Install", "Timer")
            else -> listOf()
          }
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
