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

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = CounterfactualE2ETestHelper(composeTestRule)
    }

    @Test
    fun counterfactualFlow_serviceBackedScenarioCompletesOnDevice() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyCounterfactualEntryAvailable()
        testHelper.navigateToCounterfactualScreen()

        val result = testHelper.runServiceBackedCounterfactualAndMeasureLatency()
        testHelper.verifyServiceBackedResultState(result)
    }

    @Test
    fun counterfactualFlow_serviceBackedLatencyStaysUnderFiveSeconds() {
        testHelper.startAppAndNavigateToHome()
        testHelper.verifyCounterfactualEntryAvailable()
        testHelper.navigateToCounterfactualScreen()

        val latencies = mutableListOf<Long>()
        repeat(3) { iteration ->
            val result = testHelper.runServiceBackedCounterfactualAndMeasureLatency()
            testHelper.verifyServiceBackedResultState(result)
            latencies += result.latencyMs
            println("Counterfactual E2E iteration ${iteration + 1}: state=${result.stateTag}, latencyMs=${result.latencyMs}")
            if (iteration < 2) {
                testHelper.returnToCounterfactualSetup()
            }
        }

        testHelper.verifyLatencyWithinTarget(latencies)
    }
}
