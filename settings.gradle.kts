pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SpeakFlow"

include(":app")
include(":core")
include(":domain")
include(":data")
include(":feature:library")
include(":feature:player")
include(":feature:cloud")
include(":ml:asr")
include(":ml:align")
include(":provider:dictionary-ecdict")
include(":provider:youdao")
include(":provider:baidupan")
include(":provider:quark")
