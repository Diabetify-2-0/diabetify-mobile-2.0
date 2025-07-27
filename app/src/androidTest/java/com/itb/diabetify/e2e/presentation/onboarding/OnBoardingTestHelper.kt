package com.itb.diabetify.e2e.presentation.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.itb.diabetify.presentation.onboarding.OnBoardingEvent
import com.itb.diabetify.presentation.onboarding.pages
import io.mockk.verify

class OnBoardingTestHelper(
    private val composeTestRule: ComposeTestRule
) {
    fun verifyOnboardingScreen() {
        composeTestRule.onNodeWithText("Kenali Risiko", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Mulai").assertIsDisplayed()
    }

    private fun verifyOnboardingPage(title: String, description: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    fun startOnboarding() {
        composeTestRule.onNodeWithText("Mulai").performClick()
    }

    fun navigateForwardWithSwipe() {
        for (i in 0 until pages.size - 1) {
            verifyOnboardingPage(
                pages[i].title,
                pages[i].description
            )

            composeTestRule.onNodeWithText(pages[i].title).performTouchInput {
                swipeLeft()
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(pages[i + 1].title).assertIsDisplayed()
        }
    }

    fun navigateForwardWithButton() {
        for (i in 0 until pages.size - 1) {
            verifyOnboardingPage(
                pages[i].title,
                pages[i].description
            )

            composeTestRule.onNodeWithText(">").performClick()
            composeTestRule.waitForIdle()
            verifyOnboardingPage(
                pages[i + 1].title,
                pages[i + 1].description
            )
        }
    }

    fun completeOnboardingFlow(mockEvent: (OnBoardingEvent) -> Unit) {
        composeTestRule.onNodeWithText(">").performClick()
        verify { mockEvent(OnBoardingEvent.SaveAppEntry) }
    }

    fun navigateBackWithSwipe() {
        composeTestRule.onNodeWithText(pages[pages.size - 1].title).performTouchInput {
            swipeRight()
        }
        composeTestRule.waitForIdle()

        verifyOnboardingPage(
            pages[pages.size - 2].title,
            pages[pages.size - 2].description
        )
    }
}