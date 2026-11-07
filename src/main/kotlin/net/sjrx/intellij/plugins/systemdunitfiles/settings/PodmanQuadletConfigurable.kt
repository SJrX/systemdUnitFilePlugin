package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class PodmanQuadletConfigurable(private val project: Project) : Configurable {

  private var enabledCheckbox: JBCheckBox? = null
  private var grammarEngineCheckbox: JBCheckBox? = null
  private var underlineGrammarKeysCheckbox: JBCheckBox? = null
  private var macFormatCombo: ComboBox<PreferredMacFormat>? = null

  override fun getDisplayName(): String = "systemd Unit Files"

  override fun createComponent(): JComponent {
    val settings = PodmanQuadletSettings.getInstance(project)
    val experimental = ExperimentalSettings.getInstance(project)
    enabledCheckbox = JBCheckBox("Enable Podman Quadlet support (experimental)", settings.state.enabled)
    grammarEngineCheckbox = JBCheckBox(
      "Use the new grammar engine for value validation (experimental)",
      experimental.state.useGrammarParseEngine,
    )
    underlineGrammarKeysCheckbox = JBCheckBox(
      "Underline keys backed by the new grammar engine (experimental)",
      experimental.state.underlineGrammarEngineKeys,
    )
    macFormatCombo = ComboBox(PreferredMacFormat.entries.toTypedArray()).apply {
      selectedItem = experimental.state.preferredMacFormat
    }

    return FormBuilder.createFormBuilder()
      .addComponent(enabledCheckbox!!)
      .addComponent(grammarEngineCheckbox!!)
      .addComponent(underlineGrammarKeysCheckbox!!)
      .addLabeledComponent("Preferred MAC address format:", macFormatCombo!!)
      .addComponentFillVertically(JPanel(), 0)
      .panel
  }

  override fun isModified(): Boolean {
    val settings = PodmanQuadletSettings.getInstance(project)
    val experimental = ExperimentalSettings.getInstance(project)
    return enabledCheckbox?.isSelected != settings.state.enabled ||
      grammarEngineCheckbox?.isSelected != experimental.state.useGrammarParseEngine ||
      underlineGrammarKeysCheckbox?.isSelected != experimental.state.underlineGrammarEngineKeys ||
      selectedMacFormat() != experimental.state.preferredMacFormat
  }

  override fun apply() {
    val settings = PodmanQuadletSettings.getInstance(project)
    val newEnabled = enabledCheckbox?.isSelected ?: false
    if (settings.state.enabled != newEnabled) {
      settings.state.notificationDismissed = false
    }
    settings.state.enabled = newEnabled

    val experimental = ExperimentalSettings.getInstance(project).state
    val newGrammarEngine = grammarEngineCheckbox?.isSelected ?: false
    val newUnderlineKeys = underlineGrammarKeysCheckbox?.isSelected ?: false
    val newMacFormat = selectedMacFormat()
    val changed = experimental.useGrammarParseEngine != newGrammarEngine ||
      experimental.underlineGrammarEngineKeys != newUnderlineKeys ||
      experimental.preferredMacFormat != newMacFormat
    experimental.useGrammarParseEngine = newGrammarEngine
    experimental.underlineGrammarEngineKeys = newUnderlineKeys
    experimental.preferredMacFormat = newMacFormat
    // Inspections and annotators read these when they build their visitors, so re-run them on open files.
    if (changed) DaemonCodeAnalyzer.getInstance(project).restart()
  }

  override fun reset() {
    val settings = PodmanQuadletSettings.getInstance(project)
    val experimental = ExperimentalSettings.getInstance(project)
    enabledCheckbox?.isSelected = settings.state.enabled
    grammarEngineCheckbox?.isSelected = experimental.state.useGrammarParseEngine
    underlineGrammarKeysCheckbox?.isSelected = experimental.state.underlineGrammarEngineKeys
    macFormatCombo?.selectedItem = experimental.state.preferredMacFormat
  }

  private fun selectedMacFormat(): PreferredMacFormat =
    macFormatCombo?.selectedItem as? PreferredMacFormat ?: PreferredMacFormat.COLON_LOWER
}
