package com.itb.diabetify.presentation.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.domain.usecases.activity.ActivityUseCases
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.data.remote.prediction.request.WhatIfPredictionRequest
import com.itb.diabetify.presentation.common.FieldState
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.Resource
import com.itb.diabetify.util.handleAsyncWhatIfPrediction
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

    private val _physicalActivityAverage = mutableIntStateOf(0)
    val physicalActivityAverage: State<Int> = _physicalActivityAverage

    private val _smokeToday = mutableIntStateOf(0)
    val smoke: State<Int> = _smokeToday

    private val _physicalActivityToday = mutableIntStateOf(0)
    val physicalActivityToday: State<Int> = _physicalActivityToday

    // WhatIf States
    private val _isNavigating = mutableStateOf(false)
    private val _isCalculating = mutableStateOf(false)
    private var lastCalculationTime = 0L
    private var currentJobId: String? = null

    private val _whatIfPredictionState = mutableStateOf(DataState())
    val whatIfPredictionState: State<DataState> = _whatIfPredictionState

    private val _whatIfPredictionScore = mutableDoubleStateOf(0.0)
    val whatIfPredictionScore: State<Double> = _whatIfPredictionScore

    private val _whatIfRiskFactors = mutableStateOf(listOf(
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
    val whatIfRiskFactors: State<List<RiskFactor>> = _whatIfRiskFactors

    private val _whatIfAge = mutableIntStateOf(0)
    val whatIfAge: State<Int> = _whatIfAge

    private val _whatIfYearsOfSmoking = mutableIntStateOf(0)
    val whatIfYearsOfSmoking: State<Int> = _whatIfYearsOfSmoking

    private val _whatIfMacrosomicBaby = mutableIntStateOf(0)
    val whatIfMacrosomicBaby: State<Int> = _whatIfMacrosomicBaby

    private val _whatIfIsBloodline = mutableStateOf(false)
    val whatIfIsBloodline: State<Boolean> = _whatIfIsBloodline

    // WhatIf Field States
    private val _whatIfSmokingStatusFieldState = mutableStateOf(FieldState())
    val whatIfSmokingStatusFieldState: State<FieldState> = _whatIfSmokingStatusFieldState

    private val _whatIfYearsOfSmokingFieldState = mutableStateOf(FieldState())
    val whatIfYearsOfSmokingFieldState: State<FieldState> = _whatIfYearsOfSmokingFieldState

    private val _whatIfAverageCigarettesFieldState = mutableStateOf(FieldState())
    val whatIfAverageCigarettesFieldState: State<FieldState> = _whatIfAverageCigarettesFieldState

    private val _whatIfWeightFieldState = mutableStateOf(FieldState())
    val whatIfWeightFieldState: State<FieldState> = _whatIfWeightFieldState

    private val _whatIfIsHypertensionFieldState = mutableStateOf(FieldState())
    val whatIfIsHypertensionFieldState: State<FieldState> = _whatIfIsHypertensionFieldState

    private val _whatIfPhysicalActivityFieldState = mutableStateOf(FieldState())
    val whatIfPhysicalActivityFieldState: State<FieldState> = _whatIfPhysicalActivityFieldState

    private val _whatIfIsCholesterolFieldState = mutableStateOf(FieldState())
    val whatIfIsCholesterolFieldState: State<FieldState> = _whatIfIsCholesterolFieldState

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
                    _physicalActivityAverage.intValue = latestPrediction.physicalActivityFrequency

                    _whatIfAge.intValue = latestPrediction.age
                    _whatIfSmokingStatusFieldState.value = FieldState(
                        text = latestPrediction.smokingStatus,
                        error = null
                    )
                    _whatIfAverageCigarettesFieldState.value = FieldState(
                        text = latestPrediction.avgSmokeCount.toString(),
                        error = null
                    )
                    _whatIfPhysicalActivityFieldState.value = FieldState(
                        text = latestPrediction.physicalActivityFrequency.toString(),
                        error = null
                    )
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

                    if (userProfile.ageOfSmoking == 0 && userProfile.ageOfStopSmoking == 0) {
                        _whatIfYearsOfSmoking.intValue = 0
                    } else if (userProfile.ageOfSmoking != 0 && userProfile.ageOfStopSmoking != 0) {
                        _whatIfYearsOfSmoking.intValue = userProfile.ageOfStopSmoking - userProfile.ageOfSmoking
                    } else if (userProfile.ageOfSmoking != 0) {
                        _whatIfYearsOfSmoking.intValue = _whatIfAge.intValue - userProfile.ageOfSmoking
                    } else {
                        _whatIfYearsOfSmoking.intValue = 0
                    }
                    _whatIfYearsOfSmokingFieldState.value = FieldState(
                        text = _whatIfYearsOfSmoking.intValue.toString(),
                        error = null
                    )

                    _whatIfMacrosomicBaby.intValue = userProfile.macrosomicBaby
                    _whatIfIsBloodline.value = userProfile.bloodline

                    _whatIfWeightFieldState.value = FieldState(
                        text = userProfile.weight.toString(),
                        error = null
                    )
                    _whatIfIsHypertensionFieldState.value = FieldState(
                        text = userProfile.hypertension.toString(),
                        error = null
                    )
                    _whatIfIsCholesterolFieldState.value = FieldState(
                        text = userProfile.cholesterol.toString(),
                        error = null
                    )
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

        _riskFactors.value = _riskFactors.value.map { it.copy(percentage = 0.0) }

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

    // WhatIf Methods
    // Setters for WhatIf Field States
    fun setWhatIfSmokingStatus(value: String) {
        _whatIfSmokingStatusFieldState.value = whatIfSmokingStatusFieldState.value.copy(error = null)
        _whatIfSmokingStatusFieldState.value = whatIfSmokingStatusFieldState.value.copy(text = value)
    }

    fun setWhatIfYearsOfSmoking(value: String) {
        _whatIfYearsOfSmokingFieldState.value = whatIfYearsOfSmokingFieldState.value.copy(error = null)
        _whatIfYearsOfSmokingFieldState.value = whatIfYearsOfSmokingFieldState.value.copy(text = value)
    }

    fun setWhatIfAverageCigarettes(value: String) {
        _whatIfAverageCigarettesFieldState.value = whatIfAverageCigarettesFieldState.value.copy(error = null)
        _whatIfAverageCigarettesFieldState.value = whatIfAverageCigarettesFieldState.value.copy(text = value)
    }

    fun setWhatIfWeight(value: String) {
        _whatIfWeightFieldState.value = whatIfWeightFieldState.value.copy(error = null)
        _whatIfWeightFieldState.value = whatIfWeightFieldState.value.copy(text = value)
    }

    fun setWhatIfIsHypertension(value: String) {
        _whatIfIsHypertensionFieldState.value = whatIfIsHypertensionFieldState.value.copy(error = null)
        _whatIfIsHypertensionFieldState.value = whatIfIsHypertensionFieldState.value.copy(text = value)
    }

    fun setWhatIfPhysicalActivity(value: String) {
        _whatIfPhysicalActivityFieldState.value = whatIfPhysicalActivityFieldState.value.copy(error = null)
        _whatIfPhysicalActivityFieldState.value = whatIfPhysicalActivityFieldState.value.copy(text = value)
    }

    fun setWhatIfIsCholesterol(value: String) {
        _whatIfIsCholesterolFieldState.value = whatIfIsCholesterolFieldState.value.copy(error = null)
        _whatIfIsCholesterolFieldState.value = whatIfIsCholesterolFieldState.value.copy(text = value)
    }

    // WhatIf Validation Functions
    fun validateWhatIfFields(): Boolean {
        var isValid = true

        if (whatIfSmokingStatusFieldState.value.text.isBlank()) {
            _whatIfSmokingStatusFieldState.value = whatIfSmokingStatusFieldState.value.copy(error = "Status merokok tidak boleh kosong")
            isValid = false
        }

        if (whatIfYearsOfSmokingFieldState.value.text.isBlank()) {
            _whatIfYearsOfSmokingFieldState.value = whatIfYearsOfSmokingFieldState.value.copy(error = "Lama merokok tidak boleh kosong")
            isValid = false
        } else if (whatIfYearsOfSmokingFieldState.value.text.toInt() < 0 || whatIfYearsOfSmokingFieldState.value.text.toInt() > 70) {
            _whatIfYearsOfSmokingFieldState.value = whatIfYearsOfSmokingFieldState.value.copy(error = "Lama merokok harus antara 0-70 tahun")
            isValid = false
        } else if (whatIfYearsOfSmokingFieldState.value.text.toInt() < _whatIfYearsOfSmoking.intValue) {
            _whatIfYearsOfSmokingFieldState.value = whatIfYearsOfSmokingFieldState.value.copy(error = "Lama merokok tidak boleh kurang dari lama merokok yang Anda miliki saat ini")
            isValid = false
        }

        if (whatIfAverageCigarettesFieldState.value.text.isBlank()) {
            _whatIfAverageCigarettesFieldState.value = whatIfAverageCigarettesFieldState.value.copy(error = "Jumlah rokok tidak boleh kosong")
            isValid = false
        } else if (whatIfAverageCigarettesFieldState.value.text.toInt() < 0 || whatIfAverageCigarettesFieldState.value.text.toInt() > 60) {
            _whatIfAverageCigarettesFieldState.value = whatIfAverageCigarettesFieldState.value.copy(error = "Jumlah rokok harus antara 0-60 batang")
            isValid = false
        }

        if (whatIfWeightFieldState.value.text.isBlank()) {
            _whatIfWeightFieldState.value = whatIfWeightFieldState.value.copy(error = "Berat badan tidak boleh kosong")
            isValid = false
        } else if (whatIfWeightFieldState.value.text.toInt() < 30 || whatIfWeightFieldState.value.text.toInt() > 300) {
            _whatIfWeightFieldState.value = whatIfWeightFieldState.value.copy(error = "Berat badan harus antara 30-300 kg")
            isValid = false
        }

        if (whatIfIsHypertensionFieldState.value.text.isBlank()) {
            _whatIfIsHypertensionFieldState.value = whatIfIsHypertensionFieldState.value.copy(error = "Hipertensi tidak boleh kosong")
            isValid = false
        }

        if (whatIfPhysicalActivityFieldState.value.text.isBlank()) {
            _whatIfPhysicalActivityFieldState.value = whatIfPhysicalActivityFieldState.value.copy(error = "Aktivitas fisik tidak boleh kosong")
            isValid = false
        } else if (whatIfPhysicalActivityFieldState.value.text.toInt() < 0 || whatIfPhysicalActivityFieldState.value.text.toInt() > 7) {
            _whatIfPhysicalActivityFieldState.value = whatIfPhysicalActivityFieldState.value.copy(error = "Aktivitas fisik harus antara 0-7 hari")
            isValid = false
        }

        if (whatIfIsCholesterolFieldState.value.text.isBlank()) {
            _whatIfIsCholesterolFieldState.value = whatIfIsCholesterolFieldState.value.copy(error = "Kolesterol tidak boleh kosong")
            isValid = false
        }

        return isValid
    }

    private fun clearWhatIfResults() {
        _whatIfPredictionScore.doubleValue = 0.0
        _whatIfRiskFactors.value = listOf(
            RiskFactor("Indeks Massa Tubuh", "IMT", 0.0),
            RiskFactor("Hipertensi", "H", 0.0),
            RiskFactor("Riwayat Bayi Makrosomia", "RBM", 0.0),
            RiskFactor("Aktivitas Fisik", "AF", 0.0),
            RiskFactor("Usia", "U", 0.0),
            RiskFactor("Status Merokok", "SM", 0.0),
            RiskFactor("Indeks Brinkman", "IB", 0.0),
            RiskFactor("Riwayat Keluarga", "RK", 0.0),
            RiskFactor("Kolesterol", "K", 0.0),
        )
        _isNavigating.value = false
        currentJobId = null
    }

    @SuppressLint("DefaultLocale")
    fun calculateWhatIfPrediction() {
        val currentTime = System.currentTimeMillis()
        
        if (_isCalculating.value) {
            return
        }
        
        if (currentTime - lastCalculationTime < 5000) {
            return
        }
        
        lastCalculationTime = currentTime

        viewModelScope.launch {
            _isCalculating.value = true
            
            clearWhatIfResults()
            
            _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = true)

            val smokingStatus = whatIfSmokingStatusFieldState.value.text.toInt()
            val yearsOfSmoking = whatIfYearsOfSmokingFieldState.value.text.toIntOrNull() ?: 0
            val averageCigarettes = whatIfAverageCigarettesFieldState.value.text.toIntOrNull() ?: 0
            val weight = whatIfWeightFieldState.value.text.toInt()
            val isHypertension = whatIfIsHypertensionFieldState.value.text.toBoolean()
            val physicalActivity = whatIfPhysicalActivityFieldState.value.text.toInt()
            val isCholesterol = whatIfIsCholesterolFieldState.value.text.toBoolean()

            val whatIfPredictionResult = predictionUseCases.whatIfPrediction(
                smokingStatus = smokingStatus,
                yearsOfSmoking = yearsOfSmoking,
                avgSmokeCount = averageCigarettes,
                weight = weight,
                isHypertension = isHypertension,
                physicalActivityFrequency = physicalActivity,
                isCholesterol = isCholesterol
            )

            if (whatIfPredictionResult.smokingStatusError != null) {
                _whatIfSmokingStatusFieldState.value = whatIfSmokingStatusFieldState.value.copy(error = whatIfPredictionResult.smokingStatusError)
                _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                _isCalculating.value = false
                return@launch
            }

            if (whatIfPredictionResult.avgSmokeCountError != null) {
                _whatIfAverageCigarettesFieldState.value = whatIfAverageCigarettesFieldState.value.copy(error = whatIfPredictionResult.avgSmokeCountError)
                _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                _isCalculating.value = false
                return@launch
            }

            if (whatIfPredictionResult.weightError != null) {
                _whatIfWeightFieldState.value = whatIfWeightFieldState.value.copy(error = whatIfPredictionResult.weightError)
                _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                _isCalculating.value = false
                return@launch
            }

            if (whatIfPredictionResult.physicalActivityFrequencyError != null) {
                _whatIfPhysicalActivityFieldState.value = whatIfPhysicalActivityFieldState.value.copy(error = whatIfPredictionResult.physicalActivityFrequencyError)
                _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                _isCalculating.value = false
                return@launch
            }

            val whatIfRequest = WhatIfPredictionRequest(
                smokingStatus = smokingStatus,
                yearsOfSmoking = yearsOfSmoking,
                avgSmokeCount = averageCigarettes,
                weight = weight,
                isHypertension = isHypertension,
                physicalActivityFrequency = physicalActivity,
                isCholesterol = isCholesterol
            )

            predictionUseCases.handleAsyncWhatIfPrediction(
                scope = viewModelScope,
                whatIfRequest = whatIfRequest,
                onPending = {
                },
                onProgress = { progress ->
                },
                onCompleted = { jobId ->
                    if (currentJobId == null) {
                        currentJobId = jobId
                        handleWhatIfJobCompleted(jobId)
                    }
                },
                onFailed = { error ->
                    _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                    _isCalculating.value = false
                    _errorMessage.value = error
                }
            )
        }
    }

    private suspend fun handleWhatIfJobCompleted(jobId: String) {
        try {
            val resultResponse = predictionUseCases.getWhatIfJobResult(jobId)
            
            when (resultResponse) {
                is Resource.Success -> {
                    val data = resultResponse.data?.data
                    data?.let { resultData ->
                        _whatIfPredictionScore.doubleValue = resultData.riskPercentage

                        val updatedRiskFactors = _whatIfRiskFactors.value.map { riskFactor ->
                            val featureKey = when (riskFactor.abbreviation) {
                                "IMT" -> "BMI"
                                "H" -> "is_hypertension"
                                "RBM" -> "is_macrosomic_baby"
                                "AF" -> "moderate_physical_activity_frequency"
                                "U" -> "age"
                                "SM" -> "smoking_status"
                                "IB" -> "brinkman_index"
                                "RK" -> "is_bloodline"
                                "K" -> "is_cholesterol"
                                else -> null
                            }

                            featureKey?.let { key ->
                                resultData.featureExplanations[key]?.let { explanation ->
                                    val adjustedContribution = if (explanation.impact == 0) {
                                        -explanation.contribution
                                    } else {
                                        explanation.contribution
                                    }
                                    riskFactor.copy(
                                        name = riskFactor.name,
                                        abbreviation = riskFactor.abbreviation,
                                        percentage = adjustedContribution * 100
                                    )
                                }
                            } ?: riskFactor
                        }

                        _whatIfRiskFactors.value = updatedRiskFactors
                    }

                    _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                    _isCalculating.value = false

                    if (!_isNavigating.value) {
                        _isNavigating.value = true
                        _navigationEvent.value = "WHAT_IF_RESULT_SCREEN"
                    }
                }
                is Resource.Error -> {
                    _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                    _isCalculating.value = false
                    _errorMessage.value = resultResponse.message ?: "Terjadi kesalahan saat mengambil hasil prediksi"
                }
                else -> {
                    _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
                    _isCalculating.value = false
                    _errorMessage.value = "Terjadi kesalahan yang tidak diketahui"
                }
            }
        } catch (e: Exception) {
            _whatIfPredictionState.value = whatIfPredictionState.value.copy(isLoading = false)
            _isCalculating.value = false
            _errorMessage.value = "Terjadi kesalahan saat memproses hasil: ${e.message}"
        }
    }

    // Helper Functions
    fun resetWhatIfFields() {
        collectLatestPredictionData()
        collectProfileData()

        _successMessage.value = "Data berhasil di-reset"
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
        _isNavigating.value = false
        _isCalculating.value = false
        currentJobId = null
    }
}