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
 
## Development Notes

### System Requirements

On top of Java 8, you will need the [m4 macro processor](https://www.gnu.org/software/m4/m4.html) installed on your system and available in the path. 

### Manual Installation

Simply clone the repository, and run 

```bash
./gradlew buildPlugin 
```

Then in IntelliJ navigate to 
```bash
Plugins > Install Plugins From Disk > build/distributions/systemdUnitFilePlugin-X.X-SNAPSHOT.zip
```

### Problems

#### Weird Fonts In Linux

When starting the development IDE, if the fonts are poor add the following **ENVIRONMENT VARIABLE** to the run configuration

```bash
_JAVA_OPTIONS=-Dawt.useSystemAAFontSettings\=lcd -Dsun.java2d.renderer\=sun.java2d.marlin.MarlinRenderingEngine
```

Note: If you are using the Gradle *runIde* task it must be passed as an environment variable and not a VM option, because it gets executed in a different VM.

#### SLF4J Errors when running tests

If you see errors related to duplicate SLF4J class bindings available switch the JRE in use from the Default which references IntelliJ, to a native one.


 
