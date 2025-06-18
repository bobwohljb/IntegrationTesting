# IntelliJ IDEA Plugin Integration Testing Framework

This project provides a framework for testing IntelliJ IDEA plugins using the IntelliJ Platform Test Framework. It allows you to write and run integration tests that simulate user interactions with the IDE to verify your plugin's functionality.

## Project Overview

The IntelliJ IDEA Plugin Integration Testing Framework is designed to:

- Provide a standardized environment for testing IntelliJ IDEA plugins
- Allow simulation of user interactions with the IDE
- Support testing against specific IntelliJ IDEA versions
- Facilitate automated testing of plugin functionality

## Prerequisites

- JDK 17 or later
- Gradle 8.0 or later
- IntelliJ IDEA (Community or Ultimate) 2024.3 or compatible version

## Project Structure

```
IntegrationTesting/
├── build.gradle.kts        # Gradle build configuration
├── settings.gradle.kts     # Gradle settings
├── src/
│   ├── main/               # Main source code (empty in this template)
│   │   ├── kotlin/
│   │   └── resources/
│   ├── test/               # Unit tests
│   └── integrationTest/    # Integration tests
│       └── kotlin/
│           ├── PluginTest.kt  # Integration tests for plugin functionality
│           └── utils.kt       # Utility functions for metrics collection
```

## Dependencies

The project uses the following main dependencies:

- **IntelliJ Platform Gradle Plugin** (version 2.2.1): For building and testing IntelliJ IDEA plugins
- **Kotlin JVM** (version 2.0.0): For Kotlin language support
- **IntelliJ IDEA Community Edition** (version 2024.3): The target IDE version
- **IntelliJ Test Framework (Starter)**: For integration testing
- **IDE Metrics Collector**: For collecting performance metrics during tests
- **JUnit Jupiter** (version 5.7.1): For test assertions and execution
- **Kodein DI** (version 7.20.2): For dependency injection
- **Kotlinx Coroutines** (version 1.10.1): For asynchronous programming

## Setting Up

1. Clone this repository
2. Open the project in IntelliJ IDEA
3. Make sure Gradle is configured to use JDK 17 or later

## Running Tests

To run all integration tests:

```bash
./gradlew integrationTest
```

This will:
1. Build the project
2. Prepare the sandbox environment
3. Run the integration tests defined in the `src/integrationTest` directory

To run a specific test class or method:

```bash
# Run a specific test class
./gradlew integrationTest --tests "PluginTest"

# Run a specific test method
./gradlew integrationTest --tests "PluginTest.installedPluginTest"
```

## Setting Up and Running PluginTest.kt

The `PluginTest.kt` file contains integration tests for testing IntelliJ IDEA plugins. Here's how to set it up and run it:

### Prerequisites

