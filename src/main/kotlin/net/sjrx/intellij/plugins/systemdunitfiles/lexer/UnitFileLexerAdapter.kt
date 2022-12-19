package net.sjrx.intellij.plugins.systemdunitfiles.lexer

import com.intellij.lexer.FlexAdapter
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileLexer

class UnitFileLexerAdapter : FlexAdapter(UnitFileLexer(null))
