package net.sjrx.intellij.plugins.systemdunitfiles.commenter

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType

class UnitFileCommenterTest : BasePlatformTestCase() {
  fun testCommenter() {
    myFixture.configureByText(ServiceFileType.INSTANCE, "<caret>[Service]")
    val commentAction = CommentByLineCommentAction()
    commentAction.actionPerformedImpl(project, myFixture.editor)
    myFixture.checkResult("#[Service]")
    commentAction.actionPerformedImpl(project, myFixture.editor)
    myFixture.checkResult("[Service]")
  }
}
