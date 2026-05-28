package com.itb.diabetify.presentation.guide

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.presentation.guide.components.FAQCard
import com.itb.diabetify.presentation.guide.components.GuideCard
import com.itb.diabetify.presentation.guide.components.SectionHeader
import com.itb.diabetify.presentation.guide.components.TipsCard
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import androidx.navigation.NavController
import com.itb.diabetify.presentation.navgraph.Route

@Composable
fun GuideScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(id = R.color.primary),
                                colorResource(id = R.color.primary).copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Panduan",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Guide
                SectionHeader(
                    title = "Diabetes dan XAI",
                    subtitle = "Pelajari dasar-dasar diabetes dan bagaimana XAI dapat membantu dalam pengelolaannya.",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val chunkedCards = guideCards.chunked(2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    chunkedCards.forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowCards.forEach { cardData ->
                                GuideCard(
                                    guideCardData = cardData,
                                    modifier = Modifier.weight(1f),
                                    onGuideClick = { guideId ->
                                        navController.navigate(Route.GuideDetailScreen.createRoute(guideId))
                                    }
                                )
                            }

                            if (rowCards.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.primary).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Tips
                SectionHeader(
                    title = "Tips Kesehatan",
                    subtitle = "Saran praktis untuk menjaga kesehatan Anda dalam mengelola diabetes.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                val pageCount = ceil(tipsCard.size / 2f).toInt()
                val pagerState = rememberPagerState(pageCount = { pageCount })
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5000)
                        val nextPage = (pagerState.currentPage + 1) % pageCount
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        val startIndex = page * 2

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (startIndex < tipsCard.size) {
                                Box(modifier = Modifier.weight(1f)) {
                                    TipsCard(
                                        tipsCardData = tipsCard[startIndex],
                                        onTipsClick = { tipsId ->
                                            navController.navigate(Route.TipsDetailScreen.createRoute(tipsId))
                                        }
                                    )
                                }
                            }

                            if (startIndex + 1 < tipsCard.size) {
                                Box(modifier = Modifier.weight(1f)) {
                                    TipsCard(
                                        tipsCardData = tipsCard[startIndex + 1],
                                        onTipsClick = { tipsId ->
                                            navController.navigate(Route.TipsDetailScreen.createRoute(tipsId))
                                        }
                                    )
                                }
                            } else if (startIndex < tipsCard.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until pageCount) {
                            val dotColor = if (i == pagerState.currentPage) Color.DarkGray else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color = dotColor, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.primary).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // FAQ
                SectionHeader(
                    title = "FAQ",
                    subtitle = "Pertanyaan yang sering diajukan tentang aplikasi.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                faqCards.forEach { faqCardData ->
                    FAQCard(
                        faqCardData = faqCardData
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
