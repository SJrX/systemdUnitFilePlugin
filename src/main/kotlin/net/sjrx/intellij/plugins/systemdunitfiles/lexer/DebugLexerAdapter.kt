package net.sjrx.intellij.plugins.systemdunitfiles.lexer

import com.intellij.lexer.LexerBase
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.tree.IElementType

class DebugLexerAdapter : LexerBase() {
  private val ufla = UnitFileLexerAdapter()
  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    LOG.info("start($buffer,$startOffset,$endOffset,$initialState) ==> void")
    ufla.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    val state = ufla.state
    LOG.info("getState() ==> $state")
    return state
  }

  override fun getTokenType(): IElementType? {
    val ooType = ufla.tokenType
    LOG.info("getTokenType() ==> $ooType")
    return ooType
  }

  override fun getTokenStart(): Int {
    val tokenStart = ufla.tokenStart
    LOG.info("getTokenStart() ==> $tokenStart")
    return tokenStart
  }

  override fun getTokenEnd(): Int {
    val tokenEnd = ufla.tokenEnd
    LOG.info("getTokenEnd() ==> $tokenEnd")
    return tokenEnd
  }

  override fun advance() {
    ufla.advance()
    LOG.info("advance() ==> void")
  }

  override fun getBufferSequence(): CharSequence {
    val cs = ufla.bufferSequence
    LOG.info("getBufferSequence() ==> $cs")
    return cs
  }

  override fun getBufferEnd(): Int {
    val be = ufla.bufferEnd
    LOG.info("getBufferEnd() ==> $be")
    return be
  }

  companion object {
    private val LOG = Logger.getInstance(DebugLexerAdapter::class.java)
  }
}
