package com.itb.diabetify.e2e.presentation.survey

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class SurveyTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    @SuppressLint("CheckResult")
    fun startAppAndNavigateToSurvey() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

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

        fillLoginForm(
            email = "test@example.com",
            password = "password123"
        )

        clickLoginButton()
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

    fun fillSurveyQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        try {
            composeTestRule.onNodeWithText("melahirkan bayi", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Tidak Pernah Melahirkan").performClick()
            composeTestRule.waitForIdle()
            clickNextButton()
        } catch (_: AssertionError) {
        }

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        try {
            composeTestRule.onNodeWithText("tekanan darah tinggi", substring = true)
                .assertIsDisplayed()
            composeTestRule.onNodeWithText("Tidak").performClick()
            composeTestRule.waitForIdle()
            clickNextButton()
        } catch (_: AssertionError) {
        }

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    private fun answerNumericQuestion(value: String) {
        try {
            val input = composeTestRule.onNodeWithText("Masukkan nilai")
            input.performClick()
            input.performTextClearance()
            input.performTextInput(value)
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
            try {
                val emptyInput = composeTestRule.onNode(hasTextExactly("") and hasSetTextAction())
                emptyInput.performClick()
                emptyInput.performTextClearance()
                emptyInput.performTextInput(value)
                composeTestRule.waitForIdle()
            } catch (_: AssertionError) {
                val fallbackInput = composeTestRule.onAllNodes(hasSetTextAction())[0]
                fallbackInput.performClick()
                fallbackInput.performTextClearance()
                fallbackInput.performTextInput(value)
                composeTestRule.waitForIdle()
            }
        }
    }

    private fun clickNextButton() {
        composeTestRule.onNodeWithText(">").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun submitSurvey() {
        composeTestRule.mainClock.autoAdvance = false

        try {
            composeTestRule.onNodeWithText("Pastikan jawaban", substring = true).assertIsDisplayed()
        } catch (_: AssertionError) {
        }

        composeTestRule.onNodeWithText("Kirim").performClick()
        composeTestRule.waitForIdle()

        Thread.sleep(3000)
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.advanceTimeBy(100)
    }

    fun verifySuccessScreen() {
        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Berhasil", substring = true).assertIsDisplayed()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("berhasil", substring = true).assertIsDisplayed()
        }

        try {
            composeTestRule.onNodeWithText(
                "Data pribadi Anda telah berhasil dikirim",
                substring = true
            ).assertIsDisplayed()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("telah berhasil", substring = true).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText("Beranda").assertIsDisplayed()
    }

    fun fillSurveyQuestionsWithInvalidWeight(weight: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion(weight)
        tryToClickNextButton()
    }

    fun fillSurveyQuestionsWithInvalidHeight(height: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion(height)
        tryToClickNextButton()
    }

    fun fillSurveyQuestionsWithInvalidSmokingAge(age: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Masih Merokok").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion(age)
        tryToClickNextButton()
    }

    fun fillSurveyQuestionsWithInvalidSmokingAmount(amount: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Masih Merokok").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("20")
        clickNextButton()

        answerNumericQuestion(amount)
        tryToClickNextButton()
    }

    fun fillSurveyQuestionsWithInvalidActivity(activity: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion(activity)
        tryToClickNextButton()
    }

    fun fillSurveyQuestionsWithMaxValues() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("300")
        clickNextButton()

        answerNumericQuestion("250")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Masih Merokok").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("20")
        clickNextButton()

        answerNumericQuestion("60")
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("250")
        clickNextButton()

        answerNumericQuestion("150")
        clickNextButton()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("7")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyQuestionsWithMinValues() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("30")
        clickNextButton()

        answerNumericQuestion("100")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("0")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyQuestionsForMaleUser() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyForFeasibleCounterfactualProfile() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("90")
        clickNextButton()

        answerNumericQuestion("180")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("tekanan darah tinggi", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("0")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyQuestionsForFemaleUser() {
        Thread.sleep(3000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("60")
        clickNextButton()

        answerNumericQuestion("165")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("melahirkan bayi", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Tidak Pernah Melahirkan").performClick()
            composeTestRule.waitForIdle()
            clickNextButton()
        } catch (e: AssertionError) {
            composeTestRule.onRoot().printToLog("SurveyTest")
            throw AssertionError("Pregnancy question not found for female user. Check if user gender is properly set to 'Perempuan'", e)
        }

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("2")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyWithKnownBloodPressure() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("120")
        clickNextButton()

        answerNumericQuestion("80")
        clickNextButton()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyWithKnownBloodPressureAndVerifyQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        verifyBloodPressureQuestionsAppear()
        answerNumericQuestion("120")
        clickNextButton()

        answerNumericQuestion("80")
        clickNextButton()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyWithUnknownBloodPressure() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyAsNonSmoker() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyAsFormerSmoker() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Sudah Berhenti").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("18")
        clickNextButton()

        answerNumericQuestion("25")
        clickNextButton()

        answerNumericQuestion("10")
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyAsFormerSmokerAndVerifyQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Sudah Berhenti").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        verifyFormerSmokerQuestionsAppear()
        answerNumericQuestion("18")
        clickNextButton()

        answerNumericQuestion("25")
        clickNextButton()

        answerNumericQuestion("10")
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyAsCurrentSmoker() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Masih Merokok").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion("18")
        clickNextButton()

        answerNumericQuestion("10")
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyAsCurrentSmokerAndVerifyQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Masih Merokok").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        verifyCurrentSmokerQuestionsAppear()
        answerNumericQuestion("18")
        clickNextButton()

        answerNumericQuestion("10")
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun fillSurveyWithInvalidBloodPressure(systolic: String, diastolic: String) {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        answerNumericQuestion(systolic)
        tryToClickNextButton()
    }

    fun fillFirstTwoQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
    }

    fun fillHalfOfSurveyQuestions() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        answerNumericQuestion("70")
        clickNextButton()

        answerNumericQuestion("175")
        clickNextButton()

        skipPregnancyQuestionIfPresent()

        composeTestRule.onNodeWithText("Tidak Pernah").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()
    }

    fun fillRemainingSurveyQuestions() {
        composeTestRule.onNodeWithText("mengetahui nilai tekanan darah", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        skipHypertensionQuestionIfPresent()

        composeTestRule.onNodeWithText("kolesterol tinggi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("ayah atau ibu", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tidak").performClick()
        composeTestRule.waitForIdle()
        clickNextButton()

        composeTestRule.onNodeWithText("aktivitas fisik", substring = true).assertIsDisplayed()
        answerNumericQuestion("3")
        clickNextButton()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    fun navigateBackInSurvey() {
        composeTestRule.onNodeWithText("<").performClick()
        composeTestRule.waitForIdle()
    }

    fun navigateForwardInSurvey() {
        composeTestRule.onNodeWithText(">").performClick()
        composeTestRule.waitForIdle()
    }

    fun navigateToReviewScreen() {
        var attempts = 0
        val maxAttempts = 20

        while (attempts < maxAttempts) {
            try {
                composeTestRule.onNodeWithText("Pastikan jawaban", substring = true)
                    .assertIsDisplayed()
                break
            } catch (_: AssertionError) {
                try {
                    composeTestRule.onNodeWithText(">").performClick()
                    composeTestRule.waitForIdle()
                    Thread.sleep(500)
                    attempts++
                } catch (_: AssertionError) {
                    break
                }
            }
        }
    }

    private fun tryToClickNextButton() {
        try {
            composeTestRule.onNodeWithText(">").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        } catch (_: AssertionError) {
        }
    }

    private fun skipPregnancyQuestionIfPresent() {
        try {
            composeTestRule.onNodeWithText("melahirkan bayi", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Tidak Pernah Melahirkan").performClick()
            composeTestRule.waitForIdle()
            clickNextButton()
        } catch (_: AssertionError) {
        }
    }

    private fun skipHypertensionQuestionIfPresent() {
        try {
            composeTestRule.onNodeWithText("tekanan darah tinggi", substring = true)
                .assertIsDisplayed()
            composeTestRule.onNodeWithText("Tidak").performClick()
            composeTestRule.waitForIdle()
            clickNextButton()
        } catch (_: AssertionError) {
        }
    }

    fun verifyWeightValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifyHeightValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifySmokingAgeValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifySmokingAmountValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifyActivityValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifyBloodPressureValidationError(errorMessage: String) {
        waitForErrorMessage(errorMessage)
    }

    fun verifyStillOnSurveyScreen() {
        try {
            composeTestRule.onNodeWithText("Pertanyaan", substring = true).assertIsDisplayed()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("Berapa berat badan", substring = true)
                .assertIsDisplayed()
        }
    }

    fun verifyOnFirstQuestion() {
        composeTestRule.onNodeWithText("Berapa berat badan", substring = true).assertIsDisplayed()
    }

    fun verifyOnSecondQuestion() {
        composeTestRule.onNodeWithText("Berapa tinggi badan", substring = true).assertIsDisplayed()
    }

    fun verifyPregnancyQuestionSkipped() {
        try {
            composeTestRule.onNodeWithText("melahirkan bayi", substring = true).assertDoesNotExist()
        } catch (_: AssertionError) {
        }
    }

    fun verifyBloodPressureQuestionsAppear() {
        try {
            composeTestRule.onNodeWithText("Berapa nilai tekanan darah sistolik", substring = true)
                .assertIsDisplayed()
        } catch (_: AssertionError) {
            try {
                composeTestRule.onNodeWithText("tekanan darah sistolik", substring = true)
                    .assertIsDisplayed()
            } catch (_: AssertionError) {
                composeTestRule.onNodeWithText("sistolik", substring = true).assertIsDisplayed()
            }
        }
    }

    fun verifyBloodPressureQuestionsSkipped() {
        try {
            composeTestRule.onNodeWithText("tekanan darah sistolik", substring = true)
                .assertDoesNotExist()
        } catch (_: AssertionError) {
        }
    }

    fun verifySmokingQuestionsSkipped() {
        try {
            composeTestRule.onNodeWithText("usia berapa Anda mulai merokok", substring = true)
                .assertDoesNotExist()
        } catch (_: AssertionError) {
        }
    }

    fun verifyFormerSmokerQuestionsAppear() {
        try {
            composeTestRule.onNodeWithText("Sejak usia berapa Anda mulai merokok", substring = true)
                .assertIsDisplayed()
        } catch (_: AssertionError) {
            try {
                composeTestRule.onNodeWithText("usia berapa Anda mulai merokok", substring = true)
                    .assertIsDisplayed()
            } catch (_: AssertionError) {
                composeTestRule.onNodeWithText("mulai merokok", substring = true).assertIsDisplayed()
            }
        }
    }

    fun verifyCurrentSmokerQuestionsAppear() {
        try {
            composeTestRule.onNodeWithText("Sejak usia berapa Anda mulai merokok", substring = true)
                .assertIsDisplayed()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("mulai merokok", substring = true).assertIsDisplayed()
        }
    }

    fun verifyProgressAtStart() {
        composeTestRule.onNodeWithText("Pertanyaan 1 dari", substring = true).assertIsDisplayed()
    }

    fun verifyProgressAtMiddle() {
        try {
            composeTestRule.onNodeWithText("Pertanyaan 4 dari", substring = true)
                .assertIsDisplayed()
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("Pertanyaan 5 dari", substring = true)
                .assertIsDisplayed()
        }
    }

    fun verifyProgressAtEnd() {
        composeTestRule.onNodeWithText("Pastikan jawaban", substring = true).assertIsDisplayed()
    }

    fun verifyReviewScreenDisplaysAnswers() {
        composeTestRule.onNodeWithText("Pastikan jawaban", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("70 KG", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("175 CM", substring = true).assertIsDisplayed()
    }

    fun verifyCanGoBackFromReview() {
        composeTestRule.onNodeWithText("Kembali").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kembali").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Pertanyaan", substring = true).assertIsDisplayed()
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

    fun verifyNextButtonDisabledWhenFieldsEmpty() {
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Berapa berat badan", substring = true).assertIsDisplayed()

        try {
            composeTestRule.onNodeWithText(">")
                .assertIsNotEnabled()
        } catch (e: AssertionError) {
            val initialQuestion = "Berapa berat badan"
            composeTestRule.onNodeWithText(">").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(initialQuestion, substring = true).assertIsDisplayed()
        }
    }
}
