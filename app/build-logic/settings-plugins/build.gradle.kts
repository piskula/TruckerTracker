plugins {
    `kotlin-dsl`
}

group = "com.momosi.trucktrack.buildlogic"

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    plugins {
        register("trucktrack.git-hooks") {
            id = "trucktrack.git-hooks"
            implementationClass = "com.momosi.trucktrack.GitHooksPlugin"
        }
    }
}
