package com.itb.diabetify.presentation.settings

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.domain.usecases.auth.AuthUseCases
import com.itb.diabetify.domain.usecases.planner.PlannerGoalUseCases
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import com.itb.diabetify.domain.usecases.profile.ProfileUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.domain.usecases.notification.NotificationUseCases
import com.itb.diabetify.presentation.common.FieldState
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.PlannerUpdateNotifier
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val authUseCases: AuthUseCases,
    private val notificationUseCases: NotificationUseCases,
    private val profileUseCases: ProfileUseCases,
    private val predictionUseCases: PredictionUseCases,
    private val plannerGoalUseCases: PlannerGoalUseCases
): ViewModel() {
    // Navigation, Error, and Success States
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent: State<String?> = _navigationEvent

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Operational States
    private var _userState = mutableStateOf(DataState())
    val userState: State<DataState> = _userState

    private var _editProfileState = mutableStateOf(DataState())
    val editProfileState: State<DataState> = _editProfileState

    private var _logoutState = mutableStateOf(DataState())
    val logoutState: State<DataState> = _logoutState

    private var _profileState = mutableStateOf(DataState())
    val profileState: State<DataState> = _profileState

    private var _updateHealthProfileState = mutableStateOf(DataState())
    val updateHealthProfileState: State<DataState> = _updateHealthProfileState

    // UI States
    private val _dailyReminderEnabled = mutableStateOf(true)
    val dailyReminderEnabled: State<Boolean> = _dailyReminderEnabled

    private val _showLogoutDialog = mutableStateOf(false)
    val showLogoutDialog: State<Boolean> = _showLogoutDialog

    private val _showDatePicker = mutableStateOf(false)
    val showDatePicker: State<Boolean> = _showDatePicker

    // Field States
    private val _nameFieldState = mutableStateOf(FieldState())
    val nameFieldState: State<FieldState> = _nameFieldState

    private val _emailFieldState = mutableStateOf(FieldState())
    val emailFieldState: State<FieldState> = _emailFieldState

    private val _genderFieldState = mutableStateOf(FieldState())
    val genderFieldState: State<FieldState> = _genderFieldState

    private val _dobFieldState = mutableStateOf(FieldState())
    val dobFieldState: State<FieldState> = _dobFieldState

    private val _healthProfile = mutableStateOf(HealthProfileUiState())
    val healthProfile: State<HealthProfileUiState> = _healthProfile

    private val _isEditingHealthProfile = mutableStateOf(false)
    val isEditingHealthProfile: State<Boolean> = _isEditingHealthProfile

    private val _healthWeightFieldState = mutableStateOf(FieldState())
    val healthWeightFieldState: State<FieldState> = _healthWeightFieldState

    private val _healthHeightFieldState = mutableStateOf(FieldState())
    val healthHeightFieldState: State<FieldState> = _healthHeightFieldState

    private val _healthHypertensionFieldState = mutableStateOf(FieldState())
    val healthHypertensionFieldState: State<FieldState> = _healthHypertensionFieldState

    private val _healthSystolicFieldState = mutableStateOf(FieldState())
    val healthSystolicFieldState: State<FieldState> = _healthSystolicFieldState

    private val _healthDiastolicFieldState = mutableStateOf(FieldState())
    val healthDiastolicFieldState: State<FieldState> = _healthDiastolicFieldState

    private val _healthCholesterolFieldState = mutableStateOf(FieldState())
    val healthCholesterolFieldState: State<FieldState> = _healthCholesterolFieldState

    private val _healthBloodlineFieldState = mutableStateOf(FieldState())
    val healthBloodlineFieldState: State<FieldState> = _healthBloodlineFieldState

    private val _healthMacrosomicFieldState = mutableStateOf(FieldState())
    val healthMacrosomicFieldState: State<FieldState> = _healthMacrosomicFieldState

    private val _healthSmokingStatusFieldState = mutableStateOf(FieldState())
    val healthSmokingStatusFieldState: State<FieldState> = _healthSmokingStatusFieldState

    private val _healthSmokingStartAgeFieldState = mutableStateOf(FieldState())
    val healthSmokingStartAgeFieldState: State<FieldState> = _healthSmokingStartAgeFieldState

    private val _healthSmokingStopAgeFieldState = mutableStateOf(FieldState())
    val healthSmokingStopAgeFieldState: State<FieldState> = _healthSmokingStopAgeFieldState

    private val _healthSmokingBaselineFieldState = mutableStateOf(FieldState())
    val healthSmokingBaselineFieldState: State<FieldState> = _healthSmokingBaselineFieldState

    private val _activePlannerGoal = mutableStateOf<PlannerGoal?>(null)
    private var savedDobDisplay: String = ""

    // Initialization
    init {
        collectUserData()
        collectActivePlannerGoal()
        loadNotificationPreferences()
        loadProfileData()
    }

    // Setters for UI States
    fun setDailyReminderEnabled(enabled: Boolean) {
        _dailyReminderEnabled.value = enabled
        notificationUseCases.setNotificationPreferences(enabled)
    }

    fun setShowLogoutDialog(show: Boolean) {
        _showLogoutDialog.value = show
    }

    fun setShowDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    // Setters for Field States
    fun setName(value: String) {
        _nameFieldState.value = nameFieldState.value.copy(error = null)
        _nameFieldState.value = nameFieldState.value.copy(text = value)
    }

    fun setEmail(value: String) {
        _emailFieldState.value = emailFieldState.value.copy(error = null)
        _emailFieldState.value = emailFieldState.value.copy(text = value)
    }

    fun setGender(value: String) {
        _genderFieldState.value = genderFieldState.value.copy(error = null)
        _genderFieldState.value = genderFieldState.value.copy(text = value)
    }

    fun setDob(value: String) {
        _dobFieldState.value = dobFieldState.value.copy(error = null)
        _dobFieldState.value = dobFieldState.value.copy(text = value)
    }

    fun setEditingHealthProfile(isEditing: Boolean) {
        _isEditingHealthProfile.value = isEditing
        if (!isEditing) {
            resetHealthProfileFields()
        }
    }

    fun setHealthWeight(value: String) {
        _healthWeightFieldState.value = healthWeightFieldState.value.copy(text = value, error = null)
    }

    fun setHealthHeight(value: String) {
        _healthHeightFieldState.value = healthHeightFieldState.value.copy(text = value, error = null)
    }

    fun setHealthHypertension(value: String) {
        _healthHypertensionFieldState.value = healthHypertensionFieldState.value.copy(text = value, error = null)
    }

    fun setHealthSystolic(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(3)
        _healthSystolicFieldState.value = healthSystolicFieldState.value.copy(text = sanitized, error = null)
    }

    fun setHealthDiastolic(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(3)
        _healthDiastolicFieldState.value = healthDiastolicFieldState.value.copy(text = sanitized, error = null)
    }

    fun setHealthCholesterol(value: String) {
        _healthCholesterolFieldState.value = healthCholesterolFieldState.value.copy(text = value, error = null)
    }

    fun setHealthBloodline(value: String) {
        _healthBloodlineFieldState.value = healthBloodlineFieldState.value.copy(text = value, error = null)
    }

    fun setHealthMacrosomic(value: String) {
        _healthMacrosomicFieldState.value = healthMacrosomicFieldState.value.copy(text = value, error = null)
    }

    fun setHealthSmokingStatus(value: String) {
        _healthSmokingStatusFieldState.value = healthSmokingStatusFieldState.value.copy(text = value, error = null)
        if (value == "Tidak Pernah") {
            _healthSmokingStartAgeFieldState.value = FieldState(text = "")
            _healthSmokingStopAgeFieldState.value = FieldState(text = "")
            _healthSmokingBaselineFieldState.value = FieldState(text = "")
        } else if (value == "Sudah Berhenti") {
            _healthSmokingBaselineFieldState.value = FieldState(text = "")
        } else if (value == "Masih Merokok") {
            _healthSmokingStopAgeFieldState.value = FieldState(text = "")
        }
    }

    fun setHealthSmokingStartAge(value: String) {
        _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(text = value, error = null)
    }

    fun setHealthSmokingStopAge(value: String) {
        _healthSmokingStopAgeFieldState.value = healthSmokingStopAgeFieldState.value.copy(text = value, error = null)
    }

    fun setHealthSmokingBaseline(value: String) {
        _healthSmokingBaselineFieldState.value = healthSmokingBaselineFieldState.value.copy(text = value, error = null)
    }

    // Validation Functions
    fun validateEditProfileFields(): Boolean {
        val name = nameFieldState.value.text
        val email = emailFieldState.value.text
        val gender = genderFieldState.value.text
        val dob = dobFieldState.value.text

        var isValid = true

        if (name.isEmpty()) {
            _nameFieldState.value = nameFieldState.value.copy(error = "Nama tidak boleh kosong")
            isValid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailFieldState.value = emailFieldState.value.copy(error = "Email tidak valid")
            isValid = false
        }

        if (gender.isEmpty()) {
            _genderFieldState.value = genderFieldState.value.copy(error = "Jenis kelamin tidak boleh kosong")
            isValid = false
        }

        if (dob.isEmpty()) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Tanggal lahir tidak boleh kosong")
            isValid = false
        } else if (formatDobForRequest(dob) == null) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Format tanggal lahir harus dd/MM/yyyy")
            isValid = false
        }

        return isValid
    }

    fun validateHealthProfileFields(): Boolean {
        var isValid = true

        val weight = healthWeightFieldState.value.text.toIntOrNull()
        if (weight == null || weight < 30 || weight > 300) {
            _healthWeightFieldState.value = healthWeightFieldState.value.copy(error = "Berat badan harus antara 30-300 kg")
            isValid = false
        }

        val height = healthHeightFieldState.value.text.toIntOrNull()
        if (height == null || height < 100 || height > 250) {
            _healthHeightFieldState.value = healthHeightFieldState.value.copy(error = "Tinggi badan harus antara 100-250 cm")
            isValid = false
        }

        if (healthHypertensionFieldState.value.text !in listOf("Ya", "Tidak")) {
            _healthHypertensionFieldState.value = healthHypertensionFieldState.value.copy(error = "Pilih status hipertensi")
            isValid = false
        }

        val systolicText = healthSystolicFieldState.value.text
        val diastolicText = healthDiastolicFieldState.value.text
        val hasAnyBloodPressureInput = systolicText.isNotBlank() || diastolicText.isNotBlank()
        if (hasAnyBloodPressureInput) {
            val systolic = systolicText.toIntOrNull()
            val diastolic = diastolicText.toIntOrNull()

            if (systolic == null || systolic !in 70..250) {
                _healthSystolicFieldState.value = healthSystolicFieldState.value.copy(error = "Sistolik harus antara 70-250 mmHg")
                isValid = false
            }

            if (diastolic == null || diastolic !in 40..150) {
                _healthDiastolicFieldState.value = healthDiastolicFieldState.value.copy(error = "Diastolik harus antara 40-150 mmHg")
                isValid = false
            }

            if (systolic != null && diastolic != null) {
                val hasHypertension = systolic >= 140 || diastolic >= 90
                _healthHypertensionFieldState.value = healthHypertensionFieldState.value.copy(
                    text = if (hasHypertension) "Ya" else "Tidak",
                    error = null
                )
            }
        }

        if (healthCholesterolFieldState.value.text !in listOf("Ya", "Tidak")) {
            _healthCholesterolFieldState.value = healthCholesterolFieldState.value.copy(error = "Pilih status kolesterol")
            isValid = false
        }

        if (healthBloodlineFieldState.value.text !in listOf("Ya", "Tidak")) {
            _healthBloodlineFieldState.value = healthBloodlineFieldState.value.copy(error = "Pilih riwayat keluarga")
            isValid = false
        }

        val validMacrosomicOptions = listOf("Tidak", "Pernah", "Tidak Pernah Melahirkan")
        if (healthMacrosomicFieldState.value.text !in validMacrosomicOptions) {
            _healthMacrosomicFieldState.value = healthMacrosomicFieldState.value.copy(error = "Pilih riwayat makrosomia")
            isValid = false
        }

        when (healthSmokingStatusFieldState.value.text) {
            "Tidak Pernah" -> Unit
            "Masih Merokok" -> {
                val startAge = healthSmokingStartAgeFieldState.value.text.toIntOrNull()
                val baseline = healthSmokingBaselineFieldState.value.text.toIntOrNull()
                val currentAge = currentUserAge()
                if (startAge == null || startAge !in 10..80) {
                    _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(error = "Usia mulai merokok harus 10-80 tahun")
                    isValid = false
                }
                if (startAge != null && currentAge != null && startAge > currentAge) {
                    _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(error = "Usia mulai tidak boleh melebihi usia saat ini")
                    isValid = false
                }
                if (baseline == null || baseline !in 1..60) {
                    _healthSmokingBaselineFieldState.value = healthSmokingBaselineFieldState.value.copy(error = "Baseline rokok harus 1-60 batang")
                    isValid = false
                }
            }
            "Sudah Berhenti" -> {
                val startAge = healthSmokingStartAgeFieldState.value.text.toIntOrNull()
                val stopAge = healthSmokingStopAgeFieldState.value.text.toIntOrNull()
                val currentAge = currentUserAge()
                if (startAge == null || startAge !in 10..80) {
                    _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(error = "Usia mulai merokok harus 10-80 tahun")
                    isValid = false
                }
                if (startAge != null && currentAge != null && startAge > currentAge) {
                    _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(error = "Usia mulai tidak boleh melebihi usia saat ini")
                    isValid = false
                }
                if (stopAge == null || stopAge !in 10..80) {
                    _healthSmokingStopAgeFieldState.value = healthSmokingStopAgeFieldState.value.copy(error = "Usia berhenti merokok harus 10-80 tahun")
                    isValid = false
                }
                if (stopAge != null && currentAge != null && stopAge > currentAge) {
                    _healthSmokingStopAgeFieldState.value = healthSmokingStopAgeFieldState.value.copy(error = "Usia berhenti tidak boleh melebihi usia saat ini")
                    isValid = false
                }
                if (startAge != null && stopAge != null && stopAge <= startAge) {
                    _healthSmokingStopAgeFieldState.value = healthSmokingStopAgeFieldState.value.copy(error = "Usia berhenti harus lebih besar dari usia mulai")
                    isValid = false
                }
            }
            else -> {
                _healthSmokingStatusFieldState.value = healthSmokingStatusFieldState.value.copy(error = "Pilih status merokok")
                isValid = false
            }
        }

        return isValid
    }

    // Use Case Calls
    private fun loadNotificationPreferences() {
        _dailyReminderEnabled.value = notificationUseCases.getNotificationPreferences()
    }

    private fun collectUserData() {
        viewModelScope.launch {
            _userState.value = userState.value.copy(isLoading = true)

            userUseCases.getUserRepository().onEach { user ->
                _userState.value = userState.value.copy(isLoading = false)

                user?.let {
                    _nameFieldState.value = nameFieldState.value.copy(
                        text = it.name,
                        error = null
                    )
                    _emailFieldState.value = emailFieldState.value.copy(
                        text = it.email,
                        error = null
                    )
                    _genderFieldState.value = genderFieldState.value.copy(
                        text = it.gender,
                        error = null
                    )
                    _dobFieldState.value = dobFieldState.value.copy(
                        text = it.dob,
                        error = null
                    )
                    savedDobDisplay = it.dob
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
                is Resource.Success -> collectProfileData()
                is Resource.Error -> {
                    _errorMessage.value = getProfileResult.result.message ?: "Terjadi kesalahan saat mengambil profil kesehatan"
                    getProfileResult.result.message?.let { Log.e("SettingsViewModel", it) }
                }
                else -> {
                    _errorMessage.value = "Terjadi kesalahan saat mengambil profil kesehatan"
                    Log.e("SettingsViewModel", "Unexpected error loading health profile")
                }
            }
        }
    }

    private fun collectProfileData() {
        viewModelScope.launch {
            _profileState.value = profileState.value.copy(isLoading = true)

            profileUseCases.getProfileRepository().onEach { profile ->
                _profileState.value = profileState.value.copy(isLoading = false)

                profile?.let {
                    _healthProfile.value = HealthProfileUiState(
                        weight = it.weight,
                        height = it.height,
                        bmi = it.bmi,
                        hypertension = it.hypertension,
                        cholesterol = it.cholesterol,
                        bloodline = it.bloodline,
                        macrosomicBaby = it.macrosomicBaby,
                        smokingStatus = it.smoking,
                        smokeCount = it.smokeCount,
                        ageOfSmoking = it.ageOfSmoking,
                        ageOfStopSmoking = it.ageOfStopSmoking,
                        hasProfile = true
                    )
                    resetHealthProfileFields()
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun resetHealthProfileFields() {
        val profile = healthProfile.value
        _healthWeightFieldState.value = FieldState(text = profile.weight.takeIf { it > 0 }?.toString().orEmpty())
        _healthHeightFieldState.value = FieldState(text = profile.height.takeIf { it > 0 }?.toString().orEmpty())
        _healthHypertensionFieldState.value = FieldState(text = if (profile.hypertension) "Ya" else "Tidak")
        _healthSystolicFieldState.value = FieldState()
        _healthDiastolicFieldState.value = FieldState()
        _healthCholesterolFieldState.value = FieldState(text = if (profile.cholesterol) "Ya" else "Tidak")
        _healthBloodlineFieldState.value = FieldState(text = if (profile.bloodline) "Ya" else "Tidak")
        _healthMacrosomicFieldState.value = FieldState(text = when (profile.macrosomicBaby) {
            0 -> "Tidak"
            1 -> "Pernah"
            else -> "Tidak Pernah Melahirkan"
        })
        _healthSmokingStatusFieldState.value = FieldState(text = smokingStatusLabel(profile.smokingStatus))
        _healthSmokingStartAgeFieldState.value = FieldState(text = profile.ageOfSmoking.takeIf { it > 0 }?.toString().orEmpty())
        _healthSmokingStopAgeFieldState.value = FieldState(text = profile.ageOfStopSmoking.takeIf { it > 0 }?.toString().orEmpty())
        _healthSmokingBaselineFieldState.value = FieldState(text = profile.smokeCount.takeIf { it > 0 }?.toString().orEmpty())
    }

    fun editProfile() {
        if (!validateEditProfileFields()) {
            return
        }

        val dobFormatted = formatDobForRequest(dobFieldState.value.text)
        if (dobFormatted == null) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Format tanggal lahir harus dd/MM/yyyy")
            _errorMessage.value = "Format tanggal lahir harus dd/MM/yyyy"
            return
        }

        viewModelScope.launch {
            _editProfileState.value = editProfileState.value.copy(isLoading = true)

            val gender = genderFieldState.value.text
            val genderFormatted = if (gender == "Laki-laki") { "male" } else { "female" }
            val previousAge = ageFromDisplayDob(savedDobDisplay)
            val updatedAge = ageFromDisplayDob(dobFieldState.value.text)

            val editUserResult = userUseCases.editUser(
                name = nameFieldState.value.text,
                email = emailFieldState.value.text,
                dob = dobFormatted,
                gender = genderFormatted
            )

            _editProfileState.value = editProfileState.value.copy(isLoading = false)

            if (editUserResult.nameError != null) {
                _nameFieldState.value = nameFieldState.value.copy(error = editUserResult.nameError)
            }

            if (editUserResult.emailError != null) {
                _emailFieldState.value = emailFieldState.value.copy(error = editUserResult.emailError)
            }

            if (editUserResult.genderError != null) {
                _genderFieldState.value = genderFieldState.value.copy(error = editUserResult.genderError)
            }

            if (editUserResult.dobError != null) {
                _dobFieldState.value = dobFieldState.value.copy(error = editUserResult.dobError)
            }

            when (editUserResult.result) {
                is Resource.Success -> {
                    if (previousAge != null && updatedAge != null && previousAge != updatedAge) {
                        triggerPredictionUpdate()
                    }
                    _successMessage.value = "Profil berhasil diperbarui"
                    Log.d("SettingsViewModel", "Profile updated successfully")
                }
                is Resource.Error -> {
                    _errorMessage.value = editUserResult.result.message ?: "Terjadi kesalahan saat memperbarui profil"
                    editUserResult.result.message?.let { Log.e("SettingsViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat memperbarui profil"
                    Log.e("SettingsViewModel", "Unexpected error")
                }
            }
        }
    }

    private fun formatDobForRequest(displayDob: String): String? {
        if (displayDob.split("/").size != 3) {
            return null
        }

        return try {
            val displayFormatter = SimpleDateFormat(DISPLAY_DOB_PATTERN, Locale.US).apply {
                isLenient = false
            }
            val requestFormatter = SimpleDateFormat(REQUEST_DOB_PATTERN, Locale.US)
            val parsedDate = displayFormatter.parse(displayDob) ?: return null
            requestFormatter.format(parsedDate)
        } catch (_: ParseException) {
            null
        }
    }

    private fun currentUserAge(): Int? {
        return ageFromDisplayDob(dobFieldState.value.text)
    }

    private fun ageFromDisplayDob(dob: String): Int? {
        if (dob.isBlank()) return null

        return try {
            val formatter = SimpleDateFormat(DISPLAY_DOB_PATTERN, Locale.US).apply {
                isLenient = false
            }
            val birthDate = formatter.parse(dob) ?: return null
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply {
                time = birthDate
            }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (_: ParseException) {
            null
        }
    }

    fun saveHealthProfile(
        section: HealthProfileSaveSection,
        onSuccess: (() -> Unit)? = null
    ) {
        if (!validateHealthProfileFields()) {
            return
        }

        viewModelScope.launch {
            _updateHealthProfileState.value = updateHealthProfileState.value.copy(isLoading = true)
            val updatedWeight = healthWeightFieldState.value.text.toInt()
            val updatedHeight = healthHeightFieldState.value.text.toInt()
            val updatedHypertension = healthHypertensionFieldState.value.text == "Ya"
            val updatedCholesterol = healthCholesterolFieldState.value.text == "Ya"
            val updatedSmoking = when (healthSmokingStatusFieldState.value.text) {
                "Tidak Pernah" -> 0
                "Sudah Berhenti" -> 1
                else -> 2
            }
            val updatedAgeOfSmoking = when (healthSmokingStatusFieldState.value.text) {
                "Tidak Pernah" -> 0
                else -> healthSmokingStartAgeFieldState.value.text.toInt()
            }
            val updatedAgeOfStopSmoking = when (healthSmokingStatusFieldState.value.text) {
                "Sudah Berhenti" -> healthSmokingStopAgeFieldState.value.text.toInt()
                else -> 0
            }
            val updatedSmokeCount = when (healthSmokingStatusFieldState.value.text) {
                "Masih Merokok" -> healthSmokingBaselineFieldState.value.text.toInt()
                else -> 0
            }

            val updateProfileResult = profileUseCases.updateProfile(
                weight = updatedWeight,
                height = updatedHeight,
                hypertension = updatedHypertension,
                macrosomicBaby = when (healthMacrosomicFieldState.value.text) {
                    "Tidak" -> 0
                    "Pernah" -> 1
                    else -> 2
                },
                bloodline = healthBloodlineFieldState.value.text == "Ya",
                cholesterol = updatedCholesterol,
                smoking = updatedSmoking,
                ageOfSmoking = updatedAgeOfSmoking,
                ageOfStopSmoking = updatedAgeOfStopSmoking,
                smokeCount = updatedSmokeCount
            )

            _updateHealthProfileState.value = updateHealthProfileState.value.copy(isLoading = false)

            if (updateProfileResult.weightError != null) {
                _healthWeightFieldState.value = healthWeightFieldState.value.copy(error = updateProfileResult.weightError)
            }

            if (updateProfileResult.heightError != null) {
                _healthHeightFieldState.value = healthHeightFieldState.value.copy(error = updateProfileResult.heightError)
            }

            if (updateProfileResult.macrosomicBabyError != null) {
                _healthMacrosomicFieldState.value = healthMacrosomicFieldState.value.copy(error = updateProfileResult.macrosomicBabyError)
            }

            if (updateProfileResult.smokingError != null) {
                _healthSmokingStatusFieldState.value = healthSmokingStatusFieldState.value.copy(error = updateProfileResult.smokingError)
            }

            if (updateProfileResult.ageOfSmokingError != null) {
                _healthSmokingStartAgeFieldState.value = healthSmokingStartAgeFieldState.value.copy(error = updateProfileResult.ageOfSmokingError)
            }

            if (updateProfileResult.ageOfStopSmokingError != null) {
                _healthSmokingStopAgeFieldState.value = healthSmokingStopAgeFieldState.value.copy(error = updateProfileResult.ageOfStopSmokingError)
            }

            if (updateProfileResult.smokeCountError != null) {
                _healthSmokingBaselineFieldState.value = healthSmokingBaselineFieldState.value.copy(error = updateProfileResult.smokeCountError)
            }

            when (updateProfileResult.result) {
                is Resource.Success -> {
                    triggerPredictionUpdate()
                    markHealthProfilePlannerCheckIns(
                        section = section,
                        updatedWeight = updatedWeight,
                        updatedHypertension = updatedHypertension,
                        updatedCholesterol = updatedCholesterol,
                        updatedSmoking = updatedSmoking
                    )
                    PlannerUpdateNotifier.notifyPlannerUpdated()
                    _successMessage.value = "Profil kesehatan berhasil diperbaharui"
                    _isEditingHealthProfile.value = false
                    onSuccess?.invoke()
                }
                is Resource.Error -> {
                    _errorMessage.value = updateProfileResult.result.message ?: "Terjadi kesalahan saat memperbarui profil kesehatan"
                    updateProfileResult.result.message?.let { Log.e("SettingsViewModel", it) }
                }
                else -> {
                    _errorMessage.value = "Terjadi kesalahan saat memperbarui profil kesehatan"
                    Log.e("SettingsViewModel", "Unexpected error updating health profile")
                }
            }
        }
    }

    private fun triggerPredictionUpdate() {
        predictionUseCases.predictBackground(pollingIntervalMs = 5000L)
    }

    private fun collectActivePlannerGoal() {
        plannerGoalUseCases.getActivePlannerGoal()
            .onEach { goal ->
                _activePlannerGoal.value = goal
            }
            .launchIn(viewModelScope)
    }

    private suspend fun markHealthProfilePlannerCheckIns(
        section: HealthProfileSaveSection,
        updatedWeight: Int,
        updatedHypertension: Boolean,
        updatedCholesterol: Boolean,
        updatedSmoking: Int
    ) {
        val goal = _activePlannerGoal.value
            ?.takeIf { it.status == PlannerGoalStatus.ACTIVE }
            ?: return
        val featureNames = goal.features.map { it.featureName }.toSet()

        if (section == HealthProfileSaveSection.BODY && "BMI" in featureNames) {
            recordPlannerCheckIn(
                goal = goal,
                checkInType = CHECK_IN_WEIGHT,
                label = "Berat",
                valueText = "$updatedWeight kg",
                note = "Data berat diperbarui dari profil kesehatan untuk memantau progres target berat badan."
            )
        }

        if (section == HealthProfileSaveSection.CLINICAL && "is_hypertension" in featureNames) {
            recordPlannerCheckIn(
                goal = goal,
                checkInType = CHECK_IN_HYPERTENSION,
                label = "Hipertensi",
                valueText = if (updatedHypertension) "Ya" else "Tidak",
                note = "Status hipertensi diperbarui dari profil kesehatan."
            )
        }

        if (section == HealthProfileSaveSection.CLINICAL && "is_cholesterol" in featureNames) {
            recordPlannerCheckIn(
                goal = goal,
                checkInType = CHECK_IN_CHOLESTEROL,
                label = "Kolesterol",
                valueText = if (updatedCholesterol) "Ya" else "Tidak",
                note = "Status kolesterol diperbarui dari profil kesehatan."
            )
        }

        if (
            section == HealthProfileSaveSection.SMOKING &&
            "smoking_status" in featureNames &&
            updatedSmoking in 1..2
        ) {
            recordPlannerCheckIn(
                goal = goal,
                checkInType = CHECK_IN_SMOKING,
                label = "Status Merokok",
                valueText = if (updatedSmoking == 1) "Sudah Berhenti" else "Masih aktif",
                note = "Status merokok diperbarui dari profil kesehatan."
            )
        }
    }

    private suspend fun recordPlannerCheckIn(
        goal: PlannerGoal,
        checkInType: String,
        label: String,
        valueText: String,
        note: String
    ) {
        val timestampMillis = System.currentTimeMillis()
        plannerGoalUseCases.markPlannerCheckIn(goal.id, checkInType)
        plannerGoalUseCases.recordPlannerCheckIn(
            PlannerCheckInEntry(
                id = "${goal.id}-$checkInType-$timestampMillis",
                goalId = goal.id,
                type = checkInType,
                label = label,
                valueText = valueText,
                note = note,
                createdAtMillis = timestampMillis
            )
        )
        PlannerUpdateNotifier.notifyPlannerUpdated()
    }

    fun determineHypertensionFromBloodPressure(): Boolean {
        val systolic = healthSystolicFieldState.value.text.toIntOrNull()
        val diastolic = healthDiastolicFieldState.value.text.toIntOrNull()
        var isValid = true

        if (systolic == null || systolic !in 70..250) {
            _healthSystolicFieldState.value = healthSystolicFieldState.value.copy(error = "Sistolik harus antara 70-250 mmHg")
            isValid = false
        }

        if (diastolic == null || diastolic !in 40..150) {
            _healthDiastolicFieldState.value = healthDiastolicFieldState.value.copy(error = "Diastolik harus antara 40-150 mmHg")
            isValid = false
        }

        if (!isValid || systolic == null || diastolic == null) {
            return false
        }

        val hasHypertension = systolic >= 140 || diastolic >= 90
        _healthHypertensionFieldState.value = healthHypertensionFieldState.value.copy(
            text = if (hasHypertension) "Ya" else "Tidak",
            error = null
        )
        return true
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = logoutState.value.copy(isLoading = true)

            val logoutResult = authUseCases.logout()

            _logoutState.value = logoutState.value.copy(isLoading = false)

            when (logoutResult.result) {
                is Resource.Success -> {
                    _navigationEvent.value = "LOGIN_SCREEN"
                }
                is Resource.Error -> {
                    _errorMessage.value = logoutResult.result.message ?: "Terjadi kesalahan saat logout"
                    logoutResult.result.message?.let { Log.e("SettingsViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat logout"
                    Log.e("SettingsViewModel", "Unexpected error")
                }
            }
        }
    }

    // Helper Functions
    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onSuccessShown() {
        _successMessage.value = null
    }

    private companion object {
        const val CHECK_IN_WEIGHT = "weight"
        const val CHECK_IN_HYPERTENSION = "hypertension"
        const val CHECK_IN_CHOLESTEROL = "cholesterol"
        const val CHECK_IN_SMOKING = "smoking"
    }
}

enum class HealthProfileSaveSection {
    BODY,
    CLINICAL,
    SMOKING
}

data class HealthProfileUiState(
    val weight: Int = 0,
    val height: Int = 0,
    val bmi: Double = 0.0,
    val hypertension: Boolean = false,
    val cholesterol: Boolean = false,
    val bloodline: Boolean = false,
    val macrosomicBaby: Int = 0,
    val smokingStatus: Int = 0,
    val smokeCount: Int = 0,
    val ageOfSmoking: Int = 0,
    val ageOfStopSmoking: Int = 0,
    val hasProfile: Boolean = false
)

private fun smokingStatusLabel(value: Int): String = when (value) {
    0 -> "Tidak Pernah"
    1 -> "Sudah Berhenti"
    2 -> "Masih Merokok"
    else -> ""
}

private const val DISPLAY_DOB_PATTERN = "dd/MM/yyyy"
private const val REQUEST_DOB_PATTERN = "yyyy-MM-dd"
