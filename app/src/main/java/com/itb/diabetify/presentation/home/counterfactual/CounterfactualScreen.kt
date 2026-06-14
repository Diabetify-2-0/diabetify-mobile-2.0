package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.LoadingNotification
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlin.math.floor

@Composable
fun CounterfactualScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
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

    CounterfactualScreenContent(
        options = options,
        riskTargetInput = riskTargetInput,
        latestRisk = latestRisk,
        weightText = "${viewModel.weight.value} kg",
        physicalActivityText = "${viewModel.physicalActivityAverage.value} hari/minggu",
        hypertensionText = if (viewModel.isHypertension.value) "Ya" else "Tidak",
        cholesterolText = if (viewModel.isCholesterol.value) "Ya" else "Tidak",
        smokingStatusText = smokingStatusLabel(viewModel.smokingStatus.value),
        baselineAgeText = "${viewModel.baselineAge.value} tahun",
        bloodlineText = if (viewModel.isBloodline.value) "Ada" else "Tidak ada",
        macrosomicText = macrosomicLabel(viewModel.macrosomicBaby.value),
        brinkmanIndexText = if (viewModel.smokingStatus.value == "1" || viewModel.smokingStatus.value == "2") {
            brinkmanIndexLabel(viewModel.brinkmanScore.value)
        } else {
            null
        },
        optionCurrentValues = options.associate { option ->
            option.key to currentValueForOption(viewModel, option.key)
        },
        errorMessage = errorMessage,
        successMessage = successMessage,
        loadingMessage = loadingMessage,
        isLoading = isLoading,
        onBack = { navController.popBackStack() },
        onToggleOption = viewModel::toggleCounterfactualOption,
        onTargetChange = { value -> viewModel.updateCounterfactualRiskTargetInput(value.toString()) },
        onRun = viewModel::runCounterfactualAnalysis,
        onDismissError = viewModel::onErrorShown,
        onDismissSuccess = viewModel::onSuccessShown,
    )
}

