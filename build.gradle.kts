plugins { id("io.vacco.common-build") version "0.5.3" }

group = "io.vacco.java-express"
version = "0.2.1"

configure<io.vacco.common.CbPluginProfileExtension> {
  addJ8Spec(); addPmd(); addSpotBugs()
  setPublishingUrlTransform { repo -> "${repo.url}/${project.name}" }
  sharedLibrary()
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
}
