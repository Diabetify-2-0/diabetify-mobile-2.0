package com.itb.diabetify.presentation.home.risk_detail.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlinx.coroutines.async
import kotlin.math.roundToInt

@Composable
fun RiskScoreGauge(
    score: Int,
    animationDuration: Int = 2000,
) {
    val lowRiskColor = Color(0xFF8BC34A)
    val mediumRiskColor = Color(0xFFFFC107)
    val highRiskColor = Color(0xFFFA821F)
    val veryHighRiskColor = Color(0xFFF44336)

    val targetScore = score.coerceIn(0, 100)
    val normalizedTargetScore = targetScore / 100f

    val animatedPosition = remember { Animatable(0f) }
    val animatedScoreText = remember { Animatable(0f) }

    LaunchedEffect(targetScore) {
        val positionAnimation = async {
            animatedPosition.animateTo(
                targetValue = normalizedTargetScore,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = EaseInOut
                )
            )
        }

        val scoreAnimation = async {
            animatedScoreText.animateTo(
                targetValue = targetScore.toFloat(),
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = EaseInOut
                )
            )
        }

        positionAnimation.await()
        scoreAnimation.await()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("RiskScoreGauge"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to lowRiskColor,
                            0.35f to mediumRiskColor,
                            0.55f to highRiskColor,
                            0.70f to veryHighRiskColor,
                            1.0f to veryHighRiskColor
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (animatedPosition.value > 0) {
                Spacer(
                    modifier = Modifier
                        .weight(animatedPosition.value)
                        .height(1.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = colorResource(R.color.primary), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = animatedScoreText.value.roundToInt().toString(),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.white)
                )
            }

            if (animatedPosition.value < 1) {
                Spacer(
                    modifier = Modifier
                        .weight(1f - animatedPosition.value)
                        .height(1.dp)
                )
            }
        }
    }
}