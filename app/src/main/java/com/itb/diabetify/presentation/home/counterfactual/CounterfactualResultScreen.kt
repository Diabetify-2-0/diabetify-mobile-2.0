package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.itb.diabetify.R
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualChangedFeature
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.common.CustomizableButton
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlin.math.abs

@Composable
fun CounterfactualResultScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val result by viewModel.counterfactualResult
    val resultMeta by viewModel.counterfactualJobResultMeta
    val plannerGoalDurationWeeks by viewModel.plannerGoalDurationWeeks
    val activePlannerGoal by viewModel.activePlannerGoal
    val successMessage = viewModel.successMessage.value
    val replaceableActivePlannerGoal = activePlannerGoal?.takeIf { it.status == PlannerGoalStatus.ACTIVE }
    var showReplaceGoalDialog by remember { mutableStateOf(false) }
    val isCurrentResultSaved = replaceableActivePlannerGoal?.sourceJobId != null &&
        replaceableActivePlannerGoal.sourceJobId == resultMeta?.jobId
    val hasDifferentActiveGoal = replaceableActivePlannerGoal != null && !isCurrentResultSaved

    val navigateBackToHome: () -> Unit = {
        viewModel.onSuccessShown()
        if (!navController.popBackStack(Route.HomeScreen.route, inclusive = false)) {
            navController.navigate(Route.HomeScreen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val navigateBackToPlannerSetup: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate(Route.CounterfactualScreen.route) {
                launchSingleTop = true
            }
        }
    }

    CounterfactualResultContent(
        result = result,
        resultMeta = resultMeta,
        selectedDurationWeeks = plannerGoalDurationWeeks,
        isSaved = isCurrentResultSaved,
        hasDifferentActiveGoal = hasDifferentActiveGoal,
        successMessage = successMessage,
        showReplaceGoalDialog = showReplaceGoalDialog,
        displayFeatures = result?.let {
            buildDisplayFeatureChanges(
                rawFeatures = it.plannerInput?.changedFeatures.orEmpty(),
                viewModel = viewModel
            )
        }.orEmpty(),
        onShowReplaceGoalDialogChange = { showReplaceGoalDialog = it },
        onDismissSuccess = viewModel::onSuccessShown,
        onBackToHome = navigateBackToHome,
        onBackToPlannerSetup = navigateBackToPlannerSetup,
        onDurationSelected = viewModel::updatePlannerGoalDurationWeeks,
        onSave = {
            if (hasDifferentActiveGoal) {
                showReplaceGoalDialog = true
            } else {
                viewModel.saveCounterfactualAsGoal(durationWeeks = plannerGoalDurationWeeks)
            }
        },
        onConfirmReplaceGoal = {
            showReplaceGoalDialog = false
            viewModel.saveCounterfactualAsGoal(replaceActiveGoal = true)
        }
    )
}

