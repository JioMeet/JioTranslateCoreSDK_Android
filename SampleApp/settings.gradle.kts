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
        maven {
            credentials {
                username = ""
                password = ""
            }
            url = uri("https://maven.pkg.github.com/JioMeet/JioTranslateCoreSDK_Android")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "SampleApp"
include(":app")
 