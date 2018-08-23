package net.sjrx.intellij.plugins.systemdunitfiles;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.AutomountFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.DeviceFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.MountFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.PathFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.SliceFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.SocketFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.SwapFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.TargetFileType;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.TimerFileType;
import org.jetbrains.annotations.NotNull;

public class SystemdUnitFileTypeFactory extends FileTypeFactory {

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(AutomountFileType.INSTANCE);
    consumer.consume(DeviceFileType.INSTANCE);
    consumer.consume(MountFileType.INSTANCE);
    consumer.consume(PathFileType.INSTANCE);
    consumer.consume(ServiceFileType.INSTANCE);
    consumer.consume(SliceFileType.INSTANCE);
    consumer.consume(SocketFileType.INSTANCE);
    consumer.consume(SwapFileType.INSTANCE);
    consumer.consume(TargetFileType.INSTANCE);
    consumer.consume(TimerFileType.INSTANCE);
  }
}
