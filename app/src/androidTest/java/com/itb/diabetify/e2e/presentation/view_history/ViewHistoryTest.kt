package com.itb.diabetify.e2e.presentation.view_history

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
class ViewHistoryTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: ViewHistoryTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ViewHistoryTestHelper(composeTestRule)
    }

    @Test
    fun viewHistory_Complete() {
        testHelper.startAppAndNavigateToHistory()

        testHelper.verifyHorizontalCalendarIsDisplayed()
        testHelper.verifyLineChartIsDisplayed()
        testHelper.verifyRiskPercentageCardIsDisplayed()

        testHelper.scrollDown()

        testHelper.verifyRiskFactorContributionsAreDisplayed()
        testHelper.verifyDailyInputsAreDisplayed()
    }

    @Test
    fun verifyChangeDate_WorksCorrectly() {
        testHelper.startAppAndNavigateToHistory()

        testHelper.clickPreviousDateButton()
        testHelper.verifyHorizontalCalendarIsDisplayed()
        testHelper.verifyLineChartIsDisplayed()
        testHelper.verifyRiskPercentageCardIsDisplayed()

        testHelper.scrollDown()

        testHelper.verifyRiskFactorContributionsAreDisplayed()
        testHelper.verifyDailyInputsAreDisplayed()
    }

    @Test
    fun verifyArrowButton_WorksCorrectly() {
        testHelper.startAppAndNavigateToHistory()

        testHelper.clickPreviousMonthButton()
        testHelper.verifyHorizontalCalendarIsDisplayed()

        testHelper.clickNextMonthButton()
        testHelper.verifyHorizontalCalendarIsDisplayed()
        testHelper.verifyLineChartIsDisplayed()
    }

    @Test
    fun verifyNextDateAfterToday_NotClickable() {
        testHelper.startAppAndNavigateToHistory()

        testHelper.verifyNextDateButtonIsNotClickable()
    }

    @Test
    fun viewHistory_EmptyHistory() {
        testHelper.startAppAndNavigateToHistory()

        testHelper.clickPreviousMonthButton()
        testHelper.clickPreviousMonthButton()

        testHelper.scrollDown()

        testHelper.verifyEmptyHistoryState()
    }
}