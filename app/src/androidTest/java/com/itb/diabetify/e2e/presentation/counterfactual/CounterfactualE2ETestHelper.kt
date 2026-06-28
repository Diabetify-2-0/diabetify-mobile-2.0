package com.itb.diabetify.e2e.presentation.counterfactual

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.util.Constants
import kotlin.math.max
import java.net.HttpURLConnection
import java.net.URL

class CounterfactualE2ETestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    companion object {
        private const val APP_START_MAX_ATTEMPTS = 20
        private const val MAX_HOME_LOAD_ATTEMPTS = 10
        private const val HOME_LOAD_DELAY_MS = 1000L
        private const val POST_LOGIN_WAIT_MS = 3000L
        private const val POST_RESULT_SETTLE_MS = 500L
        private const val COUNTERFACTUAL_TIMEOUT_MS = 30000L
        private const val LATENCY_TARGET_MS = 5000L
        private const val MAX_TARGET_PERCENTAGE = 44
        private const val FEASIBLE_TARGET_PERCENTAGE = 50
        private const val STRICT_INFEASIBLE_TARGET_PERCENTAGE = 1
    }

    data class CounterfactualRunResult(
        val stateTag: String,
        val latencyMs: Long,
        val targetPercentage: Int
    )

    fun verifyCounterfactualHealthReady(): String {
        val healthUrl = URL(URL(Constants.BASE_URL), "health/ready")
        val connection = healthUrl.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        try {
            val responseCode = connection.responseCode
            val body = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { reader -> reader.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
            }
            check(responseCode == 200) {
                "Backend readiness failed with HTTP $responseCode: $body"
            }
            check(body.contains("\"status\":\"success\"") || body.contains("\"status\": \"success\"")) {
                "Backend readiness response did not report success: $body"
            }
            check(body.contains("counterfactual")) {
                "Backend readiness response did not include counterfactual check: $body"
            }
            return "ready url=$healthUrl"
        } finally {
            connection.disconnect()
        }
    }

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
                    fillLoginForm(email = "christian.justin23@gmail.com", password = "bewebewe")
                    clickLoginButton()
                    waitForHomeScreenAfterLogin()
                    waitForHomeContentToLoad()
                    return
                }

                isSurveyVisible() -> {
                    error(
                        "Counterfactual E2E must start from Login -> Home -> Counterfactual. " +
                            "Survey is visible, which means the test account is missing required profile/prediction setup."
                    )
                }

                hasDisplayedText("Kenali Risiko") && hasDisplayedText("Mulai") -> {
                    error(
                        "Counterfactual E2E does not resolve onboarding. " +
                            "Complete onboarding before running this service-backed E2E test."
                    )
                }

                hasDisplayedText("Sudah memiliki akun") -> {
                    error(
                        "Counterfactual E2E does not navigate through onboarding entry screens. " +
                            "Start the app in a state where Login or Home is directly reachable."
                    )
                }
            }

            Thread.sleep(1000L)
        }

        error("Could not reach Home screen for counterfactual E2E. Expected either Login or Home; onboarding, survey, and bootstrap setup are intentionally excluded.")
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

    fun ensureCounterfactualSetup() {
        when {
            hasDisplayedNode("CounterfactualScreenRoot") -> {
                composeTestRule.onNodeWithTag("CounterfactualScreenRoot").assertIsDisplayed()
            }

            hasAnyCounterfactualResultState() -> {
                returnToCounterfactualSetup()
            }

            else -> {
                startAppAndNavigateToHome()
                verifyCounterfactualEntryAvailable()
                navigateToCounterfactualScreen()
            }
        }
    }

    fun verifyMutableToggle(): String {
        val key = "BMI"
        composeTestRule.onNodeWithTag(optionTag(key)).performScrollTo().assertIsDisplayed()
        val initiallySelected = isOptionSelected(key)

        clickMutableOption(key)
        waitForOptionSelected(key, !initiallySelected)
        composeTestRule.onNodeWithTag(optionTag(key)).assert(
            androidx.compose.ui.test.SemanticsMatcher.expectValue(
                SemanticsProperties.Selected,
                !initiallySelected
            )
        )

        clickMutableOption(key)
        waitForOptionSelected(key, initiallySelected)
        return "BMI toggled ${initiallySelected}->${!initiallySelected}->${initiallySelected}"
    }

    fun verifyNoMutableValidation(): String {
        deselectAllMutableOptions()
        composeTestRule.onNodeWithTag("CounterfactualRunButton").assertIsDisplayed().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            hasDisplayedText("Pilih minimal satu faktor")
        }
        composeTestRule.onNodeWithTag("CounterfactualScreenRoot").assertIsDisplayed()
        return "validation message shown when no mutable option is selected"
    }

    fun selectOnlyMutableOptions(keys: Set<String>): String {
        val availableKeys = availableMutableOptionKeys()
        require(keys.all { it in availableKeys }) {
            "Requested mutable keys $keys, but available keys are $availableKeys"
        }

        availableKeys.forEach { key ->
            val shouldBeSelected = key in keys
            if (isOptionSelected(key) != shouldBeSelected) {
                clickMutableOption(key)
                waitForOptionSelected(key, shouldBeSelected)
            }
        }

        val selected = availableKeys.filter(::isOptionSelected)
        check(selected.toSet() == keys) {
            "Expected selected mutable keys $keys, got $selected"
        }
        return "selected=${selected.joinToString(",")}"
    }

    fun runFeasibleBmiOnly(): CounterfactualRunResult {
        selectOnlyMutableOptions(setOf("BMI"))
        return runServiceBackedCounterfactualAndMeasureLatency(targetPercentage = FEASIBLE_TARGET_PERCENTAGE)
    }

    fun runFeasibleBmiAndActivity(): CounterfactualRunResult {
        selectOnlyMutableOptions(setOf("BMI", "moderate_physical_activity_frequency"))
        return runServiceBackedCounterfactualAndMeasureLatency(targetPercentage = FEASIBLE_TARGET_PERCENTAGE)
    }

    fun runFeasibleFullActionable(): CounterfactualRunResult {
        selectOnlyMutableOptions(
            setOf("BMI", "moderate_physical_activity_frequency", "is_hypertension", "is_cholesterol")
        )
        return runServiceBackedCounterfactualAndMeasureLatency(targetPercentage = MAX_TARGET_PERCENTAGE)
    }

    fun runStrictInfeasibleBmiOnly(): CounterfactualRunResult {
        selectOnlyMutableOptions(setOf("BMI"))
        return runServiceBackedCounterfactualAndMeasureLatency(
            targetPercentage = STRICT_INFEASIBLE_TARGET_PERCENTAGE
        )
    }

    fun runServiceBackedCounterfactualAndMeasureLatency(
        targetPercentage: Int? = null
    ): CounterfactualRunResult {
        val currentRisk = getDisplayedCurrentRiskPercentage()
        require(currentRisk > 1) {
            "Counterfactual E2E requires current risk above 1%, but got $currentRisk%"
        }

        val target = targetPercentage ?: determineFeasibleTargetPercentage(currentRisk)
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

        return CounterfactualRunResult(
            stateTag = stateTag,
            latencyMs = latencyMs,
            targetPercentage = target
        )
    }

    fun verifyServiceBackedResultState(result: CounterfactualRunResult) {
        composeTestRule.onNodeWithTag(result.stateTag).assertIsDisplayed()
        require(result.stateTag == "CounterfactualResultState_Feasible") {
            "Expected a FEASIBLE counterfactual result for the controlled profile, but got ${result.stateTag}."
        }
    }

    fun verifyPollingReachedTerminal(result: CounterfactualRunResult): String {
        composeTestRule.onNodeWithTag(result.stateTag).assertIsDisplayed()
        check(result.latencyMs > 0) { "Polling latency must be positive" }
        return "terminal=${result.stateTag}, latencyMs=${result.latencyMs}"
    }

    fun verifyFeasibleResultContent(result: CounterfactualRunResult): String {
        verifyServiceBackedResultState(result)
        composeTestRule.onNodeWithTag("CounterfactualFeasibleHero").assertIsDisplayed()
        composeTestRule.onNodeWithText("Risiko saat ini", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Setelah skenario", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Visualisasi Perubahan Fitur", substring = true).assertIsDisplayed()

        val changedFeatureTags = listOf(
            "CounterfactualChangedFeature_BMI",
            "CounterfactualChangedFeature_moderate_physical_activity_frequency",
            "CounterfactualChangedFeature_is_hypertension",
            "CounterfactualChangedFeature_is_cholesterol",
            "CounterfactualChangedFeature_smoking_status"
        )
        check(changedFeatureTags.any(::hasDisplayedNode)) {
            "No changed feature visualization was displayed"
        }

        return "feasible result content visible for target=${result.targetPercentage}, latencyMs=${result.latencyMs}"
    }

    fun saveFeasibleResultAsPlannerGoal(): String {
        composeTestRule.onNodeWithTag("CounterfactualSaveGoalButton")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        if (hasDisplayedText("Ganti Goal Aktif?")) {
            composeTestRule.onNodeWithText("Ganti Goal").performClick()
        }

        composeTestRule.waitUntil(timeoutMillis = 10000L) {
            hasDisplayedText("Rencana berhasil disimpan") ||
                hasDisplayedText("Goal Sudah Aktif")
        }
        return "planner goal saved or already active"
    }

    fun verifyInfeasibleResultState(result: CounterfactualRunResult): String {
        val allowedStates = setOf(
            "CounterfactualResultState_NoScenario",
            "CounterfactualResultState_Fallback"
        )
        composeTestRule.onNodeWithTag(result.stateTag).assertIsDisplayed()
        check(result.stateTag in allowedStates) {
            "Expected infeasible/no-scenario result, got ${result.stateTag}"
        }
        return "infeasible terminal=${result.stateTag}, target=${result.targetPercentage}, latencyMs=${result.latencyMs}"
    }

    fun verifyInfeasibleResultContent(): String {
        check(
            hasDisplayedNode("CounterfactualResultState_NoScenario") ||
                hasDisplayedNode("CounterfactualResultState_Fallback")
        ) {
            "No infeasible result state was displayed"
        }
        composeTestRule.onNodeWithText("Skenario realistis belum ditemukan", substring = true)
            .assertIsDisplayed()
        return "no-scenario message visible"
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

    private fun deselectAllMutableOptions() {
        availableMutableOptionKeys().forEach { key ->
            if (isOptionSelected(key)) {
                clickMutableOption(key)
                waitForOptionSelected(key, false)
            }
        }
    }

    private fun availableMutableOptionKeys(): List<String> {
        return listOf(
            "smoking_status",
            "BMI",
            "moderate_physical_activity_frequency",
            "is_hypertension",
            "is_cholesterol"
        ).filter { key ->
            try {
                composeTestRule.onNodeWithTag(optionTag(key)).performScrollTo().fetchSemanticsNode()
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun clickMutableOption(key: String) {
        composeTestRule.onNodeWithTag(optionTag(key))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForOptionSelected(key: String, selected: Boolean) {
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            isOptionSelected(key) == selected
        }
    }

    private fun isOptionSelected(key: String): Boolean {
        val node = composeTestRule.onNodeWithTag(optionTag(key))
            .performScrollTo()
            .fetchSemanticsNode()
        return node.config.getOrNull(SemanticsProperties.Selected) == true
    }

    private fun optionTag(key: String): String = "CounterfactualOption_$key"

    private fun setCounterfactualTarget(target: Int, currentRisk: Int) {
        composeTestRule.onNodeWithTag("CounterfactualTargetSlider")
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(target.toFloat())
            }

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            val displayedTarget = getDisplayedTargetPercentage()
            displayedTarget <= target &&
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
