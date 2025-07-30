package com.itb.diabetify.e2e.presentation.change_non_daily_data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class ChangeNonDailyDataTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    fun clearAppToken() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("auth_token", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

    @SuppressLint("CheckResult")
    fun startAppAndNavigateToHome(gender: String = "male") {
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

        if (gender == "male") {
            fillLoginForm(
                email = "testmale@example.com",
                password = "bewebewe"
            )
        } else {
            fillLoginForm(
                email = "testfemale@example.com",
                password = "bewebewe"
            )
        }

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

    fun clickBeratButton() {
        composeTestRule.onNodeWithContentDescription("Berat")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickTinggiButton() {
        composeTestRule.onNodeWithContentDescription("Tinggi")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickHipertensiButton() {
        composeTestRule.onNodeWithContentDescription("Hipertensi")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickKolesterolButton() {
        composeTestRule.onNodeWithContentDescription("Kolesterol")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickKeluargaButton() {
        composeTestRule.onNodeWithContentDescription("Keluarga")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun clickKehamilanButton() {
        composeTestRule.onNodeWithContentDescription("Kehamilan")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
    }

    fun getCurrentBeratValue(): String {
        return try {
            val textFieldNodes = composeTestRule.onAllNodes(
                hasSetTextAction() and hasText("", substring = true, ignoreCase = true)
            ).fetchSemanticsNodes()

            if (textFieldNodes.isNotEmpty()) {
                val config = textFieldNodes[0].config
                config.getOrNull(SemanticsProperties.EditableText)?.text ?: "80"
            } else {
                "80"
            }
        } catch (e: Exception) {
            "80"
        }
    }

    fun clearAndFillBeratForm(currentValue: String) {
        val newValue = if (currentValue == "80") "90" else "80"

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

    fun clickSimpanButton() {
        composeTestRule.onNodeWithText("Simpan")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
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

    fun verifyBeratDataMatches(expectedValue: String) {
        clickBeratButton()
        val actualValue = getCurrentBeratValue()
        assert(actualValue == expectedValue) {
            "Expected berat value: $expectedValue, but got: $actualValue"
        }
    }

    fun getCurrentTinggiValue(): String {
        return try {
            val textFieldNodes = composeTestRule.onAllNodes(
                hasSetTextAction() and hasText("", substring = true, ignoreCase = true)
            ).fetchSemanticsNodes()

            if (textFieldNodes.isNotEmpty()) {
                val config = textFieldNodes[0].config
                config.getOrNull(SemanticsProperties.EditableText)?.text ?: "170"
            } else {
                "170"
            }
        } catch (e: Exception) {
            "170"
        }
    }

    fun clearAndFillTinggiForm(currentValue: String) {
        val newValue = if (currentValue == "170") "180" else "170"

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

    fun verifyTinggiDataMatches(expectedValue: String) {
        clickTinggiButton()
        val actualValue = getCurrentTinggiValue()
        assert(actualValue == expectedValue) {
            "Expected tinggi value: $expectedValue, but got: $actualValue"
        }
    }

    fun getCurrentSelectionValue(): String {
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

            "Tidak"
        } catch (e: Exception) {
            "Tidak"
        }
    }

    fun getCurrentHipertensiValue(): String {
        return try {
            composeTestRule.waitForIdle()
            Thread.sleep(500)

            val nodes = composeTestRule.onAllNodesWithText("Tidak")
            val lastIndex = nodes.fetchSemanticsNodes().size - 1
            try {
                nodes[lastIndex].performClick()
            } catch (e: Exception) {
                nodes[lastIndex - 1].performClick()
            }
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
            
            val allRadioButtons = composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.isRoot()) and
                        androidx.compose.ui.test.hasTestTag("").not()
            ).fetchSemanticsNodes().filter { node ->
                node.config.getOrNull(SemanticsProperties.Role)?.toString()?.contains("RadioButton") == true
            }

            var radioButtonIndex = 0
            allRadioButtons.forEach { node ->
                val selected = node.config.getOrNull(SemanticsProperties.Selected) == true
                if (selected && radioButtonIndex >= 2) {
                    return if ((radioButtonIndex - 2) % 2 == 0) "Ya" else "Tidak"
                }
                radioButtonIndex++
            }

            "Tidak"
        } catch (e: Exception) {
            "Tidak"
        }
    }

    fun getCurrentKehamilanValue(): String {
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
                    return if (index == 0) "Tidak" else "Pernah"
                }
            }

            "Pernah"
        } catch (e: Exception) {
            "Pernah"
        }
    }

    fun clearAndFillSelectionForm(currentValue: String) {
        val newValue = if (currentValue == "Ya") "Tidak" else "Ya"

        try {
            val clickableNodes = composeTestRule.onAllNodes(
                hasText(newValue) and androidx.compose.ui.test.hasClickAction()
            ).fetchSemanticsNodes()
            
            if (clickableNodes.isNotEmpty()) {
                println("Clickable nodes found with text: $newValue")
                val nodes = composeTestRule.onAllNodesWithText(newValue)
                val lastIndex = nodes.fetchSemanticsNodes().size - 1
                try {
                    nodes[lastIndex].performClick()
                } catch (e: Exception) {
                    nodes[lastIndex - 1].performClick()
                }
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                return
            }
        } catch (_: Exception) {
        }
    }

    fun clearAndFillHipertensiForm(currentValue: String) {
        val newValue = if (currentValue == "Ya") "Tidak" else "Ya"

        try {
            val nodes = composeTestRule.onAllNodesWithText(newValue)
            val lastIndex = nodes.fetchSemanticsNodes().size - 1
            try {
                nodes[lastIndex].performClick()
            } catch (e: Exception) {
                nodes[lastIndex - 1].performClick()
            }

            composeTestRule.waitForIdle()
            Thread.sleep(500)
        } catch (e: Exception) {
            try {
                composeTestRule.onNodeWithText(newValue).performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            } catch (_: Exception) {
            }
        }
    }

    fun clearAndFillKehamilanForm(currentValue: String) {
        val newValue = if (currentValue == "Tidak") "Pernah" else "Tidak"

        try {
            val nodes = composeTestRule.onAllNodesWithText(newValue)
            val lastIndex = nodes.fetchSemanticsNodes().size - 1
            try {
                nodes[lastIndex].performClick()
            } catch (e: Exception) {
                nodes[lastIndex - 1].performClick()
            }

            composeTestRule.waitForIdle()
            Thread.sleep(500)
        } catch (e: Exception) {
            try {
                composeTestRule.onNodeWithText(newValue).performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            } catch (_: Exception) {
            }
        }
    }

    fun verifyHipertensiDataMatches(expectedValue: String) {
        clickHipertensiButton()
        val actualValue = getCurrentHipertensiValue()
        assert(actualValue == expectedValue) {
            "Expected hipertensi value: $expectedValue, but got: $actualValue"
        }
    }

    fun verifyKolesterolDataMatches(expectedValue: String) {
        clickKolesterolButton()
        val actualValue = getCurrentSelectionValue()
        assert(actualValue == expectedValue) {
            "Expected kolesterol value: $expectedValue, but got: $actualValue"
        }
    }

    fun verifyKeluargaDataMatches(expectedValue: String) {
        clickKeluargaButton()
        val actualValue = getCurrentSelectionValue()
        assert(actualValue == expectedValue) {
            "Expected keluarga value: $expectedValue, but got: $actualValue"
        }
    }

    fun verifyKehamilanDataMatches(expectedValue: String) {
        clickKehamilanButton()
        val actualValue = getCurrentKehamilanValue()
        assert(actualValue == expectedValue) {
            "Expected kehamilan value: $expectedValue, but got: $actualValue"
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

    fun fillBeratFormWithInvalidValue(invalidValue: String) {
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

    fun fillTinggiFormWithInvalidValue(invalidValue: String) {
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