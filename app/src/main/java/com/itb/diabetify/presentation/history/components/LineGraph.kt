package com.itb.diabetify.presentation.history.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.itb.diabetify.R
import com.itb.diabetify.ui.theme.poppinsFontFamily

data class PredictionScoreEntry(
    val day: Int,
    val score: Float,
    val date: String
)

@Composable
fun LineGraph(
    predictionScores: List<PredictionScoreEntry> = emptyList(),
    selectedDate: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
    ) {
        if (predictionScores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada data skor risiko dalam rentang 15 hari",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.black),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        setupChart(this, predictionScores, selectedDate)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                update = { chart ->
                    setupChart(chart, predictionScores, selectedDate)
                }
            )
        }
    }
}

private fun setupChart(
    chart: LineChart,
    data: List<PredictionScoreEntry>,
    selectedDate: String
) {
    val sortedData = data.sortedBy { it.date }

    val processedData = sortedData.mapIndexed { index, entry ->
        entry.copy(day = index + 1)
    }

    val entries = processedData.map { entry ->
        Entry(entry.day.toFloat(), entry.score)
    }

    val dataSet = LineDataSet(entries, "Risk Score").apply {
        color = Color(0xFF2196F3).toArgb()
        setCircleColor(Color(0xFF2196F3).toArgb())
        lineWidth = 2f
        circleRadius = 4f
        setDrawCircleHole(false)
        valueTextSize = 0f
        setDrawFilled(true)
        fillColor = Color(0xFF2196F3).toArgb()
        fillAlpha = 30
    }

    val entryToHighlight = processedData.find { it.date == selectedDate }

    if (entryToHighlight != null) {
        val highlightEntry = Entry(entryToHighlight.day.toFloat(), entryToHighlight.score)
        val highlightDataSet = LineDataSet(listOf(highlightEntry), "Selected Day").apply {
            color = Color(0xFFFF5722).toArgb()
            setCircleColor(Color(0xFFFF5722).toArgb())
            circleRadius = 8f
            lineWidth = 0f
            setDrawCircleHole(false)
            valueTextSize = 0f
        }

        val lineData = LineData(dataSet, highlightDataSet)
        chart.data = lineData
    } else {
        chart.data = LineData(dataSet)
    }

    chart.apply {
        description.isEnabled = false
        legend.isEnabled = false
        setTouchEnabled(true)
        setDragEnabled(true)
        setScaleEnabled(false)
        setPinchZoom(false)
        setDrawGridBackground(false)

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            granularity = 1f
            axisMinimum = if (processedData.isNotEmpty()) processedData.minOf { it.day }.toFloat() else 1f
            axisMaximum = if (processedData.isNotEmpty()) processedData.maxOf { it.day }.toFloat() else 30f
            setDrawLabels(false)
        }

        axisLeft.apply {
            setDrawGridLines(true)
            textSize = 12f
            textColor = Color.Gray.toArgb()
            axisMinimum = 0f
            axisMaximum = 100f
        }

        axisRight.isEnabled = false

        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
            }

            override fun onNothingSelected() {
            }
        })

        // Refresh chart
        invalidate()
    }
}