import org.gradle.internal.os.OperatingSystem
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter.*

fun properties(key: String) = project.findProperty(key).toString()

fun getVersionNumber() : String {
  val major = properties("pluginMajorVersion")
  val minor =  LocalDate.now().format(ofPattern("yyMMdd"))
  val build = System.getenv("BUILD_NUMBER") ?: 3


  val branchName = System.getenv("BRANCH_NAME")  ?: "undefined"

  if (branchName.matches(Regex("""[0-9]{3}\.x"""))) {
    return "${major}.${minor}.${build}"
  } else {
    return "${major}.${minor}.${build}-$branchName"
  }

}

plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.13.3"
  id("org.jetbrains.grammarkit") version "2022.3"
  id("checkstyle")
  id("com.avast.gradle.docker-compose") version "0.16.11"
  id("org.jetbrains.kotlin.jvm") version "1.7.21"
  id("idea")
}

dockerCompose {
  dockerComposeWorkingDirectory.set(file("./systemd-build/"))
}

group = "net.sjrx.intellij.plugins"
version = getVersionNumber()

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }

}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.11.0")
  implementation("com.google.guava:guava:31.1-jre")
  testImplementation("junit:junit:4.13.2")
}

intellij {
  //version "LATEST-EAP-SNAPSHOT"
  version.set(properties("intellijVersion"))
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}

tasks {
  patchPluginXml {
      changeNotes.set("""
    <h3>v0.3.10</h3>
      <ul>
        <li>Add support for IntelliJ 2022.3 and Update systemd data for v252 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/150">#150</a></li>
      </ul>
    <h3>v0.3.9</h3>
      <ul>
        <li>Add support for IntelliJ 2022.2 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/143">#143</a></li>
      </ul>
    <h3>v0.3.8</h3>
      <ul>
        <li>Fix weak warning for absolute paths when using allowed prefix <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/141">#141</a></li>
      </ul>
    <h3>v0.3.7</h3>
      <ul>
        <li>Update build to be based off of systemd v251 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/131">#131</a></li>
        <li>Fix NPE in some cases when getting doc link <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/114">#114</a></li>
        <li>Validation warning when shell meta-characters are in an Exec directive <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/60">#60</a></li>
        <li>Add support for auto-completion / validation of Unit options <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/102">#102</a></li>
        <li>Add weak warning when an absolute path isn"t used on an Exec directive<a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/133">#133</a></li>
        <li>Fix missing documentation for Condition= and Assert directives <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/96">#96</a></li>
      </ul>
    <h3>v0.3.6</h3>
      <ul>
        <li>Add support for Go Land 2022.1<a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/128">#128</a></li>
      </ul>
    <h3>v0.3.5</h3>
      <ul>
        <li>Add support for IntelliJ 2022.1 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/126">#126</a></li>
      </ul>
    <h3>v0.3.4</h3>
      <ul>
        <li>Add support for IntelliJ 2021.3 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/119">#119</a></li>
        <li>Remove support for IntelliJ 2021.1, due to plugin compatibility fixes</li>
        <li>Fix file type issue description <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/106"/>#106</a></li>
        <li>Fix deprecation warnings <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/123"/>#123</a></li>
      </ul>
    <h3>v0.3.3</h3>
      <ul>
        <li>Add support for IntelliJ 2021.2 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/111">#111</a></li>
      </ul>
    <h3>v0.3.2</h3>
      <ul>
        <li>Add support for IntelliJ 2021.1 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/107">#107</a></li>"
      </ul>
    <h3>v0.3.1</h3>
      <ul>
        <li>Add support for IntelliJ 2020.3 and systemd v247 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issue-103">#103</a> (drops support for 2020.2 <a href="https://blog.jetbrains.com/platform/2020/09/intellij-project-migrates-to-java-11/">due to Java 11 Upgrade</a>) </li>
      </ul>
    <h3>v0.3.0</h3>
      <ul>
        <li>Add support for IntelliJ 2020.2 (and drops support for anything before IntelliJ 2020.1) <a href="https://github.com/SJrX/systemdUnitFilePlugin/pull/92">#92</a></li>
        <li>Add support for systemd v245 (and maybe a little bit of v246) <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/95">#95</a></li>
      </ul>
    <h3>v0.2.6</h3>
    <ul>
      <li>Improved Error Handling, Memory Management, Misc fixes <a href="https://github.com/SJrX/systemdUnitFilePlugin/pull/88">#88></a></li>
      <li>New plugin logo <a href="https://github.com/SJrX/systemdUnitFilePlugin/pull/92">#92</a></li>
    </ul>
    <h3>v0.2.5</h3>
    <ul>
      <li>Add support IntelliJ 2020.1 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/86">#86</a>
    </ul>
    <h3>v0.2.5</h3>
    <ul>
      <li>Add support IntelliJ 2020.1 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/86">#86</a>
    </ul>
    <h3>v0.2.4</h3>
    <ul>
      <li>Add support for systemd v244 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/85">#85</a></li>
      <li>Change Logo<a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/84">#84</a></li>
      <li>Add support IntelliJ 2019.3 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/82">#82</a>
    </ul>
    <h3>v0.2.3</h3>
    <ul>
      <li>Add support for systemd v243 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/79">#79</a></li>
    </ul>
    <h3>v0.2.2</h3>
    <ul>
      <li>Add support for IntelliJ 2019.2 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/76">#76</a></li>
    </ul>
    <h3>v0.2.1</h3>
    <ul>
      <li>Add support for IntelliJ 2019.1 <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/73">#73</a></li>
      <li>Add support for <em>all</em> deprecated / undocumented options <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/67">#67</a>
      , <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/71">#71</a></li>
    </ul>
    <h3>v0.2.0</h3>
    <ul>
      <li>Updating syntax highlighting to properly handle comments with line continuations.</li>
      <li>Warning when line continuation character is followed by whitespace</li>
      <li>New inspection for deprecated options</li>
    <h3>v0.1.2</h3>
    <ul>
      <li>Added support for systemd v240 keywords.</li>
      <li>Fixed bug with tab character causing issues with syntax highlighting <a href="https://github.com/SJrX/systemdUnitFilePlugin/issues/51">#51</a>.</li>
    </ul>
    <h3>v0.1.1</h3>
    <ul>
      <li>Added support for IntelliJ 2018.3.</li>
    </ul>
    <h3>v0.1.0</h3>
    <ul>
      <li>Initial Release.</li>
    </ul>
  """)
      sinceBuild.set(properties("sinceVersion"))
      untilBuild.set(properties("untilVersion"))
  }
}


