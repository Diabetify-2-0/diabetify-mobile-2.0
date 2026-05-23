package com.itb.diabetify.presentation.home.counterfactual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualChangedFeature
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualResultPayload
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Composable
fun CounterfactualResultScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val result by viewModel.counterfactualResult
    val resultMeta by viewModel.counterfactualJobResultMeta
    val submittedOptions by viewModel.counterfactualSubmittedOptions
    val submittedTarget by viewModel.counterfactualSubmittedTarget
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
                val selectedLabels = displayMutableFeatureLabels(
                    rawFeatureNames = safeResult.plannerInput?.mutableAllowed.orEmpty(),
                    fallbackLabels = submittedOptions.filter { it.isSelected }.map { it.label }
                )
                val displayFeatures = buildDisplayFeatureChanges(
                    rawFeatures = safeResult.plannerInput?.changedFeatures.orEmpty(),
                    viewModel = viewModel
                )

                StatusHeroCard(
                    title = state.title,
                    message = heroMessageOf(safeResult, state),
                    backgroundColors = state.backgroundColors,
                    accentColor = state.accentColor,
                    runtimeMs = safeResult.runtimeMs,
                    reasonCode = safeResult.reasonCode
                )

                Spacer(modifier = Modifier.height(16.dp))

                RiskComparisonCard(result = safeResult)
                Spacer(modifier = Modifier.height(16.dp))

                HomeCard(title = "Target Yang Dipilih") {
                    SelectedTargetSection(target = submittedTarget)
                }
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
                                text = "Kondisi Anda saat ini sudah memenuhi target low risk untuk skenario yang dipilih. Planner tidak menemukan kebutuhan perubahan tambahan."
                            )
                        }
                    }

                    safeResult.status == "FEASIBLE" && safeResult.candidates.isNotEmpty() -> {
                        if (displayFeatures.isNotEmpty()) {
                            HomeCard(title = "Visualisasi Perubahan Fitur") {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    displayFeatures.forEach { feature ->
                                        FeatureTransitionCard(
                                            feature = feature
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        safeResult.prescriptivePlan?.goals
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { goals ->
                                HomeCard(title = "Tujuan Perubahan") {
                                    BulletSection(items = goals)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                    }

                    else -> {
                        val guidance = diagnosticGuidanceOf(
                            result = safeResult,
                            targetHighRiskPercentage = submittedTarget.targetHighRiskPercentage
                        )
                        HomeCard(title = "Mengapa Belum Ada Skenario") {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = guidance.title,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.primary)
                                )
                                SummaryText(
                                    text = guidance.message
                                )
                                BulletSection(
                                    items = guidance.suggestions
                                )
                            }
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
    val backgroundColors: List<Color>,
    val accentColor: Color
)

private data class CounterfactualDisplayFeature(
    val label: String,
    val baselineNumeric: Double,
    val candidateNumeric: Double,
    val range: Pair<Double, Double>,
    val baselineText: String,
    val candidateText: String,
    val chipText: String,
    val accentColor: Color,
    val detailTitle: String? = null,
    val detailDescription: String? = null
)

private data class SmokingDailyRange(
    val minDaily: Int,
    val maxDaily: Int? = null
)

private data class CounterfactualDiagnosticGuidance(
    val title: String,
    val message: String,
    val suggestions: List<String>
)

private fun counterfactualStateOf(result: CounterfactualResultPayload): CounterfactualScreenState {
    return when {
        result.reasonCode == "TARGET_ALREADY_SATISFIED" -> CounterfactualScreenState(
            title = "Sudah Memenuhi Target",
            fallbackMessage = "Kondisi Anda saat ini sudah berada pada target yang dipilih.",
            backgroundColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
            accentColor = Color(0xFFD1FAE5)
        )

        result.status == "FEASIBLE" && result.candidates.isNotEmpty() -> CounterfactualScreenState(
            title = "Rekomendasi Berhasil Ditemukan",
            fallbackMessage = "Sistem menemukan rencana perubahan yang paling mungkin membantu menurunkan risiko Anda.",
            backgroundColors = listOf(
                Color(0xFF274254),
                Color(0xFF648C9C)
            ),
            accentColor = Color(0xFFE0F2FE)
        )

        else -> CounterfactualScreenState(
            title = "Belum Ditemukan Skenario Yang Cocok",
            fallbackMessage = "Dengan faktor yang dipilih, sistem belum menemukan perubahan yang cukup untuk mencapai target.",
            backgroundColors = listOf(Color(0xFFC2410C), Color(0xFFFB923C)),
            accentColor = Color(0xFFFFEDD5)
        )
    }
}

private fun heroMessageOf(
    result: CounterfactualResultPayload,
    state: CounterfactualScreenState
): String {
    if (result.reasonCode == "TARGET_ALREADY_SATISFIED") {
        return "Kondisi Anda saat ini sudah memenuhi target risiko yang dipilih, sehingga belum diperlukan perubahan tambahan."
    }

    val rawMessage = result.message?.trim().orEmpty()
    if (rawMessage.isBlank()) {
        return state.fallbackMessage
    }

    val looksLikeEngineMessage = rawMessage.startsWith("Generated ", ignoreCase = true) &&
        rawMessage.contains("feasible counterfactual candidate", ignoreCase = true)

    return if (looksLikeEngineMessage) state.fallbackMessage else rawMessage
}

private fun diagnosticGuidanceOf(
    result: CounterfactualResultPayload,
    targetHighRiskPercentage: Int
): CounterfactualDiagnosticGuidance {
    return when (result.reasonCode) {
        "NO_MUTABLE_FEATURE" -> CounterfactualDiagnosticGuidance(
            title = "Belum Ada Faktor Yang Bisa Diubah",
            message = "Planner belum menerima faktor yang dapat dieksplorasi, sehingga tidak ada ruang perubahan yang bisa dicoba.",
            suggestions = listOf(
                "Pilih setidaknya satu faktor yang boleh dieksplorasi sebelum menjalankan counterfactual.",
                "Mulai dari faktor yang lebih actionable seperti BMI, aktivitas fisik, atau kebiasaan merokok jika masih aktif.",
                "Pastikan faktor yang tidak bisa diubah tetap berada di bagian yang terkunci."
            )
        )

        "MEDICAL_RULE_VIOLATION_ONLY" -> CounterfactualDiagnosticGuidance(
            title = "Skenario Yang Muncul Belum Cukup Masuk Akal",
            message = "Ada kandidat awal yang sempat ditemukan, tetapi semuanya gugur setelah pemeriksaan plausibilitas dan batas medis yang berlaku.",
            suggestions = listOf(
                "Mulai dari kombinasi perubahan yang lebih sederhana dan lebih dekat ke kondisi Anda saat ini.",
                "Prioritaskan faktor gaya hidup lebih dulu sebelum menambahkan terlalu banyak faktor klinis sekaligus.",
                "Perbarui profil kesehatan jika ada data dasar yang sudah berubah agar planner memakai baseline terbaru."
            )
        )

        "TIMEOUT_NO_FEASIBLE_SOLUTION" -> CounterfactualDiagnosticGuidance(
            title = "Pencarian Belum Selesai Tepat Waktu",
            message = "Planner belum menyelesaikan pencarian skenario yang valid dalam batas waktu yang tersedia.",
            suggestions = listOf(
                "Coba lagi dengan jumlah faktor yang lebih fokus agar ruang pencarian lebih ringan.",
                "Gunakan target risiko yang lebih moderat sebagai langkah awal.",
                "Jika hasilnya tetap sama, gunakan skenario ini sebagai sinyal bahwa perubahan yang diminta masih terlalu berat."
            )
        )

        else -> CounterfactualDiagnosticGuidance(
            title = "Target Belum Tercapai Dalam Batas Saat Ini",
            message = result.message
                ?: "Dengan target di bawah $targetHighRiskPercentage% dan faktor yang dipilih sekarang, planner belum menemukan skenario yang cukup untuk mencapai hasil yang diinginkan.",
            suggestions = listOf(
                "Izinkan lebih banyak faktor untuk dieksplorasi agar ruang solusi lebih luas.",
                "Mulai dari faktor gaya hidup yang lebih actionable seperti BMI, aktivitas fisik, atau merokok.",
                "Jika perlu, gunakan target risiko yang sedikit lebih moderat sebagai langkah awal."
            )
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
    backgroundColors: List<Color>,
    accentColor: Color,
    runtimeMs: Int?,
    reasonCode: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(backgroundColors)
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        runtimeMs?.let {
                            HeroBadge(
                                text = "${it} ms",
                                backgroundColor = accentColor.copy(alpha = 0.18f)
                            )
                        }
                        reasonBadgeLabelOf(reasonCode)?.let {
                            HeroBadge(
                                text = it,
                                backgroundColor = accentColor.copy(alpha = 0.14f)
                            )
                        }
                    }
                }

                Text(
                    text = message,
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.94f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private fun reasonBadgeLabelOf(reasonCode: String?): String? {
    return when (reasonCode) {
        "TARGET_ALREADY_SATISFIED" -> "Target tercapai"
        "NO_MUTABLE_FEATURE" -> "Belum ada faktor"
        "TARGET_UNREACHABLE_UNDER_CONSTRAINTS" -> "Belum feasible"
        "MEDICAL_RULE_VIOLATION_ONLY" -> "Masih belum masuk akal"
        "TIMEOUT_NO_FEASIBLE_SOLUTION" -> "Pencarian habis waktu"
        else -> null
    }
}

@Composable
private fun HeroBadge(
    text: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontSize = 10.sp,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun RiskComparisonCard(result: CounterfactualResultPayload) {
    val currentRisk = result.inputPrediction?.probabilityLowRisk?.let(::toHighRiskPercentage)
    val projectedRisk = result.candidates.firstOrNull()?.prediction?.probabilityLowRisk?.let(
        ::toHighRiskPercentage
    )
    val improvement = if (currentRisk != null && projectedRisk != null) {
        currentRisk - projectedRisk
    } else {
        null
    }

    HomeCard(title = "Perkiraan Risiko") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProbabilityItem(
                    modifier = Modifier.weight(1f),
                    label = "Risiko saat ini",
                    value = formatPercentage(currentRisk)
                )
                ProbabilityItem(
                    modifier = Modifier.weight(1f),
                    label = "Risiko setelah skenario",
                    value = formatPercentage(projectedRisk)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Interpretasi cepat",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            improvement == null -> "Belum ada perubahan risiko yang bisa dihitung dari hasil saat ini."
                            improvement > 0 -> "Skenario utama diperkirakan menurunkan risiko high risk sekitar ${String.format("%.1f", improvement)} poin persentase."
                            improvement < 0 -> "Skenario ini belum menunjukkan penurunan risiko high risk dibanding kondisi saat ini."
                            else -> "Skenario utama menghasilkan tingkat risiko yang serupa dengan kondisi saat ini."
                        },
                        fontFamily = poppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }
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
private fun SelectedTargetSection(
    target: HomeViewModel.CounterfactualRiskTarget
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = target.label,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.primary)
                )
                ScenarioChip(
                    text = "Low risk >= ${String.format("%.0f%%", target.minLowRiskProbability * 100)}",
                    backgroundColor = Color(0xFFE0F2FE),
                    textColor = Color(0xFF0369A1)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = target.description,
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FeatureTransitionCard(
    feature: CounterfactualDisplayFeature
) {
    val baseline = feature.baselineNumeric
    val candidate = feature.candidateNumeric
    val range = feature.range
    val span = (range.second - range.first).coerceAtLeast(1.0)
    val startFraction = ((baseline - range.first) / span).toFloat().coerceIn(0f, 1f)
    val endFraction = ((candidate - range.first) / span).toFloat().coerceIn(0f, 1f)
    val accentColor = feature.accentColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.label,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primary)
                )
                ScenarioChip(
                    text = feature.chipText,
                    backgroundColor = accentColor.copy(alpha = 0.12f),
                    textColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                val knobSize = 16.dp
                val startOffset = (maxWidth - knobSize) * startFraction
                val endOffset = (maxWidth - knobSize) * endFraction

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE2E8F0))
                )

                val segmentStart = minOf(startFraction, endFraction)
                val segmentEnd = maxOf(startFraction, endFraction)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = maxWidth * segmentStart)
                        .width(maxOf((maxWidth * (segmentEnd - segmentStart)), 8.dp))
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = startOffset)
                        .size(knobSize)
                        .background(colorResource(id = R.color.primary), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = endOffset)
                        .size(knobSize)
                        .background(accentColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransitionValueBlock(
                    label = "Sebelum",
                    value = feature.baselineText
                )
                TransitionValueBlock(
                    label = "Sesudah",
                    value = feature.candidateText,
                    textAlign = TextAlign.End
                )
            }

            feature.detailDescription?.let { detailDescription ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = feature.detailTitle ?: "Interpretasi perubahan",
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detailDescription,
                            fontFamily = poppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionValueBlock(
    label: String,
    value: String,
    textAlign: TextAlign = TextAlign.Start
) {
    Column(
        horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            textAlign = textAlign
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = colorResource(id = R.color.primary),
            textAlign = textAlign
        )
    }
}

private fun displayMutableFeatureLabels(
    rawFeatureNames: List<String>,
    fallbackLabels: List<String>
): List<String> {
    if (rawFeatureNames.isEmpty()) {
        return fallbackLabels
    }

    val labels = mutableListOf<String>()
    var smokingAdded = false
    rawFeatureNames.forEach { featureName ->
        if (featureName == "smoking_status" || featureName == "brinkman_index") {
            if (!smokingAdded) {
                labels.add("Kebiasaan Merokok")
                smokingAdded = true
            }
        } else {
            labels.add(featureLabel(featureName))
        }
    }
    return labels.distinct()
}

private fun buildDisplayFeatureChanges(
    rawFeatures: List<CounterfactualChangedFeature>,
    viewModel: HomeViewModel
): List<CounterfactualDisplayFeature> {
    if (rawFeatures.isEmpty()) {
        return emptyList()
    }

    val displayFeatures = mutableListOf<CounterfactualDisplayFeature>()
    val smokingStatusFeature = rawFeatures.firstOrNull { it.featureName == "smoking_status" }
    val brinkmanFeature = rawFeatures.firstOrNull { it.featureName == "brinkman_index" }
    val handledFeatureNames = mutableSetOf<String>()

    buildSmokingDisplayFeature(
        smokingStatusFeature = smokingStatusFeature,
        brinkmanFeature = brinkmanFeature,
        viewModel = viewModel
    )?.let { smokingFeature ->
        displayFeatures += smokingFeature
        handledFeatureNames += listOf("smoking_status", "brinkman_index")
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
    brinkmanFeature: CounterfactualChangedFeature?,
    viewModel: HomeViewModel
): CounterfactualDisplayFeature? {
    val baselineSmokingStatus = viewModel.smokingStatus.value.toIntOrNull() ?: 0
    if (baselineSmokingStatus != 2) {
        return null
    }

    val currentDailyCigarettes = currentSmokingDailyBaseline(viewModel)
    val yearsOfSmoking = smokingYearsOfExposure(
        currentAge = viewModel.baselineAge.value,
        ageOfSmoking = viewModel.profileAgeOfSmoking.value
    )
    val baselineCategory = viewModel.brinkmanScore.value
    val rangeMax = max(10, currentDailyCigarettes.coerceAtLeast(0)) + 10
    val safeRange = 0.0 to rangeMax.toDouble()

    val smokingStatusTarget = smokingStatusFeature?.candidateValue?.toInt()
    if (smokingStatusTarget == 1) {
        return CounterfactualDisplayFeature(
            label = "Kebiasaan Merokok",
            baselineNumeric = currentDailyCigarettes.toDouble(),
            candidateNumeric = 0.0,
            range = safeRange,
            baselineText = smokingBaselineDisplayText(
                currentDailyCigarettes = currentDailyCigarettes,
                baselineCategory = baselineCategory,
                yearsOfSmoking = yearsOfSmoking
            ),
            candidateText = "0 batang per hari",
            chipText = "berhenti merokok",
            accentColor = Color(0xFF0F766E),
            detailTitle = "Interpretasi perubahan",
            detailDescription = "Skenario ini mengarah ke berhenti merokok sepenuhnya. Status merokok berubah dari masih aktif menjadi sudah berhenti."
        )
    }

    val targetBrinkmanCategory = brinkmanFeature?.candidateValue?.toInt() ?: return null
    val targetRange = cigarettesRangeForBrinkmanCategory(
        category = targetBrinkmanCategory,
        yearsOfSmoking = yearsOfSmoking
    ) ?: return null

    val targetDailyCigarettes = derivePreferredSmokingDailyTarget(
        currentDailyCigarettes = currentDailyCigarettes,
        targetRange = targetRange
    )
    val candidateText = smokingTargetDisplayText(
        currentDailyCigarettes = currentDailyCigarettes,
        targetRange = targetRange,
        preferredTarget = targetDailyCigarettes
    ) ?: return null

    val reductionChip = smokingReductionChip(
        currentDailyCigarettes = currentDailyCigarettes,
        targetRange = targetRange,
        preferredTarget = targetDailyCigarettes
    )
    return CounterfactualDisplayFeature(
        label = "Konsumsi Rokok Harian",
        baselineNumeric = currentDailyCigarettes.toDouble(),
        candidateNumeric = (targetDailyCigarettes ?: targetRange.maxDaily ?: targetRange.minDaily).toDouble(),
        range = safeRange,
        baselineText = smokingBaselineDisplayText(
            currentDailyCigarettes = currentDailyCigarettes,
            baselineCategory = baselineCategory,
            yearsOfSmoking = yearsOfSmoking
        ),
        candidateText = candidateText,
        chipText = reductionChip,
        accentColor = Color(0xFF0F766E),
        detailTitle = "Interpretasi perubahan",
        detailDescription = buildSmokingReductionDescription(
            currentDailyCigarettes = currentDailyCigarettes,
            yearsOfSmoking = yearsOfSmoking,
            targetRange = targetRange,
            preferredTarget = targetDailyCigarettes
        )
    )
}

private fun buildGenericDisplayFeature(
    feature: CounterfactualChangedFeature,
    heightCm: Int,
    currentWeightKg: Int
): CounterfactualDisplayFeature? {
    val baseline = feature.baselineValue ?: return null
    val candidate = feature.candidateValue ?: return null
    val delta = feature.delta ?: (candidate - baseline)
    val accentColor = if (delta >= 0) Color(0xFF1D4ED8) else Color(0xFF0F766E)
    val bmiTranslation = bmiTranslation(
        featureName = feature.featureName,
        baselineValue = baseline,
        candidateValue = candidate,
        heightCm = heightCm,
        currentWeightKg = currentWeightKg
    )

    return CounterfactualDisplayFeature(
        label = featureLabel(feature.featureName),
        baselineNumeric = baseline,
        candidateNumeric = candidate,
        range = featureRange(feature.featureName),
        baselineText = formatFeatureValue(feature.featureName, baseline),
        candidateText = formatFeatureValue(feature.featureName, candidate),
        chipText = bmiTranslation?.chipText ?: formatDeltaDescription(feature.featureName, delta),
        accentColor = accentColor,
        detailTitle = bmiTranslation?.let { "Terjemahan BMI ke berat badan" },
        detailDescription = bmiTranslation?.description
    )
}

private fun currentSmokingDailyBaseline(viewModel: HomeViewModel): Int {
    return viewModel.smokeAverage.value
        .takeIf { it > 0 }
        ?: viewModel.profileSmokeCount.value.coerceAtLeast(0)
}

private fun smokingYearsOfExposure(
    currentAge: Int,
    ageOfSmoking: Int
): Int? {
    if (currentAge <= 0 || ageOfSmoking <= 0) {
        return null
    }
    return (currentAge - ageOfSmoking).coerceAtLeast(1)
}

private fun smokingBaselineDisplayText(
    currentDailyCigarettes: Int,
    baselineCategory: Int,
    yearsOfSmoking: Int?
): String {
    if (currentDailyCigarettes > 0) {
        return "$currentDailyCigarettes batang per hari"
    }

    val range = cigarettesRangeForBrinkmanCategory(
        category = baselineCategory,
        yearsOfSmoking = yearsOfSmoking
    ) ?: return "Belum ada baseline batang per hari"

    return quantityRangeText(range)
}

private fun derivePreferredSmokingDailyTarget(
    currentDailyCigarettes: Int,
    targetRange: SmokingDailyRange
): Int? {
    val upperBound = targetRange.maxDaily
    if (currentDailyCigarettes <= 0 || upperBound == null) {
        return upperBound
    }

    return min(currentDailyCigarettes - 1, upperBound.coerceAtLeast(1))
        .takeIf { it > 0 }
}

private fun smokingTargetDisplayText(
    currentDailyCigarettes: Int,
    targetRange: SmokingDailyRange,
    preferredTarget: Int?
): String? {
    if (preferredTarget != null && currentDailyCigarettes > 0 && preferredTarget < currentDailyCigarettes) {
        return "sekitar $preferredTarget batang per hari"
    }

    return when {
        targetRange.maxDaily == null -> "minimal ${targetRange.minDaily} batang per hari"
        targetRange.minDaily == targetRange.maxDaily -> "${targetRange.minDaily} batang per hari"
        else -> "${targetRange.minDaily}-${targetRange.maxDaily} batang per hari"
    }
}

private fun smokingReductionChip(
    currentDailyCigarettes: Int,
    targetRange: SmokingDailyRange,
    preferredTarget: Int?
): String {
    if (preferredTarget != null && currentDailyCigarettes > preferredTarget) {
        return "kurangi ${currentDailyCigarettes - preferredTarget} batang/hari"
    }

    return targetRange.maxDaily?.let { "target maks. $it batang/hari" }
        ?: "kurangi bertahap"
}

private fun buildSmokingReductionDescription(
    currentDailyCigarettes: Int,
    yearsOfSmoking: Int?,
    targetRange: SmokingDailyRange,
    preferredTarget: Int?
): String {
    val targetText = smokingTargetDisplayText(
        currentDailyCigarettes = currentDailyCigarettes,
        targetRange = targetRange,
        preferredTarget = preferredTarget
    )

    val yearsContext = if (yearsOfSmoking != null) {
        " Dengan riwayat merokok sekitar $yearsOfSmoking tahun, target kuantitas ini membantu menekan paparan rokok jangka panjang ke tingkat yang lebih aman."
    } else {
        " Target kuantitas ini dihitung dari baseline rokok yang tersedia."
    }

    return if (currentDailyCigarettes > 0) {
        "Skenario ini tidak mengharuskan berhenti total. Fokus utamanya adalah mengurangi konsumsi dari $currentDailyCigarettes menjadi $targetText.$yearsContext"
    } else {
        "Skenario ini mengarahkan konsumsi rokok harian ke $targetText.$yearsContext"
    }
}

private fun cigarettesRangeForBrinkmanCategory(
    category: Int,
    yearsOfSmoking: Int?
): SmokingDailyRange? {
    if (yearsOfSmoking == null || yearsOfSmoking <= 0) {
        return null
    }

    return when (category) {
        0 -> SmokingDailyRange(minDaily = 0, maxDaily = 0)
        1 -> SmokingDailyRange(
            minDaily = 1,
            maxDaily = max(1, 199 / yearsOfSmoking)
        )
        2 -> {
            val minDaily = max(1, ceil(200.0 / yearsOfSmoking).toInt())
            val maxDaily = max(minDaily, 599 / yearsOfSmoking)
            SmokingDailyRange(minDaily = minDaily, maxDaily = maxDaily)
        }
        3 -> SmokingDailyRange(
            minDaily = max(1, ceil(600.0 / yearsOfSmoking).toInt()),
            maxDaily = null
        )
        else -> null
    }
}

private fun quantityRangeText(range: SmokingDailyRange): String {
    return when {
        range.maxDaily == null -> "sekitar ${range.minDaily}+ batang per hari"
        range.minDaily == range.maxDaily -> "${range.minDaily} batang per hari"
        else -> "${range.minDaily}-${range.maxDaily} batang per hari"
    }
}

@Composable
private fun ScenarioChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor
        )
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
        "smoking_behavior" -> "Kebiasaan Merokok"
        "smoking_status" -> "Status Merokok"
        "is_cholesterol" -> "Kolesterol"
        "is_hypertension" -> "Hipertensi"
        "moderate_physical_activity_frequency" -> "Aktivitas Fisik"
        "brinkman_index" -> "Konsumsi Rokok Harian"
        "is_bloodline" -> "Riwayat Keluarga"
        "is_macrosomic_baby" -> "Riwayat Bayi Makrosomia"
        "age" -> "Usia"
        else -> name
    }
}

