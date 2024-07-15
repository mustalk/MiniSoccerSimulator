pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }

    // We want the version catalog to be available in all subprojects
    versionCatalogs {
        create("libs") {
            // Renamed from libs.versions.toml to lib.versions.toml because of: https://github.com/gradle/gradle/issues/20282
            from(files("gradle/lib.versions.toml"))
        }
    }
}

rootProject.name = "MiniSoccerSimulator"
include(":app")
