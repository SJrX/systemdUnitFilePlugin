Unit File Support for systemd
-----------------------------

## Introduction

This plugin adds support for [systemd unit files](https://www.freedesktop.org/software/systemd/man/systemd.unit.html#) to IntelliJ. 

## Features🤞
 * Syntax highlighting for unit files
 * Auto-completion for:
   * Option names in a section
   * Boolean options
   * Octal options
   * Some other common options (**KillMode=**, **Type=**, **Restart=**)
 * Inspections
   * Invalid values for boolean options
   * Unknown option in section (ignoring those starting with **X-**)
 * Annotations
   * When the section name is invalid.
   * Whitespace after a line continuation \ character.
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

At some point, this plugin should be available at the [JetBrains Plugin Repository](https://plugins.jetbrains.com/),
 in the next release we will update this.

Changelog
--------- 

### [v0.2.0](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.2.0)

* Add warning when whitespace exists between line continuation character and next new line.
* Refactored syntax highlighting to properly support comments between line continuations as added here [commit (2ca4d779)](https://github.com/systemd/systemd/commit/2ca4d779e021fdd94f4445980baa0aa8af6ffdc4).
* Add support for deprecated keys.

### [v0.1.2](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.2)
* Fixed bug with tab characters in values.
* Regenerated documentation / inspections to work with [systemd v240](https://lists.freedesktop.org/archives/systemd-devel/2018-December/041852.html).

### [v0.1.1](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.1)
* Support for IntelliJ 2018.3

### [v0.1.0](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.0)

* Initial release


Acknowledgements
----------------
* The documentation is extracted from the systemd source code.
* A number of users of the [IntelliJ Plugin Developers Gitter Room](https://gitter.im/IntelliJ-Plugin-Developers/Lobby) provided a lot of useful advice.
* As with every project I work on the help and patient tutelage of members of [##java on Freenode](http://https://javachannel.org/).
 
## Development Notes

### System Requirements

On top of Java 8, you will need the [m4 macro processor](https://www.gnu.org/software/m4/m4.html) installed on your system and available on the path. 

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