@Composable
internal fun CounterfactualResultContent(
    result: CounterfactualResultPayload?,
    resultMeta: CounterfactualJobResultData?,
    selectedDurationWeeks: Int,
    isSaved: Boolean,
    hasDifferentActiveGoal: Boolean,
    successMessage: String?,
    showReplaceGoalDialog: Boolean,
    displayFeatures: List<CounterfactualDisplayFeature>,
    onShowReplaceGoalDialogChange: (Boolean) -> Unit,
    onDismissSuccess: () -> Unit,
    onBackToHome: () -> Unit,
    onBackToPlannerSetup: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onSave: () -> Unit,
    onConfirmReplaceGoal: () -> Unit,
) {
    if (showReplaceGoalDialog) {
        AlertDialog(
            onDismissRequest = { onShowReplaceGoalDialogChange(false) },
            title = {
                Text(
                    text = "Ganti Goal Aktif?",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.primary)
                )
            },
            text = {
                Text(
                    text = "Goal aktif saat ini akan diarsipkan, lalu rencana baru ini akan menjadi goal aktif Anda.",
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmReplaceGoal) {
                    Text(
                        text = "Ganti Goal",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowReplaceGoalDialogChange(false) }) {
                    Text(
                        text = "Batal",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            result == null || resultMeta == null -> {
                CounterfactualFullStateScreen(
                    modifier = Modifier.testTag("CounterfactualResultState_NoScenario"),
                    backgroundResId = R.drawable.bg_no_scenario_state,
                    iconResId = R.drawable.ic_no_scenario_state,
                    title = "Skenario realistis belum ditemukan untuk target dan batasan faktor ini",
                    buttonLabel = "Pilih Target Baru",
                    buttonContainerColor = Color.White,
                    buttonContentColor = Color.Black,
                    onButtonClick = onBackToPlannerSetup
                )
            }

            result.reasonCode == "TARGET_ALREADY_SATISFIED" -> {
                CounterfactualFullStateScreen(
                    modifier = Modifier.testTag("CounterfactualResultState_TargetSatisfied"),
                    backgroundResId = R.drawable.bg_already_satisfied_state,
                    iconResId = R.drawable.ic_already_satisfied_state,
                    title = "Kondisi Anda saat ini sudah memenuhi target risiko yang dipilih",
                    buttonLabel = "Pilih Target Baru",
                    buttonContainerColor = Color(0xFFD9ECE7),
                    buttonContentColor = Color(0xFF0F5A55),
                    onButtonClick = onBackToPlannerSetup
                )
            }

            result.status == "FEASIBLE" && result.candidates.isNotEmpty() -> {
                FeasibleCounterfactualResultScreen(
                    modifier = Modifier.testTag("CounterfactualResultState_Feasible"),
                    result = result,
                    displayFeatures = displayFeatures,
                    selectedDurationWeeks = selectedDurationWeeks,
                    isSaved = isSaved,
                    hasDifferentActiveGoal = hasDifferentActiveGoal,
                    onBack = onBackToHome,
                    onDurationSelected = onDurationSelected,
                    onSave = onSave,
                    onTryAnother = onBackToPlannerSetup
                )
            }

            else -> {
                CounterfactualFullStateScreen(
                    modifier = Modifier.testTag("CounterfactualResultState_Fallback"),
                    backgroundResId = R.drawable.bg_no_scenario_state,
                    iconResId = R.drawable.ic_no_scenario_state,
                    title = "Skenario realistis belum ditemukan untuk target dan batasan faktor ini",
                    buttonLabel = "Pilih Target Baru",
                    buttonContainerColor = Color.White,
                    buttonContentColor = Color.Black,
                    onButtonClick = onBackToPlannerSetup
                )
            }
        }

        SuccessNotification(
            showSuccess = successMessage != null,
            successMessage = successMessage,
            onDismiss = onDismissSuccess,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )
    }
}

@Composable
private fun FeasibleCounterfactualResultScreen(
    modifier: Modifier = Modifier,
    result: CounterfactualResultPayload,
    displayFeatures: List<CounterfactualDisplayFeature>,
    selectedDurationWeeks: Int,
    isSaved: Boolean,
    hasDifferentActiveGoal: Boolean,
    onBack: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onSave: () -> Unit,
    onTryAnother: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
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
                text = "Hasil Rencana Risiko",
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
            FeasibleHeroCard(result = result)

            Spacer(modifier = Modifier.height(16.dp))

            if (displayFeatures.isNotEmpty()) {
                HomeCard(title = "Visualisasi Perubahan Fitur") {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        displayFeatures.forEachIndexed { index, feature ->
                            FeatureVisualizationRow(feature = feature)
                            if (index < displayFeatures.lastIndex) {
                                HorizontalDivider(
                                    color = Color(0xFFE8EDF2),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            SaveGoalCard(
                selectedDurationWeeks = selectedDurationWeeks,
                isSaved = isSaved,
                hasDifferentActiveGoal = hasDifferentActiveGoal,
                onDurationSelected = onDurationSelected,
                onSave = onSave,
                onTryAnother = onTryAnother
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CounterfactualFullStateScreen(
    modifier: Modifier = Modifier,
    backgroundResId: Int,
    iconResId: Int,
    title: String,
    buttonLabel: String,
    buttonContainerColor: Color,
    buttonContentColor: Color,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(148.dp)
                )

                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CustomizableButton(
                text = buttonLabel,
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = buttonContainerColor,
                textColor = buttonContentColor
            )
        }
    }
}

@Composable
private fun FeasibleHeroCard(result: CounterfactualResultPayload) {
    val currentRisk = result.inputPrediction?.probabilityLowRisk?.let(::toHighRiskPercentage)
    val projectedRisk = result.candidates.firstOrNull()?.prediction?.probabilityLowRisk?.let(::toHighRiskPercentage)
    val reduction = if (currentRisk != null && projectedRisk != null) {
        (currentRisk - projectedRisk).coerceAtLeast(0.0)
    } else {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF274254))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rosette_discount_check),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(52.dp)
                    )

                    Column {
                        Text(
                            text = "Skenario ditemukan!",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Rencana realistis berhasil disusun",
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF8AACC8)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RiskHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = "Risiko saat ini",
                        value = formatPercentage(currentRisk),
                        subtitle = riskLevelLabel(currentRisk),
                        valueColor = riskLevelColor(currentRisk),
                        subtitleColor = riskLevelColor(currentRisk)
                    )
                    RiskHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = "Setelah skenario",
                        value = formatPercentage(projectedRisk),
                        subtitle = riskLevelLabel(projectedRisk),
                        valueColor = riskLevelColor(projectedRisk),
                        subtitleColor = riskLevelColor(projectedRisk)
                    )
                }

                reduction?.let {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trending_down),
                            contentDescription = null,
                            tint = Color(0xFF8AACC8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Turun ${String.format("%.1f", it)}% dari risiko awal",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF8AACC8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskHighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    subtitleColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E5A70))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = valueColor
            )
            Text(
                text = subtitle,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun FeatureVisualizationRow(
    feature: CounterfactualDisplayFeature
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2E475A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = feature.iconResId),
                    contentDescription = feature.label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = feature.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF111827),
                modifier = Modifier.weight(1f)
            )

            FeatureChip(text = feature.chipText)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureValueCard(
                modifier = Modifier.weight(1f),
                label = "Saat ini",
                value = feature.baselineText,
                containerColor = Color(0xFFF7F7F7),
                valueColor = Color(0xFF121212)
            )
            Text(
                text = "→",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            FeatureValueCard(
                modifier = Modifier.weight(1f),
                label = "Skenario",
                value = feature.candidateText,
                containerColor = Color(0xFFD3EFE5),
                valueColor = Color(0xFF0F5132)
            )
        }
    }
}

