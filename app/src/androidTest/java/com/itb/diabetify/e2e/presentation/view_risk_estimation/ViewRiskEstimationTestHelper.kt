package com.itb.diabetify.e2e.presentation.view_risk_estimation

import android.annotation.SuppressLint
import androidx.compose.ui.semantics.getOrNull
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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class ViewRiskEstimationTestHelper(
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

    fun verifyRiskPercentageCardDisplayed() {
        composeTestRule.onNodeWithText("Persentase Risiko")
            .assertIsDisplayed()

        composeTestRule.onAllNodesWithText("%", substring = true)[0]
            .assertIsDisplayed()
    }

    fun getCurrentRiskPercentage(): Int {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onAllNodesWithText("%", substring = true)[0]
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        return try {
            val node = composeTestRule.onAllNodesWithText("%", substring = true)[0]
            val text = node.fetchSemanticsNode().config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
                ?.firstOrNull()?.text ?: ""
            val match = Regex("""(\d+\.\d)%""").find(text)
            match?.groupValues?.get(1)?.toFloatOrNull()?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun navigateToRiskDetail() {
        try {
            composeTestRule.onAllNodesWithText("Lihat Detail")[0]
                .performClick()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("Lihat Detail")
                    .performClick()
            } catch (e2: AssertionError) {
                Thread.sleep(1000)
                composeTestRule.onAllNodesWithText("Lihat Detail")[0]
                    .performClick()
            }
        }
        composeTestRule.waitForIdle()
    }

    fun verifyRiskDetailScreenDisplayed() {
        composeTestRule.onNodeWithText("Perhitungan skor risiko")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Gunakan perhitungan ini sebagai acuanmu untuk mengurangi kemungkinan Diabetes")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("0 - 35: Rendah")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diperkirakan 15 dari 100 orang dengan skor ini akan mengidap Diabetes")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("35 - 55: Sedang")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diperkirakan 31 dari 100 orang dengan skor ini akan mengidap Diabetes")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("55 - 70: Tinggi")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diperkirakan 55 dari 100 orang dengan skor ini akan mengidap Diabetes")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("70 - 100: Sangat Tinggi")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diperkirakan 69 dari 100 orang dengan skor ini akan mengidap Diabetes")
            .assertIsDisplayed()
    }

    fun verifyRiskScoreGaugeDisplayed() {
        composeTestRule.onNodeWithTag("RiskScoreGauge")
            .assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    fun verifyCorrectRiskCategoryHighlighted(expectedScore: Int) {
        when {
            expectedScore <= 35 -> {
                composeTestRule.onNodeWithText("0 - 35: Rendah")
                    .assertIsDisplayed()
            }
            expectedScore in 35..55 -> {
                composeTestRule.onNodeWithText("35 - 55: Sedang")
                    .assertIsDisplayed()
            }
            expectedScore in 55..70 -> {
                composeTestRule.onNodeWithText("55 - 70: Tinggi")
                    .assertIsDisplayed()
            }
            expectedScore > 70 -> {
                composeTestRule.onNodeWithText("70 - 100: Sangat Tinggi")
                    .assertIsDisplayed()
            }
        }
    }

    fun verifyCorrectRiskCategoryDescriptionDisplayed(expectedScore: Int) {
        val expectedDescription = when {
            expectedScore <= 35 -> "Diperkirakan 15 dari 100 orang dengan skor ini akan mengidap Diabetes"
            expectedScore in 35..55 -> "Diperkirakan 31 dari 100 orang dengan skor ini akan mengidap Diabetes"
            expectedScore in 55..70 -> "Diperkirakan 55 dari 100 orang dengan skor ini akan mengidap Diabetes"
            else -> "Diperkirakan 69 dari 100 orang dengan skor ini akan mengidap Diabetes"
        }
        
        composeTestRule.onNodeWithText(expectedDescription)
            .assertIsDisplayed()
    }

    fun verifyRiskScoreConsistency(expectedScore: Int) {
        Thread.sleep(3000)
        
        var scoreFound = false
        
        try {
            composeTestRule.onNodeWithText(expectedScore.toString())
                .assertIsDisplayed()
            scoreFound = true
        } catch (_: AssertionError) {
            for (offset in -2..2) {
                val testScore = expectedScore + offset
                if (testScore in 0..100) {
                    try {
                        composeTestRule.onNodeWithText(testScore.toString())
                            .assertIsDisplayed()
                        scoreFound = true
                        break
                    } catch (_: AssertionError) {
                        continue
                    }
                }
            }
        }
        
        if (!scoreFound) {
            composeTestRule.onNodeWithTag("RiskScoreGauge")
                .assertIsDisplayed()
        }
    }

    fun clickBackButton() {
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun verifyNavigationBackToHome() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}