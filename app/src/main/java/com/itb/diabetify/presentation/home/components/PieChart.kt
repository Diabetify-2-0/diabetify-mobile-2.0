package com.itb.diabetify.presentation.home.components

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.itb.diabetify.R
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlin.math.abs

@SuppressLint("DefaultLocale")
@Composable
fun PieChart(
    riskFactors: List<HomeViewModel.RiskFactor>,
    holeRadius: Int = 10,
    animationDuration: Int = 1000,
    modifier: Modifier
) {
    val context = LocalContext.current

    val poppinsBoldTypeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            ResourcesCompat.getFont(context, R.font.poppins_bold)
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }
    } else {
        Typeface.DEFAULT_BOLD
    }

    val processedRiskFactors = remember(riskFactors) {
        val threshold = 5.0
        val significantFactors = riskFactors.filter { abs(it.percentage) >= threshold }
        val minorFactors = riskFactors.filter { abs(it.percentage) < threshold }
        val positiveMinorFactors = minorFactors.filter { it.percentage > 0 }
        val negativeMinorFactors = minorFactors.filter { it.percentage < 0 }
        val result = mutableListOf<HomeViewModel.RiskFactor>()
        result.addAll(significantFactors)
        if (positiveMinorFactors.size > 1) {
            val positiveOthersTotalPercentage = positiveMinorFactors.sumOf { it.percentage }
            val positiveOthersItem = HomeViewModel.RiskFactor(
                name = "Others (Positive)",
                abbreviation = "L",
                percentage = positiveOthersTotalPercentage
            )
            result.add(positiveOthersItem)
        } else if (positiveMinorFactors.size == 1) {
            result.addAll(positiveMinorFactors)
        }
        if (negativeMinorFactors.size > 1) {
            val negativeOthersTotalPercentage = negativeMinorFactors.sumOf { it.percentage }
            val negativeOthersItem = HomeViewModel.RiskFactor(
                name = "Others (Negative)",
                abbreviation = "L",
                percentage = negativeOthersTotalPercentage
            )
            result.add(negativeOthersItem)
        } else if (negativeMinorFactors.size == 1) {
            result.addAll(negativeMinorFactors)
        }
        result.sortedByDescending { abs(it.percentage) }
    }
    
    val dataPercentages = remember(processedRiskFactors) {
        processedRiskFactors.map { it.percentage }
    }
    
    val maxPositiveValue = remember(dataPercentages) {
        dataPercentages.filter { it >= 0 }.maxOrNull() ?: 1.0
    }
    
    val maxNegativeValue = remember(dataPercentages) {
        dataPercentages.filter { it < 0 }.minOrNull()?.let { abs(it) } ?: 1.0
    }
    
    val chartColors = remember(dataPercentages, maxPositiveValue, maxNegativeValue) {
        dataPercentages.map { percentage ->
            when {
                percentage >= 0 -> {
                    val intensity = if (maxPositiveValue > 0) percentage / maxPositiveValue else 0.0
                    val red = (200 * (0.6 + 0.4 * intensity)).toInt()
                    val green = (80 * (1 - intensity)).toInt()
                    Color(red, green, green).toArgb()
                }
                else -> {
                    val intensity = abs(percentage) / maxNegativeValue
                    val green = (180 * (0.6f + 0.4f * intensity)).toInt()
                    val red = (80 * (1 - intensity)).toInt()
                    Color(red, green, red).toArgb()
                }
            }
        }
    }

    Column(modifier = modifier.wrapContentHeight()) {
        AndroidView(
            factory = { context ->
                PieChart(context).apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(Color.Transparent.toArgb())
                    this.holeRadius = holeRadius.toFloat()
                    transparentCircleRadius = holeRadius + 5f
                    legend.isEnabled = false
                    rotationAngle = 0f
                    isRotationEnabled = true
                    isHighlightPerTapEnabled = true
                    setDrawEntryLabels(false)
                    setEntryLabelColor(Color.Transparent.toArgb())
                    setEntryLabelTextSize(0f)
                }
            },
            update = { chart ->
                chart.setDrawCenterText(false)

                val entries = processedRiskFactors.map { riskFactor ->
                    PieEntry(abs(riskFactor.percentage.toFloat()), riskFactor.name)
                }

                val dataSet = PieDataSet(entries, "").apply {
                    colors = chartColors
                    valueTextSize = 12f
                    valueTextColor = Color.White.toArgb()
                    valueTypeface = poppinsBoldTypeface
                    sliceSpace = 2f
                    selectionShift = 5f
                    valueFormatter = object : ValueFormatter() {
                        @SuppressLint("DefaultLocale")
                        override fun getFormattedValue(value: Float): String {
                            val index = entries.indexOfFirst { it.value == value }
                            val originalValue = processedRiskFactors[index].percentage
                            return String.format("%.1f", originalValue)
                        }

                        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                            val fullName = pieEntry?.label ?: ""
                            return processedRiskFactors.find { it.name == fullName }?.abbreviation ?: ""
                        }
                    }
                }

                val hasDataChanged = chart.data?.let { currentData ->
                    val currentValues = (currentData.dataSet as? PieDataSet)?.values?.map { it.value }
                    val newValues = entries.map { it.value }
                    currentValues != newValues
                } ?: true

                if (hasDataChanged) {
                    chart.data = PieData(dataSet)
                    chart.data.setDrawValues(true)
                    chart.invalidate()
                    chart.animateY(animationDuration)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF9FAFB)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF2E7D32)),
                    )

                    Text(
                        text = "= Mengurangi Risiko",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = colorResource(id = R.color.primary)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFFC62828)),
                    )

                    Text(
                        text = "= Meningkatkan Risiko",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = colorResource(id = R.color.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF9FAFB)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight()
            ) {
                val threshold = 5.0
                val minorPositive = riskFactors.filter { it.percentage > 0 && abs(it.percentage) < threshold }
                val minorNegative = riskFactors.filter { it.percentage < 0 && abs(it.percentage) < threshold }
                val significant = riskFactors.filter { abs(it.percentage) >= threshold }

                significant.sortedByDescending { abs(it.percentage) }.forEach { riskFactor ->
                    val colorIndex = processedRiskFactors.indexOfFirst { it.name == riskFactor.name }
                    val abbreviation = riskFactor.abbreviation
                    if (colorIndex >= 0) {
                        LegendItem(
                            color = Color(chartColors[colorIndex]),
                            label = riskFactor.name,
                            abbreviation = abbreviation,
                            value = riskFactor.percentage
                        )
                    }
                }

                if (minorPositive.size > 1) {
                    val colorIndex = processedRiskFactors.indexOfFirst { it.name == "Others (Positive)" }
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("L")
                            }
                            append(": ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                append("Lainnya (meningkatkan risiko)")
                            }
                        },
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.primary),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    // Sort minorPositive by descending percentage before displaying
                    minorPositive.sortedByDescending { abs(it.percentage) }.forEach { riskFactor ->
                        if (colorIndex >= 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(chartColors[colorIndex]))
                                )
                                Text(
                                    text = riskFactor.name,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f),
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = colorResource(id = R.color.primary)
                                )
                                Text(
                                    text = "${String.format("%.1f", abs(riskFactor.percentage))}%",
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (riskFactor.percentage > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                } else if (minorPositive.size == 1) {
                    val riskFactor = minorPositive.first()
                    val colorIndex = processedRiskFactors.indexOfFirst { it.name == riskFactor.name }
                    if (colorIndex >= 0) {
                        LegendItem(
                            color = Color(chartColors[colorIndex]),
                            label = riskFactor.name,
                            abbreviation = riskFactor.abbreviation,
                            value = riskFactor.percentage
                        )
                    }
                }

                if (minorNegative.size > 1) {
                    val colorIndex = processedRiskFactors.indexOfFirst { it.name == "Others (Negative)" }
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("L")
                            }
                            append(": ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                append("Lainnya (menurunkan risiko)")
                            }
                        },
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.primary),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    // Sort minorNegative by descending absolute percentage before displaying
                    minorNegative.sortedByDescending { abs(it.percentage) }.forEach { riskFactor ->
                        if (colorIndex >= 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(chartColors[colorIndex]))
                                )
                                Text(
                                    text = riskFactor.name,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f),
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = colorResource(id = R.color.primary)
                                )
                                Text(
                                    text = "${String.format("%.1f", abs(riskFactor.percentage))}%",
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (riskFactor.percentage > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                } else if (minorNegative.size == 1) {
                    val riskFactor = minorNegative.first()
                    val colorIndex = processedRiskFactors.indexOfFirst { it.name == riskFactor.name }
                    if (colorIndex >= 0) {
                        LegendItem(
                            color = Color(chartColors[colorIndex]),
                            label = riskFactor.name,
                            abbreviation = riskFactor.abbreviation,
                            value = riskFactor.percentage
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LegendItem(
    color: Color,
    label: String,
    abbreviation: String,
    value: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )

        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("")
                    append(abbreviation)
                }
                append(": ")
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(label)
                }
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            fontFamily = poppinsFontFamily,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            color = colorResource(id = R.color.primary)
        )

        Text(
            text = "${String.format("%.1f", abs(value))}%",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = when {
                abs(value) < 0.000001 -> Color(0xFF2E7D32)
                value > 0 -> Color(0xFFC62828)
                else -> Color(0xFF2E7D32)
            }
        )
    }
}