package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * Opt-in flags for unfinished/experimental behaviour (GitHub #467).
 *
 * Kept separate from [PodmanQuadletSettings] so each experimental area owns its own storage; the
 * checkboxes are surfaced on the shared "systemd Unit Files" settings page.
 */
@Service(Service.Level.PROJECT)
@State(name = "SystemdUnitFileExperimentalSettings", storages = [Storage("systemdUnitFileExperimental.xml")])
class ExperimentalSettings : PersistentStateComponent<ExperimentalSettings.State> {

  private var myState = State()

  class State {
    /**
     * Use the new list-of-successes grammar engine (Combinator.parse / validate) for value
     * validation instead of the original SyntacticMatch/SemanticMatch path.
     */
    var useGrammarParseEngine: Boolean = false

    /**
     * Underline the KEY of every option whose value is backed by a grammar validator
     * ([GrammarOptionValue]), a debug aid for seeing which keys the new engine covers. Independent of
     * [useGrammarParseEngine]: the grammar validators exist in the registry regardless of which
     * validation path is active, so this can be toggled on its own.
     */
    var underlineGrammarEngineKeys: Boolean = false
  }

  override fun getState(): State = myState

  override fun loadState(state: State) {
    myState = state
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): ExperimentalSettings {
      return project.getService(ExperimentalSettings::class.java)
    }
  }
}
