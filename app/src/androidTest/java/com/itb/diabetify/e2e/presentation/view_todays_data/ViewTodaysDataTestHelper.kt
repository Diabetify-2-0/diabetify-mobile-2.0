package com.itb.diabetify.e2e.presentation.view_todays_data

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
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

class ViewTodaysDataTestHelper(
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

    fun scrollDown() {
        composeTestRule.onRoot().performTouchInput {
            swipeUp(
                startY = centerY + 400f,
                endY = centerY - 400f
            )
        }
        composeTestRule.waitForIdle()
    }

    fun verifyTodaysDataSectionDisplayed() {
        var attempts = 0
        var found = false

        while (attempts < 5 && !found) {
            try {
                composeTestRule.onNodeWithText("Data Hari Ini")
                    .assertIsDisplayed()
                found = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 500f,
                        endY = centerY - 500f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                attempts++
            }
        }
    }

    fun verifyBmiCardWithContents() {
        composeTestRule.onNodeWithText("BMI")
            .assertIsDisplayed()
        
        try {
            composeTestRule.onNodeWithText("Normal", substring = true)
                .assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("Kurus", substring = true)
                    .assertIsDisplayed()
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("Gemuk", substring = true)
                        .assertIsDisplayed()
                } catch (e: AssertionError) {
                    composeTestRule.onNodeWithText("Obesitas", substring = true)
                        .assertIsDisplayed()
                }
            }
        }
        
        composeTestRule.waitForIdle()
    }

    fun verifyWeightAndHeightCards() {
        composeTestRule.onNodeWithText("Berat")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("kg", substring = true)
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Tinggi")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("cm", substring = true)
            .assertIsDisplayed()
            
        composeTestRule.waitForIdle()
    }

    fun verifyHealthStatusCard() {
        composeTestRule.onNodeWithText("Status Kesehatan")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Hipertensi")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Kolesterol")
            .assertIsDisplayed()
        
        composeTestRule.waitForIdle()
    }

    fun verifyHealthHistoryCard() {
        composeTestRule.onNodeWithText("Riwayat Kesehatan")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Riwayat Keluarga")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Riwayat Kehamilan")
            .assertIsDisplayed()
        
        composeTestRule.waitForIdle()
    }

    fun verifyLifestyleFactorsCard() {
        composeTestRule.onNodeWithText("Faktor Gaya Hidup")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Jumlah Rokok Hari Ini")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Aktivitas Fisik Hari Ini")
            .assertIsDisplayed()
        
        composeTestRule.waitForIdle()
    }
}