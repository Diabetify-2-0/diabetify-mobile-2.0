package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    text = "Rencana Penurunan Risiko",
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

                HomeCard(title = "Ringkasan Kesehatan Anda") {
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
                                label = "Berat Badan",
                                value = "${viewModel.weight.value} kg"
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
                                label = "Hipertensi",
                                value = if (viewModel.isHypertension.value) "Ya" else "Tidak"
                            )
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Kolesterol",
                                value = if (viewModel.isCholesterol.value) "Ya" else "Tidak"
                            )
                        }

                        SummaryMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Status Merokok",
                            value = smokingStatusLabel(viewModel.smokingStatus.value)
                        )
                    }
                }

                HomeCard(title = "Riwayat & Kondisi Tetap") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Data berikut adalah riwayat kesehatan dan kondisi bawaan Anda yang nilainya akan tetap sama dalam simulasi ini",
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

                        if (viewModel.smokingStatus.value == "1" || viewModel.smokingStatus.value == "2") {
                            ImmutableInfoCard(
                                title = "Brinkman Index",
                                value = brinkmanIndexLabel(viewModel.brinkmanScore.value)
                            )
                        }
                    }
                }

                HomeCard(title = "Pilih Target Perubahan Anda") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pilih kebiasaan atau kondisi kesehatan yang siap Anda perbaiki. Sistem akan merancang rencana yang paling realistis berdasarkan pilihan Anda",
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
                            text = "Tentukan batas risiko yang ingin dicapai. Planner akan mencari skenario realistis dengan risiko di bawah angka ini",
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colorResource(id = R.color.primary),
                                unfocusedTextColor = colorResource(id = R.color.primary),
                                cursorColor = colorResource(id = R.color.primary),
                                focusedBorderColor = colorResource(id = R.color.primary),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedLabelColor = colorResource(id = R.color.primary),
                                unfocusedLabelColor = Color(0xFF6B7280),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Text(
                            text = "Contoh: isi 45 berarti planner akan mencari skenario dengan risiko akhir di bawah 45% ",
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            CounterfactualActionBar(
                isLoading = isLoading,
                onRun = { viewModel.runCounterfactualAnalysis() }
            )
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
private fun CounterfactualActionBar(
    isLoading: Boolean,
    onRun: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            PrimaryButton(
                text = "Cari Skenario",
                onClick = onRun,
                enabled = !isLoading,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
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
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Rancang Skenario Anda",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tentukan gaya hidup yang ingin diperbaiki, dan kami akan bantu buatkan rencana terbaik untuk menurunkan risiko Anda",
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
        }
    }
}

@Composable
private fun CounterfactualOptionCard(
    option: HomeViewModel.CounterfactualOption,
    currentValue: String,
    onToggle: () -> Unit
) {
    var showInfo by remember(option.key) { mutableStateOf(false) }

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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.label,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.primary),
                                    lineHeight = 20.sp
                                )

                                Box {
                                    IconButton(
                                        onClick = { showInfo = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info ${option.label}",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showInfo,
                                        onDismissRequest = { showInfo = false },
                                        modifier = Modifier
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .width(260.dp)
                                    ) {
                                        Text(
                                            text = counterfactualInfoText(option.key),
                                            fontFamily = poppinsFontFamily,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Nilai saat ini: $currentValue",
                                fontFamily = poppinsFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun counterfactualInfoText(key: String): String {
    return when (key) {
        "BMI" -> "Melihat potensi penurunan risiko kesehatan jika Anda berhasil menurunkan berat badan ke angka yang lebih ideal."
        "moderate_physical_activity_frequency" -> "Melihat dampak positif pada penurunan risiko dengan meningkatkan frekuensi olahraga atau aktivitas fisik Anda setiap minggunya."
        "is_hypertension" -> "Mensimulasikan kondisi jika tekanan darah Anda berhasil terkontrol dengan baik, baik melalui gaya hidup sehat maupun pendampingan medis."
        "is_cholesterol" -> "Mensimulasikan kondisi jika kadar kolesterol Anda berhasil dikendalikan secara optimal melalui perbaikan asupan gizi atau terapi medis."
        "smoking_status" -> "Melihat penurunan risiko kesehatan yang signifikan jika Anda memutuskan untuk berhenti merokok sepenuhnya mulai dari sekarang."
        else -> ""
    }
}

private fun currentValueForOption(
    viewModel: HomeViewModel,
    key: String
): String {
    return when (key) {
        "BMI" -> "${viewModel.weight.value} kg"
        "moderate_physical_activity_frequency" -> "${viewModel.physicalActivityAverage.value} hari / minggu"
        "smoking_status" -> smokingStatusLabel(viewModel.smokingStatus.value)
        "is_hypertension" -> if (viewModel.isHypertension.value) "Ya" else "Tidak"
        "is_cholesterol" -> if (viewModel.isCholesterol.value) "Ya" else "Tidak"
        else -> "-"
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

private fun smokingStatusLabel(value: String): String {
    return when (value) {
        "0" -> "Tidak pernah merokok"
        "1" -> "Sudah berhenti merokok"
        "2" -> "Masih aktif merokok"
        else -> "Tidak diketahui"
    }
}

private fun brinkmanIndexLabel(value: Int): String {
    return when (value) {
        0 -> "Sangat rendah"
        1 -> "Ringan"
        2 -> "Sedang"
        3 -> "Tinggi"
        else -> "Tidak diketahui"
    }
}
