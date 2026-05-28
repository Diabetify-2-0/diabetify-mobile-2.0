package com.itb.diabetify.presentation.history

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.history.components.HorizontalCalendar
import com.itb.diabetify.presentation.history.components.LineGraph
import com.itb.diabetify.presentation.history.components.DailySummary
import com.itb.diabetify.presentation.history.components.DailySummaryData
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi", "DefaultLocale")
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    plannerViewModel: HomeViewModel,
    navController: NavController
) {
    // States
    val predictionScores by viewModel.predictionScores.collectAsState(initial = emptyList())
    val currentPrediction by viewModel.currentPrediction.collectAsState(initial = null)
    val displayData by viewModel.displayData.collectAsState(initial = null)
    val plannerGoalHistory by plannerViewModel.plannerGoalHistory
    val allPlannerCheckInHistory by plannerViewModel.allPlannerCheckInHistory
    val errorMessage = viewModel.errorMessage.value
    val isLoading = viewModel.getPredictionByDateState.value.isLoading || viewModel.getPredictionScoreByDateState.value.isLoading
    val activePlannerGoal = plannerGoalHistory.firstOrNull { it.status == PlannerGoalStatus.ACTIVE }

    LaunchedEffect(Unit) {
        plannerViewModel.refreshPlannerGoalHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
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
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Riwayat",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            PlannerHistoryEntryCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                activeGoal = activePlannerGoal,
                totalGoalCount = plannerGoalHistory.size,
                completedGoalCount = plannerGoalHistory.count { it.status == PlannerGoalStatus.COMPLETED },
                checkInCount = allPlannerCheckInHistory.size,
                onClick = {
                    navController.navigate(Route.PlannerGoalHistoryScreen.route)
                }
            )

            Spacer(modifier = Modifier.height(15.dp))

            HorizontalCalendar(
                modifier = Modifier.testTag("HorizontalCalendar"),
                onDateClickListener = { date ->
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    viewModel.setDate(date.format(formatter))
                },
            )

            if (isLoading) {
                // Loading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = colorResource(id = R.color.primary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat data prediksi...",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.primary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (currentPrediction == null) {
                LineGraph(
                    predictionScores = predictionScores,
                    selectedDate = viewModel.date.value,
                    modifier = Modifier.testTag("LineGraph")
                )

                // No data available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak Ada Data",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.black),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Data prediksi untuk tanggal yang dipilih tidak tersedia. Silakan pilih tanggal lain atau lakukan prediksi terlebih dahulu.",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.gray),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LineGraph(
                    predictionScores = predictionScores,
                    selectedDate = viewModel.date.value,
                    modifier = Modifier.testTag("LineGraph")
                )

                // Daily Summary
                currentPrediction?.let { prediction ->
                    displayData?.let { data ->
                        DailySummary(
                            summaryData = DailySummaryData(
                                date = if (viewModel.date.value.isNotEmpty()) {
                                    LocalDate.parse(viewModel.date.value)
                                } else {
                                    LocalDate.now()
                                },
                                riskPercentage = ((prediction.riskScore ?: 0.0) * 100).toFloat(),
                                riskFactorContributions = data.riskFactorContributions,
                                dailyInputs = data.dailyInputs
                            )
                        )
                    }
                }
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
    }
}

@Composable
private fun PlannerHistoryEntryCard(
    modifier: Modifier = Modifier,
    activeGoal: PlannerGoal?,
    totalGoalCount: Int,
    completedGoalCount: Int,
    checkInCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorResource(id = R.color.primary).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Riwayat Planner",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.primary)
                        )
                        Text(
                            text = activeGoal?.title ?: "Lihat perjalanan goal counterfactual Anda",
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Buka riwayat planner",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Goal",
                    value = totalGoalCount.toString()
                )
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Selesai",
                    value = completedGoalCount.toString()
                )
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Check-in",
                    value = checkInCount.toString()
                )
            }

            if (totalGoalCount == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Goal planner akan muncul di sini setelah Anda menyimpan hasil counterfactual sebagai goal.",
                        fontFamily = poppinsFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlannerHistoryMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 10.sp,
            color = Color(0xFF6B7280),
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary),
            maxLines = 1
        )
    }
}
