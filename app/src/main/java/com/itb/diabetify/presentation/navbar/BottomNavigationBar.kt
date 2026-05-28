package com.itb.diabetify.presentation.navbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itb.diabetify.R
import com.itb.diabetify.presentation.navbar.add_activity.AddActionPopup
import com.itb.diabetify.presentation.navbar.add_activity.AddActivityViewModel

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel,
    addActivityViewModel: AddActivityViewModel,
    onOpenHealthProfile: () -> Unit,
    backgroundColor: Color = Color.White,
    selectedColor: Color = colorResource(id = R.color.primary),
    unselectedColor: Color = colorResource(id = R.color.tertiary),
    indicatorColor: Color = colorResource(id = R.color.primary),
    elevation: Dp = 8.dp,
    onItemSelected: (String) -> Unit = {},
) {
    // States
    val selectedItem = viewModel.selectedItem.value
    val items = viewModel.navigationItems
    val middleIndex = items.size / 2
    val showPopup by viewModel.showPopUp
    val hasDuePlannerCheckIn = addActivityViewModel.plannerCheckInActions().any { it.isDue }

    // Popup
    if (showPopup) {
        AddActionPopup(
            isVisible = showPopup,
            onDismissRequest = { viewModel.setShowPopUp(false) },
            viewModel = addActivityViewModel,
            onOpenHealthProfile = {
                viewModel.setShowPopUp(false)
                onOpenHealthProfile()
            }
        )
    }

    Box(
        modifier = modifier.background(color = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                containerColor = backgroundColor,
                tonalElevation = 0.dp
            ) {
                items.forEachIndexed { index, item ->
                    if (index == middleIndex) {
                        NavigationBarItem(
                            selected = false,
                            onClick = { },
                            icon = {
                                Spacer(modifier = Modifier.size(24.dp))
                            },
                            label = { Text("") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedColor,
                                unselectedIconColor = unselectedColor,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }

                    val iconSize by animateDpAsState(
                        targetValue = if (selectedItem == item.route) 28.dp else 24.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconSize"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (selectedItem == item.route) selectedColor else unselectedColor,
                        animationSpec = tween(300),
                        label = "iconColor"
                    )

                    NavigationBarItem(
                        selected = selectedItem == item.route,
                        onClick = {
                            viewModel.onNavigationItemSelected(item.route)
                            onItemSelected(item.route)
                        },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.contentDescription,
                                    modifier = Modifier.size(iconSize),
                                    tint = iconColor
                                )

                                if (selectedItem == item.route) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor)
                                    )
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .shadow(8.dp, CircleShape)
        ) {
            FloatingActionButton(
                onClick = { viewModel.setShowPopUp(true) },
                shape = CircleShape,
                containerColor = colorResource(id = R.color.primary),
                contentColor = colorResource(id = R.color.white)
            ) {
                Icon(
                    modifier = Modifier.scale(1.5f),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add"
                )
            }

            if (hasDuePlannerCheckIn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White
                    )
                }
            }
        }
    }
}
