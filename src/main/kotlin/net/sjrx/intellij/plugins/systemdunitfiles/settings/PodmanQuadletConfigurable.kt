package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class PodmanQuadletConfigurable(private val project: Project) : Configurable {

  private var enabledCheckbox: JBCheckBox? = null
  private var grammarEngineCheckbox: JBCheckBox? = null
  private var underlineGrammarKeysCheckbox: JBCheckBox? = null

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

    return FormBuilder.createFormBuilder()
      .addComponent(enabledCheckbox!!)
      .addComponent(grammarEngineCheckbox!!)
      .addComponent(underlineGrammarKeysCheckbox!!)
      .addComponentFillVertically(JPanel(), 0)
      .panel
  }

  override fun isModified(): Boolean {
    val settings = PodmanQuadletSettings.getInstance(project)
    val experimental = ExperimentalSettings.getInstance(project)
    return enabledCheckbox?.isSelected != settings.state.enabled ||
      grammarEngineCheckbox?.isSelected != experimental.state.useGrammarParseEngine ||
      underlineGrammarKeysCheckbox?.isSelected != experimental.state.underlineGrammarEngineKeys
  }

  override fun apply() {
    val settings = PodmanQuadletSettings.getInstance(project)
    val newEnabled = enabledCheckbox?.isSelected ?: false
    if (settings.state.enabled != newEnabled) {
      settings.state.notificationDismissed = false
    }
    settings.state.enabled = newEnabled

    val experimental = ExperimentalSettings.getInstance(project)
    experimental.state.useGrammarParseEngine = grammarEngineCheckbox?.isSelected ?: false
    experimental.state.underlineGrammarEngineKeys = underlineGrammarKeysCheckbox?.isSelected ?: false
  }

  override fun reset() {
    val settings = PodmanQuadletSettings.getInstance(project)
    val experimental = ExperimentalSettings.getInstance(project)
    enabledCheckbox?.isSelected = settings.state.enabled
    grammarEngineCheckbox?.isSelected = experimental.state.useGrammarParseEngine
    underlineGrammarKeysCheckbox?.isSelected = experimental.state.underlineGrammarEngineKeys
  }
}
