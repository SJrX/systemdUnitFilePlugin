gradle.settingsEvaluated {
  buildCache {
    local {
      isEnabled = false
    }
    // vvv Your custom configuration goes here
    remote<HttpBuildCache> {
      url = uri(System.getenv("GRADLE_BUILD_CACHE_URL"))
      isAllowUntrustedServer = true
      isAllowInsecureProtocol = true
      isPush = true
    }
    // ^^^ Your custom configuration goes here
  }
}
