plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "dev.hyune"
version = "0.0.1-SNAPSHOT"

dependencies {
    // Parser 모듈들
    implementation(project(":parse:parse-core"))
    implementation(project(":parse:kokoa"))
    implementation(project(":parse:maver"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
