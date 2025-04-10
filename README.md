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
│           └── PluginTest.kt  # Example integration test
```

## Dependencies

The project uses the following main dependencies:

- **IntelliJ Platform Gradle Plugin** (version 2.2.1): For building and testing IntelliJ IDEA plugins
- **Kotlin JVM** (version 2.0.0): For Kotlin language support
- **IntelliJ IDEA Community Edition** (version 2024.3): The target IDE version
- **IntelliJ Test Framework (Starter)**: For integration testing
- **JUnit Jupiter** (version 5.7.1): For test assertions and execution
- **Kodein DI** (version 7.20.2): For dependency injection
- **Kotlinx Coroutines** (version 1.10.1): For asynchronous programming

## Setting Up

1. Clone this repository
2. Open the project in IntelliJ IDEA
3. Make sure Gradle is configured to use JDK 17 or later

## Running Tests

To run the integration tests:

```bash
./gradlew integrationTest
```

This will:
1. Build the project
2. Prepare the sandbox environment
3. Run the integration tests defined in the `src/integrationTest` directory

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

## Key Features

- **IDE Automation**: Simulate user interactions with the IDE
- **UI Component Access**: Access and interact with IDE UI components
- **Action Invocation**: Invoke IDE actions programmatically
- **Wait Conditions**: Wait for IDE processes to complete
- **Project Setup**: Set up test projects from various sources (GitHub, local, etc.)

## Customizing Tests

You can customize your tests by:

1. Modifying the IntelliJ IDEA version in `build.gradle.kts`
2. Changing the test project source (GitHub, local, etc.)
3. Adding custom test utilities
4. Configuring test timeouts and other parameters

## Troubleshooting

- **Test Failures**: Check the test logs in the `build/reports/tests` directory
- **IDE Startup Issues**: Verify that the specified IntelliJ IDEA version is compatible
- **Plugin Loading Problems**: Ensure your plugin is correctly built and compatible with the specified IDE version

## License

This project is open source and available under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).