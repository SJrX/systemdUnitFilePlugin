package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata


interface ConfigFile {
  val extension : String
  val allowedSections : List<String>
  val requiredSections : List<String>
}
