package com.itb.diabetify.e2e.presentation.view_history

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
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
import java.time.LocalDate

class ViewHistoryTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    @SuppressLint("CheckResult")
    fun startAppAndNavigateToHistory() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()

            composeTestRule.onNodeWithContentDescription(
                "Navigate to history screen"
            ).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Riwayat")
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

        composeTestRule.onNodeWithContentDescription(
            "Navigate to history screen"
        ).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Riwayat")
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

    fun verifyHorizontalCalendarIsDisplayed() {
        composeTestRule.onNodeWithTag("HorizontalCalendar")
            .assertIsDisplayed()
    }

    fun verifyLineChartIsDisplayed() {
        composeTestRule.onNodeWithTag("LineGraph")
            .assertIsDisplayed()
    }

    fun verifyRiskPercentageCardIsDisplayed() {
        composeTestRule.onNodeWithText("Persentase Risiko")
            .assertIsDisplayed()
    }

    fun verifyRiskFactorContributionsAreDisplayed() {
        composeTestRule.onNodeWithText("Kontribusi Faktor Risiko")
            .assertIsDisplayed()
    }

    fun verifyDailyInputsAreDisplayed() {
        composeTestRule.onNodeWithText("Data Prediksi")
            .assertIsDisplayed()
    }

    fun clickPreviousDateButton() {
        val yesterday = LocalDate.now().minusDays(1)
        val dayOfMonth = yesterday.dayOfMonth
        
        composeTestRule.onNodeWithTag("DateItem_$dayOfMonth")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun clickNextDateButton() {
        val today = LocalDate.now()
        val dayOfMonth = today.dayOfMonth
        
        composeTestRule.onNodeWithTag("DateItem_$dayOfMonth")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun clickPreviousMonthButton() {
        composeTestRule.onNodeWithContentDescription("Previous month")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun clickNextMonthButton() {
        composeTestRule.onNodeWithContentDescription("Next month")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyNextDateButtonIsNotClickable() {
        val tomorrow = LocalDate.now().plusDays(1)
        val dayOfMonth = tomorrow.dayOfMonth
        
        composeTestRule.onNodeWithTag("DateItem_$dayOfMonth")
            .assertIsDisplayed()
            .assertHasNoClickAction()
    }

    fun verifyNoDataMessageIsDisplayed() {
        composeTestRule.onNodeWithText("Tidak ada data skor risiko dalam rentang 15 hari")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak Ada Data")
            .assertIsDisplayed()
    }

    fun verifyEmptyHistoryState() {
        verifyNoDataMessageIsDisplayed()
        composeTestRule.onNodeWithText("Data prediksi untuk tanggal yang dipilih tidak tersedia. Silakan pilih tanggal lain atau lakukan prediksi terlebih dahulu.")
            .assertIsDisplayed()
    }
}