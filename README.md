Unit File Support for systemd
-----------------------------

## Introduction

This plugin adds support for [systemd unit files](https://www.freedesktop.org/software/systemd/man/systemd.unit.html#) to IntelliJ. 

## Features
 * Syntax highlighting for unit files.
 * Auto-completion for:
   * Option names in a section
   * Boolean options
   * Octal options
   * Some other common options (e.g., **KillMode=**, **Type=**, **Restart=**)
   * Unit names in dependencies (e.g., **WantedBy=**, **Requires**)
 * Inspections
   * Invalid value for argument.
   * Shell syntax detected in **Exec___=** call.
   * Unknown option in section (ignoring those starting with **X-**)
 * Annotations
   * When the section name is invalid or used in the wrong file type
   * Whitespace after a line continuation \ character.
   * When a key has been deprecated.
 * Built-in documentation for options or section name (available when hitting <kbd>CTRL+Q</kbd> or <kbd>F1</kbd> on Mac)   

      
## Usage
To create a file simply right click on a folder and <kbd>New</kbd> > <kbd>File</kbd>, and enter a file name ending any of:
 * `.automount`
 * `.device`
 * `.mount`
 * `.path`
 * `.service`
 * `.slice`
 * `.socket`
 * `.swap`
 * `.target` 
 * `.timer` 
 
The file should then be associated with this plugin and the above features should work.
 
__NOTE__: `.scope` units are not configured via unit configuration files and so we don't support them. 

## Installation

This plugin is avaliable to install at the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/11070-unit-file-support-systemd-).

Contributors
-------------
* [Vlad Rassokhin](https://github.com/VladRassokhin)

Acknowledgements
----------------
* The documentation is extracted from the systemd source code.
* A number of users of the [IntelliJ Plugin Developers Slack](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006494439--ANN-JetBrains-Slack-for-plugin-developers) provided a lot of useful advice.
* As with every project I work on the help and patient tutelage of members of [##java on Libera.Chat](https://javachannel.org/).

License
-----------------
* Everything but the logo is GPLv2.
* Logo is CC-BY-SA, as it is a derivative work from the [brand repository](https://github.com/systemd/brand.systemd.io).

## Development Notes

### System Requirements

On top of Java 11, you will need the [m4 macro processor](https://www.gnu.org/software/m4/m4.html) installed on your system and available on the path. 

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

When starting the development IDE if the fonts are poor add the following **ENVIRONMENT VARIABLE** to the run configuration

```bash
_JAVA_OPTIONS=-Dawt.useSystemAAFontSettings\=lcd -Dsun.java2d.renderer\=sun.java2d.marlin.MarlinRenderingEngine
```

Note: If you are using the Gradle *runIde* task it must be passed as an environment variable and not a VM option, because it gets executed in a different VM.

#### SLF4J Errors when running tests

If you see errors related to duplicate SLF4J class bindings available switch the JRE in use from the Default which references IntelliJ, to a native one.  