@Composable
private fun FeatureValueCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 9.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = valueColor
        )
    }
}

@Composable
private fun FeatureChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFE9F7F0))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color(0xFF2F7D68)
        )
    }
}

@Composable
private fun SaveGoalCard(
    selectedDurationWeeks: Int,
    isSaved: Boolean,
    hasDifferentActiveGoal: Boolean,
    onDurationSelected: (Int) -> Unit,
    onSave: () -> Unit,
    onTryAnother: () -> Unit
) {
    HomeCard(title = "Jadikan Goal Aktif") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Berapa lama kamu ingin mencapai target ini?",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF111827)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plannerDurationOptions().forEach { durationWeeks ->
                    PlannerDurationOption(
                        modifier = Modifier.weight(1f),
                        durationWeeks = durationWeeks,
                        isSelected = durationWeeks == selectedDurationWeeks,
                        onClick = { onDurationSelected(durationWeeks) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            PrimaryButton(
                text = if (isSaved) "Goal Sudah Aktif" else "Jadikan Goal",
                onClick = onSave,
                enabled = !isSaved,
                modifier = Modifier
                    .testTag("CounterfactualSaveGoalButton")
                    .fillMaxWidth()
                    .height(50.dp)
            )

            CustomizableButton(
                text = "Cari Skenario Lain",
                onClick = onTryAnother,
                modifier = Modifier
                    .testTag("CounterfactualTryAnotherButton")
                    .fillMaxWidth()
                    .height(50.dp),
                backgroundColor = Color.White,
                backgroundColorSecondary = Color.White,
                textColor = colorResource(id = R.color.primary),
                borderColor = colorResource(id = R.color.primary).copy(alpha = 0.5f),
                enabledShadowElevation = 0.dp,
                disabledShadowElevation = 0.dp
            )

            if (hasDifferentActiveGoal) {
                Text(
                    text = "Menyimpan rencana ini akan menggantikan goal aktif sebelumnya",
                    fontFamily = poppinsFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlannerDurationOption(
    modifier: Modifier = Modifier,
    durationWeeks: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colorResource(id = R.color.primary) else Color(0xFFF4F4F5))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = durationWeeks.toString(),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isSelected) Color.White else Color(0xFF616161)
            )
            Text(
                text = "minggu",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color(0xFF616161)
            )
        }
    }
}

private fun plannerDurationOptions(): List<Int> = listOf(4, 8, 12, 24)

internal data class CounterfactualDisplayFeature(
    val featureName: String,
    val label: String,
    val baselineNumeric: Double,
    val candidateNumeric: Double,
    val baselineText: String,
    val candidateText: String,
    val chipText: String,
    val iconResId: Int
)

private fun buildDisplayFeatureChanges(
    rawFeatures: List<CounterfactualChangedFeature>,
    viewModel: HomeViewModel
): List<CounterfactualDisplayFeature> {
    if (rawFeatures.isEmpty()) {
        return emptyList()
    }

    val displayFeatures = mutableListOf<CounterfactualDisplayFeature>()
    val smokingStatusFeature = rawFeatures.firstOrNull { it.featureName == "smoking_status" }
    val handledFeatureNames = mutableSetOf<String>()

    buildSmokingDisplayFeature(
        smokingStatusFeature = smokingStatusFeature,
        viewModel = viewModel
    )?.let { smokingFeature ->
        displayFeatures += smokingFeature
        handledFeatureNames += "smoking_status"
    }

    rawFeatures.forEach { feature ->
        if (feature.featureName in handledFeatureNames) {
            return@forEach
        }

        buildGenericDisplayFeature(
            feature = feature,
            heightCm = viewModel.height.value,
            currentWeightKg = viewModel.weight.value
        )?.let(displayFeatures::add)
    }

    return displayFeatures
}

private fun buildSmokingDisplayFeature(
    smokingStatusFeature: CounterfactualChangedFeature?,
    viewModel: HomeViewModel
): CounterfactualDisplayFeature? {
    val baselineSmokingStatus = viewModel.smokingStatus.value.toIntOrNull() ?: 0
    if (baselineSmokingStatus != 2) {
        return null
    }

    val smokingStatusTarget = smokingStatusFeature?.candidateValue?.toInt()
    if (smokingStatusTarget == 1) {
        return CounterfactualDisplayFeature(
            featureName = "smoking_status",
            label = "Status Merokok",
            baselineNumeric = 2.0,
            candidateNumeric = 1.0,
            baselineText = "Masih Aktif",
            candidateText = "Sudah Berhenti",
            chipText = "Perubahan Status",
            iconResId = R.drawable.ic_smoking
        )
    }

    return null
}

private fun buildGenericDisplayFeature(
    feature: CounterfactualChangedFeature,
    heightCm: Int,
    currentWeightKg: Int
): CounterfactualDisplayFeature? {
    val baseline = feature.baselineValue ?: return null
    val candidate = feature.candidateValue ?: return null
    val delta = feature.delta ?: (candidate - baseline)
    val bmiTranslation = bmiTranslation(
        featureName = feature.featureName,
        baselineValue = baseline,
        candidateValue = candidate,
        heightCm = heightCm,
        currentWeightKg = currentWeightKg
    )

    return CounterfactualDisplayFeature(
        featureName = feature.featureName,
        label = if (feature.featureName == "BMI") "Berat Badan" else featureLabel(feature.featureName),
        baselineNumeric = baseline,
        candidateNumeric = candidate,
        baselineText = bmiTranslation?.baselineText ?: formatFeatureValue(feature.featureName, baseline, visualStyle = true),
        candidateText = bmiTranslation?.candidateText ?: formatFeatureValue(feature.featureName, candidate, visualStyle = true),
        chipText = bmiTranslation?.chipText ?: formatDeltaChip(feature.featureName, delta),
        iconResId = featureIconRes(feature.featureName)
    )
}

private fun featureIconRes(featureName: String): Int {
    return when (featureName) {
        "BMI" -> R.drawable.ic_weight
        "is_hypertension" -> R.drawable.ic_hypertension
        "is_cholesterol" -> R.drawable.ic_cholesterol
        "smoking_status" -> R.drawable.ic_smoking
        "moderate_physical_activity_frequency" -> R.drawable.ic_walk
        else -> R.drawable.ic_weight
    }
}

private fun featureLabel(name: String): String {
    return when (name) {
        "BMI" -> "Berat Badan"
        "smoking_status" -> "Status Merokok"
        "is_cholesterol" -> "Kolestrol"
        "is_hypertension" -> "Hipertensi"
        "moderate_physical_activity_frequency" -> "Aktivitas Fisik"
        "brinkman_index" -> "Indeks Brinkman"
        "is_bloodline" -> "Riwayat Keluarga"
        "is_macrosomic_baby" -> "Riwayat Bayi Makrosomia"
        "age" -> "Usia"
        else -> name
    }
}

private fun formatFeatureValue(
    name: String,
    value: Double,
    visualStyle: Boolean = false
): String {
    return when (name) {
        "BMI" -> "Data berat belum lengkap"
        "age" -> "${value.toInt()} tahun"
        "moderate_physical_activity_frequency" -> if (visualStyle) {
            "${value.toInt()} Per Minggu"
        } else {
            "${value.toInt()} hari / minggu"
        }

        "smoking_status" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> if (visualStyle) "Terkontrol" else "Sudah berhenti"
            2 -> if (visualStyle) "Tak Terkontrol" else "Masih aktif"
            else -> value.toInt().toString()
        }

        "brinkman_index" -> when (value.toInt()) {
            0 -> "Paparan sangat rendah"
            1 -> "Paparan ringan"
            2 -> "Paparan sedang"
            3 -> "Paparan tinggi"
            else -> value.toInt().toString()
        }

        "is_hypertension", "is_cholesterol" -> if (visualStyle) {
            if (value.toInt() == 1) "Tak Terkontrol" else "Terkontrol"
        } else {
            if (value.toInt() == 1) "Ya" else "Tidak"
        }

        "is_bloodline" -> if (value.toInt() == 1) "Ya" else "Tidak"
        "is_macrosomic_baby" -> when (value.toInt()) {
            0 -> "Tidak"
            1 -> "Ya"
            2 -> "Tidak relevan"
            else -> value.toInt().toString()
        }

        else -> String.format("%.2f", value)
    }
}

private fun formatDeltaChip(name: String, delta: Double): String {
    return when (name) {
        "BMI" -> {
            val prefix = if (delta < 0) "▼" else "▲"
            "$prefix ${String.format("%.1f", abs(delta))} kg"
        }

        "moderate_physical_activity_frequency" -> {
            val prefix = if (delta < 0) "▼" else "▲"
            "$prefix ${abs(delta).toInt()} Aktivitas"
        }

        "is_hypertension", "is_cholesterol", "smoking_status" -> "Perubahan Status"
        else -> if (delta < 0) "Menurun" else "Meningkat"
    }
}

@Composable
private fun SummaryText(text: String) {
    Text(
        text = text,
        fontFamily = poppinsFontFamily,
        fontSize = 13.sp,
        color = Color(0xFF4B5563),
        lineHeight = 20.sp,
        modifier = Modifier.padding(16.dp)
    )
}

private fun toHighRiskPercentage(lowRiskProbability: Double): Double {
    return (1.0 - lowRiskProbability) * 100
}

private fun formatPercentage(value: Double?): String {
    return value?.let { String.format("%.1f%%", it) } ?: "-"
}

private fun riskLevelLabel(value: Double?): String {
    val risk = value ?: return "Data belum tersedia"
    return when {
        risk <= 35.0 -> "Kategori rendah"
        risk <= 55.0 -> "Kategori sedang"
        risk <= 70.0 -> "Kategori tinggi"
        else -> "Kategori sangat tinggi"
    }
}

private fun riskLevelColor(value: Double?): Color {
    val risk = value ?: return Color.White
    return when {
        risk <= 35.0 -> Color(0xFF8BC34A)
        risk <= 55.0 -> Color(0xFFFFC107)
        risk <= 70.0 -> Color(0xFFFA821F)
        else -> Color(0xFFF44336)
    }
}

private data class BmiTranslation(
    val chipText: String,
    val baselineText: String,
    val candidateText: String
)

private fun bmiTranslation(
    featureName: String,
    baselineValue: Double,
    candidateValue: Double,
    heightCm: Int,
    currentWeightKg: Int
): BmiTranslation? {
    if (featureName != "BMI" || heightCm <= 0 || currentWeightKg <= 0) {
        return null
    }

    val heightMeters = heightCm / 100.0
    if (heightMeters <= 0.0) {
        return null
    }

    val baselineWeightFromBmi = baselineValue * heightMeters * heightMeters
    val targetWeight = candidateValue * heightMeters * heightMeters
    val estimatedDelta = targetWeight - currentWeightKg
    val weightDeltaAbs = abs(estimatedDelta)
    val prefix = if (estimatedDelta < 0) "▼" else "▲"

    return BmiTranslation(
        chipText = "$prefix ${String.format("%.1f", weightDeltaAbs)} kg",
        baselineText = "${String.format("%.1f", baselineWeightFromBmi)} kg",
        candidateText = "${String.format("%.1f", targetWeight)} kg"
    )
}
