package com.itb.diabetify.e2e.presentation.view_risk_estimation

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
class ViewRiskEstimationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: ViewRiskEstimationTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ViewRiskEstimationTestHelper(composeTestRule)
    }

    @Test
    fun viewRiskEstimationFlow_Complete() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskPercentageCardDisplayed()

        testHelper.navigateToRiskDetail()
        testHelper.verifyRiskDetailScreenDisplayed()
        testHelper.verifyRiskScoreGaugeDisplayed()
    }

    @Test
    fun viewRiskDetail_NavigationBackToHome() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskPercentageCardDisplayed()

        testHelper.navigateToRiskDetail()
        testHelper.verifyRiskDetailScreenDisplayed()

        testHelper.clickBackButton()
        testHelper.verifyNavigationBackToHome()
    }

    @Test
    fun viewRiskDetail_DynamicRiskScoreDisplayed() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskPercentageCardDisplayed()
        val actualRiskPercentage = testHelper.getCurrentRiskPercentage()
        testHelper.verifyCorrectRiskCategoryDescriptionDisplayed(actualRiskPercentage)

        testHelper.navigateToRiskDetail()
        testHelper.verifyRiskDetailScreenDisplayed()
        testHelper.verifyRiskScoreGaugeDisplayed()
        testHelper.verifyCorrectRiskCategoryHighlighted(actualRiskPercentage)
    }

    @Test
    fun viewRiskDetail_VerifyConsistencyBetweenHomeAndDetailScreen() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskPercentageCardDisplayed()

        val homeScreenPercentage = testHelper.getCurrentRiskPercentage()

        testHelper.navigateToRiskDetail()
        testHelper.verifyRiskDetailScreenDisplayed()
        testHelper.verifyRiskScoreGaugeDisplayed()
        
        testHelper.verifyRiskScoreConsistency(homeScreenPercentage)
        
        testHelper.verifyCorrectRiskCategoryHighlighted(homeScreenPercentage)
        testHelper.verifyCorrectRiskCategoryDescriptionDisplayed(homeScreenPercentage)
    }
}