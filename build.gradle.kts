import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

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
  id("org.jetbrains.intellij") version "1.13.2"
  id("org.jetbrains.grammarkit") version "2022.3"
  id("checkstyle")
  id("com.avast.gradle.docker-compose") version "0.16.11"
  id("org.jetbrains.kotlin.jvm") version "2.0.0"
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
  implementation("commons-io:commons-io:2.16.1")
  implementation("com.google.guava:guava:33.2.0-jre")
  testImplementation("junit:junit:4.13.2")
}

intellij {
  //version "LATEST-EAP-SNAPSHOT"
  version.set(properties("intellijVersion"))
}

val relativePath = "CHANGELOG"
val filePath = Paths.get(project.buildDir.path, relativePath)

// Check if the file exists and read its content or use a default string
val changeLogContents: String = if (Files.exists(filePath)) {
  filePath.toFile().readText()
} else {
  "Development Build"
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
      changeNotes.set(changeLogContents)
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

  checkstyleMain {
    dependsOn("generateDataFromManPages")
    dependsOn("generateUnitAutoCompleteData")
  }

  instrumentedJar {
    dependsOn("generateDataFromManPages")
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
