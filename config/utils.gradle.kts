// Install git hooks automatically.
gradle.taskGraph.whenReady {
    val from = File("${rootProject.rootDir}/config/pre-commit")
    val to = File("${rootProject.rootDir}/.git/hooks/pre-commit")
    from.copyTo(to, overwrite = true)
    to.setExecutable(true)
}

// gradle task to print project structure. usage: ./gradlew printPrettyProjectStructure
tasks.register("printPrettyProjectStructure") {
    doLast {
        fun printDirectoryStructure(
            dir: File,
            prefix: String = "",
            isLast: Boolean = true,
        ) {
            if (dir.isDirectory) {
                val children =
                    dir
                        .listFiles()
                        ?.filter { it.isDirectory || it.name.endsWith(".kt") || it.name.endsWith(".java") }
                        ?.sorted()
                children?.forEachIndexed { index, file ->
                    val isChildLast = index == children.size - 1
                    val newPrefix = if (isLast) "$prefix    " else "$prefix|   "
                    println("$prefix${if (isChildLast) "|__ " else "|__ "}${file.name}")
                    printDirectoryStructure(file, newPrefix, isChildLast)
                }
            }
        }

        val sourceSets =
            mapOf(
                "main" to file("src/main/kotlin"),
                "androidTest" to file("src/androidTest/kotlin"),
                "test" to file("src/test/kotlin")
            )

        sourceSets.forEach { (sourceSetName, sourceSetPath) ->
            println(sourceSetName)
            printDirectoryStructure(sourceSetPath)
        }
    }
}

/**
 * Task to generate JaCoCo code coverage reports for connected Android UI tests and open the HTML report in a browser.
 ** This task depends on the 'connectedDebugAndroidTest' task, ensuring that the UI tests are executed
 * before the coverage report is generated. After the report is generated, it attempts to open the HTML
 * report in the default browser for convenient viewing.
 *
 * Execute the task on the terminal: ./gradlew connectedAndroidTestDebugCoverage
 */
tasks.register("connectedAndroidTestDebugCoverage", JacocoReport::class) {
    // Ensure UI tests are run before generating the report
    dependsOn("connectedDebugAndroidTest")

    // Configure JaCoCo report output formats
    reports {
        xml.required.set(true) // Enable XML report generation
        html.required.set(true) // Enable HTML report generation
    }

    // Specify source directories for coverage analysis
    sourceDirectories.setFrom(files("$projectDir/src/main/kotlin"))

    // Specify class directories for coverage analysis, excluding R classes
    classDirectories.setFrom(
        fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory.get()}/tmp/kotlin-classes/debug",
                "exclude" to
                    listOf(
                        "**/R.class",
                        "**/R\$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*"
                    )
            )
        )
    )

    // Specify execution data file generated by connected Android tests
    executionData(
        fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory.get()}/outputs/code_coverage/debugAndroidTest/connected",
                "include" to "**/*.ec"
            )
        )
    )

    // Attempt to open the HTML report in a browser after generation
    doLast {
        val reportFile = file("${layout.buildDirectory.get()}/reports/jacoco/connectedAndroidTestDebugCoverage/html/index.html")
        if (reportFile.exists()) {
            println("Opening coverage report in default browser: ${reportFile.absolutePath}")

            // Determine the OS and execute the appropriate command
            val osName = System.getProperty("os.name").lowercase()
            val command =
                when {
                    osName.contains("win") -> listOf("cmd", "/c", "start", reportFile.absolutePath)
                    osName.contains("mac") -> listOf("open", reportFile.absolutePath)
                    osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> listOf("xdg-open", reportFile.absolutePath)
                    else -> throw GradleException("Unsupported OS: $osName")
                }

            project.exec {
                commandLine(command)
            }
        }
    }
}