private fun formatFeatureValue(name: String, value: Double): String {
    return when (name) {
        "BMI" -> String.format("%.2f kg/m²", value)
        "age" -> "${value.toInt()} tahun"
        "moderate_physical_activity_frequency" -> "${value.toInt()} hari / minggu"
        "smoking_status" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> "Sudah berhenti"
            2 -> "Masih aktif"
            else -> value.toInt().toString()
        }

        "brinkman_index" -> when (value.toInt()) {
            0 -> "Paparan sangat rendah"
            1 -> "Paparan ringan"
            2 -> "Paparan sedang"
            3 -> "Paparan tinggi"
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
        "BMI" -> if (delta < 0) {
            "turun ${String.format("%.2f", abs(delta))} kg/m²"
        } else {
            "naik ${String.format("%.2f", delta)} kg/m²"
        }

        "moderate_physical_activity_frequency" -> if (delta < 0) {
            "berkurang ${abs(delta).toInt()} hari/minggu"
        } else {
            "bertambah ${delta.toInt()} hari/minggu"
        }

        else -> if (delta < 0) "menurun" else "meningkat"
    }
}

private fun featureRange(name: String): Pair<Double, Double> {
    return when (name) {
        "age" -> 18.0 to 100.0
        "BMI" -> 10.0 to 60.0
        "smoking_status" -> 0.0 to 2.0
        "is_cholesterol" -> 0.0 to 1.0
        "is_macrosomic_baby" -> 0.0 to 2.0
        "moderate_physical_activity_frequency" -> 0.0 to 14.0
        "is_bloodline" -> 0.0 to 1.0
        "brinkman_index" -> 0.0 to 3.0
        "is_hypertension" -> 0.0 to 1.0
        else -> 0.0 to 100.0
    }
}

