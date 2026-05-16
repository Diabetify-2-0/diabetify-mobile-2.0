package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualCandidate
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualChangedFeature
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun CounterfactualResultScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val result by viewModel.counterfactualResult
    val resultMeta by viewModel.counterfactualJobResultMeta
    val submittedOptions by viewModel.counterfactualSubmittedOptions
    val scrollState = rememberScrollState()

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
                text = "Hasil Counterfactual",
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
            if (result == null || resultMeta == null) {
                EmptyResultCard()
            } else {
                val safeResult = result!!
                val state = counterfactualStateOf(safeResult)
                val selectedLabels = safeResult.plannerInput?.mutableAllowed
                    ?.map(::featureLabel)
                    ?.ifEmpty { submittedOptions.filter { it.isSelected }.map { it.label } }
                    ?: submittedOptions.filter { it.isSelected }.map { it.label }

                StatusHeroCard(
                    title = state.title,
                    message = safeResult.message ?: state.fallbackMessage,
                    backgroundColor = state.backgroundColor,
                    textColor = state.textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProbabilityCard(result = safeResult)
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedLabels.isNotEmpty()) {
                    HomeCard(title = "Faktor Yang Dieksplorasi") {
                        BulletSection(items = selectedLabels)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when {
                    safeResult.reasonCode == "TARGET_ALREADY_SATISFIED" -> {
                        HomeCard(title = "Kesimpulan") {
                            SummaryText(
                                text = "Kondisi Anda saat ini sudah memenuhi target low risk. Tidak ada perubahan tambahan yang diperlukan untuk target yang dipilih."
                            )
                        }
                    }

                    safeResult.status == "FEASIBLE" && safeResult.candidates.isNotEmpty() -> {
                        val changedFeatures = safeResult.plannerInput?.changedFeatures.orEmpty()

                        if (changedFeatures.isNotEmpty()) {
                            HomeCard(title = "Perubahan Utama Yang Disarankan") {
                                changedFeatures.forEach { feature ->
                                    ChangedFeatureRow(feature = feature)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        safeResult.prescriptivePlan?.summary?.let { summary ->
                            HomeCard(title = "Ringkasan") {
                                SummaryText(text = summary)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        safeResult.prescriptivePlan?.actionSteps
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { steps ->
                                HomeCard(title = "Langkah Pendukung") {
                                    BulletSection(items = steps)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                        safeResult.prescriptivePlan?.safetyNotes
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { notes ->
                                HomeCard(title = "Catatan Kehati-hatian") {
                                    BulletSection(items = notes)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                        if (safeResult.candidates.size > 1) {
                            HomeCard(title = "Alternatif Lain") {
                                safeResult.candidates.drop(1).forEach { candidate ->
                                    AlternativeCandidateRow(candidate = candidate)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        safeResult.prescriptivePlan?.disclaimer?.let { disclaimer ->
                            HomeCard(title = "Disclaimer") {
                                SummaryText(text = disclaimer)
                            }
                        }
                    }

                    else -> {
                        HomeCard(title = "Mengapa Belum Ada Skenario") {
                            val suggestions = listOf(
                                "Izinkan lebih banyak faktor untuk dieksplorasi.",
                                "Mulai dari faktor gaya hidup seperti BMI, aktivitas fisik, atau merokok.",
                                "Gunakan hasil ini sebagai dasar diskusi, bukan keputusan medis otomatis."
                            )
                            BulletSection(items = suggestions)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class CounterfactualScreenState(
    val title: String,
    val fallbackMessage: String,
    val backgroundColor: Color,
    val textColor: Color
)

private fun counterfactualStateOf(result: CounterfactualResultPayload): CounterfactualScreenState {
    return when {
        result.reasonCode == "TARGET_ALREADY_SATISFIED" -> CounterfactualScreenState(
            title = "Sudah Memenuhi Target",
            fallbackMessage = "Kondisi Anda saat ini sudah berada pada target yang dipilih.",
            backgroundColor = Color(0xFFECFDF5),
            textColor = Color(0xFF047857)
        )

        result.status == "FEASIBLE" && result.candidates.isNotEmpty() -> CounterfactualScreenState(
            title = "Ada Skenario Yang Dapat Dicoba",
            fallbackMessage = "Sistem menemukan skenario perubahan yang paling mungkin membantu menurunkan risiko.",
            backgroundColor = Color(0xFFE0F2FE),
            textColor = Color(0xFF0369A1)
        )

        else -> CounterfactualScreenState(
            title = "Belum Ditemukan Skenario Yang Cocok",
            fallbackMessage = "Dengan faktor yang dipilih, sistem belum menemukan perubahan yang cukup untuk mencapai target.",
            backgroundColor = Color(0xFFFFF7ED),
            textColor = Color(0xFFC2410C)
        )
    }
}

@Composable
private fun EmptyResultCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Text(
            text = "Hasil counterfactual belum tersedia.",
            fontFamily = poppinsFontFamily,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StatusHeroCard(
    title: String,
    message: String,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontFamily = poppinsFontFamily,
                fontSize = 14.sp,
                color = textColor,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ProbabilityCard(result: CounterfactualResultPayload) {
    val inputProbability = result.inputPrediction?.probabilityLowRisk?.times(100)
    val candidateProbability = result.candidates.firstOrNull()?.prediction?.probabilityLowRisk?.times(100)

    HomeCard(title = "Perkiraan Probabilitas Low Risk") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProbabilityItem(
                modifier = Modifier.weight(1f),
                label = "Sebelum",
                value = inputProbability?.let { String.format("%.1f%%", it) } ?: "-"
            )
            ProbabilityItem(
                modifier = Modifier.weight(1f),
                label = "Sesudah",
                value = candidateProbability?.let { String.format("%.1f%%", it) } ?: "-"
            )
        }
    }
}

@Composable
private fun ProbabilityItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
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
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.primary)
            )
        }
    }
}

@Composable
private fun ChangedFeatureRow(feature: CounterfactualChangedFeature) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = featureLabel(feature.featureName),
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${formatFeatureValue(feature.featureName, feature.baselineValue)} -> ${formatFeatureValue(feature.featureName, feature.candidateValue)}",
            fontFamily = poppinsFontFamily,
            fontSize = 13.sp,
            color = Color(0xFF4B5563)
        )
        feature.delta?.let { delta ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Arah perubahan: ${formatDeltaDescription(feature.featureName, delta)}",
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun AlternativeCandidateRow(candidate: CounterfactualCandidate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Kandidat ${candidate.candidateId}",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Probabilitas low risk: ${
                    candidate.prediction?.probabilityLowRisk?.times(100)?.let { String.format("%.1f%%", it) } ?: "-"
                }",
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "Jumlah faktor berubah: ${candidate.metrics?.changedFeatureCount ?: "-"}",
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun BulletSection(items: List<String>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}.",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.primary),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = item,
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            }
        }
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

private fun featureLabel(name: String): String {
    return when (name) {
        "BMI" -> "Indeks Massa Tubuh"
        "smoking_status" -> "Status Merokok"
        "is_cholesterol" -> "Kolesterol"
        "is_hypertension" -> "Hipertensi"
        "moderate_physical_activity_frequency" -> "Aktivitas Fisik"
        "brinkman_index" -> "Indeks Brinkman"
        "is_bloodline" -> "Riwayat Keluarga"
        "is_macrosomic_baby" -> "Riwayat Bayi Makrosomia"
        "age" -> "Usia"
        else -> name
    }
}

private fun formatFeatureValue(name: String, value: Double?): String {
    if (value == null) {
        return "-"
    }

    return when (name) {
        "BMI" -> String.format("%.2f kg/m²", value)
        "age" -> "${value.toInt()} tahun"
        "moderate_physical_activity_frequency" -> "${value.toInt()} hari / minggu"
        "smoking_status" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> "Sudah berhenti merokok"
            2 -> "Masih aktif merokok"
            else -> value.toInt().toString()
        }

        "brinkman_index" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> "Perokok ringan"
            2 -> "Perokok sedang"
            3 -> "Perokok berat"
            else -> value.toInt().toString()
        }

        "is_hypertension", "is_cholesterol", "is_bloodline" -> if (value.toInt() == 1) "Ya" else "Tidak"
        "is_macrosomic_baby" -> when (value.toInt()) {
            0 -> "Tidak"
            1 -> "Ya"
            2 -> "Tidak relevan"
            else -> value.toInt().toString()
        }

        else -> String.format("%.2f", value)
    }
}

private fun formatDeltaDescription(name: String, delta: Double): String {
    return when (name) {
        "BMI" -> if (delta < 0) "turun ${String.format("%.2f", kotlin.math.abs(delta))} kg/m²" else "naik ${String.format("%.2f", delta)} kg/m²"
        "moderate_physical_activity_frequency" -> if (delta < 0) "berkurang ${kotlin.math.abs(delta).toInt()} hari / minggu" else "bertambah ${delta.toInt()} hari / minggu"
        else -> if (delta < 0) "menurun" else "meningkat"
    }
}
