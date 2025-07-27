package com.itb.diabetify.e2e.presentation.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.itb.diabetify.presentation.onboarding.OnBoardingEvent
import com.itb.diabetify.presentation.onboarding.OnBoardingScreen
import com.itb.diabetify.ui.theme.DiabetifyTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.mockk.mockk

@RunWith(AndroidJUnit4::class)
class OnBoardingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var testHelper: OnBoardingTestHelper

    private val mockEvent: (OnBoardingEvent) -> Unit = mockk(relaxed = true)

    @Before
    fun setUp() {
        testHelper = OnBoardingTestHelper(composeTestRule)
    }

    @Test
    fun onboardingFlow_SwipeNavigation_Complete() {
        composeTestRule.setContent {
            DiabetifyTheme {
                OnBoardingScreen(event = mockEvent)
            }
        }

        testHelper.verifyOnboardingScreen()
        testHelper.startOnboarding()
        testHelper.navigateForwardWithSwipe()
        testHelper.completeOnboardingFlow(mockEvent)
    }

    @Test 
    fun onboardingFlow_ButtonNavigation_Complete() {
        composeTestRule.setContent {
            DiabetifyTheme {
                OnBoardingScreen(event = mockEvent)
            }
        }

        testHelper.verifyOnboardingScreen()
        testHelper.startOnboarding()
        testHelper.navigateForwardWithButton()
    }

    @Test
    fun canGoBackToPreviousPage_WorksCorrectly() {
        composeTestRule.setContent {
            DiabetifyTheme {
                OnBoardingScreen(event = mockEvent)
            }
        }

        testHelper.verifyOnboardingScreen()
        testHelper.startOnboarding()
        testHelper.navigateForwardWithSwipe()
        testHelper.navigateBackWithSwipe()
    }
}