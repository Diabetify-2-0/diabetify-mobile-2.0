package com.itb.diabetify.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.itb.diabetify.data.manager.ActivityManagerImpl
import com.itb.diabetify.data.manager.ConnectivityManagerImpl
import com.itb.diabetify.data.manager.CounterfactualJobManagerImpl
import com.itb.diabetify.data.manager.LocalUserManagerImpl
import com.itb.diabetify.data.manager.PlannerCheckInManagerImpl
import com.itb.diabetify.data.manager.PlannerCoachManagerImpl
import com.itb.diabetify.data.manager.PlannerGoalManagerImpl
import com.itb.diabetify.data.manager.PredictionJobManagerImpl
import com.itb.diabetify.data.manager.PredictionManagerImpl
import com.itb.diabetify.data.manager.ProfileManagerImpl
import com.itb.diabetify.data.manager.TokenManagerImpl
import com.itb.diabetify.data.manager.UserManagerImpl
import com.itb.diabetify.data.remote.activity.ActivityApiService
import com.itb.diabetify.data.remote.auth.AuthApiService
import com.itb.diabetify.data.remote.chatbot.ChatbotApiService
import com.itb.diabetify.data.remote.counterfactual.CounterfactualApiService
import com.itb.diabetify.data.remote.interceptor.AuthInterceptor
import com.itb.diabetify.data.remote.planner.PlannerApiService
import com.itb.diabetify.data.remote.prediction.PredictionApiService
import com.itb.diabetify.data.remote.profile.ProfileApiService
import com.itb.diabetify.data.remote.user.UserApiService
import com.itb.diabetify.data.repository.ActivityRepositoryImpl
import com.itb.diabetify.data.repository.AuthRepositoryImpl
import com.itb.diabetify.data.repository.ChatbotRepositoryImpl
import com.itb.diabetify.data.repository.CounterfactualRepositoryImpl
import com.itb.diabetify.data.repository.PredictionRepositoryImpl
import com.itb.diabetify.data.repository.ProfileRepositoryImpl
import com.itb.diabetify.data.repository.UserRepositoryImpl
import com.itb.diabetify.domain.manager.ConnectivityManager
import com.itb.diabetify.domain.manager.CounterfactualJobManager
import com.itb.diabetify.domain.manager.LocalUserManager
import com.itb.diabetify.domain.manager.PlannerCheckInManager
import com.itb.diabetify.domain.manager.PlannerCoachManager
import com.itb.diabetify.domain.manager.PlannerGoalManager
import com.itb.diabetify.domain.manager.PredictionJobManager
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.manager.UserManager
import com.itb.diabetify.domain.manager.ActivityManager
import com.itb.diabetify.domain.manager.PredictionManager
import com.itb.diabetify.domain.manager.ProfileManager
import com.itb.diabetify.domain.repository.ActivityRepository
import com.itb.diabetify.domain.repository.AuthRepository
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.domain.repository.CounterfactualRepository
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.domain.repository.ProfileRepository
import com.itb.diabetify.domain.repository.UserRepository
import com.itb.diabetify.domain.usecases.activity.AddActivityUseCase
import com.itb.diabetify.domain.usecases.activity.GetActivityTodayUseCase
import com.itb.diabetify.domain.usecases.activity.UpdateActivityUseCase
import com.itb.diabetify.domain.usecases.app_entry.AppEntryUseCase
import com.itb.diabetify.domain.usecases.app_entry.ReadAppEntry
import com.itb.diabetify.domain.usecases.app_entry.SaveAppEntry
import com.itb.diabetify.domain.usecases.auth.AuthUseCases
import com.itb.diabetify.domain.usecases.chatbot.ChatbotUseCases
import com.itb.diabetify.domain.usecases.chatbot.GetXaiProfileUseCase
import com.itb.diabetify.domain.usecases.chatbot.LoadChatHistoryUseCase
import com.itb.diabetify.domain.usecases.chatbot.LoadRecommendationsUseCase
import com.itb.diabetify.domain.usecases.chatbot.RefreshRecommendationsUseCase
import com.itb.diabetify.domain.usecases.chatbot.SendChatMessageStreamUseCase
import com.itb.diabetify.domain.usecases.chatbot.SendChatMessageUseCase
import com.itb.diabetify.domain.usecases.auth.ChangePasswordUseCase
import com.itb.diabetify.domain.usecases.auth.CreateAccountUseCase
import com.itb.diabetify.domain.usecases.auth.LoginUseCase
import com.itb.diabetify.domain.usecases.auth.LogoutUseCase
import com.itb.diabetify.domain.usecases.auth.SendVerificationUseCase
import com.itb.diabetify.domain.usecases.auth.VerifyOtpUseCase
import com.itb.diabetify.domain.usecases.connectivity.CheckConnectivityUseCase
import com.itb.diabetify.domain.usecases.connectivity.ConnectivityUseCases
import com.itb.diabetify.domain.usecases.connectivity.ObserveConnectivityUseCase
import com.itb.diabetify.domain.usecases.counterfactual.CounterfactualUseCases
import com.itb.diabetify.domain.usecases.counterfactual.GetCounterfactualJobResultUseCase
import com.itb.diabetify.domain.usecases.counterfactual.RunCounterfactualAsyncUseCase
import com.itb.diabetify.domain.usecases.counterfactual.StartCounterfactualJobUseCase
import com.itb.diabetify.domain.usecases.prediction.GetLatestPredictionUseCase
import com.itb.diabetify.domain.usecases.prediction.GetPredictionByDateUseCase
import com.itb.diabetify.domain.usecases.prediction.GetPredictionScoreByDateUseCase
import com.itb.diabetify.domain.usecases.prediction.PredictAsyncUseCase
import com.itb.diabetify.domain.usecases.prediction.PredictBackgroundUseCase
import com.itb.diabetify.domain.usecases.prediction.PredictUseCase
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.planner.ClearPlannerGoalUseCase
import com.itb.diabetify.domain.usecases.planner.ClearPlannerCheckInsUseCase
import com.itb.diabetify.domain.usecases.planner.CompletePlannerGoalUseCase
import com.itb.diabetify.domain.usecases.planner.GetPlannerCheckInsUseCase
import com.itb.diabetify.domain.usecases.planner.GetPlannerCheckInHistoryUseCase
import com.itb.diabetify.domain.usecases.planner.GetActivePlannerCoachUseCase
import com.itb.diabetify.domain.usecases.planner.GetActivePlannerGoalUseCase
import com.itb.diabetify.domain.usecases.planner.MarkPlannerCheckInUseCase
import com.itb.diabetify.domain.usecases.planner.PlannerGoalUseCases
import com.itb.diabetify.domain.usecases.planner.RecordPlannerCheckInUseCase
import com.itb.diabetify.domain.usecases.planner.RefreshPlannerCheckInHistoryUseCase
import com.itb.diabetify.domain.usecases.planner.RefreshPlannerCheckInsUseCase
import com.itb.diabetify.domain.usecases.planner.RefreshPlannerGoalUseCase
import com.itb.diabetify.domain.usecases.planner.SavePlannerGoalUseCase
import com.itb.diabetify.domain.usecases.profile.AddProfileUseCase
import com.itb.diabetify.domain.usecases.profile.GetProfileUseCase
import com.itb.diabetify.domain.usecases.profile.UpdateProfileUseCase
import com.itb.diabetify.domain.usecases.user.EditUserUseCase
import com.itb.diabetify.domain.usecases.user.GetUserUseCase
import com.itb.diabetify.domain.usecases.notification.NotificationUseCases
import com.itb.diabetify.domain.usecases.notification.ScheduleNotificationUseCase
import com.itb.diabetify.domain.usecases.notification.CancelNotificationUseCase
import com.itb.diabetify.domain.usecases.notification.GetNotificationPreferencesUseCase
import com.itb.diabetify.domain.usecases.notification.SetNotificationPreferencesUseCase
import com.itb.diabetify.domain.manager.NotificationManager
import com.itb.diabetify.data.manager.NotificationManagerImpl
import com.itb.diabetify.domain.usecases.activity.ActivityUseCases
import com.itb.diabetify.domain.usecases.activity.GetActivityRepositoryUseCase
import com.itb.diabetify.domain.usecases.prediction.ExplainPredictionUseCase
import com.itb.diabetify.domain.usecases.prediction.GetLatestPredictionRepositoryUseCase
import com.itb.diabetify.domain.usecases.profile.GetProfileRepositoryUseCase
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.GetUserRepositoryUseCase
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.util.Constants.BASE_URL
import com.itb.diabetify.util.Constants.CHATBOT_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun providesLocalUserManager(
        application: Application
    ): LocalUserManager = LocalUserManagerImpl(application)

    @Provides
    @Singleton
    fun providesAppEntryUseCases(
        localUserManager: LocalUserManager
    ) = AppEntryUseCase(
        readAppEntry = ReadAppEntry(localUserManager),
        saveAppEntry = SaveAppEntry(localUserManager)
    )

    @Provides
    @Singleton
    fun providesTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providesAuthApiService(okHttpClient: OkHttpClient): AuthApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesAuthRepository(
        authApiService: AuthApiService,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(
            authApiService = authApiService,
            tokenManager = tokenManager
        )
    }

    @Provides
    @Singleton
    fun providesAuthUseCases(
        repository: AuthRepository
    ): AuthUseCases {
        return AuthUseCases(
            changePassword = ChangePasswordUseCase(repository),
            createAccount = CreateAccountUseCase(repository),
            login = LoginUseCase(repository),
            logout = LogoutUseCase(repository),
            sendVerification = SendVerificationUseCase(repository),
            verifyOtp = VerifyOtpUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun providesUserApiService(okHttpClient: OkHttpClient): UserApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesUserManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): UserManager {
        return UserManagerImpl(context, gson)
    }

    @Provides
    @Singleton
    fun providesUserRepository(
        userApiService: UserApiService,
        tokenManager: TokenManager,
        userManager: UserManager
    ): UserRepository {
        return UserRepositoryImpl(
            userApiService = userApiService,
            tokenManager = tokenManager,
            userManager = userManager
        )
    }

    @Provides
    @Singleton
    fun providesUserUseCases(
        repository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            getUser = GetUserUseCase(repository),
            getUserRepository = GetUserRepositoryUseCase(repository),
            editUser = EditUserUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesActivityApiService(okHttpClient: OkHttpClient): ActivityApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ActivityApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesActivityManager(): ActivityManager {
        return ActivityManagerImpl()
    }

    @Provides
    @Singleton
    fun providesActivityRepository(
        activityApiService: ActivityApiService,
        tokenManager: TokenManager,
        activityManager: ActivityManager
    ): ActivityRepository {
        return ActivityRepositoryImpl(
            activityApiService = activityApiService,
            tokenManager = tokenManager,
            activityManager = activityManager
        )
    }

    @Provides
    @Singleton
    fun providesActivityUseCases(
        repository: ActivityRepository
    ): ActivityUseCases {
        return ActivityUseCases(
            addActivity = AddActivityUseCase(repository),
            getActivityToday = GetActivityTodayUseCase(repository),
            getActivityRepository = GetActivityRepositoryUseCase(repository),
            updateActivity = UpdateActivityUseCase(repository),
        )
    }

    @Provides
    @Singleton
    fun providesPredictionApiService(okHttpClient: OkHttpClient): PredictionApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PredictionApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesCounterfactualApiService(okHttpClient: OkHttpClient): CounterfactualApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CounterfactualApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesPlannerApiService(okHttpClient: OkHttpClient): PlannerApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlannerApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesPredictionManager(): PredictionManager {
        return PredictionManagerImpl()
    }

    @Provides
    @Singleton
    fun providesPlannerGoalManager(
        @ApplicationContext context: Context,
        gson: Gson,
        plannerApiService: PlannerApiService
    ): PlannerGoalManager {
        return PlannerGoalManagerImpl(context, gson, plannerApiService)
    }

    @Provides
    @Singleton
    fun providesPlannerCheckInManager(
        @ApplicationContext context: Context,
        gson: Gson,
        plannerApiService: PlannerApiService
    ): PlannerCheckInManager {
        return PlannerCheckInManagerImpl(context, gson, plannerApiService)
    }

    @Provides
    @Singleton
    fun providesPlannerCoachManager(
        plannerApiService: PlannerApiService
    ): PlannerCoachManager {
        return PlannerCoachManagerImpl(plannerApiService)
    }

    @Provides
    @Singleton
    fun providesPlannerGoalUseCases(
        plannerGoalManager: PlannerGoalManager,
        plannerCheckInManager: PlannerCheckInManager,
        plannerCoachManager: PlannerCoachManager
    ): PlannerGoalUseCases {
        return PlannerGoalUseCases(
            savePlannerGoal = SavePlannerGoalUseCase(plannerGoalManager),
            getActivePlannerGoal = GetActivePlannerGoalUseCase(plannerGoalManager),
            getActivePlannerCoach = GetActivePlannerCoachUseCase(plannerCoachManager),
            refreshPlannerGoal = RefreshPlannerGoalUseCase(plannerGoalManager),
            completePlannerGoal = CompletePlannerGoalUseCase(plannerGoalManager),
            clearPlannerGoal = ClearPlannerGoalUseCase(plannerGoalManager),
            markPlannerCheckIn = MarkPlannerCheckInUseCase(plannerCheckInManager),
            getPlannerCheckIns = GetPlannerCheckInsUseCase(plannerCheckInManager),
            clearPlannerCheckIns = ClearPlannerCheckInsUseCase(plannerCheckInManager),
            refreshPlannerCheckIns = RefreshPlannerCheckInsUseCase(plannerCheckInManager),
            refreshPlannerCheckInHistory = RefreshPlannerCheckInHistoryUseCase(plannerCheckInManager),
            recordPlannerCheckIn = RecordPlannerCheckInUseCase(plannerCheckInManager),
            getPlannerCheckInHistory = GetPlannerCheckInHistoryUseCase(plannerCheckInManager)
        )
    }

    @Provides
    @Singleton
    fun providesPredictionJobManager(
        predictionApiService: PredictionApiService
    ): PredictionJobManager {
        return PredictionJobManagerImpl(predictionApiService)
    }

    @Provides
    @Singleton
    fun providesCounterfactualJobManager(
        counterfactualApiService: CounterfactualApiService
    ): CounterfactualJobManager {
        return CounterfactualJobManagerImpl(counterfactualApiService)
    }

    @Provides
    @Singleton
    fun providesPredictionRepository(
        predictionApiService: PredictionApiService,
        tokenManager: TokenManager,
        predictionManager: PredictionManager,
        predictionJobManager: PredictionJobManager
    ): PredictionRepository {
        return PredictionRepositoryImpl(
            predictionApiService = predictionApiService,
            tokenManager = tokenManager,
            predictionManager = predictionManager,
            predictionJobManager = predictionJobManager
        )
    }

    @Provides
    @Singleton
    fun providesCounterfactualRepository(
        counterfactualApiService: CounterfactualApiService,
        counterfactualJobManager: CounterfactualJobManager
    ): CounterfactualRepository {
        return CounterfactualRepositoryImpl(
            counterfactualApiService = counterfactualApiService,
            counterfactualJobManager = counterfactualJobManager
        )
    }

    @Provides
    @Singleton
    fun providesPredictionUseCases(
        repository: PredictionRepository,
        @ApplicationScope applicationScope: CoroutineScope
    ): PredictionUseCases {
        return PredictionUseCases(
            getLatestPredictionRepository = GetLatestPredictionRepositoryUseCase(repository),
            getLatestPrediction = GetLatestPredictionUseCase(repository),
            getPredictionByDate = GetPredictionByDateUseCase(repository),
            getPredictionScoreByDate = GetPredictionScoreByDateUseCase(repository),
            predict = PredictUseCase(repository),
            predictAsync = PredictAsyncUseCase(repository),
            predictBackground = PredictBackgroundUseCase(repository, applicationScope),
            explainPrediction = ExplainPredictionUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesCounterfactualUseCases(
        repository: CounterfactualRepository
    ): CounterfactualUseCases {
        return CounterfactualUseCases(
            startCounterfactualJob = StartCounterfactualJobUseCase(repository),
            runCounterfactualAsync = RunCounterfactualAsyncUseCase(repository),
            getCounterfactualJobResult = GetCounterfactualJobResultUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesProfileApiService(okHttpClient: OkHttpClient): ProfileApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesProfileManager(): ProfileManager {
        return ProfileManagerImpl()
    }

    @Provides
    @Singleton
    fun providesProfileRepository(
        profileApiService: ProfileApiService,
        tokenManager: TokenManager,
        profileManager: ProfileManager
    ): ProfileRepository {
        return ProfileRepositoryImpl(
            profileApiService = profileApiService,
            tokenManager = tokenManager,
            profileManager = profileManager
        )
    }

    @Provides
    @Singleton
    fun provideProfileUseCases(
        repository: ProfileRepository
    ): ProfileUseCases {
        return ProfileUseCases(
            addProfile = AddProfileUseCase(repository),
            getProfile = GetProfileUseCase(repository),
            getProfileRepository = GetProfileRepositoryUseCase(repository),
            updateProfile = UpdateProfileUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return NotificationManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providesNotificationUseCases(
        notificationManager: NotificationManager
    ): NotificationUseCases {
        return NotificationUseCases(
            scheduleNotification = ScheduleNotificationUseCase(notificationManager),
            cancelNotification = CancelNotificationUseCase(notificationManager),
            getNotificationPreferences = GetNotificationPreferencesUseCase(notificationManager),
            setNotificationPreferences = SetNotificationPreferencesUseCase(notificationManager)
        )
    }

    @Provides
    @Singleton
    fun providesConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return ConnectivityManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providesConnectivityUseCases(
        connectivityManager: ConnectivityManager
    ): ConnectivityUseCases {
        return ConnectivityUseCases(
            observeConnectivity = ObserveConnectivityUseCase(connectivityManager),
            checkConnectivity = CheckConnectivityUseCase(connectivityManager)
        )
    }

    @Provides
    @Singleton
    fun providesChatbotApiService(okHttpClient: OkHttpClient): ChatbotApiService {
        return Retrofit.Builder()
            .baseUrl(CHATBOT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatbotApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesChatbotRepository(
        chatbotApiService: ChatbotApiService,
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): ChatbotRepository {
        return ChatbotRepositoryImpl(
            chatbotApiService = chatbotApiService,
            okHttpClient = okHttpClient,
            gson = gson,
        )
    }

    @Provides
    @Singleton
    fun providesChatbotUseCases(
        repository: ChatbotRepository,
    ): ChatbotUseCases {
        return ChatbotUseCases(
            sendChatMessage = SendChatMessageUseCase(repository),
            sendChatMessageStream = SendChatMessageStreamUseCase(repository),
            loadChatHistory = LoadChatHistoryUseCase(repository),
            loadRecommendations = LoadRecommendationsUseCase(repository),
            refreshRecommendations = RefreshRecommendationsUseCase(repository),
            getXaiProfile = GetXaiProfileUseCase(repository),
        )
    }
}
