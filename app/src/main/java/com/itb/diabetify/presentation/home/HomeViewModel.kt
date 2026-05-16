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
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.domain.usecases.counterfactual.CounterfactualUseCases
import com.itb.diabetify.domain.usecases.activity.ActivityUseCases
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.util.handleAsyncCounterfactual
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val activityUseCases: ActivityUseCases,
    private val predictionUseCases: PredictionUseCases,
    private val counterfactualUseCases: CounterfactualUseCases,
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

    private val _lastPredictionAt = mutableStateOf("Belum ada prediksi")
    val lastPredictionAt: State<String> = _lastPredictionAt

    private val _latestPredictionScore = mutableDoubleStateOf(0.0)
    val latestPredictionScore: State<Double> = _latestPredictionScore

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

    private val _brinkmanScore = mutableIntStateOf(0)
    val brinkmanScore: State<Int> = _brinkmanScore

    private val _physicalActivityAverage = mutableIntStateOf(0)
    val physicalActivityAverage: State<Int> = _physicalActivityAverage

    private val _smokeToday = mutableIntStateOf(0)
    val smoke: State<Int> = _smokeToday

    private val _physicalActivityToday = mutableIntStateOf(0)
    val physicalActivityToday: State<Int> = _physicalActivityToday

    private val _isNavigating = mutableStateOf(false)
    private val _baselineAge = mutableIntStateOf(0)
    val baselineAge: State<Int> = _baselineAge

    data class CounterfactualOption(
        val key: String,
        val label: String,
        val description: String,
        val iconResId: Int,
        val isSelected: Boolean = true,
        val supportingText: String? = null
    )

    private var currentCounterfactualJobId: String? = null

    private val _counterfactualState = mutableStateOf(DataState())
    val counterfactualState: State<DataState> = _counterfactualState

    private val _counterfactualOptions = mutableStateOf(defaultCounterfactualOptions())
    val counterfactualOptions: State<List<CounterfactualOption>> = _counterfactualOptions

    private val _counterfactualSubmittedOptions = mutableStateOf<List<CounterfactualOption>>(emptyList())
    val counterfactualSubmittedOptions: State<List<CounterfactualOption>> = _counterfactualSubmittedOptions

    private val _counterfactualResult = mutableStateOf<CounterfactualResultPayload?>(null)
    val counterfactualResult: State<CounterfactualResultPayload?> = _counterfactualResult

    private val _counterfactualJobResultMeta = mutableStateOf<CounterfactualJobResultData?>(null)
    val counterfactualJobResultMeta: State<CounterfactualJobResultData?> = _counterfactualJobResultMeta

    // Loading state tracking
    private var isUserDataLoaded = false
    private var isPredictionDataLoaded = false
    private var isActivityDataLoaded = false
    private var isProfileDataLoaded = false

    // Initialization
    init {
        _loadingMessage.value = "Memuat data..."
        loadUserData()
        loadLatestPredictionData()
        loadActivityTodayData()
        loadProfileData()
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
                }
            }.launchIn(viewModelScope)
        }
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
                    _smokeToday.intValue = todayActivity.smokingValue
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
        _latestPredictionScore.doubleValue = 0.0
        _bmi.doubleValue = 0.0
        _weight.intValue = 0
        _height.intValue = 0
        _isHypertension.value = false
        _macrosomicBaby.intValue = 0
        _isBloodline.value = false
        _isCholesterol.value = false
        _smokingStatus.value = "0"
        _smokeToday.intValue = 0
        _physicalActivityToday.intValue = 0
        _brinkmanScore.intValue = 0

        _riskFactors.value = _riskFactors.value.map { it.copy(percentage = 0.0) }
        _counterfactualOptions.value = defaultCounterfactualOptions()
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
        return listOf(
            CounterfactualOption(
                key = "BMI",
                label = "Indeks Massa Tubuh",
                description = "Eksplorasi perubahan berat badan untuk melihat dampaknya pada risiko.",
                iconResId = R.drawable.ic_weight
            ),
            CounterfactualOption(
                key = "moderate_physical_activity_frequency",
                label = "Aktivitas Fisik",
                description = "Gunakan frekuensi aktivitas fisik mingguan sebagai faktor yang boleh diubah.",
                iconResId = R.drawable.ic_walk
            ),
            CounterfactualOption(
                key = "smoking_status",
                label = "Status Merokok",
                description = "Izinkan sistem mengeksplorasi perubahan kategori status merokok.",
                iconResId = R.drawable.ic_smoking
            ),
            CounterfactualOption(
                key = "brinkman_index",
                label = "Indeks Brinkman",
                description = "Pertimbangkan perubahan paparan rokok kumulatif sebagai skenario alternatif.",
                iconResId = R.drawable.ic_smoking
            ),
            CounterfactualOption(
                key = "is_hypertension",
                label = "Hipertensi",
                description = "Faktor ini perlu interpretasi klinis dan tidak boleh dibaca sebagai instruksi medis langsung.",
                iconResId = R.drawable.ic_hypertension,
                supportingText = "Perlu evaluasi klinis."
            ),
            CounterfactualOption(
                key = "is_cholesterol",
                label = "Kolesterol",
                description = "Faktor ini lebih cocok dibaca sebagai decision-support daripada target aksi mandiri.",
                iconResId = R.drawable.ic_cholesterol,
                supportingText = "Perlu evaluasi klinis."
            )
        )
    }

    private fun checkAllDataLoaded() {
        if (isUserDataLoaded && isPredictionDataLoaded && isActivityDataLoaded && isProfileDataLoaded) {
            _loadingMessage.value = null
            _successMessage.value = "Data berhasil dimuat"
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onSuccessShown() {
        _successMessage.value = null
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
        _successMessage.value = "Pilihan faktor berhasil di-reset"
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

        val request = buildCounterfactualRequest(selectedOptions.map { it.key })
        _counterfactualSubmittedOptions.value = selectedOptions
        _counterfactualResult.value = null
        _counterfactualJobResultMeta.value = null
        currentCounterfactualJobId = null
        _counterfactualState.value = counterfactualState.value.copy(isLoading = true)

        viewModelScope.launch {
            counterfactualUseCases.handleAsyncCounterfactual(
                scope = viewModelScope,
                request = request,
                onCompleted = { jobId ->
                    if (currentCounterfactualJobId == null) {
                        currentCounterfactualJobId = jobId
                        handleCounterfactualJobCompleted(jobId)
                    }
                },
                onFailed = { error ->
                    _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                    _errorMessage.value = error
                }
            )
        }
    }

    private fun buildCounterfactualRequest(selectedKeys: List<String>): CounterfactualRequest {
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
                minTargetProbability = 0.5
            ),
            generation = CounterfactualGeneration(
                totalCfs = 3
            )
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

                if (!_isNavigating.value) {
                    _isNavigating.value = true
                    _navigationEvent.value = "COUNTERFACTUAL_RESULT_SCREEN"
                }
            }

            is Resource.Error -> {
                _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                _errorMessage.value = resultResponse.message ?: "Gagal mengambil hasil counterfactual"
            }

            else -> {
                _counterfactualState.value = counterfactualState.value.copy(isLoading = false)
                _errorMessage.value = "Terjadi kesalahan yang tidak diketahui"
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
        _isNavigating.value = false
        currentCounterfactualJobId = null
    }
}
