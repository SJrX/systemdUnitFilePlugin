plugins {
  id("com.gradle.develocity") version "4.5.0"
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
    // 'termsOfUseAgree' must be exactly "yes" or left unset - the plugin rejects "no".
    // Only agree (and publish) when opted in; otherwise leave it unset so a stray
    // --scan degrades to a soft "terms not agreed" notice rather than a hard error.
    if (buildScanOptIn) {
      termsOfUseAgree = "yes"
    }
    publishing.onlyIf { buildScanOptIn }
  }
}