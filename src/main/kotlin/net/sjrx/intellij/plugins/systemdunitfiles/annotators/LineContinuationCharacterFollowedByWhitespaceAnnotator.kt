package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFileValueImpl

class LineContinuationCharacterFollowedByWhitespaceAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is UnitFileValueType) {
      val node = element.getNode()
      val children = node.getChildren(UnitFileValueImpl.valueTypes)
      child@ for (child in children) {
        val value = child.text
        val length = value.length
        val backslash = value.lastIndexOf('\\')
        if (backslash == -1 || backslash == length - 1) continue
        for (i in backslash + 1 until length) {
          if (!Character.isWhitespace(value[i])) continue@child
        }

        // Problem starts after the \
        // Problem ends before the newline
        val range = TextRange.create(child.startOffset + backslash + 1, child.startOffset + length)
        holder.newAnnotation(HighlightSeverity.WARNING, WARNING_MESSAGE).range(range).create()
      }
    }
  }

  companion object {
    const val WARNING_MESSAGE = ("Excess whitespace detected after line continuation character '\\', the next line will NOT be a"
      + " continuation of this one")
  }
}
