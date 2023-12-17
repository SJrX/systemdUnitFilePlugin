java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.15.1")
  testImplementation("junit:junit:4.13.2")
}
