package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.itb.diabetify.R
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualCandidate
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualPlannerInput
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualPlannerPrediction
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualPredictionInfo
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.ui.theme.DiabetifyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterfactualResultContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feasibleResult_rendersFeatureChangesAndActions() {
        var tryAnotherClicked = false

        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualResultContent(
                    result = feasibleResultPayload(),
                    resultMeta = CounterfactualJobResultData(
                        jobId = "job-feasible",
                        jobStatus = "completed",
                        reasonCode = "OK",
                        result = feasibleResultPayload()
                    ),
                    selectedDurationWeeks = 8,
                    isSaved = false,
                    hasDifferentActiveGoal = true,
                    successMessage = null,
                    showReplaceGoalDialog = false,
                    displayFeatures = listOf(
                        CounterfactualDisplayFeature(
                            featureName = "smoking_status",
                            label = "Status Merokok",
                            baselineNumeric = 2.0,
                            candidateNumeric = 1.0,
                            baselineText = "Masih Aktif",
                            candidateText = "Sudah Berhenti",
                            chipText = "Perubahan Status",
                            iconResId = R.drawable.ic_smoking
                        )
                    ),
                    onShowReplaceGoalDialogChange = {},
                    onDismissSuccess = {},
                    onBackToHome = {},
                    onBackToPlannerSetup = { tryAnotherClicked = true },
                    onDurationSelected = {},
                    onSave = {},
                    onConfirmReplaceGoal = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualResultState_Feasible").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hasil Rencana Risiko").assertIsDisplayed()
        composeTestRule.onNodeWithText("Skenario ditemukan!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status Merokok").assertIsDisplayed()
        composeTestRule.onNodeWithText("Masih Aktif").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sudah Berhenti").assertIsDisplayed()
        composeTestRule.onNodeWithText("Menyimpan rencana ini akan menggantikan goal aktif sebelumnya").assertExists()
        composeTestRule.onNodeWithTag("CounterfactualSaveGoalButton").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("CounterfactualTryAnotherButton").performScrollTo().performClick()

        assertTrue(tryAnotherClicked)
    }

    @Test
    fun targetAlreadySatisfied_rendersSatisfiedState() {
        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualResultContent(
                    result = CounterfactualResultPayload(
                        status = "FEASIBLE",
                        reasonCode = "TARGET_ALREADY_SATISFIED",
                        message = "Target sudah terpenuhi"
                    ),
                    resultMeta = CounterfactualJobResultData(
                        jobId = "job-satisfied",
                        jobStatus = "completed",
                        reasonCode = "TARGET_ALREADY_SATISFIED"
                    ),
                    selectedDurationWeeks = 8,
                    isSaved = false,
                    hasDifferentActiveGoal = false,
                    successMessage = null,
                    showReplaceGoalDialog = false,
                    displayFeatures = emptyList(),
                    onShowReplaceGoalDialogChange = {},
                    onDismissSuccess = {},
                    onBackToHome = {},
                    onBackToPlannerSetup = {},
                    onDurationSelected = {},
                    onSave = {},
                    onConfirmReplaceGoal = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualResultState_TargetSatisfied").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kondisi Anda saat ini sudah memenuhi target risiko yang dipilih").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pilih Target Baru").assertIsDisplayed()
    }

    @Test
    fun missingResult_rendersNoScenarioState() {
        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualResultContent(
                    result = null,
                    resultMeta = null,
                    selectedDurationWeeks = 8,
                    isSaved = false,
                    hasDifferentActiveGoal = false,
                    successMessage = null,
                    showReplaceGoalDialog = false,
                    displayFeatures = emptyList(),
                    onShowReplaceGoalDialogChange = {},
                    onDismissSuccess = {},
                    onBackToHome = {},
                    onBackToPlannerSetup = {},
                    onDurationSelected = {},
                    onSave = {},
                    onConfirmReplaceGoal = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualResultState_NoScenario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Skenario realistis belum ditemukan untuk target dan batasan faktor ini").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pilih Target Baru").assertIsDisplayed()
    }

    private fun feasibleResultPayload(): CounterfactualResultPayload {
        return CounterfactualResultPayload(
            status = "FEASIBLE",
            reasonCode = "OK",
            inputPrediction = CounterfactualPredictionInfo(
                className = "high_risk",
                probabilityLowRisk = 0.32
            ),
            candidates = listOf(
                CounterfactualCandidate(
                    candidateId = "cf_80",
                    prediction = CounterfactualPredictionInfo(
                        className = "low_risk",
                        probabilityLowRisk = 0.70
                    )
                )
            ),
            plannerInput = CounterfactualPlannerInput(
                changedFeatures = emptyList(),
                inputPrediction = CounterfactualPlannerPrediction(
                    className = "high_risk",
                    probabilityLowRisk = 0.32
                ),
                candidatePrediction = CounterfactualPlannerPrediction(
                    className = "low_risk",
                    probabilityLowRisk = 0.70
                ),
                recommendedCandidateId = "cf_80"
            )
        )
    }
}
