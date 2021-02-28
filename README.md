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

Changelog
---------

### [v0.2.6](https://github.com/SJrX/systemdUnitFilePlugin/commit/06716d1e84ec60cb28c39cf1896abe9c6640133e)
* Improved Error Handling, Memory Management, Misc fixes ([Vlad Rassokhin](https://github.com/VladRassokhin)).

### [v0.2.5](https://github.com/SJrX/systemdUnitFilePlugin/commit/06716d1e84ec60cb28c39cf1896abe9c6640133e)
* Adds support for IntelliJ 2020.1

### [v0.2.4](https://github.com/SJrX/systemdUnitFilePlugin/commit/574cfbbc84470f543dd22c7d041838ea56fed30d)
* Add support for IntelliJ 2019.3
* Change logo of resources
* Add support for systemd v244

### [v0.2.3](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.2.3)
* Adds support for systemd v243 

### [v0.2.2](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.2.2)
* Add support for IntelliJ 2019.2

### [v0.2.1](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.2.1)
* Add support for IntelliJ 2019.1
* Add support for all deprecated / undocumented options.

### [v0.2.0](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.2.0)
* Add warning when whitespace exists between line continuation character and next new line.
* Refactored syntax highlighting to properly support comments between line continuations as added here [commit (2ca4d779)](https://github.com/systemd/systemd/commit/2ca4d779e021fdd94f4445980baa0aa8af6ffdc4).
* Add support for deprecated keys.
* Fixed typo in UnknownKeyInSection description.

### [v0.1.2](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.2)
* Fixed bug with tab characters in values.
* Regenerated documentation / inspections to work with [systemd v240](https://lists.freedesktop.org/archives/systemd-devel/2018-December/041852.html).

### [v0.1.1](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.1)
* Support for IntelliJ 2018.3

### [v0.1.0](https://github.com/SJrX/systemdUnitFilePlugin/releases/tag/v0.1.0)

* Initial release

Contributors
-------------
* [Vlad Rassokhin](https://github.com/VladRassokhin)

Acknowledgements
----------------
* The documentation is extracted from the systemd source code.
* A number of users of the [IntelliJ Plugin Developers Slack](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006494439--ANN-JetBrains-Slack-for-plugin-developers) provided a lot of useful advice.
* As with every project I work on the help and patient tutelage of members of [##java on Freenode](http://https://javachannel.org/).

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

