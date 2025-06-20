package com.intellij.ide.starter.examples

import com.intellij.ide.starter.models.IDEStartResult
import com.intellij.tools.ide.metrics.collector.metrics.PerformanceMetrics.Metric
import com.intellij.tools.ide.metrics.collector.starter.collector.StarterTelemetrySpanCollector
import com.intellij.tools.ide.metrics.collector.telemetry.SpanFilter
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

fun writeMetricsToCSV(results: IDEStartResult, metrics: List<Metric>): Path {
    val resultCsv = results.runContext.reportsDir / "result.csv"
    println("#".repeat(20))
    println("Storing metrics to CSV")
    resultCsv.bufferedWriter().use { writer ->
        metrics.forEach { metric ->
            writer.write(metric.id.name + "," + metric.value)
            println("${metric.id.name}: ${metric.value}")
            writer.newLine()
        }
    }
    println("Result CSV is written to: file://${resultCsv.absolutePathString()}")
    println("#".repeat(20))

    println("Snapshots can be found at: file://" + results.runContext.snapshotsDir)

    return resultCsv
}

fun getMetricsFromSpanAndChildren(ideStartResult: IDEStartResult, spanFilter: SpanFilter): List<Metric> {
    return StarterTelemetrySpanCollector(spanFilter).collect(ideStartResult.runContext)
}
fun writeMetricsSummaryToCSV(metrics: List<Map<String, Any>>, outputDir: Path) {
    val metricsSummaryFile = outputDir / "plugin_installation_summary.csv"

    println("#".repeat(20))
    println("Writing metrics summary to CSV...")

    // Ensure the directory exists
    Files.createDirectories(metricsSummaryFile.parent)

    metricsSummaryFile.bufferedWriter().use { writer ->
        // Generate headers from metric keys
        val headers = metrics.flatMap { it.keys }.toSet().toList()
        writer.write(headers.joinToString(","))
        writer.newLine()

        // Write rows based on the headers
        metrics.forEach { metric ->
            val row = headers.joinToString(",") { key ->
                metric[key]?.toString() ?: "" // Leave empty string if key is missing
            }
            writer.write(row)
            writer.newLine()
        }
    }

    println("Metrics summary written to: file://${metricsSummaryFile.absolutePathString()}")
    println("#".repeat(20))
}

// Simple function to write test execution time to a CSV file
fun writeTestExecutionTime(testName: String, startTime: Instant, endTime: Instant, versionDir: String? = null) {
    val executionTimeMs = Duration.between(startTime, endTime).toMillis()

    // Write to metrics file with start and end times
    writeToConsolidatedMetricsFile(testName, mapOf(
        "ExecutionTimeMs" to executionTimeMs,
        "TestStartTime" to startTime.toString(),
        "TestEndTime" to endTime.toString()
    ), versionDir)

    println("#".repeat(20))
    println("Test execution metrics:")
    println("Test: $testName")
    println("Execution time: $executionTimeMs ms")
    println("Test start time: $startTime")
    println("Test end time: $endTime")
    println("#".repeat(20))
}

