package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class InvalidSectionHeaderNameAnnotator implements Annotator {

  static final String ANNOTATION_ERROR_MSG = "Invalid section. Sections should start with an [, end with an ], and contain only ASCII"
                                             + " characters except for [ and ] and control characters";

  /**
   * https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s03.html#group-header
   * Group names may contain all ASCII characters except for [ and ] and control characters.
   * 20-7f are non control characters, and we subtract the [ from that set.
   */
  private static final Pattern p = Pattern.compile("^[\\x20-\\x7f&&[^\\[]]+$");

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

    if (element instanceof UnitFileSectionType) {
      UnitFileSectionType sectionElement = (UnitFileSectionType)element;

      if (!p.matcher(sectionElement.getSectionName()).find()) {

        // First child is the section group element
        holder.createErrorAnnotation(element.getFirstChild(),
                                     ANNOTATION_ERROR_MSG);
      }
    }
  }
}
