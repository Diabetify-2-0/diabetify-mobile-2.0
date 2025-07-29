package com.itb.diabetify.e2e.presentation.add_daily_data

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
class AddDailyDataTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: AddDailyDataTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = AddDailyDataTestHelper(composeTestRule)
    }

    @Test
    fun addDailyDataFlow_Complete() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickRokokButton()
        val currentRokokValue = testHelper.getCurrentRokokValue()
        testHelper.clearAndFillRokokForm(currentRokokValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedRokokValue = if (currentRokokValue == "0") "5" else "0"
        testHelper.verifyRokokDataMatches(expectedRokokValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickAktivitasButton()
        val currentAktivitasValue = testHelper.getCurrentAktivitasValue()
        testHelper.clearAndFillAktivitasForm(currentAktivitasValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedAktivitasValue = if (currentAktivitasValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyAktivitasDataMatches(expectedAktivitasValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun addRokokData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickRokokButton()
        val firstValue = testHelper.getCurrentRokokValue()
        testHelper.clearAndFillRokokForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "0") "5" else "0"
        testHelper.verifyRokokDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickRokokButton()
        val secondValue = testHelper.getCurrentRokokValue()
        testHelper.clearAndFillRokokForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "0") "5" else "0"
        testHelper.verifyRokokDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun addAktivitasData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickAktivitasButton()
        val firstValue = testHelper.getCurrentAktivitasValue()
        testHelper.clearAndFillAktivitasForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyAktivitasDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickAktivitasButton()
        val secondValue = testHelper.getCurrentAktivitasValue()
        testHelper.clearAndFillAktivitasForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyAktivitasDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun addRokokData_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickRokokButton()
        testHelper.fillRokokFormWithInvalidValue("100")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Jumlah rokok harus antara 0-60 batang")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun addRokokData_WithEmptyValue_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickRokokButton()
        testHelper.fillRokokFormWithInvalidValue("")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Mohon isi field ini")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun addRokokData_ValidAfterInvalidInput_SucceedsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickRokokButton()
        
        testHelper.fillRokokFormWithInvalidValue("100")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Jumlah rokok harus antara 0-60 batang")
        testHelper.verifyNoSuccessMessage()
        
        testHelper.fillRokokFormWithInvalidValue("10")
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }
}