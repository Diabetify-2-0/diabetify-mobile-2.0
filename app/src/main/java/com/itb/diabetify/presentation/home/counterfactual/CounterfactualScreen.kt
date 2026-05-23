package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.compose.ui.text.input.KeyboardType
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.CustomizableButton
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.LoadingNotification
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun CounterfactualScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val scrollState = rememberScrollState()
    val options by viewModel.counterfactualOptions
    val riskTargetInput by viewModel.counterfactualRiskTargetInput
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value
    val loadingMessage = viewModel.loadingMessage.value
    val isLoading = viewModel.counterfactualState.value.isLoading
    val navigationEvent = viewModel.navigationEvent.value
    val latestRisk = viewModel.latestPredictionScore.value

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
                    text = "Counterfactual Planner",
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
                PlannerIntroCard()

                Spacer(modifier = Modifier.height(16.dp))

                HomeCard(title = "Ringkasan Kondisi Saat Ini") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RiskOverviewBanner(riskPercentage = latestRisk)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "BMI",
                                value = String.format("%.1f kg/m²", viewModel.bmi.value)
                            )
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Aktivitas",
                                value = "${viewModel.physicalActivityAverage.value} hari/minggu"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Status Rokok",
                                value = smokingStatusLabel(viewModel.smokingStatus.value)
                            )
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Konsumsi Rokok",
                                value = smokingDailySummary(viewModel)
                            )
                        }
                    }
                }

                HomeCard(title = "Faktor Yang Tidak Diubah") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Bagian ini ditampilkan eksplisit agar batas aksiabilitas jelas. Sistem tidak akan mengubah faktor-faktor berikut.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 19.sp
                        )

                        ImmutableInfoCard(
                            title = "Usia",
                            value = "${viewModel.baselineAge.value} tahun"
                        )
                        ImmutableInfoCard(
                            title = "Riwayat keluarga diabetes",
                            value = if (viewModel.isBloodline.value) "Ada" else "Tidak ada"
                        )
                        ImmutableInfoCard(
                            title = "Riwayat bayi makrosomia",
                            value = macrosomicLabel(viewModel.macrosomicBaby.value)
                        )
                    }
                }

                HomeCard(title = "Faktor Yang Bisa Dieksplorasi") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pilih faktor yang memang bersedia Anda eksplorasi. Planner akan mematuhi pilihan ini saat mencari skenario yang paling feasible.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 19.sp
                        )

                        options.forEach { option ->
                            CounterfactualOptionCard(
                                option = option,
                                currentValue = currentValueForOption(viewModel, option.key),
                                onToggle = { viewModel.toggleCounterfactualOption(option.key) }
                            )
                        }
                    }
                }

                HomeCard(title = "Target Risiko Akhir") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Masukkan ambang risiko akhir yang ingin Anda capai. Angka ini dipakai sebagai batas minimum keberhasilan, sehingga hasil akhir bisa saja lebih rendah jika skenario itu yang paling feasible.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 19.sp
                        )

                        OutlinedTextField(
                            value = riskTargetInput,
                            onValueChange = viewModel::updateCounterfactualRiskTargetInput,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            label = {
                                Text(
                                    text = "Target risiko akhir (%)",
                                    fontFamily = poppinsFontFamily
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Contoh: 45",
                                    fontFamily = poppinsFontFamily,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            suffix = {
                                Text(
                                    text = "%",
                                    fontFamily = poppinsFontFamily,
                                    color = colorResource(id = R.color.primary)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Text(
                            text = "Contoh: isi 45 berarti planner akan mencari skenario dengan risiko akhir di bawah 45%. Jika skenario yang paling masuk akal justru turun sampai 38%, hasil itu yang akan ditampilkan.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomizableButton(
                        text = "Reset",
                        onClick = { viewModel.resetCounterfactualOptions() },
                        backgroundColor = Color(0xFFE5E7EB),
                        textColor = colorResource(id = R.color.primary),
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

                Spacer(modifier = Modifier.height(20.dp))
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

        LoadingNotification(
            showLoading = isLoading && loadingMessage != null,
            loadingMessage = loadingMessage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )
    }
}

@Composable
private fun PlannerIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary),
                            colorResource(id = R.color.tertiary)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Rencanakan skenario perubahan yang terarah",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "Counterfactual planner mencari kombinasi perubahan yang paling mungkin membantu menurunkan risiko tanpa mengubah faktor tetap dan tetap menghormati pilihan Anda.",
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RiskOverviewBanner(riskPercentage: Double) {
    val levelColor = when {
        riskPercentage <= 35 -> Color(0xFF0F9D58)
        riskPercentage <= 55 -> Color(0xFFEA9A00)
        riskPercentage <= 70 -> Color(0xFFFA821F)
        else -> Color(0xFFE53935)
    }
    val levelText = when {
        riskPercentage <= 35 -> "Rendah"
        riskPercentage <= 55 -> "Sedang"
        riskPercentage <= 70 -> "Tinggi"
        else -> "Sangat tinggi"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.white_2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Risiko saat ini",
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = String.format("%.1f%%", riskPercentage),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.primary)
                )
            }

            Box(
                modifier = Modifier
                    .background(levelColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Kategori $levelText",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = levelColor
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )
        }
    }
}

