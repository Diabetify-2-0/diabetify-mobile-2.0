package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.CustomizableButton
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun CounterfactualScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val scrollState = rememberScrollState()
    val options by viewModel.counterfactualOptions
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value
    val isLoading = viewModel.counterfactualState.value.isLoading
    val navigationEvent = viewModel.navigationEvent.value

    LaunchedEffect(navigationEvent) {
        if (navigationEvent == "COUNTERFACTUAL_RESULT_SCREEN") {
            navController.navigate(Route.CounterfactualResultScreen.route)
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
                    text = "Counterfactual",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF7ED)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pilih faktor yang bersedia Anda eksplorasi. Sistem akan mencari skenario perubahan yang paling memungkinkan untuk menuju low risk.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF9A3412),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Data Dasar")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("Usia", "${viewModel.baselineAge.value} tahun")
                InfoRow(
                    "Riwayat bayi makrosomia",
                    when (viewModel.macrosomicBaby.value) {
                        0 -> "Tidak"
                        1 -> "Ya"
                        2 -> "Tidak relevan"
                        else -> "Tidak diketahui"
                    }
                )
                InfoRow(
                    "Riwayat keluarga diabetes",
                    if (viewModel.isBloodline.value) "Ya" else "Tidak"
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = colorResource(id = R.color.gray_3),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Faktor Yang Bisa Dieksplorasi")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Anda dapat memilih lebih dari satu faktor. Rekomendasi utama tetap akan diprioritaskan pada hasil akhir.",
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                options.forEach { option ->
                    CounterfactualOptionCard(
                        option = option,
                        currentValue = currentValueForOption(viewModel, option.key),
                        onToggle = { viewModel.toggleCounterfactualOption(option.key) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomizableButton(
                        text = "Reset",
                        onClick = { viewModel.resetCounterfactualOptions() },
                        backgroundColor = Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    PrimaryButton(
                        text = "Cari Skenario",
                        onClick = { viewModel.runCounterfactualAnalysis() },
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

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = poppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = colorResource(id = R.color.primary)
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.white_2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )
            Text(
                text = value,
                fontFamily = poppinsFontFamily,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun CounterfactualOptionCard(
    option: HomeViewModel.CounterfactualOption,
    currentValue: String,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                colorResource(id = R.color.primary).copy(alpha = 0.08f)
            } else {
                Color(0xFFF9FAFB)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = option.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colorResource(id = R.color.primary),
                    uncheckedColor = Color(0xFF9CA3AF)
                )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.label,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.primary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nilai saat ini: $currentValue",
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = option.description,
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
                option.supportingText?.let { note ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

private fun currentValueForOption(
    viewModel: HomeViewModel,
    key: String
): String {
    return when (key) {
        "BMI" -> String.format("%.1f kg/m²", viewModel.bmi.value)
        "moderate_physical_activity_frequency" -> "${viewModel.physicalActivityAverage.value} hari / minggu"
        "smoking_status" -> when (viewModel.smokingStatus.value) {
            "0" -> "Tidak merokok"
            "1" -> "Sudah berhenti merokok"
            "2" -> "Masih aktif merokok"
            else -> "Tidak diketahui"
        }

        "brinkman_index" -> when (viewModel.brinkmanScore.value) {
            0 -> "Tidak merokok"
            1 -> "Perokok ringan"
            2 -> "Perokok sedang"
            3 -> "Perokok berat"
            else -> "Tidak diketahui"
        }

        "is_hypertension" -> if (viewModel.isHypertension.value) "Ya" else "Tidak"
        "is_cholesterol" -> if (viewModel.isCholesterol.value) "Ya" else "Tidak"
        else -> "-"
    }
}
