rootProject.name = "TruckTrack"

includeBuild("app") {
    name = "app"
}
includeBuild("server")
includeBuild("shared")
