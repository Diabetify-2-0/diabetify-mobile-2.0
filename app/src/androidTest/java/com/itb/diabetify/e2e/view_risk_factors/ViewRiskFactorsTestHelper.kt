package com.itb.diabetify.e2e.view_risk_factors

import android.annotation.SuppressLint
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.itb.diabetify.MainActivity

class ViewRiskFactorsTestHelper(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    @SuppressLint("CheckResult")
    fun startAppAndNavigateToHome() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()
            return
        } catch (_: AssertionError) {
        }

        try {
            composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Mulai").assertIsDisplayed()
            composeTestRule.onNodeWithText("Mulai").performClick()
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {
        }

        val maxOnboardingPages = 4

        repeat(maxOnboardingPages) {
            try {
                composeTestRule.onNodeWithText(">").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
            } catch (e: AssertionError) {
                try {
                    composeTestRule.onRoot().performTouchInput {
                        swipeLeft()
                    }
                    composeTestRule.waitForIdle()
                    Thread.sleep(500)
                } catch (_: AssertionError) {
                }
            }

            try {
                composeTestRule.onNodeWithText(">").assertExists()
            } catch (_: AssertionError) {
            }
        }

        try {
            composeTestRule.onNodeWithText(">").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
        } catch (_: AssertionError) {
        }

        composeTestRule.waitForIdle()

        try {
            composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                .assertIsDisplayed()
            waitForHomeContentToLoad()
            return
        } catch (_: AssertionError) {
        }

        fillLoginForm(
            email = "testmale@example.com",
            password = "bewebewe"
        )

        clickLoginButton()

        waitForHomeScreenAfterLogin()
        waitForHomeContentToLoad()
    }

    private fun fillLoginForm(email: String, password: String) {
        composeTestRule.onNodeWithTag("EmailTextField")
            .performClick()
            .performTextInput(email)

        composeTestRule.onNodeWithTag("PasswordTextField")
            .performClick()
            .performTextInput(password)

        composeTestRule.waitForIdle()
    }

    private fun clickLoginButton() {
        composeTestRule.onNodeWithText("Masuk")
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForHomeScreenAfterLogin() {
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    private fun waitForHomeContentToLoad() {
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            try {
                composeTestRule.onNodeWithText("Selamat Datang Kembali,")
                    .assertIsDisplayed()

                Thread.sleep(2000)
                break
            } catch (e: AssertionError) {
                attempts++
                Thread.sleep(1000)
            }
        }

        composeTestRule.waitForIdle()
    }

    fun verifyRiskFactorDetailCardDisplayed() {
        var attempts = 0
        var found = false

        while (attempts < 5 && !found) {
            try {
                composeTestRule.onNodeWithText("Kontribusi Faktor Risiko")
                    .assertIsDisplayed()
                found = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 400f,
                        endY = centerY - 400f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                attempts++
            }
        }

        composeTestRule.onNodeWithText("Kontribusi Faktor Risiko")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("BarChart").assertIsDisplayed()
    }

    fun verifyChartTabSwitching() {
        composeTestRule.onNodeWithText("Grafik Batang").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grafik Lingkaran").assertIsDisplayed()

        composeTestRule.onNodeWithText("Grafik Lingkaran").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("PieChart").assertIsDisplayed()

        composeTestRule.onNodeWithText("Grafik Lingkaran").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grafik Batang").assertIsDisplayed()

        composeTestRule.onNodeWithText("Grafik Batang").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("BarChart").assertIsDisplayed()
    }

    fun navigateToRiskFactorDetail() {
        var buttonAttempts = 0
        var buttonFound = false

        while (buttonAttempts < 8 && !buttonFound) {
            try {
                composeTestRule.onAllNodesWithText("Lihat Detail")[1]
                    .assertIsDisplayed()
                buttonFound = true
            } catch (e: AssertionError) {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 350f,
                        endY = centerY - 350f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(400)
                buttonAttempts++
            }
        }
        composeTestRule.onAllNodesWithText("Lihat Detail")[1]
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    fun verifyRiskFactorDetailScreenDisplayed() {
        composeTestRule.onNodeWithText("Perhitungan faktor risiko")
            .assertIsDisplayed()
    }

    fun verifyChartDisplayed() {
        composeTestRule.onNodeWithTag("BarChart").assertIsDisplayed()

        composeTestRule.onNodeWithText("Grafik Batang").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grafik Lingkaran").assertIsDisplayed()
    }

    fun verifyRiskFactorExplanationDisplayed() {
        val factorExplanations = mapOf(
            "U: Usia" to "Usia Anda yang tergolong",
            "IMT: Indeks Massa Tubuh" to "Indeks massa tubuh Anda",
            "H: Hipertensi" to "Hipertensi Anda",
            "SM: Status Merokok" to "Status merokok aktif Anda",
            "K: Kolesterol" to "Kolesterol Anda yang tinggi",
            "RBM: Riwayat Bayi Makrosomia" to "Tidak pernah melahirkan bayi besar",
            "IB: Indeks Brinkman" to "Sebagai perokok ringan",
            "AF: Aktivitas Fisik" to "Frekuensi aktivitas fisik Anda",
            "RK: Riwayat Keluarga" to "Tidak adanya riwayat keluarga"
        )
        
        val foundFactors = mutableSetOf<String>()
        var scrollAttempts = 0
        val maxScrollAttempts = 20
        
        while (scrollAttempts < maxScrollAttempts && foundFactors.size < factorExplanations.size) {
            for ((header, explanation) in factorExplanations) {
                if (!foundFactors.contains(header)) {
                    var found = false
                    try {
                        composeTestRule.onNodeWithText(header, substring = true).assertIsDisplayed()
                        found = true
                    } catch (_: AssertionError) {}
                    if (!found) {
                        try {
                            composeTestRule.onNodeWithText(explanation, substring = true).assertIsDisplayed()
                            found = true
                        } catch (_: AssertionError) {}
                    }
                    if (found) {
                        foundFactors.add(header)
                    }
                }
            }
            
            try {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY + 200f,
                        endY = centerY - 200f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
                scrollAttempts++
            } catch (_: Exception) {
                break
            }
        }
        
        assert(foundFactors.isNotEmpty()) {
            "No risk factor explanations were found. Expected to find explanations for factors like: ${factorExplanations.keys.joinToString(", ")}"
        }
        
        var explanationContentFound = false
        var contentScrollAttempts = 0
        
        repeat(5) {
            try {
                composeTestRule.onRoot().performTouchInput {
                    swipeUp(
                        startY = centerY - 400f,
                        endY = centerY + 400f
                    )
                }
                composeTestRule.waitForIdle()
                Thread.sleep(300)
            } catch (_: Exception) {
            }
        }
        
        while (contentScrollAttempts < maxScrollAttempts && !explanationContentFound) {
            try {
                val explanationIndicators = listOf(
                    "Nilai Ideal",
                    "Nilai Anda", 
                    "risiko diabetes",
                    "faktor risiko",
                    "berpengaruh",
                    "meningkatkan",
                    "menurunkan"
                )
                
                for (indicator in explanationIndicators) {
                    try {
                        composeTestRule.onNodeWithText(indicator, substring = true)
                            .assertIsDisplayed()
                        explanationContentFound = true
                        break
                    } catch (_: AssertionError) {
                    }
                }
                
                if (!explanationContentFound) {
                    composeTestRule.onRoot().performTouchInput {
                        swipeUp(
                            startY = centerY + 300f,
                            endY = centerY - 300f
                        )
                    }
                    composeTestRule.waitForIdle()
                    Thread.sleep(400)
                    contentScrollAttempts++
                }
                
            } catch (_: Exception) {
                break
            }
        }
        
        assert(explanationContentFound) {
            "Risk factor explanation content was not found. The cards may not be properly loaded with explanation text."
        }
    }

    fun clickBackButton() {
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun verifyNavigationBackToHome() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Simulasi What-If")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    fun getRiskFactorSummaryValues(isDetail: Boolean = false): Map<String, String> {
        val values = mutableMapOf<String, String>()
        val factorHeaders = listOf(
            "U: Usia", "IMT: Indeks Massa Tubuh", "H: Hipertensi", "SM: Status Merokok",
            "K: Kolesterol", "RBM: Riwayat Bayi Makrosomia", "IB: Indeks Brinkman",
            "AF: Aktivitas Fisik", "RK: Riwayat Keluarga"
        )
        val percentNodes = composeTestRule.onAllNodesWithText("%", substring = true).fetchSemanticsNodes()
        val factorPercentNodes = if (isDetail) {
            percentNodes
        } else {
            percentNodes.drop(1)
        }
        for ((header, percentNode) in factorHeaders.zip(factorPercentNodes)) {
            val percentText = percentNode.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.toString() ?: ""
            values[header] = percentText
        }
        return values
    }

    fun verifyRiskFactorValuesConsistency(summary: Map<String, String>, detail: Map<String, String>) {
        for ((header, summaryValue) in summary) {
            val detailValue = detail[header]
            if (summaryValue.isNotBlank() && !detailValue.isNullOrBlank()) {
                assert(summaryValue == detailValue) {
                    "Risk factor value mismatch for $header: summary=$summaryValue, detail=$detailValue"
                }
            }
        }
    }
}