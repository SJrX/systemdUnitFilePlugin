# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliJ plugin providing language support for systemd unit files (.service, .mount, .timer, .socket, .path, .automount, .swap, .slice, .device, .target, .nspawn). Written in Kotlin and Java, using Grammar-Kit for parsing.

## Build Commands

```bash
./gradlew buildPlugin          # Build plugin distribution zip
./gradlew test                 # Run all tests
./gradlew test --tests "net.sjrx.intellij.plugins.systemdunitfiles.lexer.UnitFileLexerTest"  # Single test class
./gradlew runIde               # Launch dev IDE with plugin loaded
./gradlew checkstyleMain       # Run checkstyle (excludes generated code)
./gradlew generateLexerTask    # Regenerate lexer from .flex grammar
./gradlew generateParserTask   # Regenerate parser from .bnf grammar
./gradlew generateDataFromManPages  # Parse systemd XML man pages → JSON semantic data
```

The metadata generation pipeline uses Docker: `docker compose --project-directory ./systemd-build up --build`

## Architecture

**Grammar & Parsing:** The lexer is defined in `src/main/resources/.../lexer/SystemdUnitFile.flex` (JFlex) and the parser grammar in `src/main/resources/.../grammar/SystemdUnitFile.bnf`. Generated code goes to `src/main/gen/`.

**Semantic Data Pipeline:** Docker builds systemd from source, extracts man pages (XML) and gperf files. A custom Groovy task in `buildSrc/` (`GenerateDataFromManPages.groovy`) transforms XML via XSLT into JSON data and HTML documentation stored under `src/main/resources/.../semanticdata/`.

**Plugin Feature Modules** (all under `src/main/kotlin/net/sjrx/intellij/plugins/systemdunitfiles/`):
- `semanticdata/` — Core data models, validators, option value parsers, documentation repository. This is the foundational layer other features depend on.
- `completion/` — Auto-completion contributors for keys, values, and sections
- `inspections/` — Code inspections (InvalidValue, UnknownKey, Deprecated, MissingRequiredKey, ShellSyntax, IPAddress)
- `annotators/` — Real-time inline annotations (invalid sections, deprecated options, whitespace)
- `documentation/` — Inline documentation provider
- `filetypes/` — File type definitions for each supported extension
- `lexer/` — Lexical analysis adapter

**Java source** (`src/main/java/`) contains syntax highlighting (`coloring/`), parser definition (`parser/`), and generated PSI elements (`psi/`).

## Testing

Tests use JUnit 4 with IntelliJ Platform Test Framework. Test sources are in `src/test/kotlin/` with fixtures in `src/test/resources/`. Tests mirror the main source structure.

## Conventions

- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) format
- Java 21 for compilation, targets IntelliJ 2024.2 (platform version 242)
- Plugin version range: 242.0–270.0
- Generated code in `src/main/gen/` — do not edit manually; regenerate with grammar-kit tasks
