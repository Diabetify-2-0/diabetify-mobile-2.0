package com.itb.diabetify.e2e.presentation.logout

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class LogoutTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    @SuppressLint("CheckResult")
    fun startAppAndNavigateToSettings() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()

            composeTestRule.onNodeWithContentDescription(
                "Navigate to settings screen"
            ).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Profil")
                .assertIsDisplayed()

            composeTestRule.waitForIdle()
            Thread.sleep(2000)

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

        verifyLoginScreenDisplayed()

        fillLoginForm(
            email = "testmale@example.com",
            password = "bewebewe"
        )

        clickLoginButton()

        waitForHomeScreenAfterLogin()
        waitForHomeContentToLoad()

        composeTestRule.onNodeWithContentDescription(
            "Navigate to settings screen"
        ).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Profil")
            .assertIsDisplayed()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
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

    fun scrollDown() {
        composeTestRule.onRoot().performTouchInput {
            swipeUp(
                startY = centerY + 400f,
                endY = centerY - 400f
            )
        }
        composeTestRule.waitForIdle()
    }

    fun clickLogoutButton() {
        composeTestRule.onNodeWithText("Logout")
            .assertHasClickAction()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Apakah Anda yakin ingin logout dari aplikasi ini?")
            .assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Logout")[2]
            .assertHasClickAction()
            .performClick()
    }

    fun verifyLoginScreenDisplayed() {
        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()
            return
        } catch (_: AssertionError) {
        }
    }
}