package com.itb.diabetify.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean? = true,
    leftImageResId: Int? = null,
    rightImageResId: Int? = null,
    isLoading: Boolean = false
) {
    val isEnabled = (enabled ?: true) && !isLoading
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            colorResource(id = R.color.primary),
            colorResource(id = R.color.primary).copy(alpha = 0.8f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isEnabled) gradient else Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary).copy(alpha = 0.5f),
                            colorResource(id = R.color.primary).copy(alpha = 0.4f)
                        )
                    )
                )
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (leftImageResId != null) {
                        Image(
                            painter = painterResource(id = leftImageResId),
                            contentDescription = "Left Image",
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                        )
                    }

                    Text(
                        text = text,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    if (rightImageResId != null) {
                        Image(
                            painter = painterResource(id = rightImageResId),
                            contentDescription = "Right Image",
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean? = true
) {
    val isEnabled = (enabled ?: true)
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            colorResource(id = R.color.primary),
            colorResource(id = R.color.primary).copy(alpha = 0.8f)
        )
    )

    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = if (isEnabled) gradient else Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(id = R.color.primary).copy(alpha = 0.5f),
                            colorResource(id = R.color.primary).copy(alpha = 0.4f)
                        )
                    )
                )
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colorResource(id = R.color.white),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomizableButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean? = true,
    leftImageResId: Int? = null,
    rightImageResId: Int? = null,
    isLoading: Boolean = false,
    backgroundColor: Color = colorResource(id = R.color.primary),
    backgroundColorSecondary: Color? = null,
    textColor: Color = Color.White,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    loadingIndicatorColor: Color = Color.White,
    enabledShadowElevation: Dp = 6.dp,
    disabledShadowElevation: Dp = 2.dp,
    disabledAlpha: Float = 0.5f
) {
    val isEnabled = (enabled ?: true) && !isLoading

    val backgroundBrush = when {
        !isEnabled -> {
            if (backgroundColorSecondary != null) {
                Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = disabledAlpha),
                        backgroundColorSecondary.copy(alpha = disabledAlpha * 0.8f)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = disabledAlpha),
                        backgroundColor.copy(alpha = disabledAlpha * 0.8f)
                    )
                )
            }
        }
        backgroundColorSecondary != null -> {
            Brush.horizontalGradient(
                colors = listOf(backgroundColor, backgroundColorSecondary)
            )
        }
        else -> {
            Brush.horizontalGradient(
                colors = listOf(
                    backgroundColor,
                    backgroundColor.copy(alpha = 0.8f)
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (isEnabled) enabledShadowElevation else disabledShadowElevation,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = borderColor?.let {
            BorderStroke(
                width = borderWidth,
                color = it.copy(alpha = if (isEnabled) 1f else disabledAlpha)
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = backgroundBrush)
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = loadingIndicatorColor,
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (leftImageResId != null) {
                        Image(
                            painter = painterResource(id = leftImageResId),
                            contentDescription = "Left Image",
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp),
                            alpha = if (isEnabled) 1f else disabledAlpha
                        )
                    }

                    Text(
                        text = text,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = textColor.copy(alpha = if (isEnabled) 1f else disabledAlpha),
                        textAlign = TextAlign.Center
                    )

                    if (rightImageResId != null) {
                        Image(
                            painter = painterResource(id = rightImageResId),
                            contentDescription = "Right Image",
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            alpha = if (isEnabled) 1f else disabledAlpha
                        )
                    }
                }
            }
        }
    }
}
