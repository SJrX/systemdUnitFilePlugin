package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineContinuationCharacterFollowedByWhitespaceAnnotator implements Annotator {
  
  static final String WARNING_MESSAGE = "Excess whitespace detected after line continuation character '\\', the next line will NOT be a"
                                        + " continuation of this one";
  
  private static final Pattern LINE_CONTINUATION_CHARACTER_TO_END_REGEX = Pattern.compile("\\\\\\s+$", Pattern.MULTILINE);
  
  
  
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof UnitFileValueType) {

      // Use the getNode().getText() because getValue() trims whitespace and internally handles line continuation
      ASTNode valueNode = element.getNode();
      String value = valueNode.getText();

      Matcher m = LINE_CONTINUATION_CHARACTER_TO_END_REGEX.matcher(value);
      
      if (m.find()) {
        int startCharacter = m.start();
        int endIndex = m.end();
  
        // Problem starts after the \
        // Problem ends before the newline
        int problemStartIndex = valueNode.getTextRange().getStartOffset() + startCharacter + 1;
        int problemEndIndex = valueNode.getTextRange().getStartOffset() + endIndex - 1;
        
        
        holder.createWarningAnnotation(new TextRange(problemStartIndex, problemEndIndex), WARNING_MESSAGE);
      }
    
    }
  
  }
}
