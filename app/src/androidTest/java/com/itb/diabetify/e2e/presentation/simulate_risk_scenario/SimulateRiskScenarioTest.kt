package com.itb.diabetify.e2e.presentation.simulate_risk_scenario

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.e2e.di.TestAppModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@UninstallModules(TestAppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SimulateRiskScenarioTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: SimulateRiskScenarioTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = SimulateRiskScenarioTestHelper(composeTestRule)
    }

    @Test
    fun simulateRiskScenario_Complete() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()

        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        testHelper.changeSmokingStatus()
        testHelper.scrollDown()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.fillSmokingDuration()
            testHelper.fillCigarettesPerDay()
        }
        testHelper.changeWeight()
        testHelper.changeHypertensionStatus()
        testHelper.changePhysicalActivityPerWeek()
        testHelper.changeCholesterolStatus()

        testHelper.clickCalculateButton()
        testHelper.verifyRiskPercentageCardDisplayed()
        testHelper.verifyRiskFactorDetailCardDisplayed()
    }

    @Test
    fun verifyResetFunctionality_WorksCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialValues = testHelper.captureCurrentFieldValues()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        testHelper.changeSmokingStatus()
        testHelper.scrollDown()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.fillSmokingDuration()
            testHelper.fillCigarettesPerDay()
        }
        testHelper.changeWeight()
        testHelper.changeHypertensionStatus()
        testHelper.changePhysicalActivityPerWeek()
        testHelper.changeCholesterolStatus()

        testHelper.clickResetButton()
        testHelper.verifyFieldsReset(initialValues)
    }

    @Test
    fun verifySmokingFieldsVisibility_WhenStatusChanges() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        testHelper.changeSmokingStatus()
        testHelper.scrollDown()
        
        testHelper.verifySmokingFieldsVisibility()
        
        if (testHelper.areSmokingFieldsVisible()) {
            testHelper.fillSmokingDuration()
            testHelper.fillCigarettesPerDay()
        }
        
        testHelper.changeSmokingStatus()
        testHelper.verifySmokingFieldsVisibility()
    }

    @Test
    fun simulateRiskScenario_NavigationBackToHome() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()

        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()
        testHelper.clickCalculateButton()

        testHelper.clickBackButton()
        testHelper.clickBackButton()
        testHelper.verifyNavigationBackToHome()
    }

    @Test
    fun changeSmokingDuration_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.changeSmokingStatus()
        }
        testHelper.scrollDown()

        testHelper.fillInvalidSmokingDuration("100")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Lama merokok harus antara 0-70 tahun"
        )
    }

    @Test
    fun changeSmokingDuration_WithEmpty_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.changeSmokingStatus()
        }
        testHelper.scrollDown()

        testHelper.fillInvalidSmokingDuration("")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Lama merokok tidak boleh kosong"
        )
    }

    @Test
    fun changeCigarettesPerDay_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.changeSmokingStatus()
        }
        testHelper.scrollDown()

        testHelper.fillInvalidCigarettesPerDay("100")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Jumlah rokok harus antara 0-60 batang"
        )
    }

    @Test
    fun changeCigarettesPerDay_WithEmpty_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()

        val initialSmokingStatus = testHelper.getCurrentSmokingStatus()
        if (initialSmokingStatus == "Tidak Pernah Merokok") {
            testHelper.changeSmokingStatus()
        }
        testHelper.scrollDown()

        testHelper.fillInvalidCigarettesPerDay("")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Jumlah rokok tidak boleh kosong"
        )
    }

    @Test
    fun changeWeight_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()

        testHelper.fillInvalidWeight("500")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Berat badan harus antara 30-300 kg"
        )
    }

    @Test
    fun changeWeight_WithValueBelowLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()

        testHelper.fillInvalidWeight("10")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Berat badan harus antara 30-300 kg"
        )
    }

    @Test
    fun changeWeight_WithEmpty_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()

        testHelper.fillInvalidWeight("")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Berat badan tidak boleh kosong"
        )
    }

    @Test
    fun changePhysicalActivity_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()

        testHelper.fillInvalidPhysicalActivity("10")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Aktivitas fisik harus antara 0-7 hari"
        )
    }

    @Test
    fun changePhysicalActivity_WithEmpty_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyWhatIfCardDisplayed()
        testHelper.navigateToWhatIfSimulation()
        testHelper.verifyWhatIfScreenDisplayed()
        testHelper.scrollDown()

        testHelper.fillInvalidPhysicalActivity("")
        testHelper.clickCalculateButton()
        testHelper.waitForErrorMessage(
            "Aktivitas fisik tidak boleh kosong"
        )
    }
}