package com.itb.diabetify.presentation.register

import android.util.Log
import android.util.Patterns
import com.itb.diabetify.presentation.common.FieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.usecases.auth.AuthUseCases
import com.itb.diabetify.util.DataState
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
): ViewModel() {
    // Navigation, Error, and Success States
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent: State<String?> = _navigationEvent

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Operational States
    private var _createAccountState = mutableStateOf(DataState())
    val createAccountState: State<DataState> = _createAccountState

    private var _sendVerificationState = mutableStateOf(DataState())
    val sendVerificationState: State<DataState> = _sendVerificationState

    private var _verifyOtpState = mutableStateOf(DataState())
    val verifyOtpState: State<DataState> = _verifyOtpState

    // UI States
    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    private val _privacyPolicyChecked = mutableStateOf(false)
    val privacyPolicyChecked: State<Boolean> = _privacyPolicyChecked

    private val _showDatePicker = mutableStateOf(false)
    val showDatePicker: State<Boolean> = _showDatePicker

    // Field States
    private val _nameFieldState = mutableStateOf(FieldState())
    val nameFieldState: State<FieldState> = _nameFieldState

    private val _emailFieldState = mutableStateOf(FieldState())
    val emailFieldState: State<FieldState> = _emailFieldState

    private val _passwordFieldState = mutableStateOf(FieldState())
    val passwordFieldState: State<FieldState> = _passwordFieldState

    private val _genderFieldState = mutableStateOf(FieldState())
    val genderFieldState: State<FieldState> = _genderFieldState

    private val _dobFieldState = mutableStateOf(FieldState())
    val dobFieldState: State<FieldState> = _dobFieldState

    private val _otpFieldState = mutableStateOf(FieldState())
    val otpFieldState: State<FieldState> = _otpFieldState

    // Setters for UI States
    fun setPrivacyPolicy(value: Boolean) {
        _privacyPolicyChecked.value = value
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun toggleDatePicker() {
        _showDatePicker.value = !_showDatePicker.value
    }

    // Setters for Field States
    fun setName(value: String) {
        _nameFieldState.value = nameFieldState.value.copy(error = null)
        _nameFieldState.value = nameFieldState.value.copy(text = value)
        _name.value = value
    }

    fun setEmail(value: String) {
        _emailFieldState.value = emailFieldState.value.copy(error = null)
        _emailFieldState.value = emailFieldState.value.copy(text = value)
    }

    fun setPassword(value: String) {
        _passwordFieldState.value = passwordFieldState.value.copy(error = null)
        _passwordFieldState.value = passwordFieldState.value.copy(text = value)
    }

    fun setGender(value: String) {
        _genderFieldState.value = genderFieldState.value.copy(error = null)
        _genderFieldState.value = genderFieldState.value.copy(text = value)
    }

    fun setDob(value: String) {
        _dobFieldState.value = dobFieldState.value.copy(error = null)
        _dobFieldState.value = dobFieldState.value.copy(text = value)
    }

    fun setOtp(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _otpFieldState.value = otpFieldState.value.copy(error = null)
            _otpFieldState.value = otpFieldState.value.copy(text = value)
        }
    }

    // Validation Functions
    fun validateRegisterFields(): Boolean {
        val name = nameFieldState.value.text
        val email = emailFieldState.value.text
        val password = passwordFieldState.value.text

        var isValid = true

        if (name.isEmpty()) {
            _nameFieldState.value = nameFieldState.value.copy(error = "Nama tidak boleh kosong")
            isValid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailFieldState.value = emailFieldState.value.copy(error = "Email tidak valid")
            isValid = false
        }

        if (password.length < 8) {
            _passwordFieldState.value = passwordFieldState.value.copy(error = "Kata sandi harus lebih dari 8 karakter")
            isValid = false
        }

        return isValid
    }

    fun validateBiodataFields(): Boolean {
        val gender = genderFieldState.value.text
        val birthDate = dobFieldState.value.text

        var isValid = true

        if (gender.isEmpty()) {
            _genderFieldState.value = genderFieldState.value.copy(error = "Jenis kelamin tidak boleh kosong")
            isValid = false
        }

        if (birthDate.isEmpty()) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Tanggal lahir tidak boleh kosong")
            isValid = false
        } else if (formatDobForRequest(birthDate) == null) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Format tanggal lahir harus dd/MM/yyyy")
            isValid = false
        }

        return isValid
    }

    fun validateOtpFields(): Boolean {
        val code = otpFieldState.value.text

        var isValid = true

        if (code.isEmpty()) {
            _otpFieldState.value = otpFieldState.value.copy(error = "Kode tidak boleh kosong")
            isValid = false
        }

        if (code.length != 6) {
            _otpFieldState.value = otpFieldState.value.copy(error = "Kode harus 6 digit")
            isValid = false
        }

        return isValid
    }

    // Use Case Calls
    fun createAccount() {
        if (!validateRegisterFields() || !validateBiodataFields()) {
            return
        }

        val dobFormatted = formatDobForRequest(dobFieldState.value.text)
        if (dobFormatted == null) {
            _dobFieldState.value = dobFieldState.value.copy(error = "Format tanggal lahir harus dd/MM/yyyy")
            _errorMessage.value = "Format tanggal lahir harus dd/MM/yyyy"
            return
        }

        viewModelScope.launch {
            _createAccountState.value = createAccountState.value.copy(isLoading = true)

            val gender = genderFieldState.value.text
            val genderFormatted = if (gender == "Laki-laki") { "male" } else { "female" }

            val createAccountResult = authUseCases.createAccount(
                name = nameFieldState.value.text,
                email = emailFieldState.value.text,
                password = passwordFieldState.value.text,
                dob = dobFormatted,
                gender = genderFormatted,
            )

            _createAccountState.value = createAccountState.value.copy(isLoading = false)

            if (createAccountResult.nameError != null) {
                _nameFieldState.value = nameFieldState.value.copy(error = createAccountResult.nameError)
            }

            if (createAccountResult.emailError != null) {
                _emailFieldState.value = emailFieldState.value.copy(error = createAccountResult.emailError)
            }

            if (createAccountResult.passwordError != null) {
                _passwordFieldState.value = passwordFieldState.value.copy(error = createAccountResult.passwordError)
            }

            if (createAccountResult.dobError != null) {
                _dobFieldState.value = dobFieldState.value.copy(error = createAccountResult.dobError)
            }

            if (createAccountResult.genderError != null) {
                _genderFieldState.value = genderFieldState.value.copy(error = createAccountResult.genderError)
            }

            when (createAccountResult.result) {
                is Resource.Success -> {
                    sendVerification()
                }
                is Resource.Error -> {
                    _errorMessage.value = createAccountResult.result.message ?: "Terjadi kesalahan saat membuat akun"
                    createAccountResult.result.message?.let { Log.e("RegisterViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat membuat akun"
                    Log.e("RegisterViewModel", "Unexpected error")
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

    fun sendVerification(
        isResend: Boolean = false
    ) {
        viewModelScope.launch {
            _sendVerificationState.value = sendVerificationState.value.copy(isLoading = true)

            val sendVerificationResult = authUseCases.sendVerification(
                email = emailFieldState.value.text,
                type = "register"
            )

            _sendVerificationState.value = sendVerificationState.value.copy(isLoading = false)

            if (sendVerificationResult.emailError != null) {
                _emailFieldState.value = emailFieldState.value.copy(error = sendVerificationResult.emailError)
            }

            when (sendVerificationResult.result) {
                is Resource.Success -> {
                    if (isResend) {
                        _successMessage.value = "Kode OTP berhasil dikirim ulang"
                    } else {
                        _navigationEvent.value = "OTP_SCREEN"
                    }
                }
                is Resource.Error -> {
                    _errorMessage.value = sendVerificationResult.result.message ?: "Terjadi kesalahan saat mengirim verifikasi"
                    sendVerificationResult.result.message?.let { Log.e("RegisterViewModel", it) }
                }
                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat mengirim verifikasi"
                    Log.e("RegisterViewModel", "Unexpected error")
                }
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            _verifyOtpState.value = verifyOtpState.value.copy(isLoading = true)

            val verifyOtpResult = authUseCases.verifyOtp(
                email = emailFieldState.value.text,
                code = otpFieldState.value.text
            )

            _verifyOtpState.value = verifyOtpState.value.copy(isLoading = false)

            if (verifyOtpResult.emailError != null) {
                _emailFieldState.value = emailFieldState.value.copy(error = verifyOtpResult.emailError)
            }

            if (verifyOtpResult.codeError != null) {
                _otpFieldState.value = otpFieldState.value.copy(error = verifyOtpResult.codeError)
            }

            when (verifyOtpResult.result) {
                is Resource.Success -> {
                    resetValues()
                    _navigationEvent.value = "SUCCESS_SCREEN"
                }
                is Resource.Error -> {
                    _errorMessage.value = verifyOtpResult.result.message ?: "Terjadi kesalahan saat verifikasi OTP"
                    verifyOtpResult.result.message?.let { Log.e("RegisterViewModel", it) }
                }

                else -> {
                    // Handle unexpected error
                    _errorMessage.value = "Terjadi kesalahan saat verifikasi OTP"
                    Log.e("RegisterViewModel", "Unexpected error")
                }
            }
        }
    }

    // Helper Functions
    private fun resetValues() {
        _nameFieldState.value = FieldState()
        _emailFieldState.value = FieldState()
        _passwordFieldState.value = FieldState()
        _privacyPolicyChecked.value = false
        _genderFieldState.value = FieldState()
        _dobFieldState.value = FieldState()
        _otpFieldState.value = FieldState()
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onSuccessShown() {
        _successMessage.value = null
    }
}

private const val DISPLAY_DOB_PATTERN = "dd/MM/yyyy"
private const val REQUEST_DOB_PATTERN = "yyyy-MM-dd"
