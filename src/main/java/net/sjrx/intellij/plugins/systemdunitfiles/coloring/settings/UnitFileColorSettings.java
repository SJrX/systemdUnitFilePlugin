package net.sjrx.intellij.plugins.systemdunitfiles.coloring.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import net.sjrx.intellij.plugins.systemdunitfiles.SystemdUnitFileIcon;
import net.sjrx.intellij.plugins.systemdunitfiles.coloring.UnitFileHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

public class UnitFileColorSettings implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{

    new AttributesDescriptor("Section", UnitFileHighlighter.SECTION),
    new AttributesDescriptor("Key", UnitFileHighlighter.KEY),
    new AttributesDescriptor("Separator", UnitFileHighlighter.SEPARATOR),
    new AttributesDescriptor("Value", UnitFileHighlighter.VALUE),
  };

  @Nullable
  @Override
  public Icon getIcon() {
    return SystemdUnitFileIcon.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new UnitFileHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "#  SPDX-License-Identifier: LGPL-2.1+\n"
           + "#\n"
           + "#  This file is part of systemd.\n"
           + "#\n"
           + "#  systemd is free software; you can redistribute it and/or modify it\n"
           + "#  under the terms of the GNU Lesser General Public License as published by\n"
           + "#  the Free Software Foundation; either version 2.1 of the License, or\n"
           + "#  (at your option) any later version.\n"
           + "\n"
           + "[Unit]\n"
           + "Description=Reload Configuration from the Real Root\n"
           + "DefaultDependencies=no\n"
           + "Requires=initrd-root-fs.target\n"
           + "After=initrd-root-fs.target\n"
           + "OnFailure=emergency.target\n"
           + "OnFailureJobMode=replace-irreversibly\n"
           + "ConditionPathExists=/etc/initrd-release\n"
           + "\n"
           + "[Service]\n"
           + "Type=oneshot\n"
           + "ExecStartPre=-/usr/bin/systemctl daemon-reload\n"
           + "; we have to retrigger initrd-fs.target after daemon-reload\n"
           + "ExecStart=-/usr/bin/systemctl --no-block start initrd-fs.target\n"
           + "ExecStart=/usr/bin/systemctl --no-block start initrd-cleanup.service\n";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Unit Files (systemd)";
  }

}
