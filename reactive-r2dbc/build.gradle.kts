plugins {
  java
  id("org.springframework.boot") version "2.2.0.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
}
group = "com.nhn"
version = "1.0-snapshot"

buildscript {
  repositories {
    jcenter()
  }
}

repositories {
  mavenCentral()
  maven("https://repo.spring.io/milestone")
}

dependencies {
  implementation("org.springframework.boot.experimental:spring-boot-starter-data-r2dbc")
  implementation("io.r2dbc:r2dbc-client:1.0.0.M7")
  implementation("com.github.jasync-sql:jasync-r2dbc-mysql:1.0.11")
//  implementation("dev.miku:r2dbc-mysql")
//    runtimeonly("mysql:mysql-connector-java")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }

  implementation("org.springframework.boot.experimental:spring-boot-starter-data-r2dbc")
  implementation("io.r2dbc:r2dbc-client:1.0.0.M7")
  implementation("com.github.jasync-sql:jasync-r2dbc-mysql:1.0.11")
  testImplementation("org.springframework.boot.experimental:spring-boot-test-autoconfigure-r2dbc")
  testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.boot.experimental:spring-boot-bom-r2dbc:0.1.0.M2")
  }
}

tasks {
  test {
    useJUnitPlatform()
  }
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_12
}