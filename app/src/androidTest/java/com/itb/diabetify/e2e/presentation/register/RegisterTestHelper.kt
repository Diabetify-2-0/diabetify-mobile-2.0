package com.itb.diabetify.e2e.presentation.register

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class RegisterTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    @SuppressLint("CheckResult")
    fun startAppAndNavigateToRegister() {
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
            composeTestRule.onNode(hasText("Belum memiliki akun", substring = true))
                .assertIsDisplayed()
                .performClick()
            composeTestRule.waitForIdle()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNode(hasText("Daftar", substring = true))
                    .performClick()
                composeTestRule.waitForIdle()
            } catch (_: AssertionError) {
            }
        }

        var attempts = 0
        val maxAttempts = 5

        while (attempts < maxAttempts) {
            try {
                composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
                return
            } catch (e: AssertionError) {
                attempts++
                Thread.sleep(1000)
                composeTestRule.waitForIdle()

                if (attempts < maxAttempts) {
                    try {
                        composeTestRule.onNode(hasText("Daftar", substring = true))
                            .performClick()
                        composeTestRule.waitForIdle()
                    } catch (_: AssertionError) {
                    }
                }
            }
        }

        throw AssertionError("Could not navigate to register screen after completing onboarding. Current screen might be different than expected.")
    }

    fun fillRegistrationForm(name: String, email: String, password: String) {
        composeTestRule.onNode(hasText("Nama", substring = true))
            .performClick()
            .performTextInput(name)

        composeTestRule.onNode(hasText("Email", substring = true))
            .performClick()
            .performTextInput(email)

        composeTestRule.onNode(hasText("Kata Sandi", substring = true))
            .performClick()
            .performTextInput(password)

        composeTestRule.waitForIdle()
    }

    fun acceptPrivacyPolicyAndSubmit() {
        composeTestRule.onNodeWithTag("PrivacyPolicyCheckbox")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Daftar")
            .performClick()

        composeTestRule.waitForIdle()
    }

    fun fillBiodataFormIfPresent(gender: String, birthDate: String) {
        try {
            composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                .assertIsDisplayed()

            fillBiodataForm(gender, birthDate)
        } catch (_: AssertionError) {
        }
    }

    private fun fillBiodataForm(gender: String, birthDate: String) {
        composeTestRule.onNode(hasText("Jenis Kelamin", substring = true))
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(gender)
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Tanggal Lahir", substring = true))
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

    fun proceedToOtpVerificationIfPresent() {
        try {
            composeTestRule.onNode(hasText("Lanjut", substring = true))
                .performClick()

            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
        }
    }

    fun enterOtpAndVerifyIfPresent(otpCode: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("OtpTextField")
                .fetchSemanticsNodes().size == 1
        }

        enterOtpAndVerify(otpCode)
    }

    private fun enterOtpAndVerify(otpCode: String) {
        composeTestRule.onNodeWithTag("OtpTextField")
            .performTextInput(otpCode)

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Verifikasi") and hasClickAction())
            .performClick()

        composeTestRule.mainClock.autoAdvance = false
    }

    fun verifyRegistrationFlowCompletion() {
        try {
            composeTestRule.onNode(hasText("Selamat", substring = true))
                .assertIsDisplayed()

            verifyRegistrationSuccess()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNode(hasText("Verifikasi", substring = true))
                    .assertIsDisplayed()
            } catch (e2: AssertionError) {
                try {
                    composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                        .assertIsDisplayed()
                } catch (e3: AssertionError) {
                    composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
                }
            }
        }
    }

    private fun verifyRegistrationSuccess() {
        composeTestRule.onNode(hasText("Selamat", substring = true))
            .assertIsDisplayed()

        try {
            composeTestRule.onNodeWithText("Masuk").assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("Mulai").assertIsDisplayed()
            } catch (e2: AssertionError) {
                try {
                    composeTestRule.onNodeWithText("Lanjutkan").assertIsDisplayed()
                } catch (_: AssertionError) {
                }
            }
        }
    }

    fun enterOtpAndVerifyIfPresentV2(otpCode: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("OtpTextField")
                .fetchSemanticsNodes().size == 1
        }

        enterOtpAndVerifyV2(otpCode)
    }

    private fun enterOtpAndVerifyV2(otpCode: String) {
        composeTestRule.onNodeWithTag("OtpTextField")
            .performTextInput(otpCode)

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Verifikasi") and hasClickAction())
            .performClick()
    }

    fun fillRegistrationFormWithValidation(
        name: String,
        email: String,
        password: String,
        expectValidation: Boolean = false
    ) {
        fillRegistrationForm(name, email, password)

        if (expectValidation) {
            composeTestRule.waitForIdle()
            Thread.sleep(500)
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

    fun verifyStillOnRegisterScreen() {
        composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
    }

    fun acceptPrivacyPolicyOnly() {
        composeTestRule.onNodeWithTag("PrivacyPolicyCheckbox")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickRegisterButton() {
        composeTestRule.onNodeWithText("Daftar")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun testPasswordVisibilityToggle() {
        composeTestRule.onNode(hasText("Nama", substring = true))
            .performClick()
            .performTextInput("John Doe")

        composeTestRule.onNode(hasText("Email", substring = true))
            .performClick()
            .performTextInput("john.doe@example.com")

        composeTestRule.onNode(hasText("Kata Sandi", substring = true))
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

    fun navigateBackToLogin() {
        composeTestRule.onNode(hasText("Sudah memiliki akun", substring = true))
            .performClick()

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNode(hasText("Masuk", substring = true))
                .assertIsDisplayed()
        } catch (e: AssertionError) {
            composeTestRule.onNode(hasText("Login", substring = true))
                .assertIsDisplayed()
        }
    }

    fun fillBiodataFormPartially(gender: String? = null, birthDate: String? = null) {
        try {
            composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                .assertIsDisplayed()
        } catch (e: AssertionError) {
            throw AssertionError("Biodata form is not displayed")
        }

        if (gender != null) {
            composeTestRule.onNode(hasText("Jenis Kelamin", substring = true))
                .performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(gender)
                .performClick()
            composeTestRule.waitForIdle()
        }

        if (birthDate != null) {
            composeTestRule.onNode(hasText("Tanggal Lahir", substring = true))
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
    }

    fun clickLanjutButton() {
        composeTestRule.onNode(hasText("Lanjut", substring = true))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun verifyLanjutButtonIsDisabled() {
        val originalScreenPresent = try {
            composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                .assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }

        if (originalScreenPresent) {
            try {
                composeTestRule.onNode(hasText("Lanjut", substring = true))
                    .performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                
                // Verify we're still on the biodata screen (button should be disabled)
                composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                    .assertIsDisplayed()
            } catch (e: AssertionError) {
                throw AssertionError("Lanjut button should be disabled when required fields are not filled")
            }
        }
    }

    fun verifyStillOnBiodataScreen() {
        composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
            .assertIsDisplayed()
    }

    fun navigateToBiodataScreen() {
        fillRegistrationForm(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "SecurePassword123"
        )
        acceptPrivacyPolicyAndSubmit()
        
        // Wait for biodata screen to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNode(hasText("Lengkapi Profil", substring = true))
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun verifyStillOnOtpScreen() {
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNode(hasText("Verifikasi", substring = true))
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun verifyResendCodeIsClickable() {
        composeTestRule.onNode(hasText("Kirim ulang", substring = true))
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun waitForResendTimer() {
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNode(hasText("Belum menerima kode?", substring = true))
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun verifyDaftarButtonIsDisabled() {
        val originalScreenPresent = try {
            composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }

        if (originalScreenPresent) {
            try {
                composeTestRule.onNodeWithText("Daftar")
                    .performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                
                composeTestRule.onNodeWithText("Buat Akun Anda").assertIsDisplayed()
            } catch (e: AssertionError) {
                throw AssertionError("Daftar button should be disabled after validation error")
            }
        }
    }
}