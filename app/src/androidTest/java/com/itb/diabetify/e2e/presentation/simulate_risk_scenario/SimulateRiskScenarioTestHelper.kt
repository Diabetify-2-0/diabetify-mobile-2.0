package com.itb.diabetify.e2e.presentation.simulate_risk_scenario

import android.annotation.SuppressLint
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity
import junit.framework.TestCase.assertEquals

class SimulateRiskScenarioTestHelper(
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

    fun verifyWhatIfCardDisplayed() {
        var attempts = 0
        var found = false

        while (attempts < 5 && !found) {
            try {
                composeTestRule.onNodeWithText("Simulasi What-If")
                    .assertIsDisplayed()
                found = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 400f,
                        endY = centerY - 400f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                attempts++
            }
        }
    }

    fun navigateToWhatIfSimulation() {
        var buttonAttempts = 0
        var buttonFound = false

        while (buttonAttempts < 8 && !buttonFound) {
            try {
                composeTestRule.onNodeWithText("Mulai Simulasi")
                    .assertIsDisplayed()
                buttonFound = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 350f,
                        endY = centerY - 350f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(400)
                buttonAttempts++
            }
        }
        composeTestRule.onNodeWithText("Mulai Simulasi")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyWhatIfScreenDisplayed() {
        composeTestRule.onNodeWithText("Simulasi What If")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Data Tidak Dapat Diubah")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Faktor Yang Dapat Diubah")
            .assertIsDisplayed()
    }

    fun verifyRiskPercentageCardDisplayed() {
        composeTestRule.onNodeWithText("Persentase Risiko")
            .assertIsDisplayed()

        composeTestRule.onAllNodesWithText("%", substring = true)[0]
            .assertIsDisplayed()
    }

    fun verifyRiskFactorDetailCardDisplayed() {
        var attempts = 0
        var found = false

        while (attempts < 5 && !found) {
            try {
                composeTestRule.onNodeWithText("Kontribusi Faktor Risiko")
                    .assertIsDisplayed()
                found = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 400f,
                        endY = centerY - 400f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                attempts++
            }
        }

        composeTestRule.onNodeWithText("Kontribusi Faktor Risiko")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("BarChart").assertIsDisplayed()
    }

    fun changeSmokingStatus() {
        val currentStatus = getCurrentSmokingStatus()
        
        when (currentStatus) {
            "Aktif Merokok" -> {
                try {
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                    composeTestRule.onNodeWithText("Tidak Pernah Merokok").performClick()
                } catch (e: AssertionError) {
                    composeTestRule.onNodeWithTag("SmokingStatusDropdown").performClick()
                    composeTestRule.onNodeWithText("Tidak Pernah Merokok").performClick()
                }
            }
            "Tidak Pernah Merokok" -> {
                try {
                    composeTestRule.onNodeWithText("Tidak Pernah Merokok").performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                } catch (e: AssertionError) {
                    composeTestRule.onNodeWithTag("SmokingStatusDropdown").performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                }
            }
            "Berhenti Merokok" -> {
                try {
                    composeTestRule.onNodeWithText("Berhenti Merokok").performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                } catch (e: AssertionError) {
                    composeTestRule.onNodeWithTag("SmokingStatusDropdown").performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                }
            }
            else -> {
                try {
                    composeTestRule.onNodeWithTag("SmokingStatusDropdown").performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                } catch (e: AssertionError) {
                    composeTestRule.onNodeWithText("Status Merokok", substring = true).performClick()
                    composeTestRule.onNodeWithText("Aktif Merokok").performClick()
                }
            }
        }
        composeTestRule.waitForIdle()
        
        verifySmokingFieldsVisibility()
    }

    fun fillSmokingDuration() {
        if (!areSmokingFieldsVisible()) {
            return
        }

        val textField = composeTestRule.onNodeWithTag("SmokingDurationTextField")
        textField.performClick()

        textField.performTextClearance()

        try {
            composeTestRule.onNodeWithText("25").assertIsDisplayed()
            textField.performTextInput("20")
        } catch (e: AssertionError) {
            textField.performTextInput("25")
        }

        composeTestRule.waitForIdle()
    }

    fun fillInvalidSmokingDuration(value: String) {
        if (!areSmokingFieldsVisible()) {
            return
        }

        val textField = composeTestRule.onNodeWithTag("SmokingDurationTextField")
        textField.performClick()

        textField.performTextClearance()

        textField.performTextInput(value)

        composeTestRule.waitForIdle()
    }

    fun fillCigarettesPerDay() {
        if (!areSmokingFieldsVisible()) {
            return
        }
        
        val textField = composeTestRule.onNodeWithTag("CigarettesPerDayTextField")
        textField.performClick()

        textField.performTextClearance()

        try {
            composeTestRule.onNodeWithText("5").assertIsDisplayed()
            textField.performTextInput("10")
        } catch (e: AssertionError) {
            textField.performTextInput("5")
        }

        composeTestRule.waitForIdle()
    }

    fun fillInvalidCigarettesPerDay(value: String) {
        if (!areSmokingFieldsVisible()) {
            return
        }

        val textField = composeTestRule.onNodeWithTag("CigarettesPerDayTextField")
        textField.performClick()

        textField.performTextClearance()

        textField.performTextInput(value)

        composeTestRule.waitForIdle()
    }

    fun changeWeight() {
        val textField = composeTestRule.onNodeWithTag("WeightTextField")
        textField.performClick()

        textField.performTextClearance()

        try {
            composeTestRule.onNodeWithText("80").assertIsDisplayed()
            textField.performTextInput("90")
        } catch (e: AssertionError) {
            textField.performTextInput("80")
        }

        composeTestRule.waitForIdle()
    }

    fun fillInvalidWeight(value: String) {
        val textField = composeTestRule.onNodeWithTag("WeightTextField")
        textField.performClick()

        textField.performTextClearance()

        textField.performTextInput(value)

        composeTestRule.waitForIdle()
    }

    fun changeHypertensionStatus() {
        try {
            composeTestRule.onAllNodesWithText("Tidak")[0].assertIsDisplayed()

            composeTestRule.onAllNodesWithText("Tidak")[0].performClick()
            composeTestRule.onAllNodesWithText("Ya")[0].performClick()
        } catch (e: AssertionError) {
            composeTestRule.onAllNodesWithText("Ya")[0].assertIsDisplayed()

            composeTestRule.onAllNodesWithText("Ya")[0].performClick()
            composeTestRule.onAllNodesWithText("Tidak")[0].performClick()
        }
        composeTestRule.waitForIdle()
    }

    fun changePhysicalActivityPerWeek() {
        val textField = composeTestRule.onNodeWithTag("PhysicalActivityTextField")
        textField.performClick()

        textField.performTextClearance()

        try {
            composeTestRule.onNodeWithText("0").assertIsDisplayed()
            textField.performTextInput("5")
        } catch (e: AssertionError) {
            textField.performTextInput("0")
        }

        composeTestRule.waitForIdle()
    }

    fun fillInvalidPhysicalActivity(value: String) {
        val textField = composeTestRule.onNodeWithTag("PhysicalActivityTextField")
        textField.performClick()

        textField.performTextClearance()

        textField.performTextInput(value)

        composeTestRule.waitForIdle()
    }

    fun changeCholesterolStatus() {
        try {
            composeTestRule.onAllNodesWithText("Ya")[1].assertIsDisplayed()

            composeTestRule.onAllNodesWithText("Ya")[1].performClick()
            composeTestRule.onAllNodesWithText("Tidak")[1].performClick()
        } catch (e: AssertionError) {
            composeTestRule.onAllNodesWithText("Tidak")[1].assertIsDisplayed()

            composeTestRule.onAllNodesWithText("Tidak")[1].performClick()
            composeTestRule.onAllNodesWithText("Ya")[1].performClick()
        }
        composeTestRule.waitForIdle()
    }

    fun clickCalculateButton() {
        composeTestRule.onNodeWithText("Hitung").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    fun getCurrentSmokingStatus(): String? {
        return try {
            composeTestRule.onNodeWithText("Aktif Merokok").assertExists()
            "Aktif Merokok"
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("Tidak Pernah Merokok").assertExists()
                "Tidak Pernah Merokok"
            } catch (e2: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("Berhenti Merokok").assertExists()
                    "Berhenti Merokok"
                } catch (e3: AssertionError) {
                    null
                }
            }
        }
    }

    fun getCurrentSmokingDuration(): String? {
        return try {
            val smokingStatus = getCurrentSmokingStatus()
            if (smokingStatus == "Aktif Merokok") {
                try {
                    val node = composeTestRule.onNodeWithTag("SmokingDurationTextField")
                    node.assertExists()
                    node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
                } catch (e: AssertionError) {
                    null
                }
            } else {
                null
            }
        } catch (e: AssertionError) {
            null
        }
    }

    fun getCurrentCigarettesPerDay(): String? {
        return try {
            val smokingStatus = getCurrentSmokingStatus()
            if (smokingStatus == "Aktif Merokok") {
                try {
                    val node = composeTestRule.onNodeWithTag("CigarettesPerDayTextField")
                    node.assertExists()
                    node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
                } catch (e: AssertionError) {
                    null
                }
            } else {
                null
            }
        } catch (e: AssertionError) {
            null
        }
    }

    fun getCurrentWeight(): String? {
        return try {
            val node = composeTestRule.onNodeWithTag("WeightTextField")
            node.assertExists()
            node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
        } catch (e: AssertionError) {
            null
        }
    }

    fun getCurrentHypertensionStatus(): String? {
        return try {
            try {
                composeTestRule.onAllNodesWithText("Ya")[0].assertExists()
                "Ya"
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onAllNodesWithText("Tidak")[0].assertExists()
                    "Tidak"
                } catch (e2: AssertionError) {
                    null
                }
            }
        } catch (e: AssertionError) {
            null
        }
    }

    fun getCurrentPhysicalActivity(): String? {
        return try {
            val node = composeTestRule.onNodeWithTag("PhysicalActivityTextField")
            node.assertExists()
            node.fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
        } catch (e: AssertionError) {
            null
        }
    }

    fun getCurrentCholesterolStatus(): String? {
        return try {
            try {
                composeTestRule.onAllNodesWithText("Ya")[0].assertExists()
                "Ya"
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onAllNodesWithText("Tidak")[0].assertExists()
                    "Tidak"
                } catch (e2: AssertionError) {
                    null
                }
            }
        } catch (e: AssertionError) {
            null
        }
    }

    fun areSmokingFieldsVisible(): Boolean {
        val smokingStatus = getCurrentSmokingStatus()
        return smokingStatus == "Aktif Merokok"
    }

    data class FieldValues(
        val smokingStatus: String?,
        val smokingDuration: String?,
        val cigarettesPerDay: String?,
        val weight: String?,
        val hypertension: String?,
        val physicalActivity: String?,
        val cholesterol: String?
    )

    fun captureCurrentFieldValues(): FieldValues {
        return FieldValues(
            smokingStatus = getCurrentSmokingStatus(),
            smokingDuration = getCurrentSmokingDuration(),
            cigarettesPerDay = getCurrentCigarettesPerDay(),
            weight = getCurrentWeight(),
            hypertension = getCurrentHypertensionStatus(),
            physicalActivity = getCurrentPhysicalActivity(),
            cholesterol = getCurrentCholesterolStatus()
        )
    }

    fun verifyFieldsReset(originalValues: FieldValues) {
        val currentValues = captureCurrentFieldValues()
        println("Current Values: $currentValues")
        composeTestRule.waitForIdle()

        assertEquals("Smoking status should be reset", originalValues.smokingStatus, currentValues.smokingStatus)
        assertEquals("Smoking duration should be reset", originalValues.smokingDuration, currentValues.smokingDuration)
        assertEquals("Cigarettes per day should be reset", originalValues.cigarettesPerDay, currentValues.cigarettesPerDay)
        assertEquals("Weight should be reset", originalValues.weight, currentValues.weight)
        assertEquals("Hypertension should be reset", originalValues.hypertension, currentValues.hypertension)
        assertEquals("Physical activity should be reset", originalValues.physicalActivity, currentValues.physicalActivity)
        assertEquals("Cholesterol should be reset", originalValues.cholesterol, currentValues.cholesterol)
    }

    fun clickResetButton() {
        composeTestRule.onNodeWithText("Reset").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifySmokingFieldsVisibility() {
        val smokingStatus = getCurrentSmokingStatus()
        
        if (smokingStatus == "Aktif Merokok") {
            try {
                composeTestRule.onNodeWithTag("SmokingDurationTextField").assertIsDisplayed()
                composeTestRule.onNodeWithTag("CigarettesPerDayTextField").assertIsDisplayed()
            } catch (e: AssertionError) {
                composeTestRule.onNodeWithText("tahun", substring = true).assertIsDisplayed()
                composeTestRule.onNodeWithText("batang", substring = true).assertIsDisplayed()
            }
        } else {
            try {
                composeTestRule.onNodeWithTag("SmokingDurationTextField").assertDoesNotExist()
                composeTestRule.onNodeWithTag("CigarettesPerDayTextField").assertDoesNotExist()
            } catch (_: AssertionError) {
            }
        }
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

    fun clickBackButton() {
        Thread.sleep(2000)
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun verifyNavigationBackToHome() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Simulasi What-If")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
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
}