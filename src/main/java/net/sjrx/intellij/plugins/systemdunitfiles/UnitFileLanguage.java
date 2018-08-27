package net.sjrx.intellij.plugins.systemdunitfiles;

import com.intellij.lang.Language;

public class UnitFileLanguage extends Language {

  public static final UnitFileLanguage INSTANCE = new UnitFileLanguage();

  private UnitFileLanguage() {
    super("Unit File (systemd)");
  }
}
