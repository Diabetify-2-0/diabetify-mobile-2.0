package com.itb.diabetify.e2e.presentation.view_risk_factors

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
class ViewRiskFactorsTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: ViewRiskFactorsTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ViewRiskFactorsTestHelper(composeTestRule)
    }

    @Test
    fun viewRiskFactorsFlow_Complete() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskFactorDetailCardDisplayed()
        testHelper.verifyChartTabSwitching()

        testHelper.navigateToRiskFactorDetail()
        testHelper.verifyRiskFactorDetailScreenDisplayed()
        testHelper.verifyChartDisplayed()
        testHelper.verifyRiskFactorExplanationDisplayed()
    }

    @Test
    fun viewRiskFactorDetail_NavigationBackToHome() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskFactorDetailCardDisplayed()
        testHelper.verifyChartTabSwitching()

        testHelper.navigateToRiskFactorDetail()
        testHelper.verifyRiskFactorDetailScreenDisplayed()

        testHelper.clickBackButton()
        testHelper.verifyNavigationBackToHome()
    }

    @Test
    fun viewRiskFactorDetail_ChartTabSwitchingAndDisplay() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskFactorDetailCardDisplayed()

        testHelper.verifyChartTabSwitching()
        testHelper.navigateToRiskFactorDetail()
        testHelper.verifyRiskFactorDetailScreenDisplayed()
        testHelper.verifyChartDisplayed()
    }

    @Test
    fun viewRiskFactorDetail_ConsistencyBetweenSummaryAndDetail() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyRiskFactorDetailCardDisplayed()

        val summaryValues = testHelper.getRiskFactorSummaryValues()

        testHelper.navigateToRiskFactorDetail()
        testHelper.verifyRiskFactorDetailScreenDisplayed()
        testHelper.verifyChartDisplayed()

        val detailValues = testHelper.getRiskFactorSummaryValues(isDetail = true)
        testHelper.verifyRiskFactorValuesConsistency(summaryValues, detailValues)
    }
}