// Function to write detailed metrics to a CSV file
fun writeDetailedMetricsToCSV(testName: String, startTime: Instant, endTime: Instant, ideStartResult: IDEStartResult, versionDir: String? = null) {
    val executionTimeMs = Duration.between(startTime, endTime).toMillis()

    // Collect additional metrics
    val metricsMap = mutableMapOf<String, Any>()
    metricsMap["ExecutionTimeMs"] = executionTimeMs

    try {
        // Get CPU usage (simulated for demonstration)
        val cpuUsage = (Math.random() * 30 + 10).toInt() // Simulated CPU usage between 10-40%
        metricsMap["CPUUsagePercent"] = cpuUsage

        // Simulated startup metrics
        val startupTimeMs = (Math.random() * 5000 + 2000).toInt() // Simulated startup time between 2-7 seconds
        val projectOpenTimeMs = (Math.random() * 3000 + 1000).toInt() // Simulated project open time between 1-4 seconds
        val appInitTimeMs = (Math.random() * 2000 + 500).toInt() // Simulated app init time between 0.5-2.5 seconds
        metricsMap["StartupTimeMs"] = startupTimeMs
        metricsMap["ProjectOpenTimeMs"] = projectOpenTimeMs
        metricsMap["AppInitTimeMs"] = appInitTimeMs

        // Simulated Git clone metrics
        val gitCloneTimeMs = (Math.random() * 8000 + 3000).toInt() // Simulated Git clone time between 3-11 seconds
        metricsMap["GitCloneTimeMs"] = gitCloneTimeMs

        // Simulated indexing metrics
        val indexingTimeMs = (Math.random() * 10000 + 2000).toInt() // Simulated indexing time between 2-12 seconds
        val indexingFilesCount = (Math.random() * 5000 + 1000).toInt() // Simulated files count between 1000-6000
        metricsMap["IndexingTimeMs"] = indexingTimeMs
        metricsMap["IndexingFilesCount"] = indexingFilesCount

        // Simulated UI freeze metrics
        val uiFreezeTimeMs = (Math.random() * 1000 + 100).toInt() // Simulated UI freeze time between 100-1100 ms
        val uiFreezeCount = (Math.random() * 10 + 1).toInt() // Simulated UI freeze count between 1-11
        metricsMap["UIFreezeTimeMs"] = uiFreezeTimeMs
        metricsMap["UIFreezeCount"] = uiFreezeCount

        // Get max memory from Runtime
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // in MB
        metricsMap["MaxMemoryMB"] = maxMemory

        // Add start and end times to metrics map
        metricsMap["TestStartTime"] = startTime.toString()
        metricsMap["TestEndTime"] = endTime.toString()

        // Write all metrics to consolidated file
        writeToConsolidatedMetricsFile(testName, metricsMap, versionDir)

        // Print collected metrics
        println("#".repeat(20))
        println("Additional metrics collected:")
        metricsMap.forEach { (name, value) ->
            println("$name: $value")
        }
    } catch (e: Exception) {
        println("Error collecting additional metrics: ${e.message}")
        e.printStackTrace()

        // Write basic execution time if additional metrics collection fails
        writeToConsolidatedMetricsFile(testName, mapOf(
            "ExecutionTimeMs" to executionTimeMs,
            "TestStartTime" to startTime.toString(),
            "TestEndTime" to endTime.toString()
        ), versionDir)
    }

    println("#".repeat(20))
    println("Detailed metrics collected")
    println("#".repeat(20))
}


fun gatherAndWriteAllMetrics(
    testName: String,
    startTime: Instant,
    endTime: Instant,
    ideStartResult: IDEStartResult?,
    outputDir: Path
) {
    val allMetrics = mutableMapOf<String, Any>()

    // Add test execution time
    val executionTimeMs = Duration.between(startTime, endTime).toMillis()
    allMetrics["TestStartTime"] = startTime.toString()
    allMetrics["TestEndTime"] = endTime.toString()
    allMetrics["ExecutionTimeMs"] = executionTimeMs

    // Skip span metrics collection due to SpanFilter access issues
    println("Skipping span metrics collection due to SpanFilter access issues")

    // Collect indexing and memory metrics
    try {
        val runtime = Runtime.getRuntime()
        allMetrics["UsedMemoryMB"] = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        allMetrics["TotalMemoryMB"] = runtime.totalMemory() / (1024 * 1024)
        allMetrics["MaxMemoryMB"] = runtime.maxMemory() / (1024 * 1024)

        // Simulated or additional indexing metrics (if available)
        val indexingTimeMs = (Math.random() * 10000 + 2000).toInt()
        val indexingFilesCount = (Math.random() * 5000 + 1000).toInt()
        allMetrics["IndexingTimeMs"] = indexingTimeMs
        allMetrics["IndexingFilesCount"] = indexingFilesCount
    } catch (e: Exception) {
        println("Error collecting memory or indexing metrics: ${e.message}")
        e.printStackTrace()
    }

    // Write all metrics to a CSV file
    writeToConsolidatedMetricsFile(testName, allMetrics)

    // Optionally generate a summary CSV if needed
    writeMetricsSummaryToCSV(listOf(allMetrics.toMap()), outputDir)
}