private fun toHighRiskPercentage(lowRiskProbability: Double): Double {
    return (1.0 - lowRiskProbability) * 100
}

private fun formatPercentage(value: Double?): String {
    return value?.let { String.format("%.1f%%", it) } ?: "-"
}

private data class BmiTranslation(
    val chipText: String,
    val description: String
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
    val weightAction = if (estimatedDelta < 0) "turun" else "naik"
    val weightDeltaAbs = abs(estimatedDelta)

    val chipText = if (weightDeltaAbs < 0.05) {
        "berat relatif tetap"
    } else {
        "sekitar $weightAction ${String.format("%.1f", weightDeltaAbs)} kg"
    }

    val description = buildString {
        append(
            "Dengan tinggi badan $heightCm cm, BMI ${String.format("%.2f", baselineValue)} "
        )
        append(
            "setara kira-kira dengan berat ${String.format("%.1f", baselineWeightFromBmi)} kg. "
        )
        append(
            "Target BMI ${String.format("%.2f", candidateValue)} berarti berat badan "
        )
        append(
            "sekitar ${String.format("%.1f", targetWeight)} kg"
        )
        if (weightDeltaAbs >= 0.05) {
            append(
                ", atau sekitar $weightAction ${String.format("%.1f", weightDeltaAbs)} kg dari berat Anda saat ini."
            )
        } else {
            append(" dengan perubahan berat yang sangat kecil dari kondisi saat ini.")
        }
    }

    return BmiTranslation(
        chipText = chipText,
        description = description
    )
}
