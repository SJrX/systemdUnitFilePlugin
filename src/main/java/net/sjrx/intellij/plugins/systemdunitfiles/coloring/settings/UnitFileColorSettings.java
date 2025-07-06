package net.sjrx.intellij.plugins.systemdunitfiles.coloring.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import net.sjrx.intellij.plugins.systemdunitfiles.UnitFileIcon;
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
    new AttributesDescriptor("Text", UnitFileHighlighter.TEXT),
    new AttributesDescriptor("Constant", UnitFileHighlighter.CONSTANT),
    new AttributesDescriptor("Number", UnitFileHighlighter.NUMBER),
    new AttributesDescriptor("Operator", UnitFileHighlighter.NUMBER),
  };

  @Nullable
  @Override
  public Icon getIcon() {
    return UnitFileIcon.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new UnitFileHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    // language="unit file (systemd)"
    return """
      # /etc/systemd/system/webapp.service
      # Comprehensive systemd unit for a Python-based web application
      
      [Unit]
      Description=Example Python Web Application
      Documentation=https://example.com/docs/webapp
      After=network.target postgresql.service
      Requires=postgresql.service
      
      [Service]
      Type=simple
      
      # Start the Python web app
      ExecStart=/usr/bin/python3 /opt/webapp/app.py --port=8080 --env=production
      
      # Reload the app if it supports SIGHUP
      ExecReload=/bin/kill -HUP $MAINPID
      
      # Gracefully stop the app
      ExecStop=/bin/kill -TERM $MAINPID
      
      # Drop privileges
      User=webapp
      Group=webapp
      
      # Set working directory
      WorkingDirectory=/opt/webapp
      
      # Environment configuration
      Environment=APP_ENV=production
      Environment=PORT=8080
      EnvironmentFile=-/etc/webapp/env
      
      # File descriptor and memory limits
      LimitNOFILE=65536
      MemoryMax=1G
      
      # Restart logic
      Restart=on-failure
      RestartSec=3s
      
      # Security hardening
      NoNewPrivileges=true
      ProtectSystem=full
      ProtectHome=yes
      PrivateTmp=true
      PrivateDevices=true
      ProtectControlGroups=true
      ProtectKernelModules=true
      CapabilityBoundingSet=
      RestrictRealtime=true
      
      # Network access control
      IPAddressAllow=10.0.0.0/16
      IPAddressDeny=any
      
      # Logging
      StandardOutput=journal
      StandardError=journal
      SyslogIdentifier=webapp
      
      # Uncomment below if your app uses systemd notifications
      # Type=notify
      # WatchdogSec=60
      
      [Install]
      WantedBy=multi-user.target
      Alias=webapp.service
      """;
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
