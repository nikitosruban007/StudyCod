plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.springframework.boot") version "3.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

springBoot {
    mainClass.set("org.example.StudyCod")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mainClass.set("org.example.StudyCod")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    doFirst {
        jvmArgs = listOf(
            "--module-path", configurations.runtimeClasspath.get().asPath,
            "--add-modules", "javafx.controls,javafx.fxml,javafx.web,javafx.swing,javafx.media"
        )
    }

    mainClass.set("org.example.StudyCod")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.1.0"))
    implementation("org.openjfx:javafx-controls:17.0.8")
    implementation("org.openjfx:javafx-fxml:17.0.8")
    implementation("org.openjfx:javafx-web:17.0.8")
    implementation("org.openjfx:javafx-swing:17.0.8")
    implementation("org.openjfx:javafx-media:17.0.8")
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }
    implementation("net.synedra:validatorfx:0.5.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("eu.hansolo:tilesfx:17.0.7") {
        exclude(group = "org.openjfx")
    }
    implementation("com.github.almasb:fxgl:17.3") {
        exclude(group = "org.openjfx")
    }
    implementation("org.fxmisc.richtext:richtextfx:0.11.6")
    implementation("org.hibernate.orm:hibernate-core:6.2.0.Final")
    implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation ("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("com.mysql:mysql-connector-j:9.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.security:spring-security-crypto:7.0.0-M3")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.json:json:20231013")
    implementation("org.fxmisc.richtext:richtextfx:0.11.2") // Совместимая версия
    implementation("com.itextpdf:itext-core:8.0.2") // Обновлено для совместимости с JDK 17
    testImplementation("ch.qos.logback:logback-classic:1.5.16")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

javafx {
    version = "17.0.8"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing", "javafx.media")
}