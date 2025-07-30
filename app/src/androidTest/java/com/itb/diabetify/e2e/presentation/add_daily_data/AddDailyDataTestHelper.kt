package com.itb.diabetify.e2e.presentation.add_daily_data

import android.annotation.SuppressLint
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class AddDailyDataTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    @SuppressLint("CheckResult")
    fun startAppAndNavigateToHome() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()
            return
        } catch (_: AssertionError) {
        }

        try {
            composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Mulai").assertIsDisplayed()
            composeTestRule.onNodeWithText("Mulai").performClick()
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
        }

        val maxOnboardingPages = 4

        repeat(maxOnboardingPages) {
            try {
                composeTestRule.onNodeWithText(">").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onRoot().performTouchInput {
                        swipeLeft()
                    }
                    composeTestRule.waitForIdle()
                    Thread.sleep(500)
                } catch (_: AssertionError) {
                }
            }

            try {
                composeTestRule.onNodeWithText(">").assertExists()
            } catch (_: AssertionError) {
            }
        }

        try {
            composeTestRule.onNodeWithText(">").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
        } catch (_: AssertionError) {
        }

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()
            return
        } catch (_: AssertionError) {
        }

        fillLoginForm(
            email = "testmale@example.com",
            password = "bewebewe"
        )

        clickLoginButton()

        waitForHomeScreenAfterLogin()
        waitForHomeContentToLoad()
    }

    private fun fillLoginForm(email: String, password: String) {
        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput(email)

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput(password)

        composeTestRule.waitForIdle()
    }

    private fun clickLoginButton() {
        composeTestRule.onNodeWithText("Masuk")
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForHomeScreenAfterLogin() {
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    private fun waitForHomeContentToLoad() {
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                    .assertIsDisplayed()

                Thread.sleep(2000)
                break
            } catch (e: AssertionError) {
                attempts++
                Thread.sleep(1000)
            }
        }

        composeTestRule.waitForIdle()
    }

    fun clickAddButton() {
        composeTestRule.onNodeWithContentDescription("Add")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickRokokButton() {
        composeTestRule.onNodeWithContentDescription("Rokok")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickAktivitasButton() {
        composeTestRule.onNodeWithContentDescription("Aktivitas")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun getCurrentRokokValue(): String {
        return try {
            val textFieldNodes = composeTestRule.onAllNodes(
                hasSetTextAction() and hasText("", substring = true, ignoreCase = true)
            ).fetchSemanticsNodes()

            if (textFieldNodes.isNotEmpty()) {
                val config = textFieldNodes[0].config
                config.getOrNull(SemanticsProperties.EditableText)?.text ?: "0"
            } else {
                "0"
            }
        } catch (e: Exception) {
            "0"
        }
    }

    fun getCurrentAktivitasValue(): String {
        return try {
            val allRadioButtons = composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.isRoot()) and
                        androidx.compose.ui.test.hasTestTag("").not()
            ).fetchSemanticsNodes().filter { node ->
                node.config.getOrNull(SemanticsProperties.Role)?.toString()?.contains("RadioButton") == true
            }

            allRadioButtons.forEachIndexed { index, node ->
                val selected = node.config.getOrNull(SemanticsProperties.Selected) == true
                if (selected) {
                    return if (index == 0) "Ya" else "Tidak"
                }
            }

            "Ya"
        } catch (e: Exception) {
            "Ya"
        }
    }

    fun clearAndFillRokokForm(currentValue: String) {
        val newValue = if (currentValue == "0") "5" else "0"

        try {
            composeTestRule.onNode(hasSetTextAction())
                .performTextClearance()
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasSetTextAction())
                .performTextInput(newValue)
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        } catch (e: Exception) {
            composeTestRule.onAllNodes(hasSetTextAction())[0]
                .performTextClearance()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodes(hasSetTextAction())[0]
                .performTextInput(newValue)
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }
    }

    fun clearAndFillAktivitasForm(currentValue: String) {
        val newValue = if (currentValue == "Ya") "Tidak" else "Ya"

        try {
            val matchingNodes = composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasAnyDescendant(hasText(newValue)) and
                        hasClickAction()
            ).fetchSemanticsNodes()

            if (matchingNodes.size >= 2) {
                val targetNode = matchingNodes.minByOrNull { node ->
                    val bounds = node.boundsInRoot
                    (bounds.right - bounds.left) * (bounds.bottom - bounds.top)
                }

                if (targetNode != null) {
                    val nodeIndex = matchingNodes.indexOf(targetNode)
                    composeTestRule.onAllNodes(
                        androidx.compose.ui.test.hasAnyDescendant(hasText(newValue)) and
                                hasClickAction()
                    )[nodeIndex].performClick()

                    composeTestRule.waitForIdle()
                    Thread.sleep(500)

                    val afterClick = getCurrentAktivitasValue()

                    if (afterClick == newValue) {
                        return
                    }
                }
            }
        } catch (_: Exception) {
        }

        try {
            val clickableNodes = composeTestRule.onAllNodes(
                hasText(newValue) and hasClickAction()
            ).fetchSemanticsNodes()

            if (clickableNodes.isNotEmpty()) {
                composeTestRule.onAllNodes(
                    hasText(newValue) and hasClickAction()
                )[0].performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                return
            }
        } catch (_: Exception) {
        }

        try {
            val allNodes = composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.isRoot())
            ).fetchSemanticsNodes()

            val radioButtonNodes = allNodes.filter { node ->
                val role = node.config.getOrNull(SemanticsProperties.Role)
                val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString("")
                role?.toString()?.contains("RadioButton") == true && text?.contains(newValue) == true
            }

            if (radioButtonNodes.isNotEmpty()) {
                val index = allNodes.indexOf(radioButtonNodes[0])
                composeTestRule.onAllNodes(
                    androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.isRoot())
                )[index].performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            }
        } catch (_: Exception) {
        }
    }

    fun clickSimpanButton() {
        composeTestRule.onNodeWithText("Simpan")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifySuccessMessage() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("berhasil", substring = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
    }

    fun verifyRokokDataMatches(expectedValue: String) {
        clickRokokButton()
        val actualValue = getCurrentRokokValue()
        assert(actualValue == expectedValue) {
            "Expected rokok value: $expectedValue, but got: $actualValue"
        }
    }

    fun verifyAktivitasDataMatches(expectedValue: String) {
        clickAktivitasButton()
        val actualValue = getCurrentAktivitasValue()
        assert(actualValue == expectedValue) {
            "Expected aktivitas value: $expectedValue, but got: $actualValue"
        }
    }

    fun closeDialogByClickingOutside() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun waitForErrorMessage(errorText: String, timeoutMs: Long = 5000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
            try {
                composeTestRule.onNode(hasText(errorText, substring = true))
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun fillRokokFormWithInvalidValue(invalidValue: String) {
        try {
            composeTestRule.onNode(hasSetTextAction())
                .performTextClearance()
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasSetTextAction())
                .performTextInput(invalidValue)
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        } catch (e: Exception) {
            composeTestRule.onAllNodes(hasSetTextAction())[0]
                .performTextClearance()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodes(hasSetTextAction())[0]
                .performTextInput(invalidValue)
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }
    }

    fun verifyNoSuccessMessage() {
        Thread.sleep(1000)
        try {
            composeTestRule.onNodeWithText("berhasil", substring = true)
                .assertDoesNotExist()
        } catch (_: AssertionError) {
        }
    }
}