package com.itb.diabetify.presentation.home.whatif

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.CustomizableButton
import com.itb.diabetify.presentation.common.DropdownField
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.InputField
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun WhatIfScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val age by viewModel.whatIfAge
    val macrosomicBaby by viewModel.whatIfMacrosomicBaby
    val isBloodline by viewModel.whatIfIsBloodline
    val smokingStatusFieldState by viewModel.whatIfSmokingStatusFieldState
    val yearsOfSmokingFieldState by viewModel.whatIfYearsOfSmokingFieldState
    val averageCigarettesFieldState by viewModel.whatIfAverageCigarettesFieldState
    val weightFieldState by viewModel.whatIfWeightFieldState
    val isHypertensionFieldState by viewModel.whatIfIsHypertensionFieldState
    val physicalActivityFrequencyFieldState by viewModel.whatIfPhysicalActivityFieldState
    val isCholesterolFieldState by viewModel.whatIfIsCholesterolFieldState
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value
    val isLoading = viewModel.whatIfPredictionState.value.isLoading
    val scrollState = rememberScrollState()

    // Navigation Event
    val navigationEvent = viewModel.navigationEvent.value
    LaunchedEffect(navigationEvent) {
        if (navigationEvent == "WHAT_IF_RESULT_SCREEN") {
            navController.navigate(Route.WhatIfResultScreen.route)
            viewModel.onNavigationHandled()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.primary)
                    )
                }

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Simulasi What If",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.primary)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                // Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.primary).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ubah faktor risiko di bawah ini untuk melihat bagaimana perubahan tersebut mempengaruhi risiko diabetes Anda.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.primary),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Data Tidak Dapat Diubah",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Age field
                Text(
                    text = "Usia",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                InputField(
                    value = age.toString(),
                    onValueChange = { },
                    placeholderText = "Usia",
                    iconResId = R.drawable.ic_profile,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Macrosomic baby field
                Text(
                    text = "Pernah melahirkan bayi > 4kg",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                InputField(
                    value = when (macrosomicBaby) {
                        0 -> "Tidak"
                        1 -> "Pernah"
                        2 -> "Tidak Pernah Melahirkan"
                        else -> "Tidak"
                    },
                    onValueChange = { },
                    placeholderText = "Pernah melahirkan bayi > 4kg",
                    iconResId = R.drawable.ic_baby,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bloodline
                Text(
                    text = "Riwayat keluarga diabetes",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                InputField(
                    value = if (isBloodline) "Ya" else "Tidak",
                    onValueChange = { },
                    placeholderText = "Riwayat keluarga diabetes",
                    iconResId = R.drawable.ic_family,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(
                    color = colorResource(id = R.color.gray_3),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Faktor Yang Dapat Diubah",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Smoking Status
                Text(
                    text = "Status merokok",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                DropdownField(
                    selectedOption = when (smokingStatusFieldState.text) {
                        "0" -> "Tidak Pernah Merokok"
                        "1" -> "Berhenti Merokok"
                        "2" -> "Aktif Merokok"
                        else -> "Tidak Pernah Merokok"
                    },
                    onOptionSelected = { selectedText ->
                        val status = when (selectedText) {
                            "Tidak Pernah Merokok" -> "0"
                            "Berhenti Merokok" -> "1"
                            "Aktif Merokok" -> "2"
                            else -> "0"
                        }
                        viewModel.setWhatIfSmokingStatus(status)
                    },
                    options = listOf(
                        "Tidak Pernah Merokok",
                        "Berhenti Merokok",
                        "Aktif Merokok"
                    ),
                    placeHolderText = "Status merokok",
                    iconResId = R.drawable.ic_smoking,
                    modifier = Modifier.fillMaxWidth(),
                    isError = smokingStatusFieldState.error != null,
                    errorMessage = smokingStatusFieldState.error ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Years of Smoking
                if (smokingStatusFieldState.text.toInt() > 0) {
                    Text(
                        text = "Berapa lama merokok (tahun)",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.primary),
                    )
                    InputField(
                        value = yearsOfSmokingFieldState.text,
                        onValueChange = { viewModel.setWhatIfYearsOfSmoking(it) },
                        placeholderText = "Berapa lama merokok (tahun)",
                        iconResId = R.drawable.ic_calendar,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        isError = yearsOfSmokingFieldState.error != null,
                        errorMessage = yearsOfSmokingFieldState.error ?: "",
                        testTag = "SmokingDurationTextField"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Average cigarettes
                if (smokingStatusFieldState.text.toInt() > 0) {
                    Text(
                        text = "Rata-rata rokok per hari (batang)",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.primary),
                    )
                    InputField(
                        value = averageCigarettesFieldState.text,
                        onValueChange = { viewModel.setWhatIfAverageCigarettes(it) },
                        placeholderText = "Rata-rata rokok per hari (batang)",
                        iconResId = R.drawable.ic_smoking,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                        isError = averageCigarettesFieldState.error != null,
                        errorMessage = averageCigarettesFieldState.error ?: "",
                        testTag = "CigarettesPerDayTextField"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Weight
                Text(
                    text = "Berat badan (kg)",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                InputField(
                    value = weightFieldState.text,
                    onValueChange = { viewModel.setWhatIfWeight(it) },
                    placeholderText = "Berat badan (kg)",
                    iconResId = R.drawable.ic_weight,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                    isError = weightFieldState.error != null,
                    errorMessage = weightFieldState.error ?: "",
                    testTag = "WeightTextField"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hypertension
                Text(
                    text = "Hipertensi",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                DropdownField(
                    selectedOption = if (isHypertensionFieldState.text == "true") "Ya" else "Tidak",
                    onOptionSelected = { selectedText ->
                        viewModel.setWhatIfIsHypertension((selectedText == "Ya").toString())
                    },
                    options = listOf("Tidak", "Ya"),
                    placeHolderText = "Hipertensi",
                    iconResId = R.drawable.ic_hypertension,
                    modifier = Modifier.fillMaxWidth(),
                    isError = isHypertensionFieldState.error != null,
                    errorMessage = isHypertensionFieldState.error ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Physical Activity
                Text(
                    text = "Aktivitas fisik per minggu (hari)",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                InputField(
                    value = physicalActivityFrequencyFieldState.text,
                    onValueChange = { viewModel.setWhatIfPhysicalActivity(it) },
                    placeholderText = "Aktivitas fisik per minggu (hari)",
                    iconResId = R.drawable.ic_walk,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                    isError = physicalActivityFrequencyFieldState.error != null,
                    errorMessage = physicalActivityFrequencyFieldState.error ?: "",
                    testTag = "PhysicalActivityTextField"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cholesterol
                Text(
                    text = "Kolesterol",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary),
                )
                DropdownField(
                    selectedOption = if (isCholesterolFieldState.text == "true") "Ya" else "Tidak",
                    onOptionSelected = { selectedText ->
                        viewModel.setWhatIfIsCholesterol((selectedText == "Ya").toString())
                    },
                    options = listOf("Tidak", "Ya"),
                    placeHolderText = "Kolesterol",
                    iconResId = R.drawable.ic_cholesterol,
                    modifier = Modifier.fillMaxWidth(),
                    isError = isCholesterolFieldState.error != null,
                    errorMessage = isCholesterolFieldState.error ?: ""
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomizableButton(
                        text = "Reset",
                        onClick = { viewModel.resetWhatIfFields() },
                        backgroundColor = Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    PrimaryButton(
                        text = "Hitung",
                        onClick = {
                            val isValid = viewModel.validateWhatIfFields()
                            if (isValid) {
                                viewModel.calculateWhatIfPrediction()
                            }
                        },
                        enabled = !isLoading,
                        isLoading = isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Error notification
        ErrorNotification(
            showError = errorMessage != null,
            errorMessage = errorMessage,
            onDismiss = { viewModel.onErrorShown() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )

        // Success notification
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