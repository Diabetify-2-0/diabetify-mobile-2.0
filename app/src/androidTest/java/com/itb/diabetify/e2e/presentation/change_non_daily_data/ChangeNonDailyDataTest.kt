package com.itb.diabetify.e2e.presentation.change_non_daily_data

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
class ChangeNonDailyDataTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: ChangeNonDailyDataTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ChangeNonDailyDataTestHelper(composeTestRule)
        testHelper.clearAppToken()
    }

    @Test
    fun changeProfileDataFlow_Complete() {
        testHelper.startAppAndNavigateToHome(gender = "female")
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        val currentBeratValue = testHelper.getCurrentBeratValue()
        testHelper.clearAndFillBeratForm(currentBeratValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedBeratValue = if (currentBeratValue == "80") "90" else "80"
        testHelper.verifyBeratDataMatches(expectedBeratValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickTinggiButton()
        val currentTinggiValue = testHelper.getCurrentTinggiValue()
        testHelper.clearAndFillTinggiForm(currentTinggiValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedTinggiValue = if (currentTinggiValue == "170") "180" else "170"
        testHelper.verifyTinggiDataMatches(expectedTinggiValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickHipertensiButton()
        val currentHipertensiValue = testHelper.getCurrentHipertensiValue()
        testHelper.clearAndFillHipertensiForm(currentHipertensiValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedHipertensiValue = if (currentHipertensiValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyHipertensiDataMatches(expectedHipertensiValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKolesterolButton()
        val currentKolesterolValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(currentKolesterolValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedKolesterolValue = if (currentKolesterolValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKolesterolDataMatches(expectedKolesterolValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKeluargaButton()
        val currentKeluargaValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(currentKeluargaValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedKeluargaValue = if (currentKeluargaValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKeluargaDataMatches(expectedKeluargaValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKehamilanButton()
        val currentKehamilanValue = testHelper.getCurrentKehamilanValue()
        testHelper.clearAndFillKehamilanForm(currentKehamilanValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedKehamilanValue = if (currentKehamilanValue == "Pernah") "Tidak" else "Pernah"
        testHelper.verifyKehamilanDataMatches(expectedKehamilanValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        testHelper.fillBeratFormWithInvalidValue("400")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Berat badan harus antara 30-300 kg")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_WithValueBelowLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        testHelper.fillBeratFormWithInvalidValue("20")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Berat badan harus antara 30-300 kg")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_WithValueAboveLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        testHelper.fillTinggiFormWithInvalidValue("300")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Tinggi badan harus antara 100-250 cm")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_WithValueBelowLimit_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        testHelper.fillTinggiFormWithInvalidValue("80")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Tinggi badan harus antara 100-250 cm")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        val firstValue = testHelper.getCurrentBeratValue()
        testHelper.clearAndFillBeratForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "80") "90" else "80"
        testHelper.verifyBeratDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickBeratButton()
        val secondValue = testHelper.getCurrentBeratValue()
        testHelper.clearAndFillBeratForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "80") "90" else "80"
        testHelper.verifyBeratDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        val firstValue = testHelper.getCurrentTinggiValue()
        testHelper.clearAndFillTinggiForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "170") "180" else "170"
        testHelper.verifyTinggiDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickTinggiButton()
        val secondValue = testHelper.getCurrentTinggiValue()
        testHelper.clearAndFillTinggiForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "170") "180" else "170"
        testHelper.verifyTinggiDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeHipertensiData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickHipertensiButton()
        val firstValue = testHelper.getCurrentHipertensiValue()
        testHelper.clearAndFillHipertensiForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyHipertensiDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickHipertensiButton()
        val secondValue = testHelper.getCurrentHipertensiValue()
        testHelper.clearAndFillHipertensiForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyHipertensiDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeKolesterolData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickKolesterolButton()
        val firstValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKolesterolDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKolesterolButton()
        val secondValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKolesterolDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeKeluargaData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickKeluargaButton()
        val firstValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKeluargaDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKeluargaButton()
        val secondValue = testHelper.getCurrentSelectionValue()
        testHelper.clearAndFillSelectionForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "Tidak") "Ya" else "Tidak"
        testHelper.verifyKeluargaDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeKehamilanData_MultipleUpdates_DataPersistsCorrectly() {
        testHelper.startAppAndNavigateToHome(gender = "female")
        testHelper.clickAddButton()

        testHelper.clickKehamilanButton()
        val firstValue = testHelper.getCurrentKehamilanValue()
        testHelper.clearAndFillKehamilanForm(firstValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedFirstValue = if (firstValue == "Pernah") "Tidak" else "Pernah"
        testHelper.verifyKehamilanDataMatches(expectedFirstValue)
        testHelper.closeDialogByClickingOutside()

        testHelper.clickKehamilanButton()
        val secondValue = testHelper.getCurrentKehamilanValue()
        testHelper.clearAndFillKehamilanForm(secondValue)
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        val expectedSecondValue = if (secondValue == "Pernah") "Tidak" else "Pernah"
        testHelper.verifyKehamilanDataMatches(expectedSecondValue)
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_WithEmptyValue_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        testHelper.fillBeratFormWithInvalidValue("")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Mohon isi field ini")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_WithEmptyValue_ShowsValidationError() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        testHelper.fillTinggiFormWithInvalidValue("")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Mohon isi field ini")
        testHelper.verifyNoSuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_ValidAfterInvalidInput_SucceedsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        
        testHelper.fillBeratFormWithInvalidValue("400")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Berat badan harus antara 30-300 kg")
        testHelper.verifyNoSuccessMessage()
        
        testHelper.fillBeratFormWithInvalidValue("80")
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_ValidAfterInvalidInput_SucceedsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        
        testHelper.fillTinggiFormWithInvalidValue("300")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Tinggi badan harus antara 100-250 cm")
        testHelper.verifyNoSuccessMessage()
        
        testHelper.fillTinggiFormWithInvalidValue("170")
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeBeratData_ValidAfterEmptyInput_SucceedsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickBeratButton()
        
        testHelper.fillBeratFormWithInvalidValue("")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Mohon isi field ini")
        testHelper.verifyNoSuccessMessage()
        
        testHelper.fillBeratFormWithInvalidValue("75")
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeTinggiData_ValidAfterEmptyInput_SucceedsCorrectly() {
        testHelper.startAppAndNavigateToHome()
        testHelper.clickAddButton()

        testHelper.clickTinggiButton()
        
        testHelper.fillTinggiFormWithInvalidValue("")
        testHelper.clickSimpanButton()
        testHelper.waitForErrorMessage("Mohon isi field ini")
        testHelper.verifyNoSuccessMessage()
        
        testHelper.fillTinggiFormWithInvalidValue("165")
        testHelper.clickSimpanButton()
        testHelper.verifySuccessMessage()
        testHelper.closeDialogByClickingOutside()
    }

    @Test
    fun changeProfileData_MaleUser_CannotAccessPregnancyHistory() {
        testHelper.startAppAndNavigateToHome(gender = "male")
        testHelper.clickAddButton()

        try {
            testHelper.clickKehamilanButton()
            throw AssertionError("Male users should not have access to pregnancy history")
        } catch (_: AssertionError) {
        }
    }
}