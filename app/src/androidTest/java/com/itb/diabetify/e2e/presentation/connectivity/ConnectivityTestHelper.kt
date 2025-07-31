package com.itb.diabetify.e2e.presentation.connectivity

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class ConnectivityTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    fun startAppAndWaitForSplash() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("Halo,").assertIsDisplayed()
                    true
                } catch (e2: AssertionError) {
                    try {
                        composeTestRule.onNodeWithText("Tidak Ada Koneksi Internet").assertIsDisplayed()
                        true
                    } catch (e3: AssertionError) {
                        false
                    }
                }
            }
        }
    }

    fun verifyNoInternetScreenDisplayed() {
        composeTestRule.onNodeWithText("Tidak Ada Koneksi Internet")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Coba Lagi")
            .assertIsDisplayed()
    }

    fun clickRetryButton() {
        composeTestRule.onNodeWithText("Coba Lagi")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun verifyOnboardingScreenDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun waitForConnectivityChange(expectedScreen: String, timeoutMs: Long = 3000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
            try {
                when (expectedScreen) {
                    "NO_INTERNET" -> {
                        composeTestRule.onNodeWithText("Tidak Ada Koneksi Internet").assertIsDisplayed()
                        true
                    }
                    "HOME" -> {
                        composeTestRule.onNodeWithText("Selamat Datang Kembali,").assertIsDisplayed()
                        true
                    }
                    "ONBOARDING" -> {
                        composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
                        true
                    }
                    else -> false
                }
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
