plugins {
  java
  id("org.springframework.boot") version "2.2.0.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  id("org.asciidoctor.convert") version "1.5.8"
}

group = "com.nhn"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.spring.io/milestone")
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val snippetsDir: String by extra("build/generated-snippets")

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
  implementation("org.springframework.boot.experimental:spring-boot-starter-data-r2dbc")
//  implementation("org.springframework.boot:spring-boot-starter-data-redis")
//  implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-rsocket")
//  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
//  implementation("org.springframework.boot:spring-boot-starter-websocket")
//  implementation("org.apache.kafka:kafka-streams")
//  implementation("org.springframework.kafka:spring-kafka")
  runtimeOnly("dev.miku:r2dbc-mysql")
  runtimeOnly("mysql:mysql-connector-java")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
  testImplementation("org.springframework.boot.experimental:spring-boot-test-autoconfigure-r2dbc")
  testImplementation("io.projectreactor:reactor-test")
//  testImplementation("org.springframework.kafka:spring-kafka-test")
  testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
  implementation(group = "io.netty", name = "netty-codec-http", version = "4.1.43.Final")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.boot.experimental:spring-boot-bom-r2dbc:0.1.0.M2")
  }
}

tasks {
  test {
    outputs.dir(snippetsDir)
    useJUnitPlatform()
  }

  asciidoctor {
    inputs.dir(snippetsDir)
  }
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_11
}