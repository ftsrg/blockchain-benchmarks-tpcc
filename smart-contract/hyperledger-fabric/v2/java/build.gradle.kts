import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.example"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.0")
    implementation("org.json:json:20230227")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.mockito:mockito-core:2.28.2")
}

application {
    mainClass.set("org.hyperledger.fabric.contract.ContractRouter")
}

tasks.named<ShadowJar>("shadowJar") {
   archiveBaseName.set("chaincode")
   archiveClassifier.set("")
   archiveVersion.set("")
}
