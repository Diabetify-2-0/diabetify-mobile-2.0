package com.itb.diabetify.e2e.presentation.counterfactual

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.e2e.presentation.survey.SurveyTestHelper
import kotlin.math.max

class CounterfactualE2ETestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    private val surveyTestHelper = SurveyTestHelper(composeTestRule)

    companion object {
        private const val APP_START_MAX_ATTEMPTS = 20
        private const val MAX_HOME_LOAD_ATTEMPTS = 10
        private const val HOME_LOAD_DELAY_MS = 1000L
        private const val POST_LOGIN_WAIT_MS = 3000L
        private const val POST_RESULT_SETTLE_MS = 500L
        private const val COUNTERFACTUAL_TIMEOUT_MS = 30000L
        private const val LATENCY_TARGET_MS = 5000L
        private const val MAX_TARGET_PERCENTAGE = 44
    }

    data class CounterfactualRunResult(
        val stateTag: String,
        val latencyMs: Long
    )

    @SuppressLint("CheckResult")
    fun startAppAndNavigateToHome() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        repeat(APP_START_MAX_ATTEMPTS) {
            when {
                isHomeVisible() -> {
                    waitForHomeContentToLoad()
                    return
                }

                isLoginVisible() -> {
                    fillLoginForm(email = "testmale@example.com", password = "bewebewe")
                    clickLoginButton()
                    Thread.sleep(1500L)
                }

                isSurveyVisible() -> {
                    completeSurveyBootstrapForCounterfactual()
                }

                hasDisplayedText("Kenali Risiko") && hasDisplayedText("Mulai") -> {
                    composeTestRule.onNodeWithText("Mulai").performClick()
                    composeTestRule.waitForIdle()
                }

                hasDisplayedText("Sudah memiliki akun") -> {
                    composeTestRule.onNodeWithText("Masuk").performClick()
                    composeTestRule.waitForIdle()
                }

                hasDisplayedText(">") -> {
                    try {
                        composeTestRule.onNodeWithText(">").performClick()
                    } catch (_: AssertionError) {
                        composeTestRule.onRoot().performTouchInput { swipeLeft() }
                    }
                    composeTestRule.waitForIdle()
                }
            }

            Thread.sleep(1000L)
        }

        error("Could not reach Home screen for counterfactual E2E after resolving onboarding/login states.")
    }

    fun verifyCounterfactualEntryAvailable() {
        scrollHomeToCounterfactualSection()
        composeTestRule.onNodeWithTag("HomeCounterfactualSection").assertIsDisplayed()
        composeTestRule.onNodeWithTag("HomeCounterfactualActionButton").assertIsDisplayed()

        when {
            hasDisplayedText("Buat Rencana") -> Unit
            hasDisplayedText("Lakukan Pemeriksaan") -> error(
                "Counterfactual mobile E2E requires an existing latest prediction. " +
                    "The device is currently showing 'Lakukan Pemeriksaan', so the service-backed flow " +
                    "cannot be validated until a fresh risk prediction is available on Home."
            )
            else -> error("Counterfactual entry is visible, but its action label could not be resolved.")
        }
    }

    fun navigateToCounterfactualScreen() {
        scrollHomeToCounterfactualSection()
        composeTestRule.onNodeWithTag("HomeCounterfactualActionButton").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            hasDisplayedNode("CounterfactualScreenRoot")
        }
        composeTestRule.onNodeWithTag("CounterfactualScreenRoot").assertIsDisplayed()
    }

    fun runServiceBackedCounterfactualAndMeasureLatency(): CounterfactualRunResult {
        val currentRisk = getDisplayedCurrentRiskPercentage()
        require(currentRisk > 1) {
            "Counterfactual E2E requires current risk above 1%, but got $currentRisk%"
        }

        val target = determineFeasibleTargetPercentage(currentRisk)
        setCounterfactualTarget(target = target, currentRisk = currentRisk)

        composeTestRule.onNodeWithTag("CounterfactualRunButton").assertIsDisplayed()
        val startedAt = SystemClock.elapsedRealtime()
        composeTestRule.onNodeWithTag("CounterfactualRunButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            hasDisplayedText("Menyiapkan baseline") ||
                hasDisplayedText("Mencari skenario") ||
                hasDisplayedText("Skenario ditemukan, menyiapkan rencana untuk Anda") ||
                hasAnyCounterfactualResultState()
        }

        composeTestRule.waitUntil(timeoutMillis = COUNTERFACTUAL_TIMEOUT_MS) {
            hasAnyCounterfactualResultState()
        }

        val latencyMs = SystemClock.elapsedRealtime() - startedAt
        val stateTag = currentCounterfactualResultStateTag()
        Thread.sleep(POST_RESULT_SETTLE_MS)

        return CounterfactualRunResult(stateTag = stateTag, latencyMs = latencyMs)
    }

    fun verifyServiceBackedResultState(result: CounterfactualRunResult) {
        composeTestRule.onNodeWithTag(result.stateTag).assertIsDisplayed()
        require(result.stateTag == "CounterfactualResultState_Feasible") {
            "Expected a FEASIBLE counterfactual result for the controlled profile, but got ${result.stateTag}."
        }
    }

    fun verifyLatencyWithinTarget(latencies: List<Long>) {
        require(latencies.isNotEmpty()) { "Latency list must not be empty" }
        val averageLatency = latencies.average()
        val p95Latency = percentile(latencies, 95.0)
        check(averageLatency < LATENCY_TARGET_MS) {
            "Average counterfactual E2E latency was ${averageLatency}ms, expected < $LATENCY_TARGET_MS ms"
        }
        check(p95Latency < LATENCY_TARGET_MS) {
            "p95 counterfactual E2E latency was ${p95Latency}ms, expected < $LATENCY_TARGET_MS ms"
        }
    }

    fun returnToCounterfactualSetup() {
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            hasAnyCounterfactualResultState()
        }

        if (hasDisplayedNode("CounterfactualTryAnotherButton")) {
            composeTestRule.onNodeWithTag("CounterfactualTryAnotherButton")
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()
        } else {
            composeTestRule.onNodeWithText("Pilih Target Baru")
                .assertIsDisplayed()
                .performClick()
        }

        composeTestRule.waitUntil(timeoutMillis = 15000L) {
            hasDisplayedNode("CounterfactualScreenRoot") &&
                hasDisplayedNode("CounterfactualRunButton") &&
                hasDisplayedNode("CounterfactualTargetSlider")
        }

        composeTestRule.onNodeWithTag("CounterfactualRunButton").assertIsDisplayed()
    }

    private fun setCounterfactualTarget(target: Int, currentRisk: Int) {
        composeTestRule.onNodeWithTag("CounterfactualTargetSlider")
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(target.toFloat())
            }

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            val displayedTarget = getDisplayedTargetPercentage()
            displayedTarget <= target &&
                displayedTarget <= MAX_TARGET_PERCENTAGE &&
                displayedTarget < currentRisk
        }
    }

    private fun getDisplayedCurrentRiskPercentage(): Int {
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            try {
                composeTestRule.onAllNodesWithText("%", substring = true)[0].fetchSemanticsNode()
                true
            } catch (_: AssertionError) {
                false
            } catch (_: Exception) {
                false
            }
        }
        val node = composeTestRule.onAllNodesWithText("%", substring = true)[0]
        val text = node.fetchSemanticsNode().config
            .getOrNull(SemanticsProperties.Text)
            ?.firstOrNull()
            ?.text
            .orEmpty()
        return extractLeadingPercentage(text)
    }

    private fun getDisplayedTargetPercentage(): Int {
        val node = composeTestRule.onNodeWithTag("CounterfactualTargetPercentage")
        val text = node.fetchSemanticsNode().config
            .getOrNull(SemanticsProperties.Text)
            ?.firstOrNull()
            ?.text
            .orEmpty()
        return extractLeadingPercentage(text)
    }

    private fun currentCounterfactualResultStateTag(): String {
        return listOf(
            "CounterfactualResultState_Feasible",
            "CounterfactualResultState_NoScenario",
            "CounterfactualResultState_Fallback",
            "CounterfactualResultState_TargetSatisfied"
        ).firstOrNull(::hasDisplayedNode)
            ?: error("No counterfactual result state tag was displayed")
    }

    private fun hasAnyCounterfactualResultState(): Boolean {
        return listOf(
            "CounterfactualResultState_Feasible",
            "CounterfactualResultState_NoScenario",
            "CounterfactualResultState_Fallback",
            "CounterfactualResultState_TargetSatisfied"
        ).any(::hasDisplayedNode)
    }

    private fun hasDisplayedNode(tag: String): Boolean {
        return try {
            composeTestRule.onNodeWithTag(tag).fetchSemanticsNode()
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun hasDisplayedText(text: String): Boolean {
        return try {
            composeTestRule.onNodeWithText(text, substring = true).fetchSemanticsNode()
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun extractLeadingPercentage(text: String): Int {
        val match = Regex("""(\d+)""").find(text)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun percentile(values: List<Long>, percentile: Double): Double {
        val ordered = values.sorted()
        if (ordered.size == 1) {
            return ordered.first().toDouble()
        }
        val rank = percentile.coerceIn(0.0, 100.0) / 100.0 * (ordered.size - 1)
        val lower = rank.toInt()
        val upper = minOf(lower + 1, ordered.size - 1)
        val fraction = rank - lower
        return ordered[lower] + (ordered[upper] - ordered[lower]) * fraction
    }

    private fun scrollHomeToCounterfactualSection() {
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            hasDisplayedNode("HomeCounterfactualSection")
        }
        composeTestRule.onNodeWithTag("HomeCounterfactualSection").performScrollTo()
        composeTestRule.waitForIdle()
    }

    private fun completeSurveyBootstrapForCounterfactual() {
        surveyTestHelper.fillSurveyForFeasibleCounterfactualProfile()
        surveyTestHelper.submitSurvey()
        surveyTestHelper.verifySuccessScreen()
        composeTestRule.onNodeWithText("Beranda").performClick()
        composeTestRule.waitForIdle()
        waitForHomeContentToLoad()
    }

    private fun fillLoginForm(email: String, password: String) {
        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            hasDisplayedNode("EmailTextField") && hasDisplayedNode("PasswordTextField")
        }
        composeTestRule.onNodeWithTag("EmailTextField").performClick().performTextInput(email)
        composeTestRule.onNodeWithTag("PasswordTextField").performClick().performTextInput(password)
        composeTestRule.waitForIdle()
    }

    private fun clickLoginButton() {
        composeTestRule.onNodeWithText("Masuk").performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForHomeScreenAfterLogin() {
        composeTestRule.waitUntil(timeoutMillis = 15000L) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,").assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    private fun waitForHomeContentToLoad() {
        composeTestRule.waitForIdle()
        Thread.sleep(POST_LOGIN_WAIT_MS)

        repeat(MAX_HOME_LOAD_ATTEMPTS) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,").assertIsDisplayed()
                Thread.sleep(2000L)
                return
            } catch (_: AssertionError) {
                Thread.sleep(HOME_LOAD_DELAY_MS)
            }
        }

        composeTestRule.waitForIdle()
    }

    private fun isHomeVisible(): Boolean {
        return hasDisplayedText("Selamat Datang Kembali,") || hasDisplayedText("Persentase Risiko")
    }

    private fun isLoginVisible(): Boolean {
        return hasDisplayedText("Halo,") && hasDisplayedText("Selamat Datang Kembali")
    }

    private fun isSurveyVisible(): Boolean {
        return hasDisplayedText("Pertanyaan") ||
            hasDisplayedText("Berapa berat badan") ||
            hasDisplayedText("Masukkan nilai")
    }

    private fun determineFeasibleTargetPercentage(currentRisk: Int): Int {
        val preferredTarget = minOf(MAX_TARGET_PERCENTAGE, currentRisk - 1)
        return max(1, preferredTarget)
    }
}
