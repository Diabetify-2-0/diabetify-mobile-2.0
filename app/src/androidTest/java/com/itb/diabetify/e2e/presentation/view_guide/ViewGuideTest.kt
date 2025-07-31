package com.itb.diabetify.e2e.presentation.view_guide

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
class ViewGuideTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: ViewGuideTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ViewGuideTestHelper(composeTestRule)
    }

    @Test
    fun viewGuide_Complete() {
        testHelper.startAppAndNavigateToGuide()
        
        testHelper.verifyDiabetesAndXAISection()
        testHelper.clickOnAboutDiabetesCard()
        testHelper.verifyGuideDetailScreen()
        testHelper.navigateBackToGuide()

        testHelper.scrollDown()

        testHelper.verifyTipsKesehatanSection()
        testHelper.clickOnFirstTipsCard()
        testHelper.verifyTipsDetailScreen()
        testHelper.navigateBackToGuide()

        testHelper.scrollDown()
        testHelper.scrollDown()

        testHelper.verifyFAQSection()
        testHelper.clickOnFirstFAQCard()
        testHelper.clickOnSecondFAQCard()
        testHelper.verifyFAQAnswerDisplayed()
    }
}