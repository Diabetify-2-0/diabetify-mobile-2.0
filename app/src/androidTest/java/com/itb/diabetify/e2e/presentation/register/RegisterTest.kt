package com.itb.diabetify.e2e.presentation.register

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.di.AppModule
import com.itb.diabetify.e2e.repository.FakeAuthRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RegisterTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @BindValue
    val fakeRepo: FakeAuthRepository = FakeAuthRepository()

    private lateinit var testHelper: RegisterTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = RegisterTestHelper(composeTestRule)
    }

    @Test
    fun registrationFlow_Complete() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "SecurePassword123"
        )
        testHelper.acceptPrivacyPolicyAndSubmit()
        testHelper.fillBiodataFormIfPresent(
            gender = "Laki-laki",
            birthDate = "15/08/1990"
        )
        testHelper.proceedToOtpVerificationIfPresent()
        testHelper.enterOtpAndVerifyIfPresent("123456")
        testHelper.verifyRegistrationFlowCompletion()
    }

    @Test
    fun registerWithEmptyName_ShowsValidationError() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationFormWithValidation(
            name = "",
            email = "john.doe@example.com",
            password = "SecurePassword123",
            expectValidation = true
        )
        testHelper.acceptPrivacyPolicyOnly()
        testHelper.clickRegisterButton()
        testHelper.waitForErrorMessage("Nama tidak boleh kosong")
        testHelper.verifyDaftarButtonIsDisabled()
        testHelper.verifyStillOnRegisterScreen()
    }

    @Test
    fun registerWithInvalidEmail_ShowsValidationError() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationFormWithValidation(
            name = "John Doe",
            email = "invalid-email",
            password = "SecurePassword123",
            expectValidation = true
        )
        testHelper.acceptPrivacyPolicyOnly()
        testHelper.clickRegisterButton()
        testHelper.waitForErrorMessage("Email tidak valid")
        testHelper.verifyDaftarButtonIsDisabled()
        testHelper.verifyStillOnRegisterScreen()
    }

    @Test
    fun registerWithShortPassword_ShowsValidationError() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationFormWithValidation(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "123",
            expectValidation = true
        )
        testHelper.acceptPrivacyPolicyOnly()
        testHelper.clickRegisterButton()
        testHelper.waitForErrorMessage("Kata sandi harus lebih dari 8 karakter")
        testHelper.verifyDaftarButtonIsDisabled()
        testHelper.verifyStillOnRegisterScreen()
    }

    @Test
    fun registerWithoutAcceptingPrivacyPolicy_ButtonDisabled() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "SecurePassword123"
        )
        testHelper.clickRegisterButton()
        testHelper.verifyStillOnRegisterScreen()
    }

    @Test
    fun registerWithDuplicateEmail_ShowsErrorMessage() {
        fakeRepo.shouldFailCreateAccount = true
        fakeRepo.createAccountErrorType = "duplicate_email"
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "John Doe",
            email = "existing@example.com",
            password = "SecurePassword123"
        )
        testHelper.acceptPrivacyPolicyAndSubmit()
        testHelper.fillBiodataFormIfPresent(
            gender = "Laki-laki",
            birthDate = "15/08/1990"
        )
        testHelper.proceedToOtpVerificationIfPresent()
        testHelper.waitForErrorMessage("Akun dengan email ini sudah terdaftar")
    }

    @Test
    fun biodataValidation_EmptyGender_ShowsValidationError() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.navigateToBiodataScreen()
        testHelper.fillBiodataFormPartially(
            gender = null,
            birthDate = "15/08/1990"
        )
        
        testHelper.clickLanjutButton()
        testHelper.waitForErrorMessage("Jenis kelamin tidak boleh kosong")
        testHelper.verifyLanjutButtonIsDisabled()
        testHelper.verifyStillOnBiodataScreen()
    }

    @Test
    fun biodataValidation_EmptyBirthDate_ShowsValidationError() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.navigateToBiodataScreen()
        testHelper.fillBiodataFormPartially(
            gender = "Laki-laki",
            birthDate = null
        )
        
        testHelper.clickLanjutButton()
        testHelper.waitForErrorMessage("Tanggal lahir tidak boleh kosong")
        testHelper.verifyLanjutButtonIsDisabled()
        testHelper.verifyStillOnBiodataScreen()
    }

    @Test
    fun otpVerificationWithInvalidCode_ShowsErrorMessage() {
        fakeRepo.shouldFailVerifyOtp = true
        fakeRepo.verifyOtpErrorType = "invalid_otp"
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "SecurePassword123"
        )
        testHelper.acceptPrivacyPolicyAndSubmit()
        testHelper.fillBiodataFormIfPresent(
            gender = "Laki-laki",
            birthDate = "15/08/1990"
        )
        testHelper.proceedToOtpVerificationIfPresent()
        testHelper.enterOtpAndVerifyIfPresentV2("000000")
        testHelper.waitForErrorMessage("Kode OTP salah atau sudah kadaluarsa")
        testHelper.verifyStillOnOtpScreen()
    }

    @Test
    fun otpScreen_ResendVerificationCode_WorksCorrectly() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "SecurePassword123"
        )
        testHelper.acceptPrivacyPolicyAndSubmit()
        testHelper.fillBiodataFormIfPresent(
            gender = "Laki-laki",
            birthDate = "15/08/1990"
        )
        testHelper.proceedToOtpVerificationIfPresent()
        testHelper.verifyResendCodeIsClickable()
        testHelper.waitForResendTimer()
    }

    @Test
    fun passwordVisibilityToggle_WorksCorrectly() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.testPasswordVisibilityToggle()
    }

    @Test
    fun navigateBackToLogin_WorksCorrectly() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.navigateBackToLogin()
    }

    @Test
    fun registerWithDifferentGenderSelection_WorksCorrectly() {
        testHelper.startAppAndNavigateToRegister()
        testHelper.fillRegistrationForm(
            name = "Jane Doe",
            email = "jane.doe@example.com",
            password = "SecurePassword123"
        )
        testHelper.acceptPrivacyPolicyAndSubmit()
        testHelper.fillBiodataFormIfPresent(
            gender = "Perempuan",
            birthDate = "20/03/1995"
        )
        testHelper.proceedToOtpVerificationIfPresent()
        testHelper.enterOtpAndVerifyIfPresent("123456")
        testHelper.verifyRegistrationFlowCompletion()
    }
}