@Composable
private fun ImmutableInfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Immutable",
                    tint = colorResource(id = R.color.primary)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary)
                )
                Text(
                    text = value,
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            StatusChip(
                text = "Tetap",
                backgroundColor = Color(0xFFE2E8F0),
                textColor = Color(0xFF475569)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CounterfactualOptionCard(
    option: HomeViewModel.CounterfactualOption,
    currentValue: String,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .border(
                width = if (option.isSelected) 1.5.dp else 1.dp,
                color = if (option.isSelected) {
                    colorResource(id = R.color.tertiary)
                } else {
                    Color(0xFFE5E7EB)
                },
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) {
                Color(0xFFF7FBFD)
            } else {
                Color(0xFFFAFAFA)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
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

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = option.iconResId),
                        contentDescription = option.label,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = option.label,
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = colorResource(id = R.color.primary),
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Nilai saat ini: $currentValue",
                                fontFamily = poppinsFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        StatusChip(
                            text = option.categoryLabel,
                            backgroundColor = Color(0xFFE0F2FE),
                            textColor = Color(0xFF0369A1)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = option.description,
                        fontFamily = poppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            text = option.idealDirectionLabel,
                            backgroundColor = Color(0xFFEFF6FF),
                            textColor = Color(0xFF1D4ED8)
                        )
                        StatusChip(
                            text = option.effortLabel,
                            backgroundColor = Color(0xFFF3F4F6),
                            textColor = Color(0xFF475569)
                        )
                        StatusChip(
                            text = option.impactLabel,
                            backgroundColor = if (option.needsClinicalReview) {
                                Color(0xFFFFF7ED)
                            } else {
                                Color(0xFFECFDF5)
                            },
                            textColor = if (option.needsClinicalReview) {
                                Color(0xFFB45309)
                            } else {
                                Color(0xFF0F766E)
                            }
                        )
                    }

                    option.supportingText?.let { note ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note,
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

private fun currentValueForOption(
    viewModel: HomeViewModel,
    key: String
): String {
    return when (key) {
        "BMI" -> String.format("%.1f kg/m²", viewModel.bmi.value)
        "moderate_physical_activity_frequency" -> "${viewModel.physicalActivityAverage.value} hari / minggu"
        "smoking_behavior" -> smokingBehaviorValue(viewModel)
        "is_hypertension" -> if (viewModel.isHypertension.value) "Ya" else "Tidak"
        "is_cholesterol" -> if (viewModel.isCholesterol.value) "Ya" else "Tidak"
        else -> "-"
    }
}

private fun smokingBehaviorValue(viewModel: HomeViewModel): String {
    val dailyCigarettes = currentSmokingDailyBaseline(viewModel)
    return when (viewModel.smokingStatus.value) {
        "2" -> "Masih aktif, sekitar $dailyCigarettes batang per hari"
        "1" -> "Sudah berhenti"
        "0" -> "Tidak pernah merokok"
        else -> "Tidak diketahui"
    }
}

private fun smokingDailySummary(viewModel: HomeViewModel): String {
    val dailyCigarettes = currentSmokingDailyBaseline(viewModel)
    return when (viewModel.smokingStatus.value) {
        "2" -> "$dailyCigarettes batang/hari"
        "1", "0" -> "0 batang/hari"
        else -> "-"
    }
}

private fun currentSmokingDailyBaseline(viewModel: HomeViewModel): Int {
    return viewModel.smokeAverage.value
        .takeIf { it > 0 }
        ?: viewModel.profileSmokeCount.value.coerceAtLeast(0)
}

private fun smokingStatusLabel(value: String): String {
    return when (value) {
        "0" -> "Tidak merokok"
        "1" -> "Sudah berhenti"
        "2" -> "Masih aktif"
        else -> "Tidak diketahui"
    }
}

private fun macrosomicLabel(value: Int): String {
    return when (value) {
        0 -> "Tidak"
        1 -> "Ya"
        2 -> "Tidak relevan"
        else -> "Tidak diketahui"
    }
}
