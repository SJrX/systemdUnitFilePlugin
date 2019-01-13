package net.sjrx.intellij.plugins.systemdunitfiles.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.impl.UnitFileSectionGroupsImpl;
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.lang.documentation.DocumentationMarkup.CONTENT_END;
import static com.intellij.lang.documentation.DocumentationMarkup.CONTENT_START;
import static com.intellij.lang.documentation.DocumentationMarkup.DEFINITION_END;
import static com.intellij.lang.documentation.DocumentationMarkup.DEFINITION_START;


public class UnitFileDocumentationProvider extends AbstractDocumentationProvider {

  private static final SemanticDataRepository sdr = SemanticDataRepository.getInstance();

  @Nullable
  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return "Hello, is it me you're looking for";
  }

  @Override
  public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.KEY)) {

      String sectionName = ((UnitFileSectionGroupsImpl)element.getParent().getParent()).getSectionName();
      String keyName = element.getNode().getText();

      String keyComment = sdr.getDocumentationContentForKeyInSection(sectionName, keyName);

      if (keyComment != null) {
        return DEFINITION_START + keyName + DEFINITION_END + CONTENT_START + keyComment + CONTENT_END;
      }
    } else if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.SECTION)) {

      String sectionName = ((UnitFileSectionGroupsImpl)element.getParent()).getSectionName();

      String sectionComment = sdr.getDocumentationContentForSection(sectionName);

      if (sectionComment != null) {
        return DEFINITION_START + sectionName + DEFINITION_END + CONTENT_START + sectionComment + CONTENT_END;
      }
    } else if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.SEPARATOR)) {
      return generateDoc(((LeafPsiElement)element.getNode()).getPrevSibling().getNode().getPsi(), originalElement);
    } else {
      return null;
    }

    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {

    if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.KEY)) {

      String sectionName = ((UnitFileSectionGroupsImpl)element.getParent().getParent()).getSectionName();
      String keyName = element.getNode().getText();

      String keyNameToPointTo = sdr.getKeywordLocationInDocumentation(sectionName, keyName);

      String filename = sdr.getKeywordFileLocationInDocumentation(sectionName, keyName);

      if ((keyNameToPointTo != null) && (filename != null)) {
        return Collections.singletonList(
          "https://www.freedesktop.org/software/systemd/man/" + filename.replaceFirst(".xml$", ".html") + "#"
          + keyNameToPointTo + "=");
      }
    } else if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.SEPARATOR)) {
      return getUrlFor(((LeafPsiElement)element.getNode()).getPrevSibling(), originalElement);
    } else if (element.getNode().getElementType().equals(UnitFileElementTypeHolder.SECTION)) {

      String sectionName = ((UnitFileSectionGroupsImpl)element.getParent()).getSectionName();

      String sectionUrl = sdr.getUrlForSectionName(sectionName);

      if (sectionUrl != null) {
        return Collections.singletonList(sectionUrl);
      }
    }

    return Collections.emptyList();

  }
  
  @Override
  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
    return null;
  }
  

  @Override
  public PsiElement getCustomDocumentationElement(@NotNull final Editor editor,
                                                  @NotNull final PsiFile file,
                                                  @Nullable PsiElement contextElement) {
  
    if (contextElement == null) {
      // If no context element just return null
      return null;
    }
  
    if ((contextElement.getParent() != null)
        && (contextElement.getParent().getParent() != null)
        && (contextElement.getParent().getParent() instanceof UnitFilePropertyType)) {
      return ((UnitFilePropertyType) contextElement.getParent().getParent()).getKeyNode().getPsi();
    }
    
    return contextElement;
  }
}
