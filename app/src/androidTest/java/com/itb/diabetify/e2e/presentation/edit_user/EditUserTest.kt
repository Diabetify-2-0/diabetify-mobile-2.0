package com.itb.diabetify.e2e.presentation.edit_user

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
class EditUserTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: EditUserTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = EditUserTestHelper(composeTestRule)
    }

    @Test
    fun editUser_Complete() {
        testHelper.startAppAndNavigateToSettings()

        testHelper.verifyProfileCard("testmale", "testmale@example.com")
        testHelper.clickEditProfilButton()
        testHelper.verifyEditUserScreenDisplayed()
        
        val originalValues = testHelper.getCurrentFieldValues()
        testHelper.changeName("testupdated")
        testHelper.changeGender("Perempuan")
        testHelper.changeDateOfBirth("15/06/1985")
        testHelper.clickSimpanPerubahan()
        testHelper.verifySuccessMessage()
        testHelper.clickBackNavigation()
        
        testHelper.clickEditProfilButton()
        testHelper.verifyChanges("testupdated", "Perempuan", "15/06/1985")
        testHelper.changeName(originalValues.name)
        testHelper.changeGender(originalValues.gender)
        testHelper.changeDateOfBirth(originalValues.dateOfBirth)
        testHelper.clickSimpanPerubahan()
        testHelper.verifySuccessMessage()
    }

    @Test
    fun changeName_WithEmptyField_ShouldShowError() {
        testHelper.startAppAndNavigateToSettings()

        testHelper.clickEditProfilButton()
        testHelper.verifyEditUserScreenDisplayed()

        testHelper.changeName("")
        testHelper.clickSimpanPerubahan()
        testHelper.waitForErrorMessage("Nama tidak boleh kosong")
    }

    @Test
    fun changeDateOfBirth_WithEmptyField_ShouldShowError() {
        testHelper.startAppAndNavigateToSettings()

        testHelper.clickEditProfilButton()
        testHelper.verifyEditUserScreenDisplayed()

        testHelper.clearDateOfBirthField()
        testHelper.clickSimpanPerubahan()
        testHelper.waitForErrorMessage("Tanggal lahir tidak boleh kosong")
    }

    @Test
    fun verifyCancelButton_WorksCorrectly() {
        testHelper.startAppAndNavigateToSettings()

        testHelper.clickEditProfilButton()
        testHelper.verifyEditUserScreenDisplayed()

        val originalValues = testHelper.getCurrentFieldValues()
        testHelper.changeName("testupdated")
        testHelper.changeGender("Perempuan")
        testHelper.changeDateOfBirth("15/06/1985")

        testHelper.clickBackNavigation()
        testHelper.verifyProfileCard(originalValues.name, originalValues.email)
        testHelper.clickEditProfilButton()
        testHelper.verifyEditUserScreenDisplayed()
        testHelper.verifyChanges(originalValues.name, originalValues.gender, originalValues.dateOfBirth)
    }
}