import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileReader
import java.util.*

plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.epages.restdocs-api-spec") version "0.18.2"
    jacoco
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    kotlin("kapt") version "1.9.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

//extra["snippetsDir"] = file("build/generated-snippets")
val snippetsDir by extra { file("build/generated-snippets") }
val asciidoctorExt: Configuration by configurations.creating

dependencies {
    implementation("org.json:json:20230227")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
    kaptTest("org.mapstruct:mapstruct-processor:1.5.5.Final")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
    //kotlin-jdsl
    val jdslVersion = "2.2.1.RELEASE"
    implementation("com.linecorp.kotlin-jdsl:hibernate-kotlin-jdsl-jakarta:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:spring-data-kotlin-jdsl-starter-jakarta:$jdslVersion")
    implementation("org.hibernate:hibernate-core:6.2.4.Final")
    //validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    //kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2") // kotlin junit 처럼 쓸 수 있는 Spec 들이 정의 됨
    testImplementation("io.kotest:kotest-assertions-core:5.6.2") // shouldBe... etc 와같이 Assertions 의 기능을 제공
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3") // spring boot test 를 위해서 추가
    //mkckk
    testImplementation("io.mockk:mockk:1.13.5")
    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    //sentry
    implementation ("io.sentry:sentry-spring-boot-starter-jakarta:6.25.2")
    //slack
    implementation ("com.slack.api:slack-api-client:1.30.0")
}
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

val properties = Properties()
val reader = FileReader(project.rootProject.file("src/main/resources/application-prod.yml"))
properties.load(reader)

val swaggerUrl: String = properties.getProperty("swagger_url")

openapi3 {
    this.setServer("")
    title = "API Documentation"
    description = "로그인과 회원가입 이외에는 JWT가 필요합니다(Access token). 'Authorize' 버튼을 눌러 JWT를 등록해주세요. JWT는 /members/login 에서 받을 수 있습니다. 이후 access token이 만료되었을 경우에는 /members/token 에서 refresh token을 통해 재발급 받을 수 있습니다."
    version = "0.0.1-SNAPSHOT"
    format = "yaml"
//    outputDirectory = "src/main/resources/static"
//    outputFileNamePrefix = "swagger"
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    outputs.dir(snippetsDir)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

// 생성된 OAS파일을 Swagger 디렉터리로 복사
tasks.register("copyOasToSwagger", Copy::class) {
    delete("src/main/resources/static/swagger-ui/openapi3.yaml") // 기존 OAS 파일 삭제
    from("build/api-spec/openapi3.yaml") // 복제할 OAS 파일 지정
    into("src/main/resources/static/swagger-ui/.") // 타겟 디렉터리로 파일 복제
    dependsOn("openapi3") // openapi3 Task가 먼저 실행되도록 설정
}

tasks.build { // 4
    dependsOn(tasks.getByName("copyOasToSwagger"))
}

tasks.bootJar { // 5
}
