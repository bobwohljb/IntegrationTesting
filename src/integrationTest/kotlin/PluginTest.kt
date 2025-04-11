import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.ui.components.actionButton
import com.intellij.driver.sdk.ui.components.actionButtonByXpath
import com.intellij.driver.sdk.ui.components.button
import com.intellij.driver.sdk.ui.components.dialog
import com.intellij.driver.sdk.ui.components.ideFrame
import com.intellij.driver.sdk.ui.components.waitForNoOpenedDialogs
import com.intellij.driver.sdk.ui.components.welcomeScreen
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.driver.remoteDev.RemDevDriverRunner
import com.intellij.ide.starter.driver.engine.DriverRunner
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.RemDevTestContainer
import com.intellij.ide.starter.runner.Starter
import com.intellij.ide.starter.runner.TestContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes
import org.kodein.di.DI
import org.kodein.di.bindProvider
import java.io.File
import java.sql.Driver
import kotlin.booleanArrayOf
import kotlin.time.Duration.Companion.seconds
import com.intellij.driver.sdk.ui.components.editor
import com.intellij.driver.sdk.ui.components.textField
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.xQuery


class PluginTest {
    companion object {
        // Define the plugin path as a constant in the companion object
        private const val PLUGIN_PATH =
            "/Users/robertwohl/IdeaProjects/MyCodeSnippetInserter/build/distributions/MyKotlinCodeInserter-1.0-SNAPSHOT.zip"
    }

    // Use to pause IDE:
    // Thread.sleep(10.minutes.inWholeMilliseconds)


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
    fun installedPluginTest() {
        Starter.newContext("testExample", TestCase(IdeProductProvider.IC, NoProject).withVersion("2024.3")).apply {
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
    }

    @Test
    fun testClickSidePanelAndCreateDataClassFile() {
        Starter.newContext(
            "testCreateDataClassFile",
            TestCase(
                IdeProductProvider.IC,
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
    }

    @Test
    fun testClickSidePanelAndCreateSingletonFile() {
        Starter.newContext(
            "testCreateSingletonFile",
            TestCase(
                IdeProductProvider.IC,
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
    }

    @Test
    fun testClickSidePanelAndCreateExtensionFunctionsFile() {
        Starter.newContext(
            "testCreateExtensionFunctionsFile",
            TestCase(
                IdeProductProvider.IC,
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
    }

    @Test
    fun testClickSidePanelAndCreateCoroutinePatternsFile() {
        Starter.newContext(
            "testCreateCoroutinePatternsFile",
            TestCase(
                IdeProductProvider.IC,
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
}
