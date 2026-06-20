plugins {
  id("com.gradle.develocity") version "4.4.3"
}

rootProject.name = "systemdUnitFilePlugin"

// Build scans upload build + environment data to Gradle's public service and are
// governed by https://gradle.com/help/legal-terms-of-use . We therefore only
// accept the terms and publish a scan when explicitly opted in via -PbuildScan
// (e.g. from CI). Local and contributor builds neither accept the terms nor upload.
val buildScanOptIn = providers.gradleProperty("buildScan").isPresent

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = if (buildScanOptIn) "yes" else "no"
    publishing.onlyIf { buildScanOptIn }
  }
}