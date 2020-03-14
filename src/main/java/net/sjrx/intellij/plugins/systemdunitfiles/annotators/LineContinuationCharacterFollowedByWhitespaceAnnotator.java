package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFileValueImpl;
import org.jetbrains.annotations.NotNull;

public class LineContinuationCharacterFollowedByWhitespaceAnnotator implements Annotator {
  
  static final String WARNING_MESSAGE = "Excess whitespace detected after line continuation character '\\', the next line will NOT be a"
                                        + " continuation of this one";
  
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof UnitFileValueType) {
      ASTNode node = element.getNode();
      ASTNode[] children = node.getChildren(UnitFileValueImpl.valueTypes);
      child:
      for (ASTNode child : children) {
        String value = child.getText();
        int length = value.length();

        int backslash = value.lastIndexOf('\\');
        if (backslash == -1 || backslash == length - 1) continue;

        for (int i = backslash + 1; i < length; i++) {
          if (!Character.isWhitespace(value.charAt(i))) continue child;
        }

        // Problem starts after the \
        // Problem ends before the newline
        TextRange range = TextRange.create(child.getStartOffset() + backslash + 1, child.getStartOffset() + length);
        holder.createAnnotation(HighlightSeverity.WARNING, range, WARNING_MESSAGE);
      }
    }
  
  }
}
