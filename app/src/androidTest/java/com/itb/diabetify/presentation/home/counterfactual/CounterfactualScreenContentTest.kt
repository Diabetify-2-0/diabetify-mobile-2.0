package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.itb.diabetify.R
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.ui.theme.DiabetifyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterfactualScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenContent_rendersOptionsAndRunAction() {
        var runClicked = false
        var toggledKey: String? = null
        var options by mutableStateOf(
            listOf(
                counterfactualOption(
                    key = "BMI",
                    label = "Berat Badan",
                    iconResId = R.drawable.ic_weight,
                    isSelected = true
                ),
                counterfactualOption(
                    key = "smoking_status",
                    label = "Status Merokok",
                    iconResId = R.drawable.ic_smoking,
                    isSelected = false
                )
            )
        )

        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualScreenContent(
                    options = options,
                    riskTargetInput = "45",
                    latestRisk = 62.0,
                    weightText = "82 kg",
                    physicalActivityText = "2 hari/minggu",
                    hypertensionText = "Ya",
                    cholesterolText = "Tidak",
                    smokingStatusText = "Masih aktif merokok",
                    baselineAgeText = "45 tahun",
                    bloodlineText = "Ada",
                    macrosomicText = "Tidak",
                    brinkmanIndexText = "Sedang",
                    optionCurrentValues = mapOf(
                        "BMI" to "82 kg",
                        "smoking_status" to "Masih aktif merokok"
                    ),
                    errorMessage = null,
                    successMessage = null,
                    loadingMessage = null,
                    isLoading = false,
                    onBack = {},
                    onToggleOption = { key ->
                        toggledKey = key
                        options = options.map { option ->
                            if (option.key == key) option.copy(isSelected = !option.isSelected) else option
                        }
                    },
                    onTargetChange = {},
                    onRun = { runClicked = true },
                    onDismissError = {},
                    onDismissSuccess = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualScreenRoot").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rencana Penurunan Risiko").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CounterfactualOption_BMI")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("CounterfactualOption_smoking_status")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("CounterfactualRunButton")
            .assertIsDisplayed()
            .performClick()

        assertEquals("BMI", toggledKey)
        assertTrue(runClicked)
    }

    @Test
    fun targetHelperText_changesWhenTargetAlreadySatisfied() {
        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualScreenContent(
                    options = listOf(counterfactualOption()),
                    riskTargetInput = "70",
                    latestRisk = 40.0,
                    weightText = "82 kg",
                    physicalActivityText = "2 hari/minggu",
                    hypertensionText = "Ya",
                    cholesterolText = "Tidak",
                    smokingStatusText = "Masih aktif merokok",
                    baselineAgeText = "45 tahun",
                    bloodlineText = "Ada",
                    macrosomicText = "Tidak",
                    brinkmanIndexText = null,
                    optionCurrentValues = mapOf("BMI" to "82 kg"),
                    errorMessage = null,
                    successMessage = null,
                    loadingMessage = null,
                    isLoading = false,
                    onBack = {},
                    onToggleOption = {},
                    onTargetChange = {},
                    onRun = {},
                    onDismissError = {},
                    onDismissSuccess = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualTargetHelperText")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("CounterfactualTargetPercentage")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("CounterfactualTargetCategory")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Risiko Anda saat ini sudah memenuhi target ini. Anda akan langsung diarahkan ke hasil tanpa pencarian skenario tambahan."
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun targetHelperText_changesWhenCounterfactualSearchIsNeeded() {
        composeTestRule.setContent {
            DiabetifyTheme {
                CounterfactualScreenContent(
                    options = listOf(counterfactualOption()),
                    riskTargetInput = "35",
                    latestRisk = 62.0,
                    weightText = "82 kg",
                    physicalActivityText = "2 hari/minggu",
                    hypertensionText = "Ya",
                    cholesterolText = "Tidak",
                    smokingStatusText = "Masih aktif merokok",
                    baselineAgeText = "45 tahun",
                    bloodlineText = "Ada",
                    macrosomicText = "Tidak",
                    brinkmanIndexText = null,
                    optionCurrentValues = mapOf("BMI" to "82 kg"),
                    errorMessage = null,
                    successMessage = null,
                    loadingMessage = null,
                    isLoading = false,
                    onBack = {},
                    onToggleOption = {},
                    onTargetChange = {},
                    onRun = {},
                    onDismissError = {},
                    onDismissSuccess = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CounterfactualTargetHelperText")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Pilihan yang sangat baik! Kami akan menyusun skenario realistis agar risiko Anda turun di bawah 35%."
        ).performScrollTo().assertIsDisplayed()
    }

    private fun counterfactualOption(
        key: String = "BMI",
        label: String = "Berat Badan",
        iconResId: Int = R.drawable.ic_weight,
        isSelected: Boolean = true,
    ): HomeViewModel.CounterfactualOption {
        return HomeViewModel.CounterfactualOption(
            key = key,
            label = label,
            iconResId = iconResId,
            isSelected = isSelected
        )
    }
}
