package com.itb.diabetify.presentation.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.history.components.HorizontalCalendar
import com.itb.diabetify.presentation.history.components.LineGraph
import com.itb.diabetify.presentation.history.components.DailySummary
import com.itb.diabetify.presentation.history.components.DailySummaryData
import com.itb.diabetify.ui.theme.poppinsFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi", "DefaultLocale")
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    // States
    val predictionScores by viewModel.predictionScores.collectAsState(initial = emptyList())
    val currentPrediction by viewModel.currentPrediction.collectAsState(initial = null)
    val displayData by viewModel.displayData.collectAsState(initial = null)
    val errorMessage = viewModel.errorMessage.value
    val isLoading = viewModel.getPredictionByDateState.value.isLoading || viewModel.getPredictionScoreByDateState.value.isLoading

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
