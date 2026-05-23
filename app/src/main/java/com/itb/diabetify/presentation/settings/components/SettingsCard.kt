package com.itb.diabetify.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.SecondaryButton
import com.itb.diabetify.presentation.settings.CardData
import com.itb.diabetify.presentation.settings.ContentData
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun SettingsCard(
    cardData: CardData,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 15.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier,
                    text = cardData.title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colorResource(id = R.color.primary),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            cardData.contents.forEachIndexed { index, content ->
                ContentItem(
                    contentData = content,
                    onClick = content.onClick
                )

                if (index < cardData.contents.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    email: String,
    onEditClick: () -> Unit,
    actionLabel: String = "Edit Profil"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.primary).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_picture),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User details
            Text(
                text = name,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorResource(id = R.color.primary),
                modifier = Modifier.testTag("ProfileCardName")
            )

            Text(
                text = email,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.testTag("ProfileCardEmail")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit button
            SecondaryButton(
                text = actionLabel,
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("EditProfilButton")
            )
        }
    }
}

@Composable
fun HealthProfileCard(
    summary: String,
    smokingSummary: String,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colorResource(id = R.color.primary).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = "Profil Kesehatan",
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.primary))
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "Profil Kesehatan",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = "Baseline medis yang dipakai untuk prediksi",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = summary,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = smokingSummary,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colorResource(id = R.color.gray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = "Lihat Profil Kesehatan",
                onClick = onViewClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ContentItem(
    contentData: ContentData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.gray).copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = contentData.icon),
                contentDescription = contentData.name,
                modifier = Modifier
                    .size(35.dp)
                    .padding(end = 10.dp),
                colorFilter = ColorFilter.tint(colorResource(id = R.color.primary))
            )

            Text(
                text = contentData.name,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorResource(id = R.color.gray),
                textAlign = TextAlign.Start
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Arrow Right",
            tint = colorResource(id = R.color.primary)
        )
    }
}
