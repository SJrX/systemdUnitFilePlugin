package net.sjrx.intellij.plugins.systemdunitfiles.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "PodmanQuadletSettings", storages = [Storage("podmanQuadlet.xml")])
class PodmanQuadletSettings : PersistentStateComponent<PodmanQuadletSettings.State> {

  private var myState = State()

  class State {
    var enabled: Boolean = false
    var notificationDismissed: Boolean = false
  }

  override fun getState(): State = myState

  override fun loadState(state: State) {
    myState = state
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PodmanQuadletSettings {
      return project.getService(PodmanQuadletSettings::class.java)
    }
  }
}
