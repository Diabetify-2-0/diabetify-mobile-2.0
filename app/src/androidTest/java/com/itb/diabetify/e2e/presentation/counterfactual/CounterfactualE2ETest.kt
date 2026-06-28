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
class CounterfactualE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var testHelper: CounterfactualE2ETestHelper
    private lateinit var reporter: CounterfactualE2EReporter

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = CounterfactualE2ETestHelper(composeTestRule)
        reporter = CounterfactualE2EReporter()
    }

    @Test
    fun counterfactualRm5_endToEndFunctionalSuite_recordsTwelveScenarios() {
        var fullActionableResult: CounterfactualE2ETestHelper.CounterfactualRunResult? = null

        reporter.scenario(
            id = "E2E-CF-01",
            name = "Health readiness counterfactual"
        ) {
            testHelper.verifyCounterfactualHealthReady()
        }

        reporter.scenario(
            id = "E2E-CF-02",
            name = "Toggle pilihan mutable"
        ) {
            testHelper.ensureCounterfactualSetup()
            testHelper.verifyMutableToggle()
        }

        reporter.scenario(
            id = "E2E-CF-03",
            name = "Validasi tanpa mutable terpilih"
        ) {
            testHelper.ensureCounterfactualSetup()
            testHelper.verifyNoMutableValidation()
        }

        reporter.scenario(
            id = "E2E-CF-04",
            name = "Submit feasible dengan BMI saja"
        ) {
            testHelper.ensureCounterfactualSetup()
            val result = testHelper.runFeasibleBmiOnly()
            testHelper.verifyServiceBackedResultState(result)
            "state=${result.stateTag}, latencyMs=${result.latencyMs}"
        }

        reporter.scenario(
            id = "E2E-CF-05",
            name = "Submit feasible dengan BMI + aktivitas"
        ) {
            testHelper.ensureCounterfactualSetup()
            val result = testHelper.runFeasibleBmiAndActivity()
            testHelper.verifyServiceBackedResultState(result)
            "state=${result.stateTag}, latencyMs=${result.latencyMs}"
        }

        reporter.scenario(
            id = "E2E-CF-06",
            name = "Submit feasible dengan full actionable"
        ) {
            testHelper.ensureCounterfactualSetup()
            fullActionableResult = testHelper.runFeasibleFullActionable()
            testHelper.verifyServiceBackedResultState(requireNotNull(fullActionableResult))
            "state=${fullActionableResult?.stateTag}, latencyMs=${fullActionableResult?.latencyMs}"
        }

        reporter.scenario(
            id = "E2E-CF-07",
            name = "Polling job sampai status terminal"
        ) {
            testHelper.verifyPollingReachedTerminal(requireNotNull(fullActionableResult))
        }

        reporter.scenario(
            id = "E2E-CF-08",
            name = "Result feasible menampilkan risiko awal, risiko hasil, dan fitur berubah"
        ) {
            testHelper.verifyFeasibleResultContent(requireNotNull(fullActionableResult))
        }

        reporter.scenario(
            id = "E2E-CF-09",
            name = "Simpan hasil feasible sebagai planner goal"
        ) {
            testHelper.saveFeasibleResultAsPlannerGoal()
        }

        reporter.scenario(
            id = "E2E-CF-10",
            name = "Infeasible scenario dengan constraint terlalu sempit atau target terlalu rendah"
        ) {
            testHelper.ensureCounterfactualSetup()
            val result = testHelper.runStrictInfeasibleBmiOnly()
            testHelper.verifyInfeasibleResultState(result)
        }

        reporter.scenario(
            id = "E2E-CF-11",
            name = "Result infeasible menampilkan state skenario realistis belum ditemukan"
        ) {
            testHelper.verifyInfeasibleResultContent()
        }

        reporter.scenario(
            id = "E2E-CF-12",
            name = "Cari skenario lain dari result screen"
        ) {
            testHelper.returnToCounterfactualSetup()
            "returned to counterfactual setup"
        }

        reporter.writeAndAssert(minimumSuccessRate = 0.90)
    }
}
