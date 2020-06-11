import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.asciidoctor.convert") version "1.5.9.2"
    id("maven-publish")
    id("io.gitlab.arturbosch.detekt").version("1.9.1")
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
}

val snippetsDir by extra { file("build/generated-snippets") }

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        outputs.dir(snippetsDir)
    }

    asciidoctor {
        inputs.dir(snippetsDir)
        dependsOn(test)
    }

    bootJar {
        dependsOn(asciidoctor)
        into("static/docs") {
            from("build/asciidoc/html5")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("bootJava") {
            artifact(tasks.getByName("bootJar"))
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/autotraderuk/chaos-kraken")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

detekt {
    config = files("detekt-config.yml")
    buildUponDefaultConfig = true
}
