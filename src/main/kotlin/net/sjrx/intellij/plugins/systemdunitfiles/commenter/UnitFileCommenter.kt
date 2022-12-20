package net.sjrx.intellij.plugins.systemdunitfiles.commenter

import com.intellij.lang.Commenter

class UnitFileCommenter : Commenter {
  override fun getLineCommentPrefix(): String? {
    return "#"
  }

  override fun getBlockCommentPrefix(): String? {
    return null
  }

  override fun getBlockCommentSuffix(): String? {
    return null
  }

  override fun getCommentedBlockCommentPrefix(): String? {
    return null
  }

  override fun getCommentedBlockCommentSuffix(): String? {
    return null
  }
}
