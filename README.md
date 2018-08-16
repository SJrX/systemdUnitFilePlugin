# Unit File Support

## About

This plugin adds support for [systemd unit files](https://www.freedesktop.org/software/systemd/man/systemd.unit.html#) to IntelliJ. It largely came about when working with Ansible (see [YAML/Ansible Support](https://plugins.jetbrains.com/plugin/7792-yaml-ansible-support) plugin), and trying to learn more about systemd. I would be surprised if anyone actually found a use for this, but heres hoping.

The main goals were to actually learn more about plugin development, and refresh my Java knowledge, as well as learn a bit about systemd.  

## Features🤞
 1. Syntax highlighting for unit files  
 2. Inspections to detected problems in unit files
      * Detecting whether a given key is invalid which causes systemd to log a warning.

## Installation

At some point, this plugin should be available at the [JetBrains Plugin Repository](https://plugins.jetbrains.com/).

### Manual Installation

Simply clone the repository, and run 

```bash
./gradlew buildPlugin 
```

Then in IntelliJ navigate to 
```bash
Plugins > Install Plugins From Disk > build/distributions/systemdUnitFilePlugin-X.X-SNAPSHOT.zip
```
 
 

 
