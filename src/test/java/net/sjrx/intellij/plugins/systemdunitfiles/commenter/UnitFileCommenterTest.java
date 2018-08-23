package net.sjrx.intellij.plugins.systemdunitfiles.commenter;

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType;

public class UnitFileCommenterTest extends LightPlatformCodeInsightFixtureTestCase {

  public void testCommenter() {
    myFixture.configureByText(ServiceFileType.INSTANCE, "<caret>[Service]");
    CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
    commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
    myFixture.checkResult("#[Service]");
    commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
    myFixture.checkResult("[Service]");
  }


}
