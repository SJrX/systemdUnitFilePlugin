import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

fun properties(key: String) = project.findProperty(key).toString()

val JavaVersion = 21

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
  id("org.jetbrains.intellij.platform") version "2.18.1"
  id("org.jetbrains.grammarkit") version "2023.3.0.4"
  id("checkstyle")
  id("com.avast.gradle.docker-compose") version "0.17.21"
  id("org.jetbrains.kotlin.jvm") version "2.4.10"
  id("idea")
}

dockerCompose {
  dockerComposeWorkingDirectory.set(file("./systemd-build/"))
  setProjectName("systemdbuild")
}

group = "net.sjrx.intellij.plugins"
version = getVersionNumber()

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(JavaVersion))
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(JavaVersion))
  }

  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }

}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  implementation("commons-io:commons-io:2.22.0")
  implementation("com.google.guava:guava:33.7.1-jre")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.opentest4j:opentest4j:1.3.0")

  intellijPlatform {
    val type = providers.gradleProperty("platformType")
    val version = providers.gradleProperty("intellijVersion")

    create(type, version)

    pluginVerifier()
    testFramework(TestFrameworkType.Platform)
  }
}

val relativePath = "CHANGELOG"
val filePath = Paths.get(project.layout.buildDirectory.get().asFile.path, relativePath)

// Check if the file exists and read its content or use a default string
val changeLogContents: String = if (Files.exists(filePath)) {
  filePath.toFile().readText()
} else {
  "Development Build"
}


tasks {
  patchPluginXml {
      changeNotes.set(changeLogContents)
      sinceBuild.set(properties("sinceVersion"))
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
     // Forward the experimental grammar-engine flag so CI can run the whole suite twice: once on the
     // original engine and once with -Dsystemd.unit.grammarParseEngine=true (see GrammarOptionValue).
     systemProperty("systemd.unit.grammarParseEngine", System.getProperty("systemd.unit.grammarParseEngine", "false"))
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
  renderedXIncludesDir = project.layout.buildDirectory.dir("tmp/rendered-xincludes").get().asFile
}
/*
 * Lexing / Parsing and Grammar Tasks
 */
// import is optional to make task creation easier


tasks.register<GenerateLexerTask>("generateLexerTask") {
  description = "Generate the lexer necessary for parsing unit files using JFlex"
  group = "generation"

  sourceFile.set(file("src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/lexer/SystemdUnitFile.flex"))
  targetOutputDir.set(file("src/main/gen/net/sjrx/intellij/plugins/systemdunitfiles/generated/"))
  purgeOldFiles.set(true)

  mustRunAfter(tasks.compileJava)
}

tasks.register<GenerateParserTask>("generateParserTask") {
  description = "Generate the grammar necessary for parsing unit files using GrammarKit"
  group = "generation"
  sourceFile.set(file("src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/grammar/SystemdUnitFile.bnf"))
  targetRootOutputDir.set(file("src/main/gen/"))

  // path to a parser file, relative to the targetRoot
  pathToParser.set("net/sjrx/intellij/plugins/systemdunitfiles/generated/UnitFileParser.java")

  // path to a directory with generated psi files, relative to the targetRoot
  pathToPsiRoot.set("net/sjrx/intellij/plugins/systemdunitfiles/psi/")

  purgeOldFiles.set(true)

  dependsOn("generateLexerTask")

}


tasks.register<Copy>("generateOptionValidator") {
  listOf(
    "journald-gperf.gperf",
      "link-config-gperf.gperf",
      "load-fragment-gperf.gperf",
      "logind-gperf.gperf",
      "netdev-gperf.gperf",
      "networkd-gperf.gperf",
      "networkd-network-gperf.gperf",
      "nspawn-gperf.gperf",
      "resolved-dnssd-gperf.gperf",
      "resolved-gperf.gperf",
      "timesyncd-gperf.gperf").forEach {
    fileName -> from("./systemd-build/build/${fileName}")
  }

  into("${sourceSets["main"].output.resourcesDir?.getAbsolutePath()}/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/")
}

tasks.register("mergePodmanDocumentation") {
  description = "Merge podman quadlet documentation JSON into the generated sectionToKeywordMapFromDoc.json"
  group = "generation"

  val semanticDataDir = file("${sourceSets["main"].output.resourcesDir?.getAbsolutePath()}/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata")
  val podmanJsonFile = file("./src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/podman/podman-sectionToKeywordMapFromDoc.json")
  val targetJsonFile = file("${semanticDataDir}/sectionToKeywordMapFromDoc.json")
  val undocumentedJsonFile = file("${semanticDataDir}/undocumentedSectionToKeywordMap.json")

  inputs.file(podmanJsonFile)

  dependsOn("generateDataFromManPages")
  mustRunAfter("processResources", "generateOptionValidator", "generateUnitAutoCompleteData")

  doLast {
    val slurper = groovy.json.JsonSlurper()
    val sharedSections = listOf("Unit", "Install", "Service")

    // Merge documented keywords
    @Suppress("UNCHECKED_CAST")
    val mainData = slurper.parse(targetJsonFile) as MutableMap<String, Any>
    @Suppress("UNCHECKED_CAST")
    val podmanData = slurper.parse(podmanJsonFile) as MutableMap<String, Any>

    @Suppress("UNCHECKED_CAST")
    val unitFileClassData = mainData["unit"] as? Map<String, Any>
    @Suppress("UNCHECKED_CAST")
    val podmanNetworkData = podmanData.getOrDefault("podman_network", mutableMapOf<String, Any>()) as MutableMap<String, Any>
    if (unitFileClassData != null) {
      for (section in sharedSections) {
        val sectionData = unitFileClassData[section]
        if (sectionData != null) {
          podmanNetworkData[section] = sectionData
        }
      }
    }
    podmanData["podman_network"] = podmanNetworkData
    mainData.putAll(podmanData)

    val output = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(mainData))
    targetJsonFile.writeText(output)

    // Merge undocumented keywords (deprecated/moved options)
    @Suppress("UNCHECKED_CAST")
    val undocData = slurper.parse(undocumentedJsonFile) as MutableMap<String, Any>
    @Suppress("UNCHECKED_CAST")
    val unitUndocData = undocData["unit"] as? Map<String, Any>
    if (unitUndocData != null) {
      val podmanUndocData = mutableMapOf<String, Any>()
      for (section in sharedSections) {
        val sectionData = unitUndocData[section]
        if (sectionData != null) {
          podmanUndocData[section] = sectionData
        }
      }
      undocData["podman_network"] = podmanUndocData
    }

    val undocOutput = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(undocData))
    undocumentedJsonFile.writeText(undocOutput)
  }
}