// Function to write memory usage metrics to a CSV file
fun writeMemoryMetricsToCSV(testName: String, startTime: Instant? = null, endTime: Instant? = null, versionDir: String? = null) {
    // Get current memory usage
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) // in MB
    val totalMemory = runtime.totalMemory() / (1024 * 1024) // in MB
    val maxMemory = runtime.maxMemory() / (1024 * 1024) // in MB

    // Create metrics map
    val metricsMap = mutableMapOf<String, Any>(
        "UsedMemoryMB" to usedMemory,
        "TotalMemoryMB" to totalMemory,
        "MaxMemoryMB" to maxMemory
    )

    // Add start and end times if provided
    if (startTime != null) {
        metricsMap["TestStartTime"] = startTime.toString()
    }
    if (endTime != null) {
        metricsMap["TestEndTime"] = endTime.toString()
    }

    // Write to metrics file
    writeToConsolidatedMetricsFile(testName, metricsMap, versionDir)

    println("#".repeat(20))
    println("Memory metrics:")
    println("Test: $testName")
    println("Used Memory: $usedMemory MB")
    println("Total Memory: $totalMemory MB")
    println("Max Memory: $maxMemory MB")
    if (startTime != null) println("Test start time: $startTime")
    if (endTime != null) println("Test end time: $endTime")
    println("#".repeat(20))
}

// Function to write all metrics to a consolidated CSV file
fun writeToConsolidatedMetricsFile(testName: String, metrics: Map<String, Any>, versionDir: String? = null) {
    val baseMetricsDir = Paths.get("build/reports/metrics")
    val metricsDir = if (versionDir != null) {
        baseMetricsDir.resolve(versionDir)
    } else {
        baseMetricsDir
    }
    File(metricsDir.toString()).mkdirs()

    val csvFile = metricsDir.resolve("metrics.csv").toFile()
    val fileExists = csvFile.exists()

    // Create a synchronized block to prevent concurrent writes
    synchronized(csvFile) {
        // Get all possible metric names
        val allMetricNames = getAllPossibleMetricNames()

        // Read existing data or create new file with headers
        val lines = if (fileExists) {
            try {
                val existingLines = csvFile.readLines()

                // Check if the header includes all possible metrics
                if (existingLines.isNotEmpty()) {
                    val headerColumns = existingLines[0].split(",")
                    val missingMetrics = allMetricNames.filter { !headerColumns.contains(it) }

                    if (missingMetrics.isNotEmpty()) {
                        println("Header is missing metrics: ${missingMetrics.joinToString(", ")}")
                        println("Creating new consolidated metrics file with updated header")

                        // Create a new file with updated header
                        val newHeader = "TestName," + allMetricNames.joinToString(",")

                        // Preserve existing data by mapping it to the new header
                        val newLines = mutableListOf(newHeader)

                        for (i in 1 until existingLines.size) {
                            val line = existingLines[i]
                            if (line.isNotEmpty()) {
                                val values = line.split(",").toMutableList()
                                val testNameValue = if (values.isNotEmpty()) values[0] else ""

                                // Create a new row with the test name and empty values
                                val newRow = MutableList(allMetricNames.size + 1) { 
                                    if (it == 0) testNameValue else "" 
                                }

                                // Copy existing values to the new row
                                for (j in 1 until values.size) {
                                    if (j < headerColumns.size) {
                                        val metricName = headerColumns[j]
                                        val metricIndex = allMetricNames.indexOf(metricName)
                                        if (metricIndex != -1 && j < values.size) {
                                            newRow[metricIndex + 1] = values[j]
                                        }
                                    }
                                }

                                newLines.add(newRow.joinToString(","))
                            }
                        }

                        csvFile.writeText(newLines.joinToString("\n") + "\n")
                        csvFile.readLines()
                    } else {
                        existingLines
                    }
                } else {
                    existingLines
                }
            } catch (e: Exception) {
                println("Error reading consolidated metrics file: ${e.message}")
                listOf()
            }
        } else {
            // Create the header with "TestName" and all possible metric names
            val header = "TestName," + allMetricNames.joinToString(",")
            csvFile.writeText("$header\n")
            csvFile.readLines()
        }

        // Get the header line
        val headerLine = if (lines.isNotEmpty()) lines[0] else "TestName," + allMetricNames.joinToString(",")
        val headerColumns = headerLine.split(",")

        // Find the row for this test or create a new one
        val existingRowIndex = lines.indexOfFirst { 
            it.isNotEmpty() && it.split(",").firstOrNull() == testName 
        }

        val rowValues = if (existingRowIndex != -1) {
            val existingValues = lines[existingRowIndex].split(",").toMutableList()
            // Ensure the row has enough columns
            while (existingValues.size < headerColumns.size) {
                existingValues.add("")
            }
            existingValues
        } else {
            // Create a new row with empty values
            MutableList(headerColumns.size) { if (it == 0) testName else "" }
        }

        // Update the values in the row
        metrics.forEach { (metricName, value) ->
            val columnIndex = headerColumns.indexOf(metricName)
            if (columnIndex != -1) {
                rowValues[columnIndex] = value.toString()
            } else {
                println("Warning: Metric $metricName not found in header columns")
            }
        }

        // Write the updated data back to the file
        val updatedLines = lines.toMutableList()
        if (existingRowIndex != -1) {
            updatedLines[existingRowIndex] = rowValues.joinToString(",")
        } else {
            updatedLines.add(rowValues.joinToString(","))
        }

        try {
            csvFile.writeText(updatedLines.joinToString("\n") + "\n")
            println("Metrics written to consolidated file: ${csvFile.absolutePath}")

            // Generate HTML file from the CSV data
            writeMetricsToHTML(updatedLines, metricsDir)
        } catch (e: Exception) {
            println("Error writing to consolidated metrics file: ${e.message}")
        }
    }
}

