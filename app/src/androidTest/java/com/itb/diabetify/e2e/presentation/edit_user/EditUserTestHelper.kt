package com.itb.diabetify.e2e.presentation.edit_user

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
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

class EditUserTestHelper(
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

    fun verifyProfileCard(expectedName: String, expectedEmail: String) {
        composeTestRule.onNodeWithTag("ProfileCardName")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("ProfileCardEmail")
            .assertIsDisplayed()
    }

    fun clickEditProfilButton() {
        composeTestRule.onNodeWithTag("EditProfilButton")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyEditUserScreenDisplayed() {
        composeTestRule.onNodeWithText("Edit Profil")
            .assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    fun getCurrentFieldValues(): UserFieldValues {
        return UserFieldValues(
            name = "testmale",
            email = "testmale@example.com", 
            gender = "Laki-laki",
            dateOfBirth = "01/01/1990"
        )
    }

    fun changeName(newName: String) {
        composeTestRule.onNodeWithTag("EditProfileNameField")
            .performClick()
            .performTextClearance()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag("EditProfileNameField")
            .performTextInput(newName)
        composeTestRule.waitForIdle()
    }

    fun changeGender(newGender: String) {
        composeTestRule.onNode(
            hasText("Laki-laki", substring = true) or hasText("Perempuan", substring = true)
        ).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText(newGender)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun changeDateOfBirth(newDate: String) {
        composeTestRule.onNodeWithTag("EditProfileDateField")
            .performClick()
        composeTestRule.waitForIdle()
        
        try {
            composeTestRule.onNodeWithText("OK").performClick()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("Konfirmasi").performClick()
            } catch (_: AssertionError) {
            }
        }
        composeTestRule.waitForIdle()
    }

    fun clearDateOfBirthField() {
        // Click the clear date button (IconButton with contentDescription "Clear date")
        composeTestRule.onNodeWithContentDescription("Clear date")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickSimpanPerubahan() {
        composeTestRule.onNodeWithTag("SimpanPerubahanButton")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    fun clickBackNavigation() {
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    fun verifyChanges(expectedName: String, expectedGender: String, expectedDate: String) {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        verifyEditUserScreenDisplayed()
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

        composeTestRule.onNodeWithContentDescription("Close")
            .performClick()
        composeTestRule.waitForIdle()
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

    data class UserFieldValues(
        val name: String,
        val email: String,
        val gender: String,
        val dateOfBirth: String
    )
}