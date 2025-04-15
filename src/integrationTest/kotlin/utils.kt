package com.intellij.ide.starter.examples

import com.intellij.ide.starter.models.IDEStartResult
import com.intellij.tools.ide.metrics.collector.OpenTelemetrySpanCollector
import com.intellij.tools.ide.metrics.collector.metrics.PerformanceMetrics.Metric
import com.intellij.tools.ide.metrics.collector.starter.collector.StarterTelemetrySpanCollector
import com.intellij.tools.ide.metrics.collector.telemetry.SpanFilter
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
//
//import com.intellij.tools.ide.metrics.collector.metrics.PerformanceMetrics
//import com.intellij.tools.ide.metrics.collector.starter.fus.StatisticsEventsHarvester
//import com.intellij.tools.ide.metrics.collector.starter.fus.filterByEventId
//import com.intellij.tools.ide.metrics.collector.starter.fus.getDataFromEvent
//import com.intellij.tools.ide.metrics.collector.starter.metrics.extractIndexingMetrics
//import com.intellij.tools.ide.performanceTesting.commands.*
//import com.intellij.util.indexing.diagnostic.dto.IndexingMetric
//import com.intellij.util.indexing.diagnostic.dto.getListOfIndexingMetrics

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

// Simple function to write test execution time to a CSV file
fun writeTestExecutionTime(testName: String, startTime: Instant, endTime: Instant) {
    val executionTimeMs = Duration.between(startTime, endTime).toMillis()
    val metricsDir = Paths.get("build/reports/metrics")
    File(metricsDir.toString()).mkdirs()

    val csvFile = metricsDir.resolve("test_metrics.csv").toFile()
    val fileExists = csvFile.exists()

    if (!fileExists) {
        csvFile.writeText("TestName,ExecutionTimeMs\n")
    }

    csvFile.appendText("$testName,$executionTimeMs\n")

    println("#".repeat(20))
    println("Test execution metrics:")
    println("Test: $testName")
    println("Execution time: $executionTimeMs ms")
    println("Metrics written to: ${csvFile.absolutePath}")
    println("#".repeat(20))
}
