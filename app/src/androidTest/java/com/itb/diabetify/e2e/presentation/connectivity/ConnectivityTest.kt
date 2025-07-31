package com.itb.diabetify.e2e.presentation.connectivity

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.itb.diabetify.MainActivity
import com.itb.diabetify.di.AppModule
import com.itb.diabetify.e2e.manager.FakeConnectivityManager
import com.itb.diabetify.e2e.repository.FakeAuthRepository
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
class ConnectivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @BindValue
    val fakeConnectivityManager: FakeConnectivityManager = FakeConnectivityManager()

    @BindValue
    val fakeAuthRepo: FakeAuthRepository = FakeAuthRepository()

    @BindValue
    val fakeProfileRepo: FakeProfileRepository = FakeProfileRepository()

    @BindValue
    val fakeUserRepo: FakeUserRepository = FakeUserRepository()

    private lateinit var testHelper: ConnectivityTestHelper

    @Before
    fun setUp() {
        hiltRule.inject()
        testHelper = ConnectivityTestHelper(composeTestRule)
        
        fakeConnectivityManager.reset()
        fakeAuthRepo.reset()
        fakeProfileRepo.shouldFailFetchProfile = false
        fakeUserRepo.shouldFailFetchUser = false
    }

    @Test
    fun appStartup_NoConnectivity_ShowsNoInternetScreen() {
        fakeConnectivityManager.setConnected(false)
        testHelper.startAppAndWaitForSplash()
        testHelper.verifyNoInternetScreenDisplayed()
    }

    @Test
    fun appStartup_WithConnectivity_ShowsOnboardingScreen() {
        fakeConnectivityManager.setConnected(true)
        testHelper.startAppAndWaitForSplash()
        testHelper.verifyOnboardingScreenDisplayed()
    }

    @Test
    fun retryButton_WorksWhenConnectivityRestored() {
        fakeConnectivityManager.setConnected(false)
        testHelper.startAppAndWaitForSplash()
        testHelper.verifyNoInternetScreenDisplayed()
        
        fakeConnectivityManager.setConnected(true)

        testHelper.verifyOnboardingScreenDisplayed()
    }

    @Test
    fun retryButton_StaysOnScreenWhenNoConnectivity() {
        fakeConnectivityManager.setConnected(false)
        testHelper.startAppAndWaitForSplash()
        testHelper.verifyNoInternetScreenDisplayed()
        
        testHelper.clickRetryButton()
        
        testHelper.verifyNoInternetScreenDisplayed()
    }

    @Test
    fun connectivityLoss_NavigatesToNoInternetScreen() {
        fakeConnectivityManager.setConnected(true)
        testHelper.startAppAndWaitForSplash()
        testHelper.verifyOnboardingScreenDisplayed()
        
        fakeConnectivityManager.setConnected(false)
        
        testHelper.waitForConnectivityChange("NO_INTERNET")
        testHelper.verifyNoInternetScreenDisplayed()
    }
}
