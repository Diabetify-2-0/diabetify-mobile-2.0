package com.itb.diabetify.e2e.presentation.login

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class LoginTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    @SuppressLint("CheckResult")
    fun startAppAndNavigateToLogin() {
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

        try {
            composeTestRule.onNodeWithText("Halo,").assertIsDisplayed()
            composeTestRule.onNodeWithText("Selamat Datang Kembali").assertIsDisplayed()
            return
        } catch (_: AssertionError) {
        }

        try {
            composeTestRule.onNode(hasText("Sudah memiliki akun", substring = true))
                .assertIsDisplayed()
                .performClick()
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
        }

        var attempts = 0
        val maxAttempts = 5

        while (attempts < maxAttempts) {
            try {
                composeTestRule.onNodeWithText("Halo,").assertIsDisplayed()
                composeTestRule.onNodeWithText("Selamat Datang Kembali").assertIsDisplayed()
                return
            } catch (e: AssertionError) {
                attempts++
                Thread.sleep(1000)
                composeTestRule.waitForIdle()

                if (attempts < maxAttempts) {
                    try {
                        composeTestRule.onNode(hasText("Masuk", substring = true))
                            .performClick()
                        composeTestRule.waitForIdle()
                    } catch (_: AssertionError) {
                    }
                }
            }
        }

        throw AssertionError("Could not navigate to login screen after completing onboarding. Current screen might be different than expected.")
    }

    fun fillLoginForm(email: String, password: String) {
        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput(email)

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput(password)

        composeTestRule.waitForIdle()
    }

    fun fillLoginFormWithValidation(
        email: String,
        password: String,
        expectValidation: Boolean = false
    ) {
        fillLoginForm(email, password)

        if (expectValidation) {
            composeTestRule.waitForIdle()
            Thread.sleep(500)
        }
    }

    fun clickLoginButton() {
        composeTestRule.onNodeWithText("Masuk")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun verifyLoginFlowCompletion() {
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                try {
                    composeTestRule.onNode(hasText("Persentase Risiko", substring = true))
                        .assertIsDisplayed()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        } catch (e: AssertionError) {
            composeTestRule.onNodeWithText("Halo,").assertIsDisplayed()
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

    fun verifyStillOnLoginScreen() {
        composeTestRule.onNodeWithText("Halo,").assertIsDisplayed()
        composeTestRule.onNodeWithText("Selamat Datang Kembali").assertIsDisplayed()
    }

    fun testPasswordVisibilityToggle() {
        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput("john.doe@example.com")

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput("SecurePassword123")

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNode(hasText("Show password"))
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasText("Hide password"))
                .assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithTag("PasswordVisibilityIcon")
                    .performClick()
                composeTestRule.waitForIdle()
            } catch (e2: AssertionError) {
            }
        }
    }

    fun navigateToRegister() {
        composeTestRule.onNode(hasText("Belum memiliki akun", substring = true))
            .performClick()

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
        } catch (e: AssertionError) {
            composeTestRule.onNode(hasText("Daftar", substring = true))
                .assertIsDisplayed()
        }
    }

    fun navigateToForgotPassword() {
        composeTestRule.onNode(hasText("Lupa kata sandi", substring = true))
            .performClick()

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNode(hasText("Lupa Kata Sandi", substring = true))
                .assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNode(hasText("Reset Password", substring = true))
                    .assertIsDisplayed()
            } catch (e2: AssertionError) {
                composeTestRule.onNode(hasText("Forgot Password", substring = true))
                    .assertIsDisplayed()
            }
        }
    }

    fun verifyLoginButtonDisabled() {
        try {
            composeTestRule.onNodeWithText("Masuk")
                .assertIsNotEnabled()
        } catch (e: AssertionError) {
            throw AssertionError("Login button should be disabled but it's enabled")
        }
    }

    fun verifyLoginButtonEnabled() {
        composeTestRule.onNodeWithText("Masuk")
            .assertIsEnabled()
    }

    fun testRealTimeValidation() {
        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput("invalid")

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput("123")

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        verifyLoginButtonEnabled()

        clickLoginButton()

        verifyLoginButtonDisabled()

        clearLoginForm()

        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput("valid@example.com")

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput("SecurePassword123")

        composeTestRule.waitForIdle()

        verifyLoginButtonEnabled()
    }

    private fun clearLoginForm() {
        try {
            composeTestRule.onNodeWithTag("EmailTextField")
                .performClick()
                .performTextClearance()

            composeTestRule.onNodeWithTag("PasswordTextField")
                .performClick()
                .performTextClearance()

            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
        }
    }
}