@Composable
internal fun CounterfactualScreenContent(
    options: List<HomeViewModel.CounterfactualOption>,
    riskTargetInput: String,
    latestRisk: Double,
    weightText: String,
    physicalActivityText: String,
    hypertensionText: String,
    cholesterolText: String,
    smokingStatusText: String,
    baselineAgeText: String,
    bloodlineText: String,
    macrosomicText: String,
    brinkmanIndexText: String?,
    optionCurrentValues: Map<String, String>,
    errorMessage: String?,
    successMessage: String?,
    loadingMessage: String?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onToggleOption: (String) -> Unit,
    onTargetChange: (Int) -> Unit,
    onRun: () -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("CounterfactualScreenRoot")
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
                    onClick = onBack
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
                                value = weightText
                            )
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Aktivitas",
                                value = physicalActivityText
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Hipertensi",
                                value = hypertensionText
                            )
                            SummaryMetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Kolesterol",
                                value = cholesterolText
                            )
                        }

                        SummaryMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Status Merokok",
                            value = smokingStatusText
                        )
                    }
                }

                HomeCard(title = "Riwayat & Kondisi Tetap") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Data berikut adalah riwayat kesehatan dan kondisi bawaan Anda yang nilainya akan tetap sama dalam simulasi ini.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 19.sp
                        )

                        ImmutableInfoCard(
                            title = "Usia",
                            value = baselineAgeText
                        )
                        ImmutableInfoCard(
                            title = "Riwayat keluarga diabetes",
                            value = bloodlineText
                        )
                        ImmutableInfoCard(
                            title = "Riwayat bayi makrosomia",
                            value = macrosomicText
                        )

                        brinkmanIndexText?.let {
                            ImmutableInfoCard(
                                title = "Brinkman Index",
                                value = it
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
                            text = "Pilih kebiasaan atau kondisi kesehatan yang siap Anda perbaiki.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 19.sp
                        )

                        options.forEach { option ->
                            CounterfactualOptionCard(
                                option = option,
                                currentValue = optionCurrentValues[option.key].orEmpty(),
                                onToggle = { onToggleOption(option.key) }
                            )
                        }
                    }
                }

                HomeCard(title = "Tentukan Target Risiko Anda") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RiskTargetThresholdSelector(
                            currentRiskPercentage = latestRisk,
                            targetInput = riskTargetInput,
                            onTargetChange = onTargetChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            CounterfactualActionBar(
                isLoading = isLoading,
                onRun = onRun
            )
        }

        ErrorNotification(
            showError = errorMessage != null,
            errorMessage = errorMessage,
            onDismiss = onDismissError,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )

        SuccessNotification(
            showSuccess = successMessage != null,
            successMessage = successMessage,
            onDismiss = onDismissSuccess,
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
                    .testTag("CounterfactualRunButton")
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
private fun RiskTargetThresholdSelector(
    currentRiskPercentage: Double,
    targetInput: String,
    onTargetChange: (Int) -> Unit
) {
    val maxRiskPercentage = counterfactualSliderUpperBound(currentRiskPercentage)
    val defaultTargetPercentage = 45.coerceAtMost(maxRiskPercentage)
    val targetPercentage = targetInput.toIntOrNull()?.coerceIn(1, maxRiskPercentage)
        ?: defaultTargetPercentage
    val targetCategory = riskCategoryText(targetPercentage.toDouble())
    val targetColor = riskCategoryColor(targetPercentage.toDouble())

    LaunchedEffect(maxRiskPercentage, targetInput) {
        val normalizedTarget = targetInput.toIntOrNull()?.coerceIn(1, maxRiskPercentage)
            ?: defaultTargetPercentage
        if (targetInput != normalizedTarget.toString()) {
            onTargetChange(normalizedTarget)
        }
    }

    val isTargetAlreadySatisfied = currentRiskPercentage <= targetPercentage
    val shouldSearchCounterfactual = currentRiskPercentage > targetPercentage

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-4).dp)
            ) {
                Text(
                    text = "<$targetPercentage%",
                    modifier = Modifier.testTag("CounterfactualTargetPercentage"),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    color = targetColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = targetCategory,
                    modifier = Modifier.testTag("CounterfactualTargetCategory"),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    color = targetColor,
                    textAlign = TextAlign.Center
                )
            }

            RiskTargetGradientSlider(
                value = targetPercentage,
                maxRiskPercentage = maxRiskPercentage,
                onValueChange = onTargetChange,
                modifier = Modifier.offset(y = (-8).dp)
            )

            Text(
                text = if (isTargetAlreadySatisfied) {
                    "Risiko Anda saat ini sudah memenuhi target ini. Anda akan langsung diarahkan ke hasil tanpa pencarian skenario tambahan."
                } else if (shouldSearchCounterfactual) {
                    "Pilihan yang sangat baik! Kami akan menyusun skenario realistis agar risiko Anda turun di bawah $targetPercentage%."
                } else {
                    "Target ini masih lebih tinggi dari risiko Anda saat ini. Geser terus ke kiri untuk merencanakan target kesehatan yang lebih baik."
                },
                modifier = Modifier.testTag("CounterfactualTargetHelperText"),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isTargetAlreadySatisfied || shouldSearchCounterfactual) {
                    Color(0xFF475569)
                } else {
                    Color(0xFFB45309)
                },
                lineHeight = 18.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RiskTargetGradientSlider(
    value: Int,
    maxRiskPercentage: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbSize = 24.dp
    val scaleLabelWidth = 42.dp
    val scaleLabels = remember(maxRiskPercentage) {
        buildRiskScaleLabels(maxRiskPercentage)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        contentAlignment = Alignment.Center
    ) {
        val travelWidth = maxWidth - thumbSize

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(1, maxRiskPercentage)) },
            valueRange = 1f..maxRiskPercentage.toFloat(),
            steps = (maxRiskPercentage - 2).coerceAtLeast(0),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .background(colorResource(id = R.color.primary), CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )
            },
            track = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(riskGradientBrush(maxRiskPercentage))
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        scaleLabels.forEach { marker ->
            RiskTargetScaleLabel(
                text = marker.label,
                color = marker.color,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(scaleLabelWidth)
                    .offset(
                        x = riskScaleLabelOffset(
                            travelWidth = travelWidth,
                            value = marker.value,
                            maxValue = maxRiskPercentage,
                            labelWidth = scaleLabelWidth,
                            thumbSize = thumbSize,
                            containerWidth = maxWidth
                        ),
                        y = 42.dp
                    )
            )
        }
    }
}

@Composable
private fun RiskTargetScaleLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontFamily = poppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

private fun riskMarkerOffset(
    travelWidth: Dp,
    value: Int,
    maxValue: Int,
    markerWidth: Dp,
    thumbSize: Dp,
    containerWidth: Dp
): Dp {
    return riskPositionOffset(
        travelWidth = travelWidth,
        value = value,
        maxValue = maxValue,
        itemWidth = markerWidth,
        thumbSize = thumbSize,
        containerWidth = containerWidth
    )
}

private fun riskScaleLabelOffset(
    travelWidth: Dp,
    value: Int,
    maxValue: Int,
    labelWidth: Dp,
    thumbSize: Dp,
    containerWidth: Dp
): Dp {
    return riskPositionOffset(
        travelWidth = travelWidth,
        value = value,
        maxValue = maxValue,
        itemWidth = labelWidth,
        thumbSize = thumbSize,
        containerWidth = containerWidth
    )
}

private fun riskPositionOffset(
    travelWidth: Dp,
    value: Int,
    maxValue: Int,
    itemWidth: Dp,
    thumbSize: Dp,
    containerWidth: Dp
): Dp {
    val safeMaxValue = maxValue.coerceAtLeast(1)
    val denominator = (safeMaxValue - 1).coerceAtLeast(1)
    val boundedValue = value.coerceIn(1, safeMaxValue)
    val positionFraction = ((boundedValue - 1).toFloat() / denominator.toFloat()).coerceIn(0f, 1f)
    val centerPosition = (thumbSize / 2) + (positionFraction * travelWidth)
    return (centerPosition - (itemWidth / 2)).coerceIn(0.dp, containerWidth - itemWidth)
}

private data class RiskScaleMarker(
    val value: Int,
    val label: String,
    val color: Color
)

private fun buildRiskScaleLabels(maxRiskPercentage: Int): List<RiskScaleMarker> {
    val safeMaxRiskPercentage = maxRiskPercentage.coerceIn(1, 100)
    val fixedMarkers = listOf(
        RiskScaleMarker(value = 1, label = "1%", color = Color(0xFF8BC34A)),
        RiskScaleMarker(value = 35, label = "35%", color = Color(0xFFFFC107)),
        RiskScaleMarker(value = 55, label = "55%", color = Color(0xFFFA821F)),
        RiskScaleMarker(value = 70, label = "70%", color = Color(0xFFF44336))
    )
    val endpointMarker = RiskScaleMarker(
        value = safeMaxRiskPercentage,
        label = "${safeMaxRiskPercentage}%",
        color = riskCategoryColor(safeMaxRiskPercentage.toDouble())
    )

    val minFractionGap = 0.12f
    val selectedMarkers = mutableListOf(fixedMarkers.first())
    val intermediateMarkers = fixedMarkers
        .drop(1)
        .filter { it.value < safeMaxRiskPercentage }

    intermediateMarkers.forEach { marker ->
        val previousFraction = riskValueFraction(selectedMarkers.last().value, safeMaxRiskPercentage)
        val markerFraction = riskValueFraction(marker.value, safeMaxRiskPercentage)
        val endpointFraction = riskValueFraction(endpointMarker.value, safeMaxRiskPercentage)
        val isFarEnoughFromPrevious = markerFraction - previousFraction >= minFractionGap
        val isFarEnoughFromEndpoint = endpointFraction - markerFraction >= minFractionGap

        if (isFarEnoughFromPrevious && isFarEnoughFromEndpoint) {
            selectedMarkers += marker
        }
    }

    if (selectedMarkers.last().value != endpointMarker.value) {
        selectedMarkers += endpointMarker
    } else {
        selectedMarkers[selectedMarkers.lastIndex] = endpointMarker
    }

    return selectedMarkers
}

private fun riskGradientBrush(maxRiskPercentage: Int): Brush {
    val safeMaxRiskPercentage = maxRiskPercentage.coerceIn(1, 100)
    val baseStops = listOf(
        1 to Color(0xFF8BC34A),
        35 to Color(0xFFFFC107),
        55 to Color(0xFFFA821F),
        70 to Color(0xFFF44336),
        100 to Color(0xFFF44336)
    )
    val denominator = (safeMaxRiskPercentage - 1).coerceAtLeast(1).toFloat()
    val colorStops = mutableListOf<Pair<Float, Color>>()

    baseStops
        .filter { (value, _) -> value <= safeMaxRiskPercentage }
        .forEach { (value, color) ->
            val fraction = if (safeMaxRiskPercentage == 1) {
                0f
            } else {
                ((value - 1).toFloat() / denominator).coerceIn(0f, 1f)
            }
            if (colorStops.isEmpty() || fraction > colorStops.last().first) {
                colorStops += fraction to color
            }
        }

    val endpointColor = riskCategoryColor(safeMaxRiskPercentage.toDouble())
    if (colorStops.isEmpty()) {
        colorStops += 0f to endpointColor
    }
    if (colorStops.last().first < 1f) {
        colorStops += 1f to endpointColor
    } else {
        colorStops[colorStops.lastIndex] = 1f to endpointColor
    }

    return Brush.horizontalGradient(colorStops = colorStops.toTypedArray())
}

private fun counterfactualSliderUpperBound(currentRiskPercentage: Double): Int {
    if (!currentRiskPercentage.isFinite()) {
        return 100
    }
    return floor(currentRiskPercentage).toInt().coerceIn(1, 100)
}

private fun riskValueFraction(value: Int, maxValue: Int): Float {
    val safeMaxValue = maxValue.coerceAtLeast(1)
    val denominator = (safeMaxValue - 1).coerceAtLeast(1)
    val boundedValue = value.coerceIn(1, safeMaxValue)
    return ((boundedValue - 1).toFloat() / denominator.toFloat()).coerceIn(0f, 1f)
}

private fun riskCategoryText(riskPercentage: Double): String {
    return when {
        riskPercentage <= 35 -> "Rendah"
        riskPercentage <= 55 -> "Sedang"
        riskPercentage <= 70 -> "Tinggi"
        else -> "Sangat tinggi"
    }
}

private fun riskCategoryColor(riskPercentage: Double): Color {
    return when {
        riskPercentage <= 35 -> Color(0xFF0F9D58)
        riskPercentage <= 55 -> Color(0xFFEA9A00)
        riskPercentage <= 70 -> Color(0xFFFA821F)
        else -> Color(0xFFE53935)
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
            .testTag("CounterfactualOption_${option.key}")
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
