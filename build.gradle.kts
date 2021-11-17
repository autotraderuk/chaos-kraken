import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    id("org.asciidoctor.convert") version "1.5.9.2"
    id("io.gitlab.arturbosch.detekt").version("1.9.1")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_11.toString()
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
