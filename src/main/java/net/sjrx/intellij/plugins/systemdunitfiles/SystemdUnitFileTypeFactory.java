package net.sjrx.intellij.plugins.systemdunitfiles;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import net.sjrx.intellij.plugins.systemdunitfiles.filetypes.ServiceFileType;
import org.jetbrains.annotations.NotNull;

public class SystemdUnitFileTypeFactory extends FileTypeFactory {

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(ServiceFileType.INSTANCE);
  }
}
