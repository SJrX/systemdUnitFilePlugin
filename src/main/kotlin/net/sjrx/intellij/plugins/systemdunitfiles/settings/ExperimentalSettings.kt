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
     *
     * Release 1 of the rollout (GitHub #467) flips this default to `true` so every project that has
     * not explicitly opted out lands on the new engine. The old engine remains reachable as an escape
     * hatch: the settings checkbox, or the "Switch back to legacy validation" action on
     * [NewGrammarEngineEditorNotificationProvider]. Once the bake period is over the old engine and
     * this flag are removed and the new engine becomes unconditional.
     */
    var useGrammarParseEngine: Boolean = true

    /**
     * Set once the user has dismissed (or acted on) the "you are now on the new grammar engine"
     * banner shown by [NewGrammarEngineEditorNotificationProvider], so it does not reappear on every
     * unit file they open. Independent of [useGrammarParseEngine]: a user can dismiss the banner while
     * staying on the new engine.
     */
    var newEngineBannerDismissed: Boolean = false

    /**
     * Underline the KEY of every option whose value is backed by a grammar validator
     * ([net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator]), a debug aid for seeing which keys the new engine covers. Independent of
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
