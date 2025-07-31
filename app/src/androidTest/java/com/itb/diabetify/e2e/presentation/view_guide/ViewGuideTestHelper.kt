package com.itb.diabetify.e2e.presentation.view_guide

import android.annotation.SuppressLint
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

class ViewGuideTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    @SuppressLint("CheckResult")
    fun startAppAndNavigateToGuide() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()

            composeTestRule.onNodeWithContentDescription(
                "Navigate to guide screen"
            ).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Panduan")
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
            "Navigate to guide screen"
        ).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Panduan")
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
                startY = centerY + 200f,
                endY = centerY - 200f
            )
        }
        composeTestRule.waitForIdle()
    }

    fun verifyDiabetesAndXAISection() {
        composeTestRule.onNodeWithText("Diabetes dan XAI")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Tentang Diabetes")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Faktor Risiko")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tentang XAI")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Perhitungan AI")
            .assertIsDisplayed()
    }

    fun clickOnAboutDiabetesCard() {
        composeTestRule.onNodeWithText("Tentang Diabetes")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyGuideDetailScreen() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    fun navigateBackToGuide() {
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Panduan")
            .assertIsDisplayed()
    }

    fun verifyTipsKesehatanSection() {
        composeTestRule.onNodeWithText("Tips Kesehatan")
            .assertIsDisplayed()
        
        var tipsFound = false
        val tipsTitles = listOf(
            "Rekomendasi Nutrisi Sehat",
            "Rekomendasi Olahraga", 
            "Tips Berhenti Merokok",
            "Mengelola Hipertensi",
            "Mengelola Kolesterol"
        )
        
        for (title in tipsTitles) {
            try {
                composeTestRule.onNodeWithText(title)
                    .assertIsDisplayed()
                tipsFound = true
                break
            } catch (_: AssertionError) {
            }
        }
        
        if (!tipsFound) {
            throw AssertionError("No tips cards found in Tips Kesehatan section")
        }
    }

    fun clickOnFirstTipsCard() {
        val tipsTitles = listOf(
            "Rekomendasi Nutrisi Sehat",
            "Rekomendasi Olahraga", 
            "Tips Berhenti Merokok",
            "Mengelola Hipertensi",
            "Mengelola Kolesterol"
        )
        
        for (title in tipsTitles) {
            try {
                composeTestRule.onNodeWithText(title)
                    .assertIsDisplayed()
                composeTestRule.onNodeWithText(title)
                    .performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
                return
            } catch (_: AssertionError) {
            }
        }
        
        throw AssertionError("Could not find and click any tips card")
    }

    fun verifyTipsDetailScreen() {
        composeTestRule.onNodeWithText("Tips")
            .assertIsDisplayed()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    fun verifyFAQSection() {
        composeTestRule.onNodeWithText("FAQ")
            .assertIsDisplayed()
        
        val faqQuestions = listOf(
            "Seberapa akurat prediksi risiko diabetes dari aplikasi ini?",
            "Apakah hasil prediksi bisa menggantikan konsultasi dokter?",
            "Apakah data pribadi saya aman di aplikasi ini?",
            "Seberapa sering saya harus memperbarui data kesehatan?",
            "Apa yang harus dilakukan jika hasil prediksi menunjukkan risiko tinggi?",
            "Apakah aplikasi ini cocok untuk semua usia?"
        )
        
        var faqFound = false
        for (question in faqQuestions) {
            try {
                composeTestRule.onNodeWithText(question, substring = true)
                    .assertIsDisplayed()
                faqFound = true
                println("FAQ found: $question")
                break
            } catch (_: AssertionError) {
            }
        }
        
        if (!faqFound) {
            throw AssertionError("No FAQ cards found in FAQ section")
        }
    }

    fun clickOnFirstFAQCard() {
        val faqQuestion = "Seberapa akurat estimasi risiko diabetes dari aplikasi ini?"

        composeTestRule.onNodeWithText(faqQuestion, substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(faqQuestion, substring = true)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun clickOnSecondFAQCard() {
        val faqQuestion = "Apakah hasil estimasi risiko bisa menggantikan konsultasi dokter?"

        composeTestRule.onNodeWithText(faqQuestion, substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(faqQuestion, substring = true)
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyFAQAnswerDisplayed() {
        val answerKeyword = "Tidak. Hasil estimasi risiko Diabetify adalah alat bantu untuk mengetahui risiko diabetes, bukan pengganti konsultasi medis profesional."

        composeTestRule.onNodeWithText(answerKeyword, substring = true)
            .assertIsDisplayed()
    }
}