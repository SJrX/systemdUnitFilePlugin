java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("commons-io:commons-io:2.17.0")
  testImplementation("junit:junit:4.13.2")
}
