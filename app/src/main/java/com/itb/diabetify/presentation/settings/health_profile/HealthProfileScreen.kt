package com.itb.diabetify.presentation.settings.health_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.DropdownField
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.InputField
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SecondaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.presentation.settings.HealthProfileUiState
import com.itb.diabetify.presentation.settings.SettingsViewModel
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun HealthProfileScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val healthProfile by viewModel.healthProfile
    val isLoading = viewModel.profileState.value.isLoading
    val isUpdating = viewModel.updateHealthProfileState.value.isLoading
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value
    val weightField by viewModel.healthWeightFieldState
    val heightField by viewModel.healthHeightFieldState
    val hypertensionField by viewModel.healthHypertensionFieldState
    val systolicField by viewModel.healthSystolicFieldState
    val diastolicField by viewModel.healthDiastolicFieldState
    val cholesterolField by viewModel.healthCholesterolFieldState
    val bloodlineField by viewModel.healthBloodlineFieldState
    val macrosomicField by viewModel.healthMacrosomicFieldState
    val smokingStatusField by viewModel.healthSmokingStatusFieldState
    val smokingStartAgeField by viewModel.healthSmokingStartAgeFieldState
    val smokingStopAgeField by viewModel.healthSmokingStopAgeFieldState
    val smokingBaselineField by viewModel.healthSmokingBaselineFieldState
    var editingSection by remember { mutableStateOf<HealthProfileSection?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = {
                        if (!navController.popBackStack(Route.SettingsScreen.route, inclusive = false)) {
                            navController.navigate(Route.SettingsScreen.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.primary)
                    )
                }

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Profil Kesehatan",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.primary)
                )
            }

            if (isLoading && !healthProfile.hasProfile) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.primary)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    if (!healthProfile.hasProfile) {
                        EmptyHealthProfileCard()
                    } else {
                        BodySectionCard(
                            healthProfile = healthProfile,
                            isEditing = editingSection == HealthProfileSection.Body,
                            weight = weightField.text,
                            onWeightChange = viewModel::setHealthWeight,
                            weightError = weightField.error,
                            height = heightField.text,
                            onHeightChange = viewModel::setHealthHeight,
                            heightError = heightField.error,
                            onEditClick = { editingSection = HealthProfileSection.Body },
                            onSave = {
                                viewModel.saveHealthProfile {
                                    editingSection = null
                                }
                            },
                            isSaving = isUpdating
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ClinicalSectionCard(
                            healthProfile = healthProfile,
                            isEditing = editingSection == HealthProfileSection.Clinical,
                            hypertension = hypertensionField.text,
                            onHypertensionChange = viewModel::setHealthHypertension,
                            hypertensionError = hypertensionField.error,
                            systolic = systolicField.text,
                            onSystolicChange = viewModel::setHealthSystolic,
                            systolicError = systolicField.error,
                            diastolic = diastolicField.text,
                            onDiastolicChange = viewModel::setHealthDiastolic,
                            diastolicError = diastolicField.error,
                            onDetermineHypertension = viewModel::determineHypertensionFromBloodPressure,
                            cholesterol = cholesterolField.text,
                            onCholesterolChange = viewModel::setHealthCholesterol,
                            cholesterolError = cholesterolField.error,
                            bloodline = bloodlineField.text,
                            onBloodlineChange = viewModel::setHealthBloodline,
                            bloodlineError = bloodlineField.error,
                            macrosomic = macrosomicField.text,
                            onMacrosomicChange = viewModel::setHealthMacrosomic,
                            macrosomicError = macrosomicField.error,
                            onEditClick = { editingSection = HealthProfileSection.Clinical },
                            onSave = {
                                viewModel.saveHealthProfile {
                                    editingSection = null
                                }
                            },
                            isSaving = isUpdating
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SmokingHistoryCard(
                            healthProfile = healthProfile,
                            isEditing = editingSection == HealthProfileSection.Smoking,
                            smokingStatus = smokingStatusField.text,
                            onSmokingStatusChange = viewModel::setHealthSmokingStatus,
                            smokingStatusError = smokingStatusField.error,
                            smokingStartAge = smokingStartAgeField.text,
                            onSmokingStartAgeChange = viewModel::setHealthSmokingStartAge,
                            smokingStartAgeError = smokingStartAgeField.error,
                            smokingStopAge = smokingStopAgeField.text,
                            onSmokingStopAgeChange = viewModel::setHealthSmokingStopAge,
                            smokingStopAgeError = smokingStopAgeField.error,
                            smokingBaseline = smokingBaselineField.text,
                            onSmokingBaselineChange = viewModel::setHealthSmokingBaseline,
                            smokingBaselineError = smokingBaselineField.error,
                            onEditClick = { editingSection = HealthProfileSection.Smoking },
                            onSave = {
                                viewModel.saveHealthProfile {
                                    editingSection = null
                                }
                            },
                            isSaving = isUpdating
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        ErrorNotification(
            showError = errorMessage != null,
            errorMessage = errorMessage,
            onDismiss = { viewModel.onErrorShown() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )

        SuccessNotification(
            showSuccess = successMessage != null,
            successMessage = successMessage,
            onDismiss = { viewModel.onSuccessShown() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )
    }
}

private enum class HealthProfileSection {
    Body,
    Clinical,
    Smoking
}

@Composable
private fun BodySectionCard(
    healthProfile: HealthProfileUiState,
    isEditing: Boolean,
    weight: String,
    onWeightChange: (String) -> Unit,
    weightError: String?,
    height: String,
    onHeightChange: (String) -> Unit,
    heightError: String?,
    onEditClick: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    HealthSectionScaffold(
        title = "Kondisi Tubuh",
        isEditing = isEditing,
        onEditClick = onEditClick
    ) {
        if (isEditing) {
            FieldLabel("Berat badan")
            InputField(
                value = weight,
                onValueChange = onWeightChange,
                placeholderText = "Berat badan",
                iconResId = R.drawable.ic_weight,
                keyboardType = KeyboardType.Number,
                isError = weightError != null,
                errorMessage = weightError ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Tinggi badan")
            InputField(
                value = height,
                onValueChange = onHeightChange,
                placeholderText = "Tinggi badan",
                iconResId = R.drawable.ic_height,
                keyboardType = KeyboardType.Number,
                isError = heightError != null,
                errorMessage = heightError ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))

            HealthInfoRow(
                item = HealthInfoItem("BMI", String.format("%.1f kg/m²", healthProfile.bmi), R.drawable.ic_scale)
            )

            SaveSectionButton(onClick = onSave, isSaving = isSaving)
        } else {
            HealthInfoRows(
                items = listOf(
                    HealthInfoItem("Berat badan", "${healthProfile.weight} kg", R.drawable.ic_weight),
                    HealthInfoItem("Tinggi badan", "${healthProfile.height} cm", R.drawable.ic_height),
                    HealthInfoItem("BMI", String.format("%.1f kg/m²", healthProfile.bmi), R.drawable.ic_scale)
                )
            )
        }
    }
}

@Composable
private fun ClinicalSectionCard(
    healthProfile: HealthProfileUiState,
    isEditing: Boolean,
    hypertension: String,
    onHypertensionChange: (String) -> Unit,
    hypertensionError: String?,
    systolic: String,
    onSystolicChange: (String) -> Unit,
    systolicError: String?,
    diastolic: String,
    onDiastolicChange: (String) -> Unit,
    diastolicError: String?,
    onDetermineHypertension: () -> Boolean,
    cholesterol: String,
    onCholesterolChange: (String) -> Unit,
    cholesterolError: String?,
    bloodline: String,
    onBloodlineChange: (String) -> Unit,
    bloodlineError: String?,
    macrosomic: String,
    onMacrosomicChange: (String) -> Unit,
    macrosomicError: String?,
    onEditClick: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    var isBloodPressureHelperVisible by remember { mutableStateOf(false) }

    HealthSectionScaffold(
        title = "Riwayat Klinis",
        isEditing = isEditing,
        onEditClick = onEditClick
    ) {
        if (isEditing) {
            if (isBloodPressureHelperVisible) {
                BloodPressureHelperCard(
                    systolic = systolic,
                    onSystolicChange = onSystolicChange,
                    systolicError = systolicError,
                    diastolic = diastolic,
                    onDiastolicChange = onDiastolicChange,
                    diastolicError = diastolicError,
                    onDetermine = {
                        if (onDetermineHypertension()) {
                            isBloodPressureHelperVisible = false
                        }
                    },
                    onCancel = {
                        onSystolicChange("")
                        onDiastolicChange("")
                        isBloodPressureHelperVisible = false
                    }
                )
            } else {
                FieldLabel("Hipertensi")
                DropdownField(
                    selectedOption = hypertension,
                    onOptionSelected = onHypertensionChange,
                    options = listOf("Ya", "Tidak"),
                    placeHolderText = "Pilih status hipertensi",
                    iconResId = R.drawable.ic_hypertension,
                    isError = hypertensionError != null,
                    errorMessage = hypertensionError ?: ""
                )

                Spacer(modifier = Modifier.height(10.dp))

                HelperTriggerButton(
                    text = "Bantu tentukan dari tekanan darah",
                    onClick = { isBloodPressureHelperVisible = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Kolesterol")
            DropdownField(
                selectedOption = cholesterol,
                onOptionSelected = onCholesterolChange,
                options = listOf("Ya", "Tidak"),
                placeHolderText = "Pilih status kolesterol",
                iconResId = R.drawable.ic_cholesterol,
                isError = cholesterolError != null,
                errorMessage = cholesterolError ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Riwayat keluarga")
            DropdownField(
                selectedOption = bloodline,
                onOptionSelected = onBloodlineChange,
                options = listOf("Ya", "Tidak"),
                placeHolderText = "Pilih riwayat keluarga",
                iconResId = R.drawable.ic_family,
                isError = bloodlineError != null,
                errorMessage = bloodlineError ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Riwayat bayi makrosomia")
            DropdownField(
                selectedOption = macrosomic,
                onOptionSelected = onMacrosomicChange,
                options = listOf("Tidak", "Pernah", "Tidak Pernah Melahirkan"),
                placeHolderText = "Pilih riwayat makrosomia",
                iconResId = R.drawable.ic_baby,
                isError = macrosomicError != null,
                errorMessage = macrosomicError ?: ""
            )

            SaveSectionButton(onClick = onSave, isSaving = isSaving)
        } else {
            HealthInfoRows(
                items = listOf(
                    HealthInfoItem("Hipertensi", yesNoLabel(healthProfile.hypertension), R.drawable.ic_hypertension),
                    HealthInfoItem("Kolesterol", yesNoLabel(healthProfile.cholesterol), R.drawable.ic_cholesterol),
                    HealthInfoItem("Riwayat keluarga", yesNoLabel(healthProfile.bloodline), R.drawable.ic_family),
                    HealthInfoItem("Riwayat bayi makrosomia", macrosomicLabel(healthProfile.macrosomicBaby), R.drawable.ic_baby)
                )
            )
        }
    }
}

@Composable
private fun HealthSectionScaffold(
    title: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colorResource(id = R.color.primary)
                )

                if (!isEditing) {
                    EditSectionButton(onClick = onEditClick)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EditSectionButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.primary).copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit section",
            tint = colorResource(id = R.color.primary),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SaveSectionButton(
    onClick: () -> Unit,
    isSaving: Boolean
) {
    Spacer(modifier = Modifier.height(18.dp))
    PrimaryButton(
        text = "Simpan Perubahan",
        onClick = onClick,
        isLoading = isSaving
    )
}

@Composable
private fun HealthInfoRows(
    items: List<HealthInfoItem>
) {
    items.forEachIndexed { index, item ->
        HealthInfoRow(item = item)
        if (index < items.lastIndex) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HealthInfoRow(item: HealthInfoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = item.iconResId),
                contentDescription = item.label,
                tint = colorResource(id = R.color.primary),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = item.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.padding(start = 10.dp, end = 8.dp)
            )
        }

        Text(
            text = item.value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun SmokingHistoryCard(
    healthProfile: HealthProfileUiState,
    isEditing: Boolean,
    smokingStatus: String,
    onSmokingStatusChange: (String) -> Unit,
    smokingStatusError: String?,
    smokingStartAge: String,
    onSmokingStartAgeChange: (String) -> Unit,
    smokingStartAgeError: String?,
    smokingStopAge: String,
    onSmokingStopAgeChange: (String) -> Unit,
    smokingStopAgeError: String?,
    smokingBaseline: String,
    onSmokingBaselineChange: (String) -> Unit,
    smokingBaselineError: String?,
    onEditClick: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    HealthSectionScaffold(
        title = "Riwayat Merokok",
        isEditing = isEditing,
        onEditClick = onEditClick
    ) {
        if (isEditing) {
            FieldLabel("Status merokok")
            DropdownField(
                selectedOption = smokingStatus,
                onOptionSelected = onSmokingStatusChange,
                options = listOf("Tidak Pernah", "Sudah Berhenti", "Masih Merokok"),
                placeHolderText = "Pilih status merokok",
                iconResId = R.drawable.ic_smoking,
                isError = smokingStatusError != null,
                errorMessage = smokingStatusError ?: ""
            )

            if (smokingStatus != "Tidak Pernah") {
                Spacer(modifier = Modifier.height(12.dp))

                FieldLabel("Usia mulai merokok")
                InputField(
                    value = smokingStartAge,
                    onValueChange = onSmokingStartAgeChange,
                    placeholderText = "Usia mulai merokok",
                    iconResId = R.drawable.ic_calendar,
                    keyboardType = KeyboardType.Number,
                    isError = smokingStartAgeError != null,
                    errorMessage = smokingStartAgeError ?: ""
                )

                if (smokingStatus == "Masih Merokok") {
                    Spacer(modifier = Modifier.height(12.dp))

                    FieldLabel("Baseline konsumsi rokok")
                    InputField(
                        value = smokingBaseline,
                        onValueChange = onSmokingBaselineChange,
                        placeholderText = "Rata-rata batang per hari",
                        iconResId = R.drawable.ic_smoking,
                        keyboardType = KeyboardType.Number,
                        isError = smokingBaselineError != null,
                        errorMessage = smokingBaselineError ?: ""
                    )
                }
            }

            if (smokingStatus == "Sudah Berhenti") {
                Spacer(modifier = Modifier.height(12.dp))

                FieldLabel("Usia berhenti merokok")
                InputField(
                    value = smokingStopAge,
                    onValueChange = onSmokingStopAgeChange,
                    placeholderText = "Usia berhenti merokok",
                    iconResId = R.drawable.ic_calendar,
                    keyboardType = KeyboardType.Number,
                    isError = smokingStopAgeError != null,
                    errorMessage = smokingStopAgeError ?: ""
                )
            }

            SaveSectionButton(onClick = onSave, isSaving = isSaving)
        } else {
            HealthInfoRows(
                items = buildList {
                    add(HealthInfoItem("Status merokok", smokingStatusLabel(healthProfile.smokingStatus), R.drawable.ic_smoking))
                    when (healthProfile.smokingStatus) {
                        1 -> {
                            add(HealthInfoItem("Usia mulai merokok", smokingStartLabel(healthProfile), R.drawable.ic_calendar))
                            add(HealthInfoItem("Usia berhenti merokok", smokingStopLabel(healthProfile), R.drawable.ic_calendar))
                        }
                        2 -> {
                            add(HealthInfoItem("Usia mulai merokok", smokingStartLabel(healthProfile), R.drawable.ic_calendar))
                            add(HealthInfoItem("Baseline konsumsi rokok", smokingBaselineLabel(healthProfile), R.drawable.ic_smoking))
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun HelperTriggerButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.primary).copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun BloodPressureHelperCard(
    systolic: String,
    onSystolicChange: (String) -> Unit,
    systolicError: String?,
    diastolic: String,
    onDiastolicChange: (String) -> Unit,
    diastolicError: String?,
    onDetermine: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.primary).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Helper tekanan darah",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Masukkan tekanan darah terakhir Anda untuk membantu menentukan status hipertensi",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = colorResource(id = R.color.gray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Sistolik")
            InputField(
                value = systolic,
                onValueChange = onSystolicChange,
                placeholderText = "Contoh: 120",
                iconResId = R.drawable.ic_hypertension,
                keyboardType = KeyboardType.Number,
                isError = systolicError != null,
                errorMessage = systolicError ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel("Diastolik")
            InputField(
                value = diastolic,
                onValueChange = onDiastolicChange,
                placeholderText = "Contoh: 80",
                iconResId = R.drawable.ic_hypertension,
                keyboardType = KeyboardType.Number,
                isError = diastolicError != null,
                errorMessage = diastolicError ?: ""
            )

            Spacer(modifier = Modifier.height(14.dp))

            CompactPrimaryButton(
                text = "Tentukan Status",
                onClick = onDetermine
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Batal",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompactPrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary),
                            colorResource(id = R.color.primary).copy(alpha = 0.8f)
                        )
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = poppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = colorResource(id = R.color.primary)
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun EmptyHealthProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Profil kesehatan belum tersedia",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lengkapi survey kesehatan terlebih dahulu agar aplikasi dapat menampilkan baseline medis Anda secara lengkap.",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colorResource(id = R.color.gray)
            )
        }
    }
}

private data class HealthInfoItem(
    val label: String,
    val value: String,
    val iconResId: Int
)

private fun yesNoLabel(value: Boolean): String = if (value) "Ya" else "Tidak"

private fun macrosomicLabel(value: Int): String = when (value) {
    0 -> "Tidak"
    1 -> "Ya"
    2 -> "Tidak relevan"
    else -> "Tidak diketahui"
}

private fun smokingStatusLabel(value: Int): String = when (value) {
    0 -> "Tidak pernah"
    1 -> "Sudah berhenti"
    2 -> "Masih merokok"
    else -> "Tidak diketahui"
}

private fun smokingStartLabel(healthProfile: HealthProfileUiState): String {
    return when (healthProfile.smokingStatus) {
        0 -> "Tidak berlaku"
        1, 2 -> if (healthProfile.ageOfSmoking > 0) "${healthProfile.ageOfSmoking} tahun" else "Belum diisi"
        else -> "Tidak diketahui"
    }
}

private fun smokingStopLabel(healthProfile: HealthProfileUiState): String {
    return when (healthProfile.smokingStatus) {
        0 -> "Tidak berlaku"
        1 -> if (healthProfile.ageOfStopSmoking > 0) "${healthProfile.ageOfStopSmoking} tahun" else "Belum diisi"
        2 -> "Belum berhenti"
        else -> "Tidak diketahui"
    }
}

private fun smokingBaselineLabel(healthProfile: HealthProfileUiState): String {
    return when (healthProfile.smokingStatus) {
        0 -> "Tidak relevan"
        1, 2 -> if (healthProfile.smokeCount > 0) "${healthProfile.smokeCount} batang per hari" else "Belum diisi"
        else -> "Tidak diketahui"
    }
}
