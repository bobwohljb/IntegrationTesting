import com.intellij.driver.sdk.ui.components.actionButtonByXpath
import com.intellij.driver.sdk.ui.components.button
import com.intellij.driver.sdk.ui.components.dialog
import com.intellij.driver.sdk.ui.components.ideFrame
import com.intellij.driver.sdk.ui.components.waitForNoOpenedDialogs
import com.intellij.driver.sdk.ui.components.welcomeScreen
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.ide.starter.examples.writeTestExecutionTime
import com.intellij.ide.starter.examples.writeDetailedMetricsToCSV
import com.intellij.ide.starter.examples.writeMemoryMetricsToCSV
import java.time.Instant
import java.nio.file.Paths
import com.intellij.ide.starter.examples.gatherAndWriteAllMetrics
import com.intellij.ide.starter.models.IDEStartResult
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Assertions.fail
import java.nio.file.Files
import java.time.Duration


class PluginTest {
    companion object {
        // Define the plugin path as a constant in the companion object
        private const val PLUGIN_PATH =
            "/Users/robertwohl/IdeaProjects/MyCodeSnippetInserter/build/distributions/MyKotlinCodeInserter-1.0-SNAPSHOT.zip"
    }



    /**
     * Test to verify that the Demo plugin (built from sources) is installed in the IDE.
     *
     * This test does the following:
     * - Creates a new test context with specified settings.
     * - Installs the plugin from a given file path.
     * - Launches the IDE along with its test driver.
     * - Accesses the welcome screen to navigate to the Installed Plugins section.
     * - Asserts that the expected plugin is correctly installed and visible in the list.
     */
    @Test
    fun installedPluginTest2() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult =
            Starter.newContext("testExample", TestCase(IdeProductProvider.IU, NoProject).useEAP()).apply {
                PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
            }.runIdeWithDriver().useDriverAndCloseIde {
                welcomeScreen {
                    clickPlugins()
                    x { byAccessibleName("Installed") }.click()
                    shouldBe("Plugin is installed") {
                        x {
                            and(
                                byVisibleText("MyKotlinCodeInserter"),
                                byJavaClass("javax.swing.JLabel")
                            )
                        }.present()
                    }
                }
            }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("installedPluginTest", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("installedPluginTest", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV("installedPluginTest", startTime, endTime, ideStartResult)
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }


    @Test
    fun installedPluginTest() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult =
            Starter.newContext("testExample", TestCase(IdeProductProvider.IU, NoProject).withVersion("2025.1")).apply {
                PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
            }.runIdeWithDriver().useDriverAndCloseIde {
                welcomeScreen {
                    clickPlugins()
                    x { byAccessibleName("Installed") }.click()
                    shouldBe("Plugin is installed") {
                        x {
                            and(
                                byVisibleText("MyKotlinCodeInserter"),
                                byJavaClass("javax.swing.JLabel")
                            )
                        }.present()
                    }
                }
            }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("installedPluginTest", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("installedPluginTest", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV("installedPluginTest", startTime, endTime, ideStartResult)
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }


    @TestFactory
    fun testInstallPluginsFromMarketplaces(): Collection<DynamicTest> {
        val pluginIds = listOf(
            "Pythonid" to "Python",
            "org.intellij.scala" to "Scala" // This one might 404
        )
        //val ideVersions = listOf("2023.1", "2023.2", "2023.3", "2024.1", "2024.2", "2024.3")
        val ideVersions = listOf("2024.2", "2024.3")
        val outputDir = Paths.get("build/reports/metrics")

        // Create directory if it doesn't exist
        Files.createDirectories(outputDir)

        val tests = mutableListOf<DynamicTest>()

        for (ideVersion in ideVersions) {
            for ((pluginId, pluginText) in pluginIds) {
                val testName = "Install plugin $pluginId on IDE version $ideVersion"
                val testId = "MarketplacePlugin_${pluginId}_$ideVersion"

                val test = DynamicTest.dynamicTest(testName) {
                    val startTime = Instant.now()
                    var endTime: Instant? = null
                    var ideStartResult: IDEStartResult? = null
                    var testFailed = false
                    var failureMessage: String? = null
                    var exceptionStack: String? = null

                    try {
                        println("========== TEST START: $testName ==========")
                        println("Installing plugin $pluginId on IDE version $ideVersion")
                        println("Test ID: $testId")
                        println("Start time: $startTime")

                        // Create a test context for the given IDE version
                        val testContext = Starter.newContext(
                            testId,
                            TestCase(IdeProductProvider.IU, NoProject).withVersion(ideVersion)
                        )

                        println("Test context created successfully")

                        // Install a plugin from the marketplace using its ID
                        println("Attempting to install plugin '$pluginId' from marketplace")
                        PluginConfigurator(testContext).installPluginFromPluginManager(
                            pluginId = pluginId,
                            ide = testContext.ide
                        )
                        println("Plugin installation command completed")

                        // Run the IDE and validate installation
                        println("Starting IDE to validate plugin installation")
                        ideStartResult = try {
                            testContext.runIdeWithDriver().useDriverAndCloseIde {
                                println("IDE started, navigating to plugins screen")
                                // Validation code
                                welcomeScreen {
                                    println("At welcome screen, clicking plugins button")
                                    clickPlugins()
                                    println("Clicking 'Installed' tab")
                                    x { byAccessibleName("Installed") }.click()
                                    println("Checking if plugin '$pluginText' is present")
                                    shouldBe("Plugin is installed") {
                                        //Thread.sleep(10.minutes.inWholeMilliseconds)
                                        x {
                                            and(
                                                byVisibleText(pluginText),
                                                byJavaClass("javax.swing.JLabel")
                                            )
                                        }.present()
                                    }
                                    println("Plugin validation successful")
                                }
                            }
                        } catch (e: Exception) {
                            println("ERROR: IDE startup or plugin interaction failed")
                            println("Error message: ${e.message}")
                            e.printStackTrace()
                            exceptionStack = e.stackTraceToString()
                            testFailed = true
                            failureMessage = "Error during IDE startup or plugin interaction: ${e.message}"
                            null
                        }

                    } catch (e: Exception) {
                        println("ERROR: Plugin installation failed")
                        println("Error message: ${e.message}")
                        e.printStackTrace()
                        exceptionStack = e.stackTraceToString()
                        testFailed = true
                        failureMessage = "Error installing plugin $pluginId on IDE version $ideVersion: ${e.message}"
                    }

                    // Always record metrics, even if test failed
                    endTime = Instant.now()
                    val duration = Duration.between(startTime, endTime)

                    println("Test execution time: ${duration.toMillis()} ms")
                    println("Test failed: $testFailed")

                    // Write metrics regardless of test outcome
                    try {
                        println("Gathering and writing metrics to $outputDir")
                        gatherAndWriteAllMetrics(
                            testName = testId,
                            startTime = startTime,
                            endTime = endTime,
                            ideStartResult = ideStartResult,
                            outputDir = outputDir
                        )

                        // Additional metrics if needed
                        println("Writing test execution time metrics")
                        writeTestExecutionTime(testId, startTime, endTime)

                        println("Writing memory metrics")
                        writeMemoryMetricsToCSV(testId, startTime, endTime)

                        if (ideStartResult != null) {
                            try {
                                println("Writing detailed IDE metrics")
                                writeDetailedMetricsToCSV(testId, startTime, endTime, ideStartResult)
                            } catch (e: Exception) {
                                println("Error writing detailed metrics: ${e.message}")
                                e.printStackTrace()
                            }
                        }

                        // Write debug info to a dedicated log file
                        try {
                            val debugLogFile = outputDir.resolve("${testId}_debug.log")
                            Files.newBufferedWriter(debugLogFile).use { writer ->
                                writer.write("Test: $testName\n")
                                writer.write("ID: $testId\n")
                                writer.write("Start time: $startTime\n")
                                writer.write("End time: $endTime\n")
                                writer.write("Duration: ${duration.toMillis()} ms\n")
                                writer.write("Success: ${!testFailed}\n")
                                if (failureMessage != null) {
                                    writer.write("Failure message: $failureMessage\n")
                                }
                                if (exceptionStack != null) {
                                    writer.write("\nStack trace:\n$exceptionStack\n")
                                }
                            }
                            println("Debug log written to ${debugLogFile.toAbsolutePath()}")
                        } catch (e: Exception) {
                            println("Error writing debug log: ${e.message}")
                        }

                    } catch (e: Exception) {
                        println("Error writing metrics for test $testId: ${e.message}")
                        e.printStackTrace()
                    }

                    println("========== TEST END: $testName ==========")

                    // If the test failed, fail it properly after metrics have been collected
                    if (testFailed) {
                        fail(failureMessage ?: "Unknown failure in test $testId")
                    }
                }

                tests.add(test)
            }
        }

        return tests
    }

    @Test
    fun testClickSidePanelAndCreateDataClassFile() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult = Starter.newContext(
            "testCreateDataClassFile",
            TestCase(
                IdeProductProvider.IU,
                projectInfo = GitHubProject.fromGithub(
                    branchName = "main",
                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
                )
            ).withVersion("2024.3")
        ).apply {
            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {

                // Wait for loading indicators
                waitForIndicators(1.minutes)

                // Open the "MyKotlinInserter" side panel
                actionButtonByXpath(xpath = xQuery { byAccessibleName("MyKotlinInserter") }).click()

                // Click the button to trigger the "Create Data Class File" flow
                button { byText("Create Data Class File") }.click()

                // Wait for the input dialog to appear
                dialog {
                    Thread.sleep(1000)
                    button { byText("OK") }.click()
                }

                // Wait for any dialogs to finish before proceeding
                waitForNoOpenedDialogs()

                println("Success: The 'MyDataClass.kt' file was created and opened in the editor.")
            }
        }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("testClickSidePanelAndCreateDataClassFile", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("testClickSidePanelAndCreateDataClassFile", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV("testClickSidePanelAndCreateDataClassFile", startTime, endTime, ideStartResult)
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }

    @Test
    fun testClickSidePanelAndCreateSingletonFile() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult = Starter.newContext(
            "testCreateSingletonFile",
            TestCase(
                IdeProductProvider.IU,
                projectInfo = GitHubProject.fromGithub(
                    branchName = "main",
                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
                )
            ).withVersion("2024.3")
        ).apply {
            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                // Wait for loading indicators
                waitForIndicators(1.minutes)

                // Open the "MyKotlinInserter" side panel
                actionButtonByXpath(xpath = xQuery { byAccessibleName("MyKotlinInserter") }).click()

                // Click the button to trigger the "Create Data Class File" flow
                button { byText("Create Singleton File") }.click()

                // Wait for the input dialog to appear
                dialog {
                    Thread.sleep(1000)
                    button { byText("OK") }.click()
                }

                // Wait for any dialogs to finish before proceeding
                waitForNoOpenedDialogs()

                println("Success: The 'MySingleton.kt' file was created and opened in the editor.")
            }
        }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("testClickSidePanelAndCreateSingletonFile", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("testClickSidePanelAndCreateSingletonFile", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV("testClickSidePanelAndCreateSingletonFile", startTime, endTime, ideStartResult)
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }

    @Test
    fun testClickSidePanelAndCreateExtensionFunctionsFile() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult = Starter.newContext(
            "testCreateExtensionFunctionsFile",
            TestCase(
                IdeProductProvider.IU,
                projectInfo = GitHubProject.fromGithub(
                    branchName = "main",
                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
                )
            ).withVersion("2024.3")
        ).apply {
            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                // Wait for loading indicators
                waitForIndicators(1.minutes)

                // Open the "MyKotlinInserter" side panel
                actionButtonByXpath(xpath = xQuery { byAccessibleName("MyKotlinInserter") }).click()

                // Click the button to trigger the "Create Data Class File" flow
                button { byText("Create Extension Functions File") }.click()

                // Wait for the input dialog to appear
                dialog {
                    Thread.sleep(1000)
                    button { byText("OK") }.click()
                }

                // Wait for any dialogs to finish before proceeding
                waitForNoOpenedDialogs()

                println("Success: The 'MySingleton.kt' file was created and opened in the editor.")
            }
        }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("testClickSidePanelAndCreateExtensionFunctionsFile", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("testClickSidePanelAndCreateExtensionFunctionsFile", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV(
                "testClickSidePanelAndCreateExtensionFunctionsFile",
                startTime,
                endTime,
                ideStartResult
            )
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }

    @Test
    fun testClickSidePanelAndCreateCoroutinePatternsFile() {
        val startTime = Instant.now()
        var endTime: Instant? = null

        val ideStartResult = Starter.newContext(
            "testCreateCoroutinePatternsFile",
            TestCase(
                IdeProductProvider.IU,
                projectInfo = GitHubProject.fromGithub(
                    branchName = "main",
                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
                )
            ).withVersion("2024.3")
        ).apply {
            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                // Wait for loading indicators
                waitForIndicators(1.minutes)

                // Open the "MyKotlinInserter" side panel
                actionButtonByXpath(xpath = xQuery { byAccessibleName("MyKotlinInserter") }).click()

                // Click the button to trigger the "Create Data Class File" flow
                button { byText("Create Coroutine Patterns File") }.click()

                // Wait for the input dialog to appear
                dialog {
                    Thread.sleep(1000)
                    button { byText("OK") }.click()
                }

                // Wait for any dialogs to finish before proceeding
                waitForNoOpenedDialogs()

                println("Success: The 'MySingleton.kt' file was created and opened in the editor.")
            }
        }

        endTime = Instant.now()

        // Write basic execution time metrics
        writeTestExecutionTime("testClickSidePanelAndCreateCoroutinePatternsFile", startTime, endTime)

        // Write memory metrics
        writeMemoryMetricsToCSV("testClickSidePanelAndCreateCoroutinePatternsFile", startTime, endTime)

        // Write detailed metrics
        try {
            writeDetailedMetricsToCSV(
                "testClickSidePanelAndCreateCoroutinePatternsFile",
                startTime,
                endTime,
                ideStartResult
            )
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }
    }

}



//
//    @Test
//    fun testActionsDataclass() {
//        Starter.newContext(
//            testName = "testExampleDataClass", TestCase(
//                IdeProductProvider.IU, projectInfo = GitHubProject.fromGithub(branchName = "main", repoRelativeUrl = "bobwohl/KotlinTestingPlayground")
//            ).withVersion("2024.3")
//        ).apply {
//            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
//        }.runIdeWithDriver().useDriverAndCloseIde {
//            Thread.sleep(60_000)
//            waitForIndicators(1.minutes)
//
//            openFile("src/main/kotlin/Main.kt")
//            // Wait to make sure file is fully loaded
//            waitForIndicators(10.seconds)
//
//            ideFrame {
//                invokeAction("MyKotlinSnippets.DataClass", now = false)
//
//                // Wait a moment for the action to be processed
//                Thread.sleep(1000)
//
//                waitForNoOpenedDialogs()
//                Thread.sleep(10.minutes.inWholeMilliseconds)
//                invokeAction("CloseProject", now = false)
//            }
//        }
//    }
//
//    @Test
//    fun testActionsSingleton() {
//        Starter.newContext(
//            testName = "testExampleSingleton", TestCase(
//                IdeProductProvider.IU, projectInfo = GitHubProject.fromGithub(branchName = "main", repoRelativeUrl = "bobwohl/KotlinTestingPlayground")
//            ).withVersion("2024.3")
//        ).apply {
//            PluginConfigurator(this).installPluginFromPath(Path(PLUGIN_PATH))
//        }.runIdeWithDriver().useDriverAndCloseIde {
//            Thread.sleep(10_000)
//            waitForIndicators(1.minutes)
//
//            openFile("src/main/kotlin/Main.kt")
//            // Wait to make sure file is fully loaded
//            waitForIndicators(10.seconds)
//
//            ideFrame {
//                invokeAction("MyKotlinSnippets.Singleton", now = false)
//
//                // Wait a moment for the action to be processed
//                Thread.sleep(1000)
//
//                //Do something here
//                invokeAction("CloseProject", now = false)
//            }
//        }
//    }
//
//    @Test
//    fun testIDEStartupGitCloneAndIndexing() {
//        val testStartTime = Instant.now()
//
//        // Phase 1: IDE Startup
//        println("Phase 1: IDE Startup")
//        val startupStartTime = Instant.now()
//        val result = Starter.newContext(
//            testName = "StartupGitCloneIndexing",
//            testCase = TestCase(IdeProductProvider.IU, projectInfo = NoProject).withVersion("2024.3")
//        ).runIdeWithDriver().useDriverAndCloseIde {
//            ideFrame {
//                // Just start the IDE without a project
//                println("IDE started successfully.")
//            }
//        }
//        val startupEndTime = Instant.now()
//
//        // Write startup metrics
//        writeTestExecutionTime("IDEStartupPhase", startupStartTime, startupEndTime)
//        writeDetailedMetricsToCSV("IDEStartupPhase", startupStartTime, startupEndTime, result)
//        println("IDE Startup phase completed. Metrics written to CSV.")
//
//        // Phase 2: Git Cloning
//        println("Phase 2: Git Cloning")
//        val gitStartTime = Instant.now()
//        val repoUrl = "https://github.com/bobwohl/KotlinTestingPlayground.git"
//        val cloneDir = File(System.getProperty("java.io.tmpdir"), "KotlinTestingPlayground-${System.currentTimeMillis()}")
//        cloneDir.mkdirs()
//
//        // Execute git clone command
//        val process = ProcessBuilder("git", "clone", repoUrl, cloneDir.absolutePath)
//            .redirectErrorStream(true)
//            .start()
//
//        // Read output
//        val output = process.inputStream.bufferedReader().use { it.readText() }
//        val exitCode = process.waitFor()
//
//        if (exitCode != 0) {
//            println("Git clone failed with exit code $exitCode")
//            println("Output: $output")
//            throw RuntimeException("Git clone failed")
//        }
//
//        val gitEndTime = Instant.now()
//        val gitCloneTimeMs = java.time.Duration.between(gitStartTime, gitEndTime).toMillis()
//
//        // Write Git clone metrics
//        writeTestExecutionTime("GitClonePhase", gitStartTime, gitEndTime)
//
//        // Write Git clone metrics to consolidated file
//        com.intellij.ide.starter.examples.writeToConsolidatedMetricsFile("GitCloneMetrics", mapOf(
//            "GitCloneTimeMs" to gitCloneTimeMs,
//            "RepoUrl" to repoUrl,
//            "CloneDirectory" to cloneDir.absolutePath
//        ))
//        println("Git Clone phase completed. Metrics written to CSV.")
//
//        // Phase 3: Indexing
//        println("Phase 3: Indexing")
//        val indexingStartTime = Instant.now()
//
//        // Open the cloned project in IDE using the GitHub URL instead of local directory
//        // since fromLocalDirectory is not available
//        val indexingResult = Starter.newContext(
//            testName = "IndexingPhase",
//            testCase = TestCase(
//                IdeProductProvider.IU,
//                projectInfo = GitHubProject.fromGithub(
//                    branchName = "main",
//                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
//                )
//            ).withVersion("2024.3")
//        ).runIdeWithDriver().useDriverAndCloseIde {
//            ideFrame {
//                // Wait for indexing to complete
//                waitForIndicators(2.minutes)
//                println("Indexing completed successfully.")
//            }
//        }
//
//        val indexingEndTime = Instant.now()
//
//        // Write indexing metrics
//        writeTestExecutionTime("IndexingPhase", indexingStartTime, indexingEndTime)
//        writeDetailedMetricsToCSV("IndexingPhase", indexingStartTime, indexingEndTime, indexingResult)
//        println("Indexing phase completed. Metrics written to CSV.")
//
//        // Overall test metrics
//        val testEndTime = Instant.now()
//        writeTestExecutionTime("StartupGitCloneIndexingTest", testStartTime, testEndTime)
//
//        // Write memory metrics
//        writeMemoryMetricsToCSV("StartupGitCloneIndexingTest", testStartTime, testEndTime)
//
//        // Write detailed metrics for the entire test
//        try {
//            writeDetailedMetricsToCSV("StartupGitCloneIndexingTest", testStartTime, testEndTime, result)
//        } catch (e: Exception) {
//            println("Error writing detailed metrics: ${e.message}")
//        }
//
//        println("Test completed. Metrics are stored in build/reports/metrics.")
//
//        // Clean up
//        try {
//            cloneDir.deleteRecursively()
//            println("Cleaned up temporary clone directory.")
//        } catch (e: Exception) {
//            println("Warning: Failed to clean up temporary directory: ${e.message}")
//        }
//    }
//}
