import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1"

	// auto update dependencies with 'useLatestVersions' task
//	id("se.patrikerdes.use-latest-versions") version "0.2.18"
//	id("com.github.ben-manes.versions") version "0.51.0"
}

dependencies {
	val jadxVersion = "1.5.2-SNAPSHOT"
	val isJadxSnapshot = jadxVersion.endsWith("-SNAPSHOT")

	// use compile only scope to exclude jadx-core and its dependencies from result jar

    compileOnly("io.github.skylot:jadx-core:$jadxVersion") {
        isChanging = false
    }
//	implementation("io.github.skylot:jadx-core:$jadxVersion")
	compileOnly("io.github.skylot:jadx-gui:$jadxVersion"){
        isChanging = false
    }

	testImplementation("io.github.skylot:jadx-smali-input:$jadxVersion") {
        isChanging = isJadxSnapshot
    }
	testImplementation("ch.qos.logback:logback-classic:1.5.9")
	testImplementation("org.assertj:assertj-core:3.26.3")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.2")


	implementation("org.jgrapht:jgrapht-core:1.5.1")
	implementation("com.github.jgraph:jgraphx:v4.0.4")
	implementation("com.fifesoft:rsyntaxtextarea:3.5.1")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
	implementation("dev.langchain4j:langchain4j-open-ai:0.36.2")
	implementation ("org.commonmark:commonmark:0.21.0")
	implementation ("org.apache.commons:commons-lang3:3.12.0")
}

repositories {
	mavenLocal()
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    google()
	maven(url = "https://jitpack.io")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//version = System.getenv("VERSION") ?: "dev"
version = "1.0"
tasks {
    withType(Test::class) {
        useJUnitPlatform()
    }
    val shadowJar = withType(ShadowJar::class) {
        archiveClassifier.set("") // remove '-all' suffix
    }

    // copy result jar into "build/dist" directory
    register<Copy>("dist") {
		group = "jadx-plugin"
        dependsOn(shadowJar)
        dependsOn(withType(Jar::class))

        from(shadowJar)
        into(layout.buildDirectory.dir("dist"))
    }
}
