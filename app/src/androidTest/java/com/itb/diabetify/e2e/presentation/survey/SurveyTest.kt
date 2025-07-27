package com.itb.diabetify.e2e.presentation.survey

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.di.AppModule
import com.itb.diabetify.e2e.repository.FakeProfileRepository
import com.itb.diabetify.e2e.repository.FakeUserRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SurveyTest  {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @BindValue
    val fakeRepo: FakeProfileRepository = FakeProfileRepository()

    @BindValue
    val fakeUserRepo: FakeUserRepository = FakeUserRepository()

    private lateinit var testHelper: SurveyTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = SurveyTestHelper(composeTestRule)
    }

    @Test
    fun surveyFlow_Complete() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestions()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyWithInvalidWeight_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsWithInvalidWeight("25")
        testHelper.verifyWeightValidationError("Berat badan harus antara 30-300 kg")
    }

    @Test
    fun surveyWithInvalidHeight_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsWithInvalidHeight("90")
        testHelper.verifyHeightValidationError("Tinggi badan harus antara 100-250 cm")
    }

    @Test
    fun surveyWithInvalidSmokingAge_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsWithInvalidSmokingAge("5")
        testHelper.verifySmokingAgeValidationError("Usia mulai merokok harus antara 10-80 tahun")
    }

    @Test
    fun surveyWithInvalidSmokingAmount_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsWithInvalidSmokingAmount("65")
        testHelper.verifySmokingAmountValidationError("Jumlah rokok harus antara 0-60 batang")
    }

    @Test
    fun surveyWithInvalidBloodPressure_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyWithInvalidBloodPressure("300", "200")
        testHelper.verifyBloodPressureValidationError("Tekanan sistolik harus antara 70-250 mmHg")
    }

    @Test
    fun surveyWithInvalidPhysicalActivity_ShowsValidationError() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsWithInvalidActivity("8")
        testHelper.verifyActivityValidationError("Aktivitas fisik harus antara 0-7 hari")
    }

    @Test
    fun surveyNavigation_BackAndForward() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillFirstTwoQuestions()
        testHelper.navigateBackInSurvey()
        testHelper.verifyOnFirstQuestion()
        testHelper.navigateForwardInSurvey()
        testHelper.verifyOnSecondQuestion()
    }

    @Test
    fun surveyWithEmptyFields_DisablesNextButton() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.verifyNextButtonDisabledWhenFieldsEmpty()
    }

    @Test
    fun surveyReviewScreen_DisplaysAnswersCorrectly() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestions()
        testHelper.navigateToReviewScreen()
        testHelper.verifyReviewScreenDisplaysAnswers()
        testHelper.verifyCanGoBackFromReview()
    }

    @Test
    fun surveyForMaleUser_SkipsPregnancyQuestion() {
        fakeRepo.shouldFailFetchProfile = true
        fakeUserRepo.userGender = "Laki-laki"
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyQuestionsForMaleUser()
        testHelper.verifyPregnancyQuestionSkipped()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyForFemaleUser_ShowsPregnancyQuestion() {
        fakeRepo.shouldFailFetchProfile = true
        fakeUserRepo.userGender = "Perempuan"
        testHelper.startAppAndNavigateToSurvey()
        Thread.sleep(2000)
        testHelper.fillSurveyQuestionsForFemaleUser()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyWithHypertensionKnowledge_ShowsBloodPressureQuestions() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyWithKnownBloodPressureAndVerifyQuestions()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyWithoutHypertensionKnowledge_SkipsBloodPressureQuestions() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyWithUnknownBloodPressure()
        testHelper.verifyBloodPressureQuestionsSkipped()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyNonSmoker_SkipsSmokingQuestions() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyAsNonSmoker()
        testHelper.verifySmokingQuestionsSkipped()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyFormerSmoker_ShowsRelevantSmokingQuestions() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyAsFormerSmokerAndVerifyQuestions()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyCurrentSmoker_ShowsAllSmokingQuestions() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.fillSurveyAsCurrentSmokerAndVerifyQuestions()
        testHelper.submitSurvey()
        testHelper.verifySuccessScreen()
    }

    @Test
    fun surveyProgressIndicator_WorksCorrectly() {
        fakeRepo.shouldFailFetchProfile = true
        testHelper.startAppAndNavigateToSurvey()
        testHelper.verifyProgressAtStart()
        testHelper.fillHalfOfSurveyQuestions()
        testHelper.verifyProgressAtMiddle()
        testHelper.fillRemainingSurveyQuestions()
        testHelper.verifyProgressAtEnd()
    }
}