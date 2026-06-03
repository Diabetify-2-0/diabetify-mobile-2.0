package com.itb.diabetify.presentation.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.R
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualConstraints
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualFeatureSet
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualGeneration
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualInstance
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualTarget
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualChangedFeature
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualPredictionInfo
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalFeature
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.domain.usecases.counterfactual.CounterfactualUseCases
import com.itb.diabetify.domain.usecases.activity.ActivityUseCases
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.planner.PlannerGoalUseCases
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.util.handleAsyncCounterfactual
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.PredictionUpdateNotifier
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

internal const val NO_PREDICTION_TIMESTAMP = "Belum ada prediksi"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val activityUseCases: ActivityUseCases,
    private val predictionUseCases: PredictionUseCases,
    private val counterfactualUseCases: CounterfactualUseCases,
    private val plannerGoalUseCases: PlannerGoalUseCases,
    private val profileUseCases: ProfileUseCases,
    private val predictionRepository: PredictionRepository,
): ViewModel() {
    // Navigation, Error, and Success States
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent: State<String?> = _navigationEvent

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    private val _loadingMessage = mutableStateOf<String?>(null)
    val loadingMessage: State<String?> = _loadingMessage

    // Operational States
    private var _userState = mutableStateOf(DataState())
    val userState: State<DataState> = _userState

    private var _activityTodayState = mutableStateOf(DataState())
    val activityTodayState: State<DataState> = _activityTodayState

    private var _latestPredictionState = mutableStateOf(DataState())
    val latestPredictionState: State<DataState> = _latestPredictionState

    private var _explainPredictionState = mutableStateOf(DataState())
    val explainPredictionState: State<DataState> = _explainPredictionState

    private var _profileState = mutableStateOf(DataState())
    val profileState: State<DataState> = _profileState

    // UI States
    private val _userName = mutableStateOf("Pengguna")
    val userName: State<String> = _userName

    private val _lastPredictionAt = mutableStateOf(NO_PREDICTION_TIMESTAMP)
    val lastPredictionAt: State<String> = _lastPredictionAt

    private val _latestPredictionScore = mutableDoubleStateOf(0.0)
    val latestPredictionScore: State<Double> = _latestPredictionScore

    private val _isPredictionRefreshing = mutableStateOf(PredictionUpdateNotifier.isPredictionUpdating)
    val isPredictionRefreshing: State<Boolean> = _isPredictionRefreshing

    data class RiskFactor(
        val name: String,
        val abbreviation: String,
        val percentage: Double
    )

    private val _riskFactors = mutableStateOf(listOf(
        RiskFactor("Indeks Massa Tubuh", "IMT", 0.0),
        RiskFactor("Hipertensi", "H", 0.0),
        RiskFactor("Riwayat Bayi Makrosomia", "RBM", 0.0),
        RiskFactor("Aktivitas Fisik", "AF", 0.0),
        RiskFactor("Usia", "U", 0.0),
        RiskFactor("Status Merokok", "SM", 0.0),
        RiskFactor("Indeks Brinkman", "IB", 0.0),
        RiskFactor("Riwayat Keluarga", "RK", 0.0),
        RiskFactor("Kolesterol", "K", 0.0),
    ))
    val riskFactors: State<List<RiskFactor>> = _riskFactors

    data class RiskFactorDetails(
        val name: String,
        val fullName: String,
        val description: String? = null,
        val impactPercentage: Double,
        val explanation: String,
        val idealValue: String,
        val currentValue: String,
    )

    private val _riskFactorDetails = mutableStateOf(listOf(
        RiskFactorDetails(
            name = "IMT",
            fullName = "Indeks Massa Tubuh",
            description = "Rasio berat badan terhadap tinggi badan",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "18.5 - 22.9 kg/m²",
            currentValue = "0.0 kg/m² (Kurus)",
        ),
        RiskFactorDetails(
            name = "H",
            fullName = "Hipertensi",
            description = "Status tekanan darah tinggi",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak",
            currentValue = "Tidak"
        ),
        RiskFactorDetails(
            name = "RBM",
            fullName = "Riwayat Bayi Makrosomia",
            description = "Riwayat melahirkan bayi dengan berat badan lahir di atas 4 kg",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak",
            currentValue = "Tidak",
        ),
        RiskFactorDetails(
            name = "AF",
            fullName = "Aktivitas Fisik",
            description = "Jumlah total hari dalam seminggu saat pengguna melakukan aktivitas fisik dengan intensitas sedang",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "7 hari per minggu",
            currentValue = "0 hari per minggu"
        ),
        RiskFactorDetails(
            name = "U",
            fullName = "Usia",
            description = "Usia saat ini",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "< 45 tahun",
            currentValue = "0 tahun",
        ),
        RiskFactorDetails(
            name = "SM",
            fullName = "Status Merokok",
            description = "Kondisi kebiasaan merokok.",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak merokok",
            currentValue = "Tidak merokok",
        ),
        RiskFactorDetails(
            name = "IB",
            fullName = "Indeks Brinkman",
            description = "Kategori perokok berdasarkan kebiasaan merokok (jumlah rokok per hari x lama merokok dalam tahun)",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak pernah merokok",
            currentValue = "Tidak pernah merokok",
        ),
        RiskFactorDetails(
            name = "RK",
            fullName = "Riwayat Keluarga",
            description = "Riwayat orang tua kandung meninggal akibat komplikasi diabetes",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak",
            currentValue = "Tidak"
        ),
        RiskFactorDetails(
            name = "K",
            fullName = "Kolesterol",
            description = "Status kadar kolesterol tinggi",
            impactPercentage = 0.0,
            explanation = "",
            idealValue = "Tidak",
            currentValue = "Tidak"
        )
    ))
    val riskFactorDetails: State<List<RiskFactorDetails>> = _riskFactorDetails

    private val _predictionSummary = mutableStateOf("")
    val predictionSummary: State<String> = _predictionSummary

    private val _isHypertension = mutableStateOf(false)
    val isHypertension: State<Boolean> = _isHypertension

    private val _weight = mutableIntStateOf(0)
    val weight: State<Int> = _weight

    private val _height = mutableIntStateOf(0)
    val height: State<Int> = _height

    private val _bmi = mutableDoubleStateOf(0.0)
    val bmi: State<Double> = _bmi

    private val _smokingStatus = mutableStateOf("0")
    val smokingStatus: State<String> = _smokingStatus

    private val _macrosomicBaby = mutableIntStateOf(0)
    val macrosomicBaby: State<Int> = _macrosomicBaby

    private val _isBloodline = mutableStateOf(false)
    val isBloodline: State<Boolean> = _isBloodline

    private val _isCholesterol = mutableStateOf(false)
    val isCholesterol: State<Boolean> = _isCholesterol

    private val _smokeAverage = mutableIntStateOf(0)
    val smokeAverage: State<Int> = _smokeAverage

    private val _profileSmokeCount = mutableIntStateOf(0)
    val profileSmokeCount: State<Int> = _profileSmokeCount

    private val _profileAgeOfSmoking = mutableIntStateOf(0)
    val profileAgeOfSmoking: State<Int> = _profileAgeOfSmoking

    private val _profileAgeOfStopSmoking = mutableIntStateOf(0)
    val profileAgeOfStopSmoking: State<Int> = _profileAgeOfStopSmoking

    private val _brinkmanScore = mutableIntStateOf(0)
    val brinkmanScore: State<Int> = _brinkmanScore

    private val _physicalActivityAverage = mutableIntStateOf(0)
    val physicalActivityAverage: State<Int> = _physicalActivityAverage

    private val _physicalActivityToday = mutableIntStateOf(0)
    val physicalActivityToday: State<Int> = _physicalActivityToday

    private val _isNavigating = mutableStateOf(false)
    private val _baselineAge = mutableIntStateOf(0)
    val baselineAge: State<Int> = _baselineAge

    data class CounterfactualOption(
        val key: String,
        val label: String,

        val iconResId: Int,
        val isSelected: Boolean = true,
    )

    data class CounterfactualRiskTarget(
        val label: String,
        val description: String,
        val targetHighRiskPercentage: Int,
        val minLowRiskProbability: Double
    )

    private var currentCounterfactualJobId: String? = null

    private val _counterfactualState = mutableStateOf(DataState())
    val counterfactualState: State<DataState> = _counterfactualState

    private val _counterfactualOptions = mutableStateOf(defaultCounterfactualOptions())
    val counterfactualOptions: State<List<CounterfactualOption>> = _counterfactualOptions

    private val _counterfactualRiskTargetInput = mutableStateOf(DEFAULT_COUNTERFACTUAL_RISK_TARGET_INPUT)
    val counterfactualRiskTargetInput: State<String> = _counterfactualRiskTargetInput

    private val _counterfactualSubmittedTarget = mutableStateOf(defaultCounterfactualRiskTarget())
    val counterfactualSubmittedTarget: State<CounterfactualRiskTarget> = _counterfactualSubmittedTarget

    private val _counterfactualSubmittedOptions = mutableStateOf<List<CounterfactualOption>>(emptyList())
    val counterfactualSubmittedOptions: State<List<CounterfactualOption>> = _counterfactualSubmittedOptions

    private val _counterfactualResult = mutableStateOf<CounterfactualResultPayload?>(null)
    val counterfactualResult: State<CounterfactualResultPayload?> = _counterfactualResult

    private val _counterfactualJobResultMeta = mutableStateOf<CounterfactualJobResultData?>(null)
    val counterfactualJobResultMeta: State<CounterfactualJobResultData?> = _counterfactualJobResultMeta

    private val _activePlannerGoal = mutableStateOf<PlannerGoal?>(null)
    val activePlannerGoal: State<PlannerGoal?> = _activePlannerGoal

    private val _plannerCheckInHistory = mutableStateOf<List<PlannerCheckInEntry>>(emptyList())
    val plannerCheckInHistory: State<List<PlannerCheckInEntry>> = _plannerCheckInHistory

    private val _allPlannerCheckInHistory = mutableStateOf<List<PlannerCheckInEntry>>(emptyList())
    val allPlannerCheckInHistory: State<List<PlannerCheckInEntry>> = _allPlannerCheckInHistory

    private var allPlannerCheckInHistoryCache: List<PlannerCheckInEntry> = emptyList()

    // Loading state tracking
    private var isUserDataLoaded = false
    private var isPredictionDataLoaded = false
    private var isActivityDataLoaded = false
    private var isProfileDataLoaded = false
    private var isCollectingLatestPrediction = false
    private val predictionUpdateListener = {
        _successMessage.value = "Prediksi risiko telah diperbaharui"
        loadLatestPredictionData()
    }
    private val predictionUpdatingListener: (Boolean) -> Unit = { isUpdating ->
        _isPredictionRefreshing.value = isUpdating
    }

    // Initialization
    init {
        PredictionUpdateNotifier.addListener(predictionUpdateListener)
        PredictionUpdateNotifier.addUpdatingListener(predictionUpdatingListener)
        _loadingMessage.value = "Memuat data..."
        loadUserData()
        loadLatestPredictionData()
        loadActivityTodayData()
        loadProfileData()
        collectActivePlannerGoal()
        collectPlannerCheckInHistory()
        refreshPlannerGoal()
    }

    // Use Case Calls
    private fun loadUserData() {
        viewModelScope.launch {
            _userState.value = userState.value.copy(isLoading = true)

            val getUserResult = userUseCases.getUser()

            _userState.value = userState.value.copy(isLoading = false)

            when (getUserResult.result) {
                is Resource.Success -> {
                    collectUserData()
                    isUserDataLoaded = true
                    checkAllDataLoaded()
                }
                is Resource.Error -> {
                    _errorMessage.value = getUserResult.result.message ?: "Terjadi kesalahan saat mengambil data pengguna"
                    getUserResult.result.message?.let { Log.e("HomeViewModel", it) }
                    _loadingMessage.value = null
                    isUserDataLoaded = true
                    checkAllDataLoaded()
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat mengambil data pengguna"
                    Log.e("HomeViewModel", "Unexpected error")
                    _loadingMessage.value = null
                    isUserDataLoaded = true
                    checkAllDataLoaded()
                }
            }
        }
    }

    private fun collectUserData() {
        viewModelScope.launch {
            _userState.value = userState.value.copy(isLoading = true)

            userUseCases.getUserRepository().onEach { user ->
                _userState.value = userState.value.copy(isLoading = false)

                user?.let {
                    _userName.value = it.name
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun loadLatestPredictionData() {
        viewModelScope.launch {
            _latestPredictionState.value = latestPredictionState.value.copy(isLoading = true)

            val getLatestPredictionResult = predictionUseCases.getLatestPrediction()

            _latestPredictionState.value = latestPredictionState.value.copy(isLoading = false)

            when (getLatestPredictionResult.result) {
                is Resource.Success -> {
                    collectLatestPredictionData()
                    isPredictionDataLoaded = true
                    checkAllDataLoaded()
                }
                is Resource.Error -> {
                    _errorMessage.value = getLatestPredictionResult.result.message ?: "Terjadi kesalahan saat mengambil data prediksi terbaru"
                    getLatestPredictionResult.result.message?.let { Log.e("HomeViewModel", it) }
                    _loadingMessage.value = null
                    isPredictionDataLoaded = true
                    checkAllDataLoaded()
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat mengambil data prediksi terbaru"
                    Log.e("HomeViewModel", "Unexpected error loading latest prediction data")
                    _loadingMessage.value = null
                    isPredictionDataLoaded = true
                    checkAllDataLoaded()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun collectLatestPredictionData() {
        if (isCollectingLatestPrediction) {
            return
        }
        isCollectingLatestPrediction = true

        viewModelScope.launch {
            _latestPredictionState.value = latestPredictionState.value.copy(isLoading = true)

            predictionRepository.getLatestPrediction().onEach { prediction ->
                _latestPredictionState.value = latestPredictionState.value.copy(isLoading = false)

                if (prediction == null) {
                    resetToDefaultValues()
                    return@onEach
                }

                prediction.let { latestPrediction ->
                    _lastPredictionAt.value = latestPrediction.createdAt
                    _latestPredictionScore.doubleValue = latestPrediction.riskScore

                    _riskFactors.value = listOf(
                        RiskFactor("Indeks Massa Tubuh", "IMT", latestPrediction.bmiContribution),
                        RiskFactor("Hipertensi", "H", latestPrediction.isHypertensionContribution),
                        RiskFactor("Riwayat Bayi Makrosomia", "RBM", latestPrediction.isMacrosomicBabyContribution),
                        RiskFactor("Aktivitas Fisik", "AF", latestPrediction.physicalActivityFrequencyContribution),
                        RiskFactor("Usia", "U", latestPrediction.ageContribution),
                        RiskFactor("Status Merokok", "SM", latestPrediction.smokingStatusContribution),
                        RiskFactor("Indeks Brinkman", "IB", latestPrediction.brinkmanScoreContribution),
                        RiskFactor("Riwayat Keluarga", "RK", latestPrediction.isBloodlineContribution),
                        RiskFactor("Kolesterol", "K", latestPrediction.isCholesterolContribution)
                    )

                    _riskFactorDetails.value = listOf(
                        RiskFactorDetails(
                            name = "IMT",
                            fullName = "Indeks Massa Tubuh",
                            description = "Rasio berat badan terhadap tinggi badan",
                            impactPercentage = latestPrediction.bmiContribution,
                            explanation = latestPrediction.bmiExplanation,
                            idealValue = "18.5 - 22.9 kg/m²",
                            currentValue = String.format("%.1f kg/m²", latestPrediction.bmi)
                        ),
                        RiskFactorDetails(
                            name = "H",
                            fullName = "Hipertensi",
                            description = "Status tekanan darah tinggi",
                            impactPercentage = latestPrediction.isHypertensionContribution,
                            explanation = latestPrediction.isHypertensionExplanation,
                            idealValue = "Tidak",
                            currentValue = if (latestPrediction.isHypertension) "Ya" else "Tidak"
                        ),
                        RiskFactorDetails(
                            name = "RBM",
                            fullName = "Riwayat Bayi Makrosomia",
                            description = "Riwayat melahirkan bayi dengan berat badan lahir di atas 4 kg",
                            impactPercentage = latestPrediction.isMacrosomicBabyContribution,
                            explanation = latestPrediction.isMacrosomicBabyExplanation,
                            idealValue = "Tidak",
                            currentValue = when (latestPrediction.isMacrosomicBaby) {
                                0 -> "Tidak"
                                1 -> "Ya"
                                2 -> "Tidak relevan (pria atau belum pernah hamil)"
                                else -> "Tidak diketahui"
                            },
                        ),
                        RiskFactorDetails(
                            name = "AF",
                            fullName = "Aktivitas Fisik",
                            description = "Jumlah total hari dalam seminggu saat pengguna melakukan aktivitas fisik dengan intensitas sedang",
                            impactPercentage = latestPrediction.physicalActivityFrequencyContribution,
                            explanation = latestPrediction.physicalActivityFrequencyExplanation,
                            idealValue = "7 hari per minggu",
                            currentValue = "${latestPrediction.physicalActivityFrequency} hari per minggu"
                        ),
                        RiskFactorDetails(
                            name = "U",
                            fullName = "Usia",
                            description = "Usia saat ini",
                            impactPercentage = latestPrediction.ageContribution,
                            explanation = latestPrediction.ageExplanation,
                            idealValue = "< 45 tahun",
                            currentValue = "${latestPrediction.age} tahun",
                        ),
                        RiskFactorDetails(
                            name = "SM",
                            fullName = "Status Merokok",
                            description = "Kondisi kebiasaan merokok.",
                            impactPercentage = latestPrediction.smokingStatusContribution,
                            explanation = latestPrediction.smokingStatusExplanation,
                            idealValue = "Tidak merokok",
                            currentValue = when (latestPrediction.smokingStatus) {
                                "0" -> "Tidak merokok"
                                "1" -> "Sudah berhenti merokok"
                                "2" -> "Masih aktif merokok"
                                else -> "Tidak diketahui"
                            },
                        ),
                        RiskFactorDetails(
                            name = "IB",
                            fullName = "Indeks Brinkman",
                            description = "Kategori perokok berdasarkan kebiasaan merokok (jumlah rokok per hari x lama merokok dalam tahun)",
                            impactPercentage = latestPrediction.brinkmanScoreContribution,
                            explanation = latestPrediction.brinkmanScoreExplanation,
                            idealValue = "Tidak pernah merokok",
                            currentValue = when (latestPrediction.brinkmanScore) {
                                0 -> "Tidak pernah merokok"
                                1 -> "Perokok ringan"
                                2 -> "Perokok sedang"
                                3 -> "Perokok berat"
                                else -> "Tidak diketahui"
                            },
                        ),
                        RiskFactorDetails(
                            name = "RK",
                            fullName = "Riwayat Keluarga",
                            description = "Riwayat orang tua kandung meninggal akibat komplikasi diabetes",
                            impactPercentage = latestPrediction.isBloodlineContribution,
                            explanation = latestPrediction.isBloodlineExplanation,
                            idealValue = "Tidak",
                            currentValue = if (latestPrediction.isBloodline) "Ya" else "Tidak"
                        ),
                        RiskFactorDetails(
                            name = "K",
                            fullName = "Kolesterol",
                            description = "Status kadar kolesterol tinggi",
                            impactPercentage = latestPrediction.isCholesterolContribution,
                            explanation = latestPrediction.isCholesterolExplanation,
                            idealValue = "Tidak",
                            currentValue = if (latestPrediction.isCholesterol) "Ya" else "Tidak"
                        )
                    )

                    _predictionSummary.value = latestPrediction.predictionSummary
                    _smokingStatus.value = latestPrediction.smokingStatus
                    _smokeAverage.intValue = latestPrediction.avgSmokeCount
                    _brinkmanScore.intValue = latestPrediction.brinkmanScore
                    _physicalActivityAverage.intValue = latestPrediction.physicalActivityFrequency

                    _baselineAge.intValue = latestPrediction.age
                    refreshCounterfactualOptions()
                }
            }.launchIn(viewModelScope)
        }
    }

    override fun onCleared() {
        PredictionUpdateNotifier.removeListener(predictionUpdateListener)
        PredictionUpdateNotifier.removeUpdatingListener(predictionUpdatingListener)
        super.onCleared()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _profileState.value = profileState.value.copy(isLoading = true)

            val getProfileResult = profileUseCases.getProfile()

            _profileState.value = profileState.value.copy(isLoading = false)

            when (getProfileResult.result) {
                is Resource.Success -> {
                    collectProfileData()
                    isProfileDataLoaded = true
                    checkAllDataLoaded()
                }
                is Resource.Error -> {
                    if (getProfileResult.result.message?.contains("404") == true) {
                        Log.d("HomeViewModel", "Profile not found, navigating to survey screen")
                        resetToDefaultValues()
                        _navigationEvent.value = "SURVEY_SCREEN"
                        _loadingMessage.value = null
                    } else {
                        _errorMessage.value = getProfileResult.result.message ?: "Terjadi kesalahan saat mengambil data profil"
                        getProfileResult.result.message?.let { Log.e("HomeViewModel", it) }
                        _loadingMessage.value = null
                    }
                    isProfileDataLoaded = true
                    checkAllDataLoaded()
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat mengambil data profil"
                    Log.e("HomeViewModel", "Unexpected error loading profile data")
                    _loadingMessage.value = null
                    isProfileDataLoaded = true
                    checkAllDataLoaded()
                }
            }
        }
    }

    private fun collectProfileData() {
        viewModelScope.launch {
            _profileState.value = profileState.value.copy(isLoading = true)

            profileUseCases.getProfileRepository().onEach { profile ->
                _profileState.value = profileState.value.copy(isLoading = false)

                profile?.let { userProfile ->
                    _weight.intValue = userProfile.weight
                    _height.intValue = userProfile.height
                    _bmi.doubleValue = userProfile.bmi
                    _isHypertension.value = userProfile.hypertension
                    _macrosomicBaby.intValue = userProfile.macrosomicBaby
                    _isBloodline.value = userProfile.bloodline
                    _isCholesterol.value = userProfile.cholesterol
                    _profileSmokeCount.intValue = userProfile.smokeCount
                    _profileAgeOfSmoking.intValue = userProfile.ageOfSmoking
                    _profileAgeOfStopSmoking.intValue = userProfile.ageOfStopSmoking
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun loadActivityTodayData() {
        viewModelScope.launch {
            _activityTodayState.value = activityTodayState.value.copy(isLoading = true)

            val getActivityTodayResult = activityUseCases.getActivityToday()

            _activityTodayState.value = activityTodayState.value.copy(isLoading = false)

            when (getActivityTodayResult.result) {
                is Resource.Success -> {
                    collectActivityTodayData()
                    isActivityDataLoaded = true
                    checkAllDataLoaded()
                }
                is Resource.Error -> {
                    _errorMessage.value = getActivityTodayResult.result.message ?: "Terjadi kesalahan saat mengambil data aktivitas hari ini"
                    getActivityTodayResult.result.message?.let { Log.e("HomeViewModel", it) }
                    _loadingMessage.value = null
                    isActivityDataLoaded = true
                    checkAllDataLoaded()
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat mengambil data aktivitas hari ini"
                    Log.e("HomeViewModel", "Unexpected error")
                    _loadingMessage.value = null
                    isActivityDataLoaded = true
                    checkAllDataLoaded()
                }
            }
        }
    }

    private fun collectActivityTodayData() {
        viewModelScope.launch {
            _activityTodayState.value = activityTodayState.value.copy(isLoading = true)

            activityUseCases.getActivityRepository().onEach { activity ->
                _activityTodayState.value = activityTodayState.value.copy(isLoading = false)

                activity?.let { todayActivity ->
                    _physicalActivityToday.intValue = todayActivity.workoutValue
                }
            }.launchIn(viewModelScope)
        }
    }

    fun loadExplanationData() {
        viewModelScope.launch {
            _explainPredictionState.value = explainPredictionState.value.copy(isLoading = true)

            val explainPredictionResult = predictionUseCases.explainPrediction()

            _explainPredictionState.value = explainPredictionState.value.copy(isLoading = false)

            when (explainPredictionResult.result) {
                is Resource.Success -> {
                    // Do nothing
                }
                is Resource.Error -> {
                    _errorMessage.value = explainPredictionResult.result.message ?: "Terjadi kesalahan saat memuat penjelasan"
                    explainPredictionResult.result.message?.let { Log.e("HomeViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat memuat penjelasan"
                    Log.e("HomeViewModel", "Unexpected error loading explanation data")
                }
            }
        }
    }

    // Helper Functions
    private fun resetToDefaultValues() {
        _baselineAge.intValue = 0
        _lastPredictionAt.value = NO_PREDICTION_TIMESTAMP
        _latestPredictionScore.doubleValue = 0.0
        _bmi.doubleValue = 0.0
        _weight.intValue = 0
        _height.intValue = 0
        _isHypertension.value = false
        _macrosomicBaby.intValue = 0
        _isBloodline.value = false
        _isCholesterol.value = false
        _smokingStatus.value = "0"
        _profileSmokeCount.intValue = 0
        _profileAgeOfSmoking.intValue = 0
        _profileAgeOfStopSmoking.intValue = 0
        _physicalActivityToday.intValue = 0
        _brinkmanScore.intValue = 0

        _riskFactors.value = _riskFactors.value.map { it.copy(percentage = 0.0) }
        _counterfactualOptions.value = defaultCounterfactualOptions()
        _counterfactualRiskTargetInput.value = DEFAULT_COUNTERFACTUAL_RISK_TARGET_INPUT
        _counterfactualSubmittedTarget.value = defaultCounterfactualRiskTarget()
        _counterfactualSubmittedOptions.value = emptyList()
        _counterfactualResult.value = null
        _counterfactualJobResultMeta.value = null

        _riskFactorDetails.value = _riskFactorDetails.value.map {
            it.copy(impactPercentage = 0.0, currentValue = when(it.name) {
                "IMT" -> "0 kg/m²"
                "H" -> "0/0 mmHg"
                "RBM" -> "-"
                "AF" -> "0 menit"
                "U" -> "0 tahun"
                "SM" -> "0 batang per hari"
                "IB" -> "0 batang per hari"
                "RK" -> "-"
                "K" -> "0 mg/dL"
                else -> "0"
            })
        }
        
        isUserDataLoaded = false
        isPredictionDataLoaded = false
        isActivityDataLoaded = false
        isProfileDataLoaded = false
    }

    private fun defaultCounterfactualOptions(): List<CounterfactualOption> {
        return buildList {
            if (_smokingStatus.value == "2") {
                add(
                    CounterfactualOption(
                        key = "smoking_status",
                        label = "Status Merokok",
                        iconResId = R.drawable.ic_smoking,


                    )
                )
            }

            add(
                CounterfactualOption(
                    key = "BMI",
                    label = "Berat Badan",
                    iconResId = R.drawable.ic_weight,

                )
            )
            add(
                CounterfactualOption(
                    key = "moderate_physical_activity_frequency",
                    label = "Aktivitas Fisik",
                    iconResId = R.drawable.ic_walk,

                )
            )
            add(
                CounterfactualOption(
                    key = "is_hypertension",
                    label = "Hipertensi",
                    iconResId = R.drawable.ic_hypertension,
                )
            )
            add(
                CounterfactualOption(
                    key = "is_cholesterol",
                    label = "Kolesterol",
                    iconResId = R.drawable.ic_cholesterol,
                )
            )
        }
    }

    private fun defaultCounterfactualRiskTarget(): CounterfactualRiskTarget {
        return buildCounterfactualRiskTarget(DEFAULT_COUNTERFACTUAL_RISK_TARGET_INPUT.toInt())
    }

    private fun checkAllDataLoaded() {
        if (isUserDataLoaded && isPredictionDataLoaded && isActivityDataLoaded && isProfileDataLoaded) {
            _loadingMessage.value = null
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onSuccessShown() {
        _successMessage.value = null
    }

    private fun collectActivePlannerGoal() {
        plannerGoalUseCases.getActivePlannerGoal()
            .onEach { goal ->
                _activePlannerGoal.value = goal
                filterPlannerCheckInHistory()
                goal?.let { refreshPlannerCheckIns(it.id) }
            }
            .launchIn(viewModelScope)
    }

    private fun collectPlannerCheckInHistory() {
        plannerGoalUseCases.getPlannerCheckInHistory()
            .onEach { history ->
                allPlannerCheckInHistoryCache = history
                _allPlannerCheckInHistory.value = history
                filterPlannerCheckInHistory()
            }
            .launchIn(viewModelScope)
    }

    private fun filterPlannerCheckInHistory() {
        val goalId = activePlannerGoal.value?.id
        _plannerCheckInHistory.value = if (goalId == null) {
            emptyList()
        } else {
            allPlannerCheckInHistoryCache
                .filter { it.goalId == goalId }
                .sortedByDescending { it.createdAtMillis }
        }
    }

    private fun refreshPlannerGoal() {
        viewModelScope.launch {
            plannerGoalUseCases.refreshPlannerGoal()
        }
    }

    private fun refreshPlannerCheckIns(goalId: String) {
        viewModelScope.launch {
            plannerGoalUseCases.refreshPlannerCheckIns(goalId)
        }
    }

    fun refreshPlannerCheckInsForGoal(goalId: String) {
        refreshPlannerCheckIns(goalId)
    }

    fun refreshPlannerCheckInHistoryForGoal(goalId: String) {
        viewModelScope.launch {
            plannerGoalUseCases.refreshPlannerCheckInHistory(goalId)
        }
    }

    fun toggleCounterfactualOption(key: String) {
        _counterfactualOptions.value = counterfactualOptions.value.map { option ->
            if (option.key == key) {
                option.copy(isSelected = !option.isSelected)
            } else {
                option
            }
        }
    }

    fun resetCounterfactualOptions() {
        _counterfactualOptions.value = defaultCounterfactualOptions()
        _counterfactualRiskTargetInput.value = DEFAULT_COUNTERFACTUAL_RISK_TARGET_INPUT
        _successMessage.value = "Pilihan faktor berhasil di-reset"
    }

    fun updateCounterfactualRiskTargetInput(value: String) {
        _counterfactualRiskTargetInput.value = value.filter { it.isDigit() }.take(3)
    }

    fun runCounterfactualAnalysis() {
        if (counterfactualState.value.isLoading) {
            return
        }

        val selectedOptions = counterfactualOptions.value.filter { it.isSelected }
        if (selectedOptions.isEmpty()) {
            _errorMessage.value = "Pilih minimal satu faktor yang ingin dieksplorasi"
            return
        }

        if (_baselineAge.intValue <= 0 || _bmi.doubleValue <= 0.0) {
            _errorMessage.value = "Data dasar belum lengkap untuk menjalankan counterfactual"
            return
        }

        val parsedRiskTarget = parseCounterfactualRiskTarget()
        if (parsedRiskTarget == null) {
            _errorMessage.value = "Masukkan target risiko akhir antara 1% sampai 100%"
            return
        }

        _counterfactualSubmittedOptions.value = selectedOptions
        _counterfactualSubmittedTarget.value = parsedRiskTarget

        if (isRiskTargetAlreadySatisfied(parsedRiskTarget)) {
            showTargetAlreadySatisfiedResult()
            return
        }

        val request = buildCounterfactualRequest(
            selectedKeys = selectedOptions.flatMap(::mutableKeysForCounterfactualOption).distinct()
        )
        _counterfactualResult.value = null
        _counterfactualJobResultMeta.value = null
        currentCounterfactualJobId = null
        _counterfactualState.value = counterfactualState.value.copy(isLoading = true)
        _loadingMessage.value = "Menyiapkan baseline dan batas pencarian counterfactual..."

        viewModelScope.launch {
            counterfactualUseCases.handleAsyncCounterfactual(
                scope = viewModelScope,
                request = request,
                onPending = {
                    _loadingMessage.value =
                        "Mencari skenario yang tetap realistis dan sesuai faktor yang Anda izinkan..."
                },
                onProgress = {
                    _loadingMessage.value =
                        "Memilih rekomendasi yang paling feasible untuk ditampilkan..."
                },
                onCompleted = { jobId ->
                    if (currentCounterfactualJobId == null) {
                        currentCounterfactualJobId = jobId
                        _loadingMessage.value = "Skenario ditemukan, menyiapkan rencana untuk Anda..."
                        handleCounterfactualJobCompleted(jobId)
                    }
                },
                onFailed = { error ->
                    _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                    _loadingMessage.value = null
                    _errorMessage.value = error
                }
            )
        }
    }

    private fun buildCounterfactualRequest(selectedKeys: List<String>): CounterfactualRequest {
        val selectedTarget = parseCounterfactualRiskTarget() ?: defaultCounterfactualRiskTarget()
        return CounterfactualRequest(
            instance = CounterfactualInstance(
                features = CounterfactualFeatureSet(
                    age = _baselineAge.intValue,
                    smokingStatus = _smokingStatus.value.toIntOrNull() ?: 0,
                    isCholesterol = if (_isCholesterol.value) 1 else 0,
                    isMacrosomicBaby = _macrosomicBaby.intValue,
                    moderatePhysicalActivityFrequency = _physicalActivityAverage.intValue,
                    isBloodline = if (_isBloodline.value) 1 else 0,
                    brinkmanIndex = _brinkmanScore.intValue,
                    bmi = _bmi.doubleValue,
                    isHypertension = if (_isHypertension.value) 1 else 0
                )
            ),
            constraints = CounterfactualConstraints(
                mutableAllowed = selectedKeys
            ),
            target = CounterfactualTarget(
                targetClass = "low_risk",
                minTargetProbability = selectedTarget.minLowRiskProbability
            ),
            generation = CounterfactualGeneration(
                totalCfs = 3
            )
        )
    }

    private fun isRiskTargetAlreadySatisfied(target: CounterfactualRiskTarget): Boolean {
        if (lastPredictionAt.value == NO_PREDICTION_TIMESTAMP) {
            return false
        }

        return latestPredictionScore.value <= target.targetHighRiskPercentage.toDouble()
    }

    private fun showTargetAlreadySatisfiedResult() {
        val lowRiskProbability = (1 - (latestPredictionScore.value / 100.0)).coerceIn(0.0, 1.0)
        val localJobId = "local-target-satisfied-${System.currentTimeMillis()}"

        _counterfactualResult.value = CounterfactualResultPayload(
            candidates = emptyList(),
            inputPrediction = CounterfactualPredictionInfo(
                className = if (lowRiskProbability >= 0.5) "low_risk" else "high_risk",
                probabilityLowRisk = lowRiskProbability
            ),
            message = "Kondisi Anda saat ini sudah memenuhi target risiko yang dipilih, sehingga belum diperlukan perubahan tambahan.",
            reasonCode = "TARGET_ALREADY_SATISFIED",
            status = "FEASIBLE"
        )
        _counterfactualJobResultMeta.value = CounterfactualJobResultData(
            jobId = localJobId,
            jobStatus = "completed",
            reasonCode = "TARGET_ALREADY_SATISFIED",
            result = _counterfactualResult.value
        )
        _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
        _loadingMessage.value = null
        currentCounterfactualJobId = null

        if (!_isNavigating.value) {
            _isNavigating.value = true
            _navigationEvent.value = "COUNTERFACTUAL_RESULT_SCREEN"
        }
    }

    private fun mutableKeysForCounterfactualOption(
        option: CounterfactualOption
    ): List<String> {
        return listOf(option.key)
    }

    private fun refreshCounterfactualOptions() {
        val existingSelections = _counterfactualOptions.value.associateBy({ it.key }, { it.isSelected })
        _counterfactualOptions.value = defaultCounterfactualOptions().map { option ->
            option.copy(isSelected = existingSelections[option.key] ?: option.isSelected)
        }
    }

    private fun parseCounterfactualRiskTarget(): CounterfactualRiskTarget? {
        val highRiskPercentage = counterfactualRiskTargetInput.value.toIntOrNull() ?: return null
        if (highRiskPercentage !in 1..100) {
            return null
        }
        return buildCounterfactualRiskTarget(highRiskPercentage)
    }

    private fun buildCounterfactualRiskTarget(highRiskPercentage: Int): CounterfactualRiskTarget {
        val minLowRiskProbability = (100 - highRiskPercentage) / 100.0
        return CounterfactualRiskTarget(
            label = "Di bawah $highRiskPercentage%",
            description = "Planner akan mencari skenario yang mendorong risiko akhir Anda berada di bawah $highRiskPercentage%. Semakin kecil target ini, semakin sulit solusi ditemukan.",
            targetHighRiskPercentage = highRiskPercentage,
            minLowRiskProbability = minLowRiskProbability
        )
    }

    private suspend fun handleCounterfactualJobCompleted(jobId: String) {
        when (val resultResponse = counterfactualUseCases.getCounterfactualJobResult(jobId)) {
            is Resource.Success -> {
                val data = resultResponse.data?.data
                if (data?.result == null) {
                    _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                    _errorMessage.value = "Hasil counterfactual tidak ditemukan"
                    return
                }

                _counterfactualJobResultMeta.value = data
                _counterfactualResult.value = data.result
                _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                _loadingMessage.value = null

                if (!_isNavigating.value) {
                    _isNavigating.value = true
                    _navigationEvent.value = "COUNTERFACTUAL_RESULT_SCREEN"
                }
            }

            is Resource.Error -> {
                _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                _loadingMessage.value = null
                _errorMessage.value = resultResponse.message ?: "Gagal mengambil hasil counterfactual"
            }

            else -> {
                _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                _loadingMessage.value = null
                _errorMessage.value = "Terjadi kesalahan yang tidak diketahui"
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
        _isNavigating.value = false
        currentCounterfactualJobId = null
    }

    fun saveCounterfactualAsGoal(replaceActiveGoal: Boolean = false) {
        val result = counterfactualResult.value
        if (result == null || result.status != "FEASIBLE" || result.candidates.isEmpty()) {
            _errorMessage.value = "Belum ada rencana feasible yang bisa disimpan sebagai goal"
            return
        }

        val goal = buildPlannerGoalFromCounterfactual(result)
        viewModelScope.launch {
            val existingGoal = activePlannerGoal.value
            if (
                replaceActiveGoal &&
                existingGoal != null &&
                existingGoal.sourceJobId != goal.sourceJobId
            ) {
                plannerGoalUseCases.clearPlannerGoal(existingGoal.id)
            }
            plannerGoalUseCases.savePlannerGoal(goal)
            _successMessage.value = "Rencana berhasil disimpan sebagai goal aktif"
        }
    }

    fun clearActivePlannerGoal() {
        val goal = activePlannerGoal.value
        if (goal == null) {
            _errorMessage.value = "Tidak ada goal aktif yang bisa dihapus"
            return
        }

        viewModelScope.launch {
            plannerGoalUseCases.clearPlannerGoal(goal.id)
            plannerGoalUseCases.clearPlannerCheckIns()
            _successMessage.value = "Goal aktif berhasil dihapus"
        }
    }

    fun completeActivePlannerGoal() {
        val goal = activePlannerGoal.value
        if (goal == null) {
            _errorMessage.value = "Tidak ada goal aktif yang bisa diselesaikan"
            return
        }

        viewModelScope.launch {
            plannerGoalUseCases.completePlannerGoal(goal.id)
            plannerGoalUseCases.clearPlannerCheckIns()
            _successMessage.value = "Goal selesai dan berhasil dihapus"
        }
    }

    private fun buildPlannerGoalFromCounterfactual(
        result: CounterfactualResultPayload
    ): PlannerGoal {
        val currentRisk = result.inputPrediction?.probabilityLowRisk?.let(::toHighRiskPercentage)
        val projectedRisk = result.candidates.firstOrNull()?.prediction?.probabilityLowRisk?.let(
            ::toHighRiskPercentage
        )
        val changedFeatures = result.plannerInput?.changedFeatures.orEmpty()
        val goalFeatures = changedFeatures.mapNotNull(::buildPlannerGoalFeature)

        return PlannerGoal(
            id = _counterfactualJobResultMeta.value?.jobId ?: "planner-${System.currentTimeMillis()}",
            title = "Turunkan risiko ke bawah ${counterfactualSubmittedTarget.value.targetHighRiskPercentage}%",
            currentRiskPercentage = currentRisk,
            targetRiskPercentage = counterfactualSubmittedTarget.value.targetHighRiskPercentage,
            projectedRiskPercentage = projectedRisk,
            sourceJobId = _counterfactualJobResultMeta.value?.jobId,
            createdAtMillis = System.currentTimeMillis(),
            summary = result.prescriptivePlan?.summary ?: result.message,
            actionSteps = result.prescriptivePlan?.actionSteps.orEmpty(),
            features = goalFeatures
        )
    }

    private fun buildPlannerGoalFeature(
        feature: CounterfactualChangedFeature
    ): PlannerGoalFeature? {
        val baseline = feature.baselineValue
        val target = feature.candidateValue
        if (baseline == null && target == null) {
            return null
        }

        return PlannerGoalFeature(
            featureName = feature.featureName,
            label = plannerFeatureLabel(feature.featureName),
            baselineValue = baseline,
            targetValue = target,
            baselineText = formatPlannerFeatureValue(feature.featureName, baseline),
            targetText = formatPlannerFeatureValue(feature.featureName, target),
            actionLabel = plannerActionLabel(feature)
        )
    }

    private fun plannerActionLabel(feature: CounterfactualChangedFeature): String {
        val delta = feature.delta ?: run {
            val baseline = feature.baselineValue
            val target = feature.candidateValue
            if (baseline != null && target != null) target - baseline else null
        }

        return when (feature.featureName) {
            "BMI" -> {
                val baselineWeight = feature.baselineValue?.let(::bmiToWeight)
                val targetWeight = feature.candidateValue?.let(::bmiToWeight)
                val weightDelta = if (baselineWeight != null && targetWeight != null) {
                    targetWeight - baselineWeight
                } else {
                    null
                }
                val absDelta = abs(weightDelta ?: 0.0)
                if ((weightDelta ?: 0.0) < 0) {
                    "Turunkan berat sekitar ${String.format("%.1f", absDelta)} kg"
                } else {
                    "Naikkan berat sekitar ${String.format("%.1f", absDelta)} kg"
                }
            }
            "moderate_physical_activity_frequency" -> {
                val absDelta = abs(delta ?: 0.0).toInt()
                if ((delta ?: 0.0) >= 0) {
                    "Tambah aktivitas $absDelta hari/minggu"
                } else {
                    "Sesuaikan aktivitas $absDelta hari/minggu"
                }
            }
            "smoking_status" -> "Ubah status merokok sesuai skenario"
            "brinkman_index" -> "Indeks Brinkman merupakan faktor historis dan tidak digunakan sebagai target aksi"
            "is_hypertension" -> "Kendalikan hipertensi dengan pendampingan klinis"
            "is_cholesterol" -> "Kendalikan kolesterol dengan pendampingan klinis"
            else -> "Ubah dari ${formatPlannerFeatureValue(feature.featureName, feature.baselineValue)} ke ${formatPlannerFeatureValue(feature.featureName, feature.candidateValue)}"
        }
    }

    private fun plannerFeatureLabel(name: String): String {
        return when (name) {
            "BMI" -> "Berat Badan"
            "smoking_status" -> "Status Merokok"
            "brinkman_index" -> "Paparan Rokok"
            "is_cholesterol" -> "Kolesterol"
            "is_hypertension" -> "Hipertensi"
            "moderate_physical_activity_frequency" -> "Aktivitas Fisik"
            "is_bloodline" -> "Riwayat Keluarga"
            "is_macrosomic_baby" -> "Riwayat Bayi Makrosomia"
            "age" -> "Usia"
            else -> name
        }
    }

    private fun formatPlannerFeatureValue(name: String, value: Double?): String {
        if (value == null) {
            return "-"
        }

        return when (name) {
            "BMI" -> bmiToWeight(value)?.let { "${String.format("%.1f", it)} kg" } ?: "-"
            "age" -> "${value.toInt()} tahun"
            "moderate_physical_activity_frequency" -> "${value.toInt()} hari/minggu"
            "smoking_status" -> when (value.toInt()) {
                0 -> "Tidak merokok"
                1 -> "Sudah berhenti"
                2 -> "Masih aktif"
                else -> value.toInt().toString()
            }
            "brinkman_index" -> when (value.toInt()) {
                0 -> "Sangat rendah"
                1 -> "Ringan"
                2 -> "Sedang"
                3 -> "Tinggi"
                else -> value.toInt().toString()
            }
            "is_hypertension", "is_cholesterol", "is_bloodline" -> if (value.toInt() == 1) "Ya" else "Tidak"
            "is_macrosomic_baby" -> when (value.toInt()) {
                0 -> "Tidak"
                1 -> "Ya"
                2 -> "Tidak relevan"
                else -> value.toInt().toString()
            }
            else -> String.format("%.2f", value)
        }
    }

    private fun bmiToWeight(bmi: Double): Double? {
        val heightMeters = _height.intValue / 100.0
        if (heightMeters <= 0.0) {
            return null
        }
        return bmi * heightMeters * heightMeters
    }

    private fun toHighRiskPercentage(lowRiskProbability: Double): Double {
        return (1.0 - lowRiskProbability) * 100
    }

    companion object {
        private const val DEFAULT_COUNTERFACTUAL_RISK_TARGET_INPUT = "45"
    }
}
