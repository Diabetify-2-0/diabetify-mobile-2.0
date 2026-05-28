package com.itb.diabetify.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.itb.diabetify.ui.theme.poppinsFontFamily

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
                .padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.primary).copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart_bold),
                        contentDescription = "Profil Kesehatan",
                        modifier = Modifier.size(38.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.primary))
                    )
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Lihat dan kelola profil kesehatan Anda di sini",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            SecondaryButton(
                text = "Lihat Detail Kesehatan",
                onClick = onViewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("LihatDetailKesehatanButton")
            )
        }
    }
}