// Add generated sources root

sourceSets {
  main {
    java {
      srcDirs("src/main/gen")
    }
  }
}

idea {
  module {
    generatedSourceDirs.add(file("src/main/gen"))

    setDownloadJavadoc(true)
    setDownloadSources(true)
  }
}

/*
 * CI Tasks
 */
checkstyle {
  // Exclude the generated sources
  toolVersion = "8.11"
}

tasks {
  checkstyleMain {
    source = fileTree("src/main/java")
  }
}


tasks {
   test {
     testLogging.showExceptions = true
     testLogging.setExceptionFormat("full")
   }
}

tasks {
  buildSearchableOptions {
    enabled = false
  }
}

/*
 * Build Tasks
 */
tasks.register<GenerateDataFromManPages>("generateDataFromManPages") {
  description = "Regenerate semantic data (used for documentation and inspections) by parsing the documentation from systemd git repository"
  group = "generation"

  systemdSourceCodeRoot = file("./systemd-build/build/")
  generatedJsonFileLocation =
    file(sourceSets["main"].output.resourcesDir?.getAbsolutePath() + "/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata")
}
/*
 * Lexing / Parsing and Grammar Tasks
 */
// import is optional to make task creation easier


tasks.register<GenerateLexerTask>("generateLexerTask") {
  description = "Generate the lexer necessary for parsing unit files using JFlex"
  group = "generation"

  source.set("src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/lexer/SystemdUnitFile.flex")
  targetDir.set("src/main/gen/net/sjrx/intellij/plugins/systemdunitfiles/generated/")
  targetClass.set("UnitFileLexer")
  purgeOldFiles.set(true)

  mustRunAfter(tasks.compileJava)
}

tasks.register<GenerateParserTask>("generateParserTask") {
  description = "Generate the grammar necessary for parsing unit files using GrammarKit"
  group = "generation"
  source.set("src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/grammar/SystemdUnitFile.bnf")
  targetRoot.set("src/main/gen/")

  // path to a parser file, relative to the targetRoot
  pathToParser.set("net/sjrx/intellij/plugins/systemdunitfiles/generated/UnitFileParser.java")

  // path to a directory with generated psi files, relative to the targetRoot
  pathToPsiRoot.set("net/sjrx/intellij/plugins/systemdunitfiles/psi/")

  purgeOldFiles.set(true)

  dependsOn("generateLexerTask")

}


tasks.register<Copy>("generateOptionValidator") {
  from("./systemd-build/build/load-fragment-gperf.gperf")
  into("${sourceSets["main"].output.resourcesDir?.getAbsolutePath()}/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/")
}


tasks {
  runIde {
    jvmArgs("-XX:+UnlockDiagnosticVMOptions")
  }
}

//classes.dependsOn += generateParserTask

tasks {
  classes {
    dependsOn("generateOptionValidator")
  }
}


if (!(project.file("./systemd-build/build/load-fragment-gperf.gperf").exists())) {
  println("Could not find metadata file")
  tasks.named("generateOptionValidator") {
    dependsOn("composeUp")
  }
}

if (!(project.file("./systemd-build/build/man").exists())) {
  println("Could not find man pages")

  tasks.named("generateDataFromManPages") {
    dependsOn("composeUp")
  }
}

tasks.register<Copy>("generateUnitAutoCompleteData") {
  from("./systemd-build/build/ubuntu-units.txt")
  into("${sourceSets["main"].output.resourcesDir?.getAbsolutePath()}/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/")
}



if (!(project.file("./systemd-build/build/ubuntu-units.txt").exists())) {
  println("Could not find ubuntu units")

  tasks.named("generateUnitAutoCompleteData") {
    dependsOn("composeUp")
  }
}

tasks {
  jar {
    dependsOn("generateDataFromManPages")
    dependsOn("generateOptionValidator")
    dependsOn("generateUnitAutoCompleteData")
  }

  compileTestKotlin {
    dependsOn("generateUnitAutoCompleteData")
    dependsOn("generateDataFromManPages")
  }

  compileTestJava {
    dependsOn("generateUnitAutoCompleteData")
    dependsOn("generateDataFromManPages")
  }
}


if (hasProperty("buildScan")) {
  extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
  }
}