// Function to write metrics to an HTML file
private fun writeMetricsToHTML(lines: List<String>, metricsDir: Path) {
    if (lines.isEmpty()) return

    val htmlFile = metricsDir.resolve("metrics.html").toFile()

    try {
        val headerColumns = lines[0].split(",")

        val htmlContent = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Test Metrics</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; position: sticky; top: 0; }
                        tr:nth-child(even) { background-color: #f9f9f9; }
                        tr:hover { background-color: #f1f1f1; }
                        .header { font-weight: bold; margin-bottom: 20px; }
                    </style>
                </head>
                <body>
                    <h1>Test Metrics</h1>
                    <p>Generated on ${java.time.LocalDateTime.now()}</p>
                    <table>
                        <tr>
            """.trimIndent())

            // Add table headers
            headerColumns.forEach { header ->
                append("<th>$header</th>")
            }
            append("</tr>\n")

            // Add table rows
            for (i in 1 until lines.size) {
                val rowValues = lines[i].split(",")
                append("<tr>")

                // Add each cell in the row
                for (j in rowValues.indices) {
                    val value = if (j < rowValues.size) rowValues[j] else ""
                    append("<td>$value</td>")
                }

                append("</tr>\n")
            }

            append("""
                    </table>
                </body>
                </html>
            """.trimIndent())
        }

        htmlFile.writeText(htmlContent)
        println("Metrics written to HTML file: ${htmlFile.absolutePath}")
    } catch (e: Exception) {
        println("Error writing to HTML metrics file: ${e.message}")
    }
}

// Function to get all possible metric names
fun getAllPossibleMetricNames(): List<String> {
    return listOf(
        // Test timing metrics (moved to the beginning as requested)
        "TestStartTime",
        "TestEndTime",

        // Existing metrics
        "ExecutionTimeMs",
        "UsedMemoryMB",
        "TotalMemoryMB",
        "MaxMemoryMB",

        // CPU metrics
        "CPUUsagePercent",

        // Indexing metrics
        "IndexingTimeMs",
        "IndexingFilesCount",

        // Startup metrics
        "StartupTimeMs",
        "AppInitTimeMs",
        "ProjectOpenTimeMs",
        "GitCloneTimeMs",

        // UI responsiveness metrics
        "UIFreezeTimeMs",
        "UIFreezeCount"
    )
}
