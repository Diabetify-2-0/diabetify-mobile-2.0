package com.itb.diabetify.presentation.navbar.add_activity

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.model.activity.AddActivityResult
import com.itb.diabetify.domain.model.activity.UpdateActivityResult
import com.itb.diabetify.domain.usecases.activity.ActivityUseCases
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.planner.PlannerGoalUseCases
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.common.FieldState
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.handleAsyncPrediction
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    private val activityUseCases: ActivityUseCases,
    private val profileUseCases: ProfileUseCases,
    private val predictionUseCases: PredictionUseCases,
    private val userUseCases: UserUseCases,
    private val plannerGoalUseCases: PlannerGoalUseCases
) : ViewModel() {
    // Error and Success States
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Operational States
    private var _activityTodayState = mutableStateOf(DataState())
    val activityTodayState: State<DataState> = _activityTodayState

    private var _profileState = mutableStateOf(DataState())
    val profileState: State<DataState> = _profileState

    private var _addActivityState = mutableStateOf(DataState())
    val addActivityState: State<DataState> = _addActivityState

    private var _updateActivityState = mutableStateOf(DataState())
    val updateActivityState: State<DataState> = _updateActivityState

    private var _updateProfileState = mutableStateOf(DataState())
    val updateProfileState: State<DataState> = _updateProfileState

    private var _predictionState = mutableStateOf(DataState())
    val predictionState: State<DataState> = _predictionState

    private val _userState = mutableStateOf(DataState())
    val userState: State<DataState> = _userState

    private val _activePlannerGoal = mutableStateOf<PlannerGoal?>(null)
    val activePlannerGoal: State<PlannerGoal?> = _activePlannerGoal

    private val _plannerCheckIns = mutableStateOf<Map<String, Long>>(emptyMap())

    // UI States
    val surveyQuestions = questions

    @SuppressLint("NewApi")
    val activityDate = ZonedDateTime.now(ZoneOffset.UTC).toString()

    private val _userGender = mutableStateOf<String?>(null)
    val userGender: State<String?> = _userGender

    private val _workoutId = mutableStateOf<Int?>(null)
    val workoutId: State<Int?> = _workoutId

    private val _currentQuestionType = mutableStateOf("weight")
    val currentQuestionType: State<String> = _currentQuestionType

    private val _showBottomSheet = mutableStateOf(false)
    val showBottomSheet: State<Boolean> = _showBottomSheet

    // Field States
    private val _workoutFieldState = mutableStateOf(FieldState())
    val workoutFieldState: State<FieldState> = _workoutFieldState

    private val _weightFieldState = mutableStateOf(FieldState())
    val weightFieldState: State<FieldState> = _weightFieldState

    private val _heightFieldState = mutableStateOf(FieldState())
    val heightFieldState: State<FieldState> = _heightFieldState

    private val _birthFieldState = mutableStateOf(FieldState())
    val birthFieldState: State<FieldState> = _birthFieldState

    private val _hypertensionFieldState = mutableStateOf(FieldState())
    val hypertensionFieldState: State<FieldState> = _hypertensionFieldState

    private val _systolicFieldState = mutableStateOf(FieldState())
    val systolicFieldState: State<FieldState> = _systolicFieldState

    private val _diastolicFieldState = mutableStateOf(FieldState())
    val diastolicFieldState: State<FieldState> = _diastolicFieldState

    private val _bloodlineFieldState = mutableStateOf(FieldState())
    val bloodlineFieldState: State<FieldState> = _bloodlineFieldState

    private val _cholesterolFieldState = mutableStateOf(FieldState())
    val cholesterolFieldState: State<FieldState> = _cholesterolFieldState

    // Initialization
    init {
        collectActivityTodayData()
        collectProfileData()
        collectUserData()
        collectActivePlannerGoal()
        collectPlannerCheckIns()
        refreshPlannerGoal()
    }

    // Setters for UI States
    fun setCurrentQuestionType(type: String) {
        _currentQuestionType.value = type
    }

    fun setShowBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
    }

    fun currentValueFor(questionType: String): String? {
        return when (questionType) {
            "activity" -> workoutFieldState.value.text
            "weight" -> weightFieldState.value.text
            "height" -> heightFieldState.value.text
            "hypertension" -> hypertensionFieldState.value.text
            "cholesterol" -> cholesterolFieldState.value.text
            "bloodline" -> bloodlineFieldState.value.text
            "birth" -> birthFieldState.value.text
            else -> null
        }
    }

    // Setters for Field States
    fun setWorkoutValue(value: String) {
        val validationError = validateField("workout", value)
        _workoutFieldState.value = workoutFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setWeightValue(value: String) {
        val validationError = validateField("weight", value)
        _weightFieldState.value = weightFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setHeightValue(value: String) {
        val validationError = validateField("height", value)
        _heightFieldState.value = heightFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setBirthValue(value: String) {
        val validationError = validateField("birth", value)
        _birthFieldState.value = birthFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setHypertensionValue(value: String) {
        val validationError = validateField("hypertension", value)
        _hypertensionFieldState.value = hypertensionFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setSystolicValue(value: String) {
        val validationError = validateField("systolic", value)
        _systolicFieldState.value = systolicFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setDiastolicValue(value: String) {
        val validationError = validateField("diastolic", value)
        _diastolicFieldState.value = diastolicFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setBloodlineValue(value: String) {
        val validationError = validateField("bloodline", value)
        _bloodlineFieldState.value = bloodlineFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    fun setCholesterolValue(value: String) {
        val validationError = validateField("cholesterol", value)
        _cholesterolFieldState.value = cholesterolFieldState.value.copy(
            text = value,
            error = validationError
        )
    }

    // Validation Function
    private fun validateField(fieldType: String, value: String): String? {
        if (value.isBlank()) {
            return "Mohon isi field ini"
        }

        when (fieldType) {
            "weight" -> {
                val numericValue = value.toIntOrNull() ?: return "Harap masukkan angka yang valid"
                if (numericValue < 30 || numericValue > 300) {
                    return "Berat badan harus antara 30-300 kg"
                }
            }
            "height" -> {
                val numericValue = value.toIntOrNull() ?: return "Harap masukkan angka yang valid"
                if (numericValue < 100 || numericValue > 250) {
                    return "Tinggi badan harus antara 100-250 cm"
                }
            }
            "birth" -> {
                val numericValue = value.toIntOrNull() ?: return "Harap masukkan angka yang valid"
                if (numericValue < 0 || numericValue > 2) {
                    return "Status bayi makrosomia harus 0, 1, atau 2"
                }
            }
            "systolic" -> {
                val numericValue = value.toIntOrNull() ?: return "Harap masukkan angka yang valid"
                if (numericValue < 70 || numericValue > 250) {
                    return "Tekanan sistolik harus antara 70-250 mmHg"
                }
            }
            "diastolic" -> {
                val numericValue = value.toIntOrNull() ?: return "Harap masukkan angka yang valid"
                if (numericValue < 40 || numericValue > 150) {
                    return "Tekanan diastolik harus antara 40-150 mmHg"
                }
            }
            "workout" -> {
                if (value != "true" && value != "false") {
                    return "Pilihan workout tidak valid"
                }
            }
            "hypertension", "bloodline", "cholesterol" -> {
                if (value != "true" && value != "false") {
                    return "Pilihan tidak valid"
                }
            }
        }

        return null
    }

    fun isFieldValid(fieldType: String): Boolean {
        val fieldState = when (fieldType) {
            "workout" -> workoutFieldState.value
            "weight" -> weightFieldState.value
            "height" -> heightFieldState.value
            "birth" -> birthFieldState.value
            "hypertension" -> hypertensionFieldState.value
            "systolic" -> systolicFieldState.value
            "diastolic" -> diastolicFieldState.value
            "bloodline" -> bloodlineFieldState.value
            "cholesterol" -> cholesterolFieldState.value
            else -> return false
        }

        return fieldState.error == null && fieldState.text.isNotBlank()
    }

    fun validateWorkoutField(): Boolean {
        val workoutValueText = workoutFieldState.value.text
        val validationError = validateField("workout", workoutValueText)

        if (validationError != null) {
            _workoutFieldState.value = workoutFieldState.value.copy(error = validationError)
            _errorMessage.value = validationError
            return false
        }
        return true
    }

    fun validateProfileFields(): Boolean {
        val validationErrors = mutableListOf<String>()

        val weightError = validateField("weight", weightFieldState.value.text)
        if (weightError != null) {
            _weightFieldState.value = weightFieldState.value.copy(error = weightError)
            validationErrors.add("Berat badan: $weightError")
        }

        val heightError = validateField("height", heightFieldState.value.text)
        if (heightError != null) {
            _heightFieldState.value = heightFieldState.value.copy(error = heightError)
            validationErrors.add("Tinggi badan: $heightError")
        }

        val hypertensionError = validateField("hypertension", hypertensionFieldState.value.text)
        if (hypertensionError != null) {
            _hypertensionFieldState.value = hypertensionFieldState.value.copy(error = hypertensionError)
            validationErrors.add("Hipertensi: $hypertensionError")
        }

        val birthError = validateField("birth", birthFieldState.value.text)
        if (birthError != null) {
            _birthFieldState.value = birthFieldState.value.copy(error = birthError)
            validationErrors.add("Bayi makrosomik: $birthError")
        }

        val bloodlineError = validateField("bloodline", bloodlineFieldState.value.text)
        if (bloodlineError != null) {
            _bloodlineFieldState.value = bloodlineFieldState.value.copy(error = bloodlineError)
            validationErrors.add("Riwayat keluarga: $bloodlineError")
        }

        val cholesterolError = validateField("cholesterol", cholesterolFieldState.value.text)
        if (cholesterolError != null) {
            _cholesterolFieldState.value = cholesterolFieldState.value.copy(error = cholesterolError)
            validationErrors.add("Kolesterol: $cholesterolError")
        }

        if (validationErrors.isNotEmpty()) {
            _errorMessage.value = "Harap perbaiki kesalahan berikut:\n${validationErrors.joinToString("\n")}"
            return false
        }
        return true
    }

    fun canUpdateBloodPressure(): Boolean {
        return isFieldValid("systolic") && isFieldValid("diastolic")
    }

    // Use Case Calls
    private fun collectUserData() {
        viewModelScope.launch {
            _userState.value = userState.value.copy(isLoading = true)

            userUseCases.getUserRepository().onEach { user ->
                _userState.value = userState.value.copy(isLoading = false)

                user?.let {
                    _userGender.value = it.gender
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun collectActivePlannerGoal() {
        plannerGoalUseCases.getActivePlannerGoal()
            .onEach { goal ->
                _activePlannerGoal.value = goal
                if (goal?.status == PlannerGoalStatus.ACTIVE) {
                    refreshPlannerCheckIns(goal.id)
                } else {
                    clearPlannerCheckIns()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun collectPlannerCheckIns() {
        plannerGoalUseCases.getPlannerCheckIns()
            .onEach { checkIns ->
                _plannerCheckIns.value = checkIns
            }
            .launchIn(viewModelScope)
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

    private fun clearPlannerCheckIns() {
        viewModelScope.launch {
            plannerGoalUseCases.clearPlannerCheckIns()
        }
    }

    data class PlannerCheckInAction(
        val label: String,
        val description: String,
        val checkInType: String,
        val cadenceLabel: String,
        val dueText: String,
        val isDue: Boolean,
        val isTargetAchieved: Boolean = false,
        val questionType: String? = null,
        val opensHealthProfile: Boolean = false
    )

    fun plannerCheckInActions(): List<PlannerCheckInAction> {
        val goal = activePlannerGoal.value ?: return emptyList()
        if (goal.status != PlannerGoalStatus.ACTIVE) {
            return emptyList()
        }

        val featureNames = goal.features.map { it.featureName }.toSet()
        val featureByName = goal.features.associateBy { it.featureName }
        val actions = mutableListOf<PlannerCheckInAction>()

        if ("BMI" in featureNames) {
            val bmiFeature = featureByName["BMI"]
            actions += plannerCheckInAction(
                label = "Berat",
                description = "Update berat untuk memantau progres target berat badan.",
                checkInType = CHECK_IN_WEIGHT,
                intervalMillis = WEEKLY_INTERVAL_MILLIS,
                cadenceLabel = "Mingguan",
                isTargetAchieved = bmiFeature?.let { feature ->
                    currentBmi()?.let { bmi ->
                        hasReachedNumericTarget(
                            currentValue = bmi,
                            baselineValue = feature.baselineValue,
                            targetValue = feature.targetValue
                        )
                    }
                } == true,
                keepsCheckingAfterTarget = true,
                questionType = "weight"
            )
        }

        if ("moderate_physical_activity_frequency" in featureNames) {
            actions += plannerCheckInAction(
                label = "Aktivitas",
                description = "Catat aktivitas fisik hari ini.",
                checkInType = CHECK_IN_ACTIVITY,
                intervalMillis = DAILY_INTERVAL_MILLIS,
                cadenceLabel = "Harian",
                questionType = "activity"
            )
        }

        if ("is_hypertension" in featureNames) {
            val hypertensionFeature = featureByName["is_hypertension"]
            actions += plannerCheckInAction(
                label = "Hipertensi",
                description = "Update tekanan darah atau status hipertensi.",
                checkInType = CHECK_IN_HYPERTENSION,
                intervalMillis = WEEKLY_INTERVAL_MILLIS,
                cadenceLabel = "Mingguan",
                isTargetAchieved = hypertensionFeature?.targetValue?.toInt() ==
                        booleanFieldAsInt(hypertensionFieldState.value.text),
                keepsCheckingAfterTarget = false,
                questionType = "hypertension"
            )
        }

        if ("is_cholesterol" in featureNames) {
            val cholesterolFeature = featureByName["is_cholesterol"]
            actions += plannerCheckInAction(
                label = "Kolesterol",
                description = "Update status kolesterol bila ada data terbaru.",
                checkInType = CHECK_IN_CHOLESTEROL,
                intervalMillis = MONTHLY_INTERVAL_MILLIS,
                cadenceLabel = "Bulanan",
                isTargetAchieved = cholesterolFeature?.targetValue?.toInt() ==
                        booleanFieldAsInt(cholesterolFieldState.value.text),
                keepsCheckingAfterTarget = false,
                questionType = "cholesterol"
            )
        }

        return actions
            .distinctBy { it.label }
            .sortedWith(compareByDescending<PlannerCheckInAction> { it.isDue }.thenBy { it.label })
            .take(4)
    }

    private fun plannerCheckInAction(
        label: String,
        description: String,
        checkInType: String,
        intervalMillis: Long,
        cadenceLabel: String,
        isTargetAchieved: Boolean = false,
        keepsCheckingAfterTarget: Boolean = true,
        questionType: String? = null,
        opensHealthProfile: Boolean = false
    ): PlannerCheckInAction {
        val lastCheckIn = _plannerCheckIns.value[checkInType]
        val isDue = if (isTargetAchieved && !keepsCheckingAfterTarget) {
            false
        } else {
            lastCheckIn == null || System.currentTimeMillis() - lastCheckIn >= intervalMillis
        }
        return PlannerCheckInAction(
            label = label,
            description = description,
            checkInType = checkInType,
            cadenceLabel = cadenceLabel,
            dueText = when {
                isTargetAchieved && !keepsCheckingAfterTarget -> "Sudah sesuai target"
                lastCheckIn == null -> "Belum melakukan check-in"
                isDue -> "Check-in tersedia"
                else -> "Sudah check-in"
            },
            isDue = isDue,
            isTargetAchieved = isTargetAchieved,
            questionType = questionType,
            opensHealthProfile = opensHealthProfile
        )
    }

    private fun currentBmi(): Double? {
        val weight = weightFieldState.value.text.toDoubleOrNull() ?: return null
        val heightCm = heightFieldState.value.text.toDoubleOrNull() ?: return null
        if (heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        return weight / (heightM * heightM)
    }

    private fun hasReachedNumericTarget(
        currentValue: Double,
        baselineValue: Double?,
        targetValue: Double?
    ): Boolean {
        val target = targetValue ?: return false
        val baseline = baselineValue ?: return false
        return if (target <= baseline) {
            currentValue <= target
        } else {
            currentValue >= target
        }
    }

    private fun booleanFieldAsInt(value: String): Int? {
        return when (value) {
            "true" -> 1
            "false" -> 0
            else -> null
        }
    }

    private fun collectActivityTodayData() {
        viewModelScope.launch {
            _activityTodayState.value = DataState(isLoading = true)

            activityUseCases.getActivityRepository().onEach { activity ->
                _activityTodayState.value = activityTodayState.value.copy(isLoading = false)

                activity?.let {
                    _workoutId.value = it.workoutId
                    _workoutFieldState.value = FieldState(
                        text = if (it.workoutValue == 0) "false" else "true",
                        error = null
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun collectProfileData() {
        viewModelScope.launch {
            _profileState.value = profileState.value.copy(isLoading = true)

            profileUseCases.getProfileRepository().onEach { profile ->
                _profileState.value = profileState.value.copy(isLoading = false)

                profile?.let {
                    _weightFieldState.value = FieldState(
                        text = it.weight.toString(),
                        error = null
                    )
                    _heightFieldState.value = FieldState(
                        text = it.height.toString(),
                        error = null
                    )
                    _birthFieldState.value = FieldState(
                        text = it.macrosomicBaby.toString(),
                        error = null
                    )
                    _hypertensionFieldState.value = FieldState(
                        text = it.hypertension.toString(),
                        error = null
                    )
                    _bloodlineFieldState.value = FieldState(
                        text = it.bloodline.toString(),
                        error = null
                    )
                    _cholesterolFieldState.value = FieldState(
                        text = it.cholesterol.toString(),
                        error = null
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun handleWorkout() {
        val workoutIdValue = workoutId.value
        val workoutValue = if (workoutFieldState.value.text.lowercase() == "true" || workoutFieldState.value.text == "1") 1 else 0

        if (workoutIdValue != null) {
            updateWorkoutActivity(workoutIdValue, workoutValue)
        } else {
            addWorkoutActivity(workoutValue)
        }
    }

    private fun addWorkoutActivity(value: Int) {
        viewModelScope.launch {
            _addActivityState.value = addActivityState.value.copy(isLoading = true)

            val addActivityResult = activityUseCases.addActivity(
                activityDate = activityDate,
                activityType = "workout",
                value = value
            )

            _addActivityState.value = addActivityState.value.copy(isLoading = false)

            if (addActivityResult.activityDateError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = addActivityResult.activityDateError)
            }

            if (addActivityResult.activityTypeError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = addActivityResult.activityTypeError)
            }

            if (addActivityResult.valueError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = addActivityResult.valueError)
            }

            handleActivityResult(addActivityResult, "add workout activity")
        }
    }

    private fun updateWorkoutActivity(activityId: Int, value: Int) {
        viewModelScope.launch {
            _updateActivityState.value = updateActivityState.value.copy(isLoading = true)

            val updateActivityResult = activityUseCases.updateActivity(
                activityId = activityId,
                activityDate = activityDate,
                activityType = "workout",
                value = value
            )

            _updateActivityState.value = updateActivityState.value.copy(isLoading = false)

            if (updateActivityResult.activityIdError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = updateActivityResult.activityIdError)
            }

            if (updateActivityResult.activityDateError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = updateActivityResult.activityDateError)
            }

            if (updateActivityResult.activityTypeError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = updateActivityResult.activityTypeError)
            }

            if (updateActivityResult.valueError != null) {
                _workoutFieldState.value = workoutFieldState.value.copy(error = updateActivityResult.valueError)
            }

            handleActivityResult(updateActivityResult, "update workout activity")
        }
    }

    private fun handleActivityResult(result: Any?, operationType: String) {
        val resourceResult = when (result) {
            is AddActivityResult -> result.result
            is UpdateActivityResult -> result.result
            else -> null
        }

        when (resourceResult) {
            is Resource.Success -> {
                markPlannerCheckInForQuestion(currentQuestionType.value)
                triggerPredictionUpdate()
            }
            is Resource.Error -> {
                _errorMessage.value = resourceResult.message ?: "Terjadi kesalahan saat $operationType"
                Log.e("AddActivityViewModel", "Failed to $operationType: ${resourceResult.message}")
            }

            else -> {
                _errorMessage.value = "Terjadi kesalahan saat $operationType"
                Log.e("AddActivityViewModel", "Unexpected error during: $operationType")
            }
        }
    }

    fun updateProfile(type: String) {
        val weight = parseProfileInt(
            value = weightFieldState.value.text,
            fieldName = "Berat badan",
            setError = { error -> _weightFieldState.value = weightFieldState.value.copy(error = error) }
        ) ?: return
        val height = parseProfileInt(
            value = heightFieldState.value.text,
            fieldName = "Tinggi badan",
            setError = { error -> _heightFieldState.value = heightFieldState.value.copy(error = error) }
        ) ?: return
        val hypertension = parseProfileBoolean(
            value = hypertensionFieldState.value.text,
            fieldName = "Hipertensi",
            setError = { error -> _hypertensionFieldState.value = hypertensionFieldState.value.copy(error = error) }
        ) ?: return
        val macrosomicBaby = parseProfileInt(
            value = birthFieldState.value.text,
            fieldName = "Bayi makrosomik",
            setError = { error -> _birthFieldState.value = birthFieldState.value.copy(error = error) }
        ) ?: return
        val bloodline = parseProfileBoolean(
            value = bloodlineFieldState.value.text,
            fieldName = "Riwayat keluarga",
            setError = { error -> _bloodlineFieldState.value = bloodlineFieldState.value.copy(error = error) }
        ) ?: return
        val cholesterol = parseProfileBoolean(
            value = cholesterolFieldState.value.text,
            fieldName = "Kolesterol",
            setError = { error -> _cholesterolFieldState.value = cholesterolFieldState.value.copy(error = error) }
        ) ?: return

        viewModelScope.launch {
            _updateProfileState.value = updateProfileState.value.copy(isLoading = true)

            val updateProfileResult = when (type) {
                "weight", "height", "hypertension", "birth", "bloodline", "cholesterol" -> {
                    profileUseCases.updateProfile(
                        weight = weight,
                        height = height,
                        hypertension = hypertension,
                        macrosomicBaby = macrosomicBaby,
                        bloodline = bloodline,
                        cholesterol = cholesterol
                    )
                }
                else -> {
                    _errorMessage.value = "Tipe pembaruan profil tidak valid"
                    Log.e("AddActivityViewModel", "Invalid profile update type")
                    null
                }
            }

            _updateProfileState.value = updateProfileState.value.copy(isLoading = false)

            if (updateProfileResult?.weightError != null) {
                _weightFieldState.value = weightFieldState.value.copy(error = updateProfileResult.weightError)
            }

            if (updateProfileResult?.heightError != null) {
                _heightFieldState.value = heightFieldState.value.copy(error = updateProfileResult.heightError)
            }

            if (updateProfileResult?.macrosomicBabyError != null) {
                _birthFieldState.value = birthFieldState.value.copy(error = updateProfileResult.macrosomicBabyError)
            }

            when (updateProfileResult?.result) {
                is Resource.Success -> {
                    markPlannerCheckInForQuestion(type)
                    triggerPredictionUpdate()
                }
                is Resource.Error -> {
                    _errorMessage.value = updateProfileResult.result.message ?: "Terjadi kesalahan saat memperbarui profil"
                    updateProfileResult.result.message?.let { Log.e("AddActivityViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat memperbarui profil"
                    Log.e("AddActivityViewModel", "Unexpected error")
                }
            }
        }
    }

    private fun parseProfileInt(
        value: String,
        fieldName: String,
        setError: (String) -> Unit
    ): Int? {
        val parsedValue = value.toIntOrNull()
        if (parsedValue == null) {
            val error = "$fieldName harus berupa angka yang valid"
            setError(error)
            _errorMessage.value = error
        }
        return parsedValue
    }

    private fun parseProfileBoolean(
        value: String,
        fieldName: String,
        setError: (String) -> Unit
    ): Boolean? {
        return when (value) {
            "true" -> true
            "false" -> false
            else -> {
                val error = "Pilihan $fieldName tidak valid"
                setError(error)
                _errorMessage.value = error
                null
            }
        }
    }

    private fun triggerPredictionUpdate() {
        predictionUseCases.predictBackground(pollingIntervalMs = 5000L)
        _successMessage.value = "Data berhasil disimpan dan prediksi akan diperbarui dalam beberapa saat."
    }

    private fun markPlannerCheckInForQuestion(questionType: String) {
        val checkInType = when (questionType) {
            "weight" -> CHECK_IN_WEIGHT
            "activity" -> CHECK_IN_ACTIVITY
            "hypertension" -> CHECK_IN_HYPERTENSION
            "cholesterol" -> CHECK_IN_CHOLESTEROL
            else -> return
        }
        val goal = activePlannerGoal.value
            ?.takeIf { it.status == PlannerGoalStatus.ACTIVE }
            ?: return

        viewModelScope.launch {
            plannerGoalUseCases.markPlannerCheckIn(goal.id, checkInType)
            plannerGoalUseCases.recordPlannerCheckIn(
                PlannerCheckInEntry(
                    id = "${goal.id}-$checkInType-${System.currentTimeMillis()}",
                    goalId = goal.id,
                    type = checkInType,
                    label = checkInLabel(checkInType),
                    valueText = checkInValueText(questionType),
                    note = checkInNote(checkInType),
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    private fun checkInLabel(checkInType: String): String {
        return when (checkInType) {
            CHECK_IN_WEIGHT -> "Berat"
            CHECK_IN_ACTIVITY -> "Aktivitas"
            CHECK_IN_HYPERTENSION -> "Hipertensi"
            CHECK_IN_CHOLESTEROL -> "Kolesterol"
            else -> "Check-in"
        }
    }

    private fun checkInValueText(questionType: String): String {
        return when (questionType) {
            "weight" -> "${weightFieldState.value.text} kg"
            "activity" -> if (workoutFieldState.value.text == "true") "Aktif hari ini" else "Tidak aktif hari ini"
            "hypertension" -> if (hypertensionFieldState.value.text == "true") "Ya" else "Tidak"
            "cholesterol" -> if (cholesterolFieldState.value.text == "true") "Ya" else "Tidak"
            else -> "-"
        }
    }

    private fun checkInNote(checkInType: String): String {
        return when (checkInType) {
            CHECK_IN_WEIGHT -> "Data berat diperbarui untuk memantau progres target berat badan."
            CHECK_IN_ACTIVITY -> "Aktivitas harian dicatat untuk memantau konsistensi minggu ini."
            CHECK_IN_HYPERTENSION -> "Status hipertensi diperbarui dari data kesehatan terbaru."
            CHECK_IN_CHOLESTEROL -> "Status kolesterol diperbarui dari data kesehatan terbaru."
            else -> "Check-in planner berhasil dicatat."
        }
    }

    // Helper Functions
    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onSuccessShown() {
        _successMessage.value = null
    }

    private companion object {
        const val CHECK_IN_WEIGHT = "weight"
        const val CHECK_IN_ACTIVITY = "activity"
        const val CHECK_IN_HYPERTENSION = "hypertension"
        const val CHECK_IN_CHOLESTEROL = "cholesterol"

        const val DAILY_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L
        const val WEEKLY_INTERVAL_MILLIS = 7L * DAILY_INTERVAL_MILLIS
        const val MONTHLY_INTERVAL_MILLIS = 30L * DAILY_INTERVAL_MILLIS
    }
}
