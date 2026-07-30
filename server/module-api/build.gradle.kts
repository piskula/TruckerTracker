// Pure contract module — no Spring Boot plugin, no fat jar.
// Consumers (module-server) provide spring-web and springdoc at runtime.
//
// Deliberately does NOT apply io.spring.dependency-management here: applying the
// Spring BOM import in more than one subproject of this build corrupts Kotlin's
// classpath-entry-snapshot transform (NoClassDefFoundError: GradleBuildTimeMetric,
// reproduced in isolation outside this repo) — module-server's BOM import is enough
// for spring-web/springdoc to resolve here too.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-java-parameters")
    }
}

dependencies {
    compileOnly(libs.springdoc.openapi.webmvc.api)   // @Tag, @Operation, etc.
    compileOnly("org.springframework:spring-web")     // @GetMapping, @PathVariable, etc. — version from BOM
}
