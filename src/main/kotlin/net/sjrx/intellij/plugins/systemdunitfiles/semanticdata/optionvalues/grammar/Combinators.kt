package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

val BYTES = RegexTerminal("[0-9]+[a-zA-Z]*\\s*", "[0-9]+[KMGT]?\\s*")
val DEVICE = RegexTerminal("\\S+\\s*", "/[^\\u0000. ]+\\s*")
val IOPS = RegexTerminal("[0-9]+[a-zA-Z]*\\s*", "[0-9]+[KMGT]?\\s*")
