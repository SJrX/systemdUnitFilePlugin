package net.sjrx.intellij.plugins.systemdunitfiles

import com.intellij.openapi.util.IconLoader

object UnitFileIcon {

  @JvmField
  val FILE = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/systemd.svg", UnitFileIcon::class.java)
  val AUTOMOUNT = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/automount.svg", UnitFileIcon::class.java)
  val DEVICE = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/device.svg", UnitFileIcon::class.java)
  val MOUNT = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/mount.svg", UnitFileIcon::class.java)
  val PATH = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/path.svg", UnitFileIcon::class.java)
  val SERVICE = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/service.svg", UnitFileIcon::class.java)
  val SLICE = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/slice.svg", UnitFileIcon::class.java)
  val SOCKET = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/socket.svg", UnitFileIcon::class.java)
  val SWAP = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/swap.svg", UnitFileIcon::class.java)
  val TARGET = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/target.svg", UnitFileIcon::class.java)
  val TIMER = IconLoader.getIcon("/net/sjrx/intellij/plugins/systemdunitfiles/timer.svg", UnitFileIcon::class.java)
}
