package net.sjrx.intellij.plugins.systemdunitfiles;

import com.intellij.lang.Language;

public class SystemdUnitFileLanguage extends Language {

  public static final SystemdUnitFileLanguage INSTANCE = new SystemdUnitFileLanguage();

  private SystemdUnitFileLanguage() {
    super("Systemd Unit File");
  }
}
