package com.itb.diabetify.presentation.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.XaiProfile
import com.itb.diabetify.presentation.home.components.BarChart
import com.itb.diabetify.presentation.home.components.BarChartEntry
import com.itb.diabetify.presentation.home.components.RiskIndicator
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XaiInfoSheet(
    xaiProfile: XaiProfile?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Analisis Risiko XAI",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.primary),
            )
            Text(
                text = "Faktor risiko berdasarkan analisis SHAP terbaru Anda",
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 48.dp)
                            .size(40.dp),
                        color = colorResource(id = R.color.primary),
                    )
                }

                xaiProfile == null -> {
                    NoXaiDataPlaceholder()
                }

                else -> {
                    XaiContent(xaiProfile = xaiProfile)
                }
            }
        }
    }
}

@Composable
private fun NoXaiDataPlaceholder() {
    Column(
        modifier = Modifier.padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFFD1D5DB),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Data XAI belum tersedia",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color(0xFF6B7280),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Lakukan prediksi diabetes terlebih dahulu\nuntuk melihat analisis risiko Anda.",
            fontFamily = poppinsFontFamily,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun XaiContent(xaiProfile: XaiProfile) {
    val riskPercentage = xaiProfile.riskScore * 100.0

    RiskIndicator(
        percentage = riskPercentage,
        size = 160.dp,
        strokeWidth = 18.dp,
    )

    Spacer(modifier = Modifier.height(8.dp))

    val riskLabel = when {
        riskPercentage <= 35.0 -> "Risiko Rendah"
        riskPercentage <= 55.0 -> "Risiko Sedang"
        riskPercentage <= 70.0 -> "Risiko Tinggi"
        else -> "Risiko Sangat Tinggi"
    }
    val riskColor = when {
        riskPercentage <= 35.0 -> Color(0xFF8BC34A)
        riskPercentage <= 55.0 -> Color(0xFFFFC107)
        riskPercentage <= 70.0 -> Color(0xFFFA821F)
        else -> Color(0xFFF44336)
    }

    Text(
        text = riskLabel,
        fontFamily = poppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = riskColor,
    )

    Spacer(modifier = Modifier.height(20.dp))
    HorizontalDivider(color = Color(0xFFE5E7EB))
    Spacer(modifier = Modifier.height(16.dp))

    if (xaiProfile.features.isNotEmpty()) {
        Text(
            text = "Faktor-Faktor Risiko",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = colorResource(id = R.color.primary),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        val barEntries = remember(xaiProfile.features) {
            xaiProfile.features.map { feature ->
                val isNegative = feature.impact.contains("negative")
                val abbreviation = feature.alias.split(" ").firstOrNull() ?: feature.alias
                BarChartEntry(
                    label = feature.alias,
                    abbreviation = abbreviation,
                    value = abs(feature.contribution) * 100.0,
                    isNegative = isNegative,
                )
            }
        }

        BarChart(
            entries = barEntries,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    xaiProfile.xaiSummary?.takeIf { it.isNotBlank() }?.let { summary ->
        HorizontalDivider(color = Color(0xFFE5E7EB))
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = colorResource(id = R.color.primary),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = summary,
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp,
                    fontStyle = FontStyle.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Dianalisis oleh AI berdasarkan data prediksi terbaru Anda",
            fontFamily = poppinsFontFamily,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
