package com.itb.diabetify.presentation.history

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.presentation.history.components.PredictionScoreEntry
import com.itb.diabetify.presentation.history.components.RiskFactorContribution
import com.itb.diabetify.presentation.history.components.DailyInput
import com.itb.diabetify.data.remote.prediction.response.PredictionData
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.PredictionUpdateNotifier
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val predictionUseCases: PredictionUseCases
) : ViewModel() {
    // Error State
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Operational States
    private var _getPredictionByDateState = mutableStateOf(DataState())
    val getPredictionByDateState: State<DataState> = _getPredictionByDateState

    private var _getPredictionScoreByDateState = mutableStateOf(DataState())
    val getPredictionScoreByDateState: State<DataState> = _getPredictionScoreByDateState

    // UI States
    data class PredictionDisplayData(
        val riskFactorContributions: List<RiskFactorContribution>,
        val dailyInputs: List<DailyInput>
    )

    private val _displayData = MutableStateFlow<PredictionDisplayData?>(null)
    val displayData: StateFlow<PredictionDisplayData?> = _displayData

    private val _predictionScores = MutableStateFlow<List<PredictionScoreEntry>>(emptyList())
    val predictionScores: StateFlow<List<PredictionScoreEntry>> = _predictionScores

    private val _currentPrediction = MutableStateFlow<PredictionData?>(null)
    val currentPrediction: StateFlow<PredictionData?> = _currentPrediction

    private val _date = mutableStateOf(
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
    val date: State<String> = _date

    // Initialization
    init {
        loadPredictionScores()
        loadPredictionForDate(_date.value)
        
        PredictionUpdateNotifier.addListener {
            refreshPredictionData()
        }
    }

    // Setters for UI States
    fun setDate(date: String) {
        _date.value = date
        loadPredictionScores()
        loadPredictionForDate(date)
    }

    // Use Case Calls
    private fun loadPredictionForDate(date: String) {
        viewModelScope.launch {
            _getPredictionByDateState.value = getPredictionByDateState.value.copy(isLoading = true)

            val getPredictionResult = predictionUseCases.getPredictionByDate(
                startDate = date,
                endDate = date
            )

            _getPredictionByDateState.value = getPredictionByDateState.value.copy(isLoading = false)

            when (getPredictionResult.result) {
                is Resource.Success -> {
                    val prediction = getPredictionResult.result.data?.data?.firstOrNull()
                    _currentPrediction.value = prediction
                    _displayData.value = prediction?.let { mapPredictionToDisplayData(it) }
                }
                is Resource.Error -> {
                    _errorMessage.value = getPredictionResult.result.message ?: "Terjadi kesalahan saat memuat prediksi"
                    getPredictionResult.result.message?.let { Log.e("HistoryViewModel", it) }
                }

                else -> {
                    _errorMessage.value = "Terjadi kesalahan saat memuat prediksi"
                    Log.e("HistoryViewModel", "Unexpected error")
                }
            }
        }
    }

    private fun loadPredictionScores() {
        viewModelScope.launch {
            _getPredictionScoreByDateState.value = getPredictionScoreByDateState.value.copy(isLoading = true)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedDate = LocalDate.parse(_date.value ?: LocalDate.now().format(formatter))

            val startDate = selectedDate.minusDays(15).format(formatter)
            val endDate = selectedDate.plusDays(15).format(formatter)

            val getPredictionScoreByDateResult = predictionUseCases.getPredictionScoreByDate(
                startDate = startDate,
                endDate = endDate
            )

            _getPredictionScoreByDateState.value = getPredictionScoreByDateState.value.copy(isLoading = false)

            when (getPredictionScoreByDateResult.result) {
                is Resource.Success -> {
                    val scores = if (getPredictionScoreByDateResult.result.data?.data != null) {
                        getPredictionScoreByDateResult.result.data.data.mapIndexedNotNull { index, scoreData ->
                            scoreData?.let {
                                val localDate = convertUtcToLocalDate(it.createdAt.orEmpty())
                                PredictionScoreEntry(
                                    day = index + 1,
                                    score = ((it.riskScore ?: 0.0).toFloat()) * 100f,
                                    date = localDate
                                )
                            }
                        }.sortedBy { it.date }
                    } else {
                        Log.d("HistoryViewModel", "No prediction scores data available")
                        emptyList()
                    }
                    _predictionScores.value = scores
                }
                is Resource.Error -> {
                    _errorMessage.value = getPredictionScoreByDateResult.result.message ?: "Terjadi kesalahan saat memuat skor prediksi"
                    getPredictionScoreByDateResult.result.message?.let { Log.e("HistoryViewModel", it) }
                }

                else -> {
                    _errorMessage.value = "Terjadi kesalahan saat memuat skor prediksi"
                    Log.e("HistoryViewModel", "Unexpected error")
                }
            }
        }
    }

    // Helper Functions
    @SuppressLint("DefaultLocale")
    private fun mapPredictionToDisplayData(prediction: PredictionData): PredictionDisplayData {
        return PredictionDisplayData(
            riskFactorContributions = listOf(
                RiskFactorContribution(
                    "Indeks Massa Tubuh",
                    String.format("%.1f", (prediction.bmiContribution ?: 0.0) * 100),
                    prediction.bmiImpact == 1
                ),
                RiskFactorContribution(
                    "Riwayat Hipertensi",
                    String.format("%.1f", (prediction.isHypertensionContribution ?: 0.0) * 100),
                    prediction.isHypertensionImpact == 1
                ),
                RiskFactorContribution(
                    "Riwayat Bayi Makrosomia",
                    String.format("%.1f", (prediction.isMacrosomicBabyContribution ?: 0.0) * 100),
                    prediction.isMacrosomicBabyImpact == 1
                ),
                RiskFactorContribution(
                    "Aktivitas Fisik",
                    String.format("%.1f", (prediction.physicalActivityFrequencyContribution ?: 0.0) * 100),
                    prediction.physicalActivityFrequencyImpact == 1
                ),
                RiskFactorContribution(
                    "Usia",
                    String.format("%.1f", (prediction.ageContribution ?: 0.0) * 100),
                    prediction.ageImpact == 1
                ),
                RiskFactorContribution(
                    "Status Merokok",
                    String.format("%.1f", (prediction.smokingStatusContribution ?: 0.0) * 100),
                    prediction.smokingStatusImpact == 1
                ),
                RiskFactorContribution(
                    "Indeks Brinkman",
                    String.format("%.1f", (prediction.brinkmanScoreContribution ?: 0.0) * 100),
                    prediction.brinkmanScoreImpact == 1
                ),
                RiskFactorContribution(
                    "Riwayat Keluarga",
                    String.format("%.1f", (prediction.isBloodlineContribution ?: 0.0) * 100),
                    prediction.isBloodlineImpact == 1
                ),
                RiskFactorContribution(
                    "Kolesterol",
                    String.format("%.1f", (prediction.isCholesterolContribution ?: 0.0) * 100),
                    prediction.isCholesterolImpact == 1
                )
            ),
            dailyInputs = listOf(
                DailyInput("Usia", "${prediction.age ?: 0} tahun"),
                DailyInput("Indeks Massa Tubuh", "${String.format("%.1f", prediction.bmi ?: 0.0)} kg/m²"),
                DailyInput("Hipertensi", if (prediction.isHypertension == true) "Ya" else "Tidak"),
                DailyInput("Kolesterol", if (prediction.isCholesterol == true) "Ya" else "Tidak"),
                DailyInput("Riwayat Keluarga", if (prediction.isBloodline == true) "Ya" else "Tidak"),
                DailyInput("Riwayat Bayi Makrosomia", if (prediction.isMacrosomicBaby == 1) "Ya" else "Tidak"),
                DailyInput("Status Merokok",
                    when (prediction.smokingStatus) {
                        "0" -> "Tidak Pernah"
                        "1" -> "Sudah Berhenti"
                        "2" -> "Masih Merokok"
                        else -> "Tidak Diketahui"
                    }),
                DailyInput("Indeks Brinkman", "${prediction.brinkmanScore ?: 0}"),
                DailyInput("Jumlah Rokok", "${prediction.avgSmokeCount ?: 0} batang / hari"),
                DailyInput("Frekuensi Aktivitas Fisik", "${prediction.physicalActivityFrequency ?: 0}x / minggu")
            )
        )
    }

    private fun convertUtcToLocalDate(timestamp: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(timestamp.replace(" ", "T"))
            val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            Log.e("HistoryViewModel", "Error parsing timestamp: $timestamp", e)

            try {
                val isoFormatted = timestamp.replace(" ", "T")
                val zonedDateTime = ZonedDateTime.parse(isoFormatted)
                val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e2: Exception) {
                Log.e("HistoryViewModel", "Fallback parsing also failed for: $timestamp", e2)
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
    
    private fun refreshPredictionData() {
        loadPredictionForDate(_date.value)
        loadPredictionScores()
    }
}
