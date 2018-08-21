package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType;
import org.jetbrains.annotations.NotNull;

public class InvalidSectionHeaderNameAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

    if (element instanceof UnitFileSectionType) {
      UnitFileSectionType sectionElement = (UnitFileSectionType)element;

      // Todo fix this annonation to match the text
      if (!sectionElement.getSectionName().matches("^[A-Za-z0-9_-]+$")) {

        // First child is the section group element
        holder.createErrorAnnotation(element.getFirstChild(),
                                     "Invalid section. Sections should start with an [, end with an ], and contain only ASCII"
                                     + " characters except for [ and ] and control characters");
      }
    }
  }
}
