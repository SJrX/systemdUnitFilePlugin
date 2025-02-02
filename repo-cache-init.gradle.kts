val nexusRepo = System.getenv("NEXUS_REPO")

if (nexusRepo != null) {
  println("🔗 Using Nexus repo at: $nexusRepo")
  allprojects {
    buildscript {
      repositories {
        maven { url = uri("$nexusRepo/repository/gradle-plugin/") }
      }
    }
    repositories {
      maven { url = uri("$nexusRepo/repository/maven-central/") }
    }
  }
} else {
  println("⚠️ NEXUS_REPO is not set. Using default repositories.")
}
