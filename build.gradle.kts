plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.jooq.jooq-codegen-gradle") version "3.19.11"
}

group = "com.isaki-kaji"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.flywaydb:flyway-core:10.15.0")
	implementation("org.flywaydb:flyway-database-postgresql:10.15.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("org.postgresql:postgresql:42.7.3")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("com.h2database:h2")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.jooq:jooq-meta")
	implementation("org.jooq:jooq-codegen")
	implementation("org.jooq:jooq-postgres-extensions:3.19.11")
	jooqCodegen("org.postgresql:postgresql:42.7.3")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

jooq {
	configuration {
		jdbc {
			driver = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/book"
			user = "postgres"
			password = "password"
		}
		generator {
			database {
				name = "org.jooq.meta.postgres.PostgresDatabase"
				inputSchema = "public"
				includes = ".*"
				excludes = "flyway_schema_history"
			}
			target {
				packageName = "org.example.db"
				directory = "build/generated-sources/jooq"
			}
		}
	}
}

sourceSets.main {
	java.srcDirs("build/generated-sources/jooq")
}

tasks.named("jooqCodegen") {
	inputs.files(fileTree("src/main/resources/db/migration"))
}
