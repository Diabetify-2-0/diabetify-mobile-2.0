package com.itb.diabetify.e2e.presentation.counterfactual

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
class CounterfactualTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: CounterfactualTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = CounterfactualTestHelper(composeTestRule)
    }

    @Test
    fun counterfactualBackend_ReportsReady() {
        testHelper.verifyCounterfactualHealthReady()
    }

    @Test
    fun mutableOption_TogglesSelection() {
        testHelper.ensureCounterfactualSetup()
        testHelper.verifyMutableToggle()
    }

    @Test
    fun submitWithoutMutableOptions_ShowsValidationError() {
        testHelper.ensureCounterfactualSetup()
        testHelper.verifyNoMutableValidation()
    }

    @Test
    fun submitWithBmiOnly_ShowsFeasibleResult() {
        testHelper.ensureCounterfactualSetup()
        val result = testHelper.runFeasibleBmiOnly()
        testHelper.verifyServiceBackedResultState(result)
    }

    @Test
    fun submitWithBmiAndActivity_ShowsFeasibleResult() {
        testHelper.ensureCounterfactualSetup()
        val result = testHelper.runFeasibleBmiAndActivity()
        testHelper.verifyServiceBackedResultState(result)
    }

    @Test
    fun submitWithFullActionableOptions_ShowsFeasibleResult() {
        runFullActionableCounterfactual()
    }

    @Test
    fun counterfactualPolling_ReachesTerminalResult() {
        val result = runFullActionableCounterfactual()
        testHelper.verifyPollingReachedTerminal(result)
    }

    @Test
    fun feasibleResult_ShowsRiskAndFeatureChanges() {
        val result = runFullActionableCounterfactual()
        testHelper.verifyFeasibleResultContent(result)
    }

    @Test
    fun saveFeasibleResult_CreatesPlannerGoal() {
        runFullActionableCounterfactual()
        testHelper.saveFeasibleResultAsPlannerGoal()
    }

    @Test
    fun submitWithStrictTarget_ShowsInfeasibleResult() {
        runStrictInfeasibleCounterfactual()
    }

    @Test
    fun infeasibleResult_ShowsNoScenarioMessage() {
        runStrictInfeasibleCounterfactual()
        testHelper.verifyInfeasibleResultContent()
    }

    @Test
    fun tryAnotherScenario_ReturnsToCounterfactualSetup() {
        runStrictInfeasibleCounterfactual()
        testHelper.returnToCounterfactualSetup()
    }

    private fun runFullActionableCounterfactual(): CounterfactualTestHelper.CounterfactualRunResult {
        testHelper.ensureCounterfactualSetup()
        val result = testHelper.runFeasibleFullActionable()
        testHelper.verifyServiceBackedResultState(result)
        return result
    }

    private fun runStrictInfeasibleCounterfactual(): CounterfactualTestHelper.CounterfactualRunResult {
        testHelper.ensureCounterfactualSetup()
        val result = testHelper.runStrictInfeasibleBmiOnly()
        testHelper.verifyInfeasibleResultState(result)
        return result
    }
}
