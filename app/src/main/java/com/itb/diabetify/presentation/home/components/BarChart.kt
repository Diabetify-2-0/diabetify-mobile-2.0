package com.itb.diabetify.presentation.home.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.itb.diabetify.R
import com.itb.diabetify.ui.theme.poppinsFontFamily
import com.itb.diabetify.util.PredictionUpdateNotifier
import kotlin.math.abs

data class BarChartEntry(
    val label: String,
    val abbreviation: String,
    val value: Double,
    val isNegative: Boolean = false
)

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000
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

    val sortedDescendingEntries = remember(entries) {
        entries.sortedByDescending { abs(it.value) }
    }
    val sortedAscendingEntries = remember(entries) {
        entries.sortedBy { abs(it.value) }
    }

    val maxValue = remember(entries) {
        val max = entries.maxOf { abs(it.value) }
        (((max + 10f) / 10f).toInt() * 10f)
    }

    DisposableEffect(Unit) {
        val listener = {
        }
        PredictionUpdateNotifier.addListener(listener)
        onDispose {
            PredictionUpdateNotifier.removeListener(listener)
        }
    }

    Column(modifier = modifier.wrapContentHeight().testTag("BarChart")) {
        AndroidView(
            factory = { context ->
                HorizontalBarChart(context).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    setDrawGridBackground(false)
                    setDrawBarShadow(false)
                    setDrawValueAboveBar(false)

                    setPinchZoom(false)
                    setScaleEnabled(false)

                    xAxis.isEnabled = false

                    axisRight.isEnabled = false

                    axisLeft.apply {
                        setDrawGridLines(true)
                        setDrawAxisLine(true)
                        axisMinimum = 0f
                        axisMaximum = maxValue
                        setLabelCount((maxValue / 10f).toInt() + 1, true)
                    }

                    extraLeftOffset = 35f

                    setFitBars(true)
                }
            },
            update = { chart ->
                chart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = maxValue
                    setLabelCount((maxValue / 10f).toInt() + 1, true)
                }

                val dataSets = mutableListOf<IBarDataSet>()

                val labelEntries = mutableListOf<BarEntry>()
                val labelColors = mutableListOf<Int>()

                val barEntries = mutableListOf<BarEntry>()
                val barColors = mutableListOf<Int>()

                sortedAscendingEntries.forEachIndexed { index, entry ->
                    barEntries.add(BarEntry(index.toFloat(), abs(entry.value.toFloat())))
                    barColors.add(
                        if (entry.isNegative) {
                            Color.rgb(46, 125, 50) // Green for negative values
                        } else {
                            Color.rgb(198, 40, 40) // Red for positive values
                        }
                    )

                    labelEntries.add(BarEntry(index.toFloat(), 0f))
                    labelColors.add(Color.TRANSPARENT)
                }

                val mainDataSet = BarDataSet(barEntries, "Main").apply {
                    colors = barColors
                    valueTextColor = Color.TRANSPARENT
                    valueTextSize = 12f
                    setDrawValues(false)
                }
                dataSets.add(mainDataSet)

                val labelDataSet = BarDataSet(labelEntries, "Labels").apply {
                    colors = labelColors
                    valueTextColor = Color.BLACK
                    valueTextSize = 12f
                    valueTypeface = poppinsBoldTypeface
                    setDrawValues(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getBarLabel(barEntry: BarEntry?): String {
                            return if (barEntry != null) {
                                val index = barEntry.x.toInt()
                                val originalEntry = sortedAscendingEntries.getOrNull(index)
                                originalEntry?.abbreviation ?: ""
                            } else ""
                        }
                    }
                }
                dataSets.add(labelDataSet)

                val barData = BarData(dataSets).apply {
                    barWidth = 0.5f
                }

                val hasDataChanged = chart.data?.let { currentData ->
                    val currentValues = (currentData.getDataSetByIndex(0) as? BarDataSet)?.values?.map { it.y }
                    val newValues = barEntries.map { it.y }
                    currentValues != newValues
                } ?: true

                if (hasDataChanged) {
                    chart.data = barData
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
                containerColor = androidx.compose.ui.graphics.Color(0xFFF9FAFB)
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
                            .background(androidx.compose.ui.graphics.Color(0xFF2E7D32)),
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
                            .background(androidx.compose.ui.graphics.Color(0xFFC62828)),
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
                containerColor = androidx.compose.ui.graphics.Color(0xFFF9FAFB)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight()
            ) {
                sortedDescendingEntries.forEach { entry ->
                    LegendItems(
                        color = if (entry.isNegative) {
                            androidx.compose.ui.graphics.Color(0xFF2E7D32)
                        } else {
                            androidx.compose.ui.graphics.Color(0xFFC62828)
                        },
                        label = entry.label,
                        abbreviation = entry.abbreviation,
                        value = entry.value
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LegendItems(
    color: androidx.compose.ui.graphics.Color,
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
            color = colorResource(id = R.color.primary)
        )

        Text(
            text = "${String.format("%.1f", abs(value))}%",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = when {
                abs(value) < 0.000001 -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                value > 0 -> androidx.compose.ui.graphics.Color(0xFFC62828)
                else -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
            }
        )
    }
}