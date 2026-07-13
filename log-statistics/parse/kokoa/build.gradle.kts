plugins {
    java
}

group = "dev.hyune"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":parse:parse-core"))
    implementation("org.slf4j:slf4j-api:2.0.9")
}