tasks.register("generatePodmanNetworkGperf") {
  description = "Generate podman-network-gperf.gperf by merging systemd unit sections with podman quadlet network keys"
  group = "generation"

  val loadFragmentFile = file("./systemd-build/build/load-fragment-gperf.gperf")
  val podmanNetworkFile = file("./src/main/resources/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/podman/podman-network.gperf")
  val outputDir = file("${sourceSets["main"].output.resourcesDir?.getAbsolutePath()}/net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/")
  val outputFile = file("${outputDir}/podman-network-gperf.gperf")

  inputs.file(loadFragmentFile)
  inputs.file(podmanNetworkFile)
  outputs.file(outputFile)

  dependsOn("generateOptionValidator")

  doLast {
    val unitSections = setOf("Unit", "Install", "Service")
    val unitLines = loadFragmentFile.readLines().filter { line ->
      val trimmed = line.trim()
      trimmed.isNotEmpty() && unitSections.any { section -> trimmed.startsWith("$section.") }
    }
    val podmanLines = podmanNetworkFile.readLines()

    outputFile.parentFile.mkdirs()
    outputFile.writeText((unitLines + podmanLines).joinToString("\n") + "\n")
  }
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
    dependsOn("generatePodmanNetworkGperf")
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
    dependsOn("mergePodmanDocumentation")
    dependsOn("generateOptionValidator")
    dependsOn("generatePodmanNetworkGperf")
    dependsOn("generateUnitAutoCompleteData")
  }

  checkstyleMain {
    dependsOn("generateDataFromManPages")
    dependsOn("generateUnitAutoCompleteData")
  }

  instrumentedJar {
    dependsOn("generateDataFromManPages")
    dependsOn("mergePodmanDocumentation")
    dependsOn("generatePodmanNetworkGperf")
    dependsOn("generateUnitAutoCompleteData")
  }

  compileTestKotlin {
    dependsOn("generateUnitAutoCompleteData")
    dependsOn("generateDataFromManPages")
    dependsOn("mergePodmanDocumentation")
  }

  compileTestJava {
    dependsOn("generateUnitAutoCompleteData")
    dependsOn("generateDataFromManPages")
    dependsOn("mergePodmanDocumentation")
  }
}


tasks.register<org.jetbrains.intellij.platform.gradle.tasks.PublishPluginTask>("publishPluginStandalone") {
  token.set(System.getenv("PUBLISH_TOKEN"))
  // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
  // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
  // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
  channels.set(listOf(System.getenv("RELEASE_CHANNEL")?:"dev"))
  host.set("https://plugins.jetbrains.com")

  // Set the distribution file in gradle build to the archive file of the buildPlugin task
  archiveFile.set(project.file("build/distributions/${project.name}-${project.version}.zip"))
}