1. JDK 17 or later (required by the project's Kotlin JVM toolchain)
2. Gradle 8.0 or later
3. IntelliJ IDEA 2024.3 (Community or Ultimate)
4. A plugin to test (ZIP file)

### Configuration

1. **Plugin Path**: Update the `PLUGIN_PATH` constant in the `PluginTest` companion object to point to your plugin ZIP file:

```kotlin
private const val PLUGIN_PATH = "/path/to/your/plugin.zip"
```

2. **Test Project**: The tests use a GitHub project as the test environment. You can modify the GitHub project URL in each test method:

```kotlin
projectInfo = GitHubProject.fromGithub(
    branchName = "main",
    repoRelativeUrl = "your-username/your-repo"
)
```

3. **IDE Version**: The tests are configured to use IntelliJ IDEA 2024.3. You can change the version in each test method:

```kotlin
TestCase(IdeProductProvider.IC, projectInfo = NoProject)
    .withVersion("2024.3")
```

### Running PluginTest.kt

To run all tests in PluginTest.kt:

```bash
./gradlew integrationTest --tests "PluginTest"
```

To run a specific test method:

```bash
./gradlew integrationTest --tests "PluginTest.installedPluginTest"
```

### Metrics Collection

PluginTest.kt collects various performance metrics during test execution:

1. **Basic Execution Time**: Each test records its start time, end time, and total execution time.
2. **Memory Usage**: Tests collect memory metrics (used, total, max).
3. **Detailed Metrics**: Tests collect additional metrics like CPU usage, startup time, indexing time, and UI responsiveness.

All metrics are stored in CSV files in the `build/reports/metrics` directory:

```
build/reports/metrics/
└── metrics.csv       # Consolidated metrics file
```

### Customizing Tests

You can customize the tests in PluginTest.kt by:

1. Modifying the UI interaction steps to match your plugin's UI
2. Adding assertions to verify your plugin's behavior
3. Adjusting wait times for specific operations
4. Adding new test methods for additional plugin features

### Troubleshooting

- **Plugin Not Found**: Ensure the `PLUGIN_PATH` points to a valid plugin ZIP file
- **GitHub Project Not Found**: Verify the GitHub repository URL and branch name
- **Test Timeout**: Increase wait times for operations that take longer than expected
- **UI Element Not Found**: Update the UI element selectors to match your plugin's UI

## Writing Tests

Integration tests are written in Kotlin and use the IntelliJ Platform Test Framework. Here's a basic example of what a test looks like:

```kotlin
@Test
fun testPluginAction() {
    Starter.newContext(
        testName = "testPluginAction", 
        TestCase(IdeProductProvider.IU, projectInfo = NoProject)
            .withVersion("2024.3")
    ).apply {
        PluginConfigurator(this).installPluginFromPath(Path("/path/to/your/plugin"))
    }.runIdeWithDriver().useDriverAndCloseIde {
        // Test code here
        // For example:
        waitForIndicators(1.minutes)
        ideFrame {
            invokeAction("YourPluginAction", now = false)
            // Assert expected behavior
        }
    }
}
```

### Example Test Cases

The project includes several example test cases for a Kotlin code generation plugin:

1. **Plugin Installation Test**: Verifies that the plugin is correctly installed in the IDE
2. **Data Class Generation**: Tests creating a Kotlin data class file through the plugin UI
3. **Singleton Pattern Generation**: Tests creating a Kotlin singleton class
4. **Extension Functions Generation**: Tests creating Kotlin extension functions
5. **Coroutine Patterns Generation**: Tests creating Kotlin coroutine pattern templates

Each test simulates user interactions with the IDE, such as:
- Opening the plugin's side panel
- Clicking buttons to trigger code generation
- Entering data in dialog boxes
- Verifying the generated code

### Performance Measurement

The project includes utilities for measuring and recording test performance:

```kotlin
// Record test execution time
val startTime = Instant.now()
// ... test code ...
val endTime = Instant.now()
writeTestExecutionTime("testName", startTime, endTime)
```

Test metrics are saved to CSV files in the `build/reports/metrics` directory for analysis.

## Example Metrics

Example metrics collected from test runs are available in the `metrics-examples` directory:

- [View Metrics Report](https://htmlpreview.github.io/?https://github.com/bobwohljb/IntegrationTesting/blob/main/metrics-examples/metrics.html) - Example html page for the visualization of the metrics in table format. 
- [metrics.csv](metrics-examples/metrics.csv) - Raw metrics data in CSV format
- [metrics.html](metrics-examples/metrics.html) - HTML source code for the visualization of the metrics

These examples demonstrate the type of performance data collected during test execution, including:
- Test execution time
- Memory usage (used, total, max)
- CPU usage
- Startup time
- Indexing time
- UI responsiveness

You can use these metrics to:
1. Establish performance baselines for your plugin
2. Identify performance regressions
3. Compare performance across different plugin versions
4. Optimize resource-intensive operations

## Key Features

- **IDE Automation**: Simulate user interactions with the IDE
- **UI Component Access**: Access and interact with IDE UI components
- **Action Invocation**: Invoke IDE actions programmatically
- **Wait Conditions**: Wait for IDE processes to complete
- **Project Setup**: Set up test projects from various sources (GitHub, local, etc.)
- **Performance Measurement**: Collect and analyze test execution metrics

## Customizing Tests

You can customize your tests by:

1. Modifying the IntelliJ IDEA version in `build.gradle.kts`
2. Changing the test project source (GitHub, local, etc.)
3. Adding custom test utilities
4. Configuring test timeouts and other parameters
5. Customizing performance metrics collection and reporting

## Troubleshooting

- **Test Failures**: Check the test logs in the `build/reports/tests` directory
- **IDE Startup Issues**: Verify that the specified IntelliJ IDEA version is compatible
- **Plugin Loading Problems**: Ensure your plugin is correctly built and compatible with the specified IDE version
- **Performance Metrics Issues**: Check the metrics CSV files in the `build/reports/metrics` directory

## License

This project is open source and available under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
