package net.sjrx.intellij.plugins.systemdunitfiles.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class PropertyIsNotInSectionAnnotator implements Annotator {

  static final String ANNOTATION_ERROR_MSG = "Property must be under section";

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

    if (element instanceof UnitFileProperty) {
      UnitFileProperty property = (UnitFileProperty) element;
      UnitFileSectionType section = PsiTreeUtil.getParentOfType(property, UnitFileSectionType.class);
      if (section == null) {
        holder.createErrorAnnotation(element, ANNOTATION_ERROR_MSG);
      }
    }
  }
}
