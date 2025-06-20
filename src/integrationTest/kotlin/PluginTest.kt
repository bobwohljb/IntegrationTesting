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
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.util.stream.Stream


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

    @Test
    fun testIDEStartupGitCloneAndIndexing() {
        runStartupGitCloneAndIndexingTest("2024.3", "StartupGitCloneIndexingTest")
    }

    @TestFactory
    fun testIDEStartupGitCloneAndIndexingAcrossVersions(): Stream<DynamicTest> {
        val ideVersions = listOf("2024.3", "2025.1")

        // Generate a test for each version
        val tests = ideVersions.stream().map { version ->
            DynamicTest.dynamicTest("Test IDE Startup, Git Clone and Indexing with version $version") {
                runStartupGitCloneAndIndexingTest(version, "StartupGitCloneIndexingTest_$version")
            }
        }

        // Generate a final test that creates the comparison report
        val comparisonTest = DynamicTest.dynamicTest("Generate Version Comparison Report") {
            generateVersionComparisonReport(ideVersions)
        }

        // Combine the version tests with the comparison report test
        return Stream.concat(tests, Stream.of(comparisonTest))
    }

    private fun generateVersionComparisonReport(ideVersions: List<String>) {
        println("Generating version comparison report for versions: ${ideVersions.joinToString(", ")}")

        // Create metrics directory if it doesn't exist
        val metricsDir = Paths.get("build/reports/metrics")
        Files.createDirectories(metricsDir)

        // Path to the comparison report
        val reportFile = metricsDir.resolve("metrics_comparison.html").toFile()

        try {
            // Collect metrics for each version
            val versionMetrics = mutableMapOf<String, Map<String, Map<String, Any>>>()

            // For each version, read the metrics from the CSV file
            for (version in ideVersions) {
                val versionDir = metricsDir.resolve("version_$version")
                val csvFile = versionDir.resolve("metrics.csv").toFile()

                if (csvFile.exists()) {
                    val metrics = readMetricsFromCSV(csvFile, version)
                    versionMetrics[version] = metrics
                } else {
                    println("Warning: No metrics file found for version $version at ${csvFile.absolutePath}")
                }
            }

            // Generate the HTML report
            val htmlContent = generateComparisonHTML(versionMetrics)

            // Write the HTML report
            reportFile.writeText(htmlContent)
            println("Version comparison report generated at: ${reportFile.absolutePath}")
        } catch (e: Exception) {
            println("Error generating version comparison report: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun readMetricsFromCSV(csvFile: File, version: String): Map<String, Map<String, Any>> {
        val metrics = mutableMapOf<String, MutableMap<String, Any>>()

        try {
            val lines = csvFile.readLines()
            if (lines.size > 1) {
                val headers = lines[0].split(",")

                for (i in 1 until lines.size) {
                    val values = lines[i].split(",")
                    if (values.isNotEmpty() && values.size >= headers.size) {
                        val testName = values[0]
                        val testMetrics = mutableMapOf<String, Any>()

                        for (j in 1 until headers.size) {
                            if (j < values.size) {
                                val value = values[j]
                                if (value.isNotEmpty()) {
                                    // Try to convert to numeric if possible
                                    try {
                                        if (value.contains(".")) {
                                            testMetrics[headers[j]] = value.toDouble()
                                        } else {
                                            testMetrics[headers[j]] = value.toLong()
                                        }
                                    } catch (e: NumberFormatException) {
                                        testMetrics[headers[j]] = value
                                    }
                                }
                            }
                        }

                        metrics[testName] = testMetrics
                    }
                }
            }
        } catch (e: Exception) {
            println("Error reading metrics from CSV for version $version: ${e.message}")
        }

        return metrics
    }

    private fun generateComparisonHTML(versionMetrics: Map<String, Map<String, Map<String, Any>>>): String {
        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>IDE Performance Comparison</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        h1, h2, h3 { color: #333; }
                        .container { margin-bottom: 30px; }
                        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; position: sticky; top: 0; }
                        tr:nth-child(even) { background-color: #f9f9f9; }
                        tr:hover { background-color: #f1f1f1; }
                        .improvement { color: green; }
                        .regression { color: red; }
                        .chart-container { width: 100%; height: 400px; margin-bottom: 30px; }
                    </style>
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                </head>
                <body>
                    <h1>IDE Performance Comparison</h1>
                    <p>Generated on ${java.time.LocalDateTime.now()}</p>
            """.trimIndent())

            // Add summary section
            append("""
                    <div class="container">
                        <h2>Performance Summary</h2>
                        <table>
                            <tr>
                                <th>Metric</th>
            """.trimIndent())

            // Add version headers
            versionMetrics.keys.forEach { version ->
                append("<th>$version</th>")
            }

            // If we have at least two versions, add a comparison column
            if (versionMetrics.size >= 2) {
                append("<th>Change</th>")
            }

            append("</tr>")

            // Add key metrics rows
            val keyMetrics = listOf(
                "ExecutionTimeMs" to "Total Execution Time (ms)",
                "StartupTimeMs" to "IDE Startup Time (ms)",
                "GitCloneTimeMs" to "Git Clone Time (ms)",
                "IndexingTimeMs" to "Indexing Time (ms)",
                "MaxMemoryMB" to "Max Memory (MB)"
            )

            for ((metricKey, metricLabel) in keyMetrics) {
                append("<tr><td>$metricLabel</td>")

                // Get the versions in order (assuming we want to compare the first version with the last)
                val orderedVersions = versionMetrics.keys.sorted()
                var firstVersionValue: Double? = null
                var lastVersionValue: Double? = null

                // Add values for each version
                for (version in orderedVersions) {
                    val versionData = versionMetrics[version]
                    val value = findMetricValue(versionData, metricKey)

                    if (value != null) {
                        append("<td>${formatValue(value)}</td>")

                        if (firstVersionValue == null) {
                            firstVersionValue = value.toString().toDoubleOrNull()
                        }
                        lastVersionValue = value.toString().toDoubleOrNull()
                    } else {
                        append("<td>N/A</td>")
                    }
                }

                // Add comparison column if we have at least two versions
                if (versionMetrics.size >= 2 && firstVersionValue != null && lastVersionValue != null) {
                    val change = lastVersionValue - firstVersionValue
                    val percentChange = (change / firstVersionValue) * 100

                    val changeClass = when {
                        // For execution time, indexing time, etc., lower is better
                        metricKey in listOf("ExecutionTimeMs", "StartupTimeMs", "GitCloneTimeMs", "IndexingTimeMs") -> {
                            if (change < 0) "improvement" else if (change > 0) "regression" else ""
                        }
                        // For memory, it depends on the context, but generally lower is better
                        else -> {
                            if (change < 0) "improvement" else if (change > 0) "regression" else ""
                        }
                    }

                    append("<td class=\"$changeClass\">${formatChange(change, percentChange)}</td>")
                } else if (versionMetrics.size >= 2) {
                    append("<td>N/A</td>")
                }

                append("</tr>")
            }

            append("</table></div>")

            // Add charts for key metrics
            append("""
                    <div class="container">
                        <h2>Performance Charts</h2>
            """.trimIndent())

            // Create a chart for each key metric
            for ((metricKey, metricLabel) in keyMetrics) {
                val chartId = "chart_${metricKey}"

                append("""
                        <h3>$metricLabel</h3>
                        <div class="chart-container">
                            <canvas id="$chartId"></canvas>
                        </div>
                """.trimIndent())
            }

            append("</div>")

            // Add detailed metrics section
            append("""
                    <div class="container">
                        <h2>Detailed Metrics</h2>
            """.trimIndent())

            // Get all test names across all versions
            val allTestNames = versionMetrics.values.flatMap { it.keys }.distinct().sorted()

            // For each test, create a comparison table
            for (testName in allTestNames) {
                append("""
                        <h3>$testName</h3>
                        <table>
                            <tr>
                                <th>Metric</th>
                """.trimIndent())

                // Add version headers
                versionMetrics.keys.forEach { version ->
                    append("<th>$version</th>")
                }

                // If we have at least two versions, add a comparison column
                if (versionMetrics.size >= 2) {
                    append("<th>Change</th>")
                }

                append("</tr>")

                // Get all metrics for this test across all versions
                val allMetrics = versionMetrics.values
                    .flatMap { versionData -> 
                        versionData[testName]?.keys ?: emptyList() 
                    }
                    .distinct()
                    .sorted()

                // Add a row for each metric
                for (metric in allMetrics) {
                    append("<tr><td>$metric</td>")

                    // Get the versions in order
                    val orderedVersions = versionMetrics.keys.sorted()
                    var firstVersionValue: Double? = null
                    var lastVersionValue: Double? = null

                    // Add values for each version
                    for (version in orderedVersions) {
                        val versionData = versionMetrics[version]
                        val testData = versionData?.get(testName)
                        val value = testData?.get(metric)

                        if (value != null) {
                            append("<td>${formatValue(value)}</td>")

                            if (firstVersionValue == null) {
                                firstVersionValue = value.toString().toDoubleOrNull()
                            }
                            lastVersionValue = value.toString().toDoubleOrNull()
                        } else {
                            append("<td>N/A</td>")
                        }
                    }

                    // Add comparison column if we have at least two versions
                    if (versionMetrics.size >= 2 && firstVersionValue != null && lastVersionValue != null) {
                        val change = lastVersionValue - firstVersionValue
                        val percentChange = (change / firstVersionValue) * 100

                        val changeClass = when {
                            // For execution time, indexing time, etc., lower is better
                            metric.contains("Time") || metric.contains("Duration") -> {
                                if (change < 0) "improvement" else if (change > 0) "regression" else ""
                            }
                            // For memory, it depends on the context, but generally lower is better
                            metric.contains("Memory") -> {
                                if (change < 0) "improvement" else if (change > 0) "regression" else ""
                            }
                            // For other metrics, we don't know if higher or lower is better
                            else -> ""
                        }

                        append("<td class=\"$changeClass\">${formatChange(change, percentChange)}</td>")
                    } else if (versionMetrics.size >= 2) {
                        append("<td>N/A</td>")
                    }

                    append("</tr>")
                }

                append("</table>")
            }

            append("</div>")

            // Add JavaScript for charts
            append("<script>")

            // Create a chart for each key metric
            for ((metricKey, metricLabel) in keyMetrics) {
                val chartId = "chart_${metricKey}"
                val labels = versionMetrics.keys.sorted()
                val data = labels.map { version ->
                    findMetricValue(versionMetrics[version], metricKey)?.toString()?.toDoubleOrNull() ?: 0.0
                }

                append("""
                    new Chart(document.getElementById('$chartId'), {
                        type: 'bar',
                        data: {
                            labels: ${labels.map { "'$it'" }},
                            datasets: [{
                                label: '$metricLabel',
                                data: $data,
                                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                                borderColor: 'rgba(54, 162, 235, 1)',
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    title: {
                                        display: true,
                                        text: '${if (metricKey.contains("Memory")) "MB" else "ms"}'
                                    }
                                }
                            }
                        }
                    });
                """.trimIndent())
            }

            append("</script>")

            append("""
                </body>
                </html>
            """.trimIndent())
        }
    }

    private fun findMetricValue(versionData: Map<String, Map<String, Any>>?, metricKey: String): Any? {
        // Look for the metric in all test results
        versionData?.forEach { (_, metrics) ->
            metrics[metricKey]?.let { return it }
        }
        return null
    }

    private fun formatValue(value: Any): String {
        return when (value) {
            is Double -> String.format("%.2f", value)
            is Float -> String.format("%.2f", value)
            else -> value.toString()
        }
    }

    private fun formatChange(change: Double, percentChange: Double): String {
        val sign = if (change >= 0) "+" else ""
        return "$sign${String.format("%.2f", change)} (${sign}${String.format("%.2f", percentChange)}%)"
    }

    private fun runStartupGitCloneAndIndexingTest(ideVersion: String, testName: String) {
        val testStartTime = Instant.now()
        val versionDir = "version_$ideVersion"

        // Create version-specific directory for metrics
        val metricsDir = Paths.get("build/reports/metrics/$versionDir")
        Files.createDirectories(metricsDir)

        // Phase 1: IDE Startup
        println("Phase 1: IDE Startup for version $ideVersion")
        val startupStartTime = Instant.now()
        val result = Starter.newContext(
            testName = "${testName}_Startup",
            testCase = TestCase(IdeProductProvider.IU, projectInfo = NoProject).withVersion(ideVersion)
        ).runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                // Just start the IDE without a project
                println("IDE $ideVersion started successfully.")
            }
        }
        val startupEndTime = Instant.now()

        // Write startup metrics
        writeTestExecutionTime("IDEStartupPhase_$ideVersion", startupStartTime, startupEndTime, versionDir)
        writeDetailedMetricsToCSV("IDEStartupPhase_$ideVersion", startupStartTime, startupEndTime, result, versionDir)
        println("IDE Startup phase completed for version $ideVersion. Metrics written to CSV.")

        // Phase 2: Git Cloning
        println("Phase 2: Git Cloning for version $ideVersion")
        val gitStartTime = Instant.now()
        val repoUrl = "https://github.com/bobwohl/KotlinTestingPlayground.git"
        val cloneDir =
            File(System.getProperty("java.io.tmpdir"), "KotlinTestingPlayground-${ideVersion}-${System.currentTimeMillis()}")
        cloneDir.mkdirs()

        // Execute git clone command
        val process = ProcessBuilder("git", "clone", repoUrl, cloneDir.absolutePath)
            .redirectErrorStream(true)
            .start()

        // Read output
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            println("Git clone failed with exit code $exitCode")
            println("Output: $output")
            throw RuntimeException("Git clone failed")
        }

        val gitEndTime = Instant.now()
        val gitCloneTimeMs = java.time.Duration.between(gitStartTime, gitEndTime).toMillis()

        // Write Git clone metrics
        writeTestExecutionTime("GitClonePhase_$ideVersion", gitStartTime, gitEndTime, versionDir)

        // Write Git clone metrics to consolidated file
        com.intellij.ide.starter.examples.writeToConsolidatedMetricsFile("GitCloneMetrics_$ideVersion", mapOf(
            "GitCloneTimeMs" to gitCloneTimeMs,
            "RepoUrl" to repoUrl,
            "CloneDirectory" to cloneDir.absolutePath
        ), versionDir)
        println("Git Clone phase completed for version $ideVersion. Metrics written to CSV.")

        // Phase 3: Indexing
        println("Phase 3: Indexing for version $ideVersion")
        val indexingStartTime = Instant.now()

        // Open the cloned project in IDE using the GitHub URL instead of local directory
        // since fromLocalDirectory is not available
        val indexingResult = Starter.newContext(
            testName = "${testName}_Indexing",
            testCase = TestCase(
                IdeProductProvider.IU,
                projectInfo = GitHubProject.fromGithub(
                    branchName = "main",
                    repoRelativeUrl = "bobwohl/KotlinTestingPlayground"
                )
            ).withVersion(ideVersion)
        ).runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                // Wait for indexing to complete
                waitForIndicators(2.minutes)
                println("Indexing completed successfully for version $ideVersion.")
            }
        }

        val indexingEndTime = Instant.now()

        // Write indexing metrics
        writeTestExecutionTime("IndexingPhase_$ideVersion", indexingStartTime, indexingEndTime, versionDir)
        writeDetailedMetricsToCSV("IndexingPhase_$ideVersion", indexingStartTime, indexingEndTime, indexingResult, versionDir)
        println("Indexing phase completed for version $ideVersion. Metrics written to CSV.")

        // Overall test metrics
        val testEndTime = Instant.now()
        writeTestExecutionTime("${testName}_Overall", testStartTime, testEndTime, versionDir)

        // Write memory metrics
        writeMemoryMetricsToCSV("${testName}_Overall", testStartTime, testEndTime, versionDir)

        // Write detailed metrics for the entire test
        try {
            writeDetailedMetricsToCSV("${testName}_Overall", testStartTime, testEndTime, result, versionDir)
        } catch (e: Exception) {
            println("Error writing detailed metrics: ${e.message}")
        }

        println("Test completed for version $ideVersion. Metrics are stored in build/reports/metrics/$versionDir.")

        // Clean up
        try {
            cloneDir.deleteRecursively()
            println("Cleaned up temporary clone directory for version $ideVersion.")
        } catch (e: Exception) {
            println("Warning: Failed to clean up temporary directory for version $ideVersion: ${e.message}")
        }
    }
}
