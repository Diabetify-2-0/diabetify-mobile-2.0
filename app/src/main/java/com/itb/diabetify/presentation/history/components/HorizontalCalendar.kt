package com.itb.diabetify.presentation.history.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.DatePickerModal
import com.itb.diabetify.ui.theme.poppinsFontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.stream.Collectors
import java.util.stream.Stream

val INDONESIAN_LOCALE = Locale("id", "ID")

@SuppressLint("NewApi")
@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    onDateClickListener: (LocalDate) -> Unit
) {
    val dataSource = CalendarDataSource()
    var data by remember {
        mutableStateOf(
            dataSource.getMonthData(
                dataSource.today,
                lastSelectedDate = dataSource.today
            )
        )
    }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var currentMonth by remember { mutableStateOf(dataSource.today.month) }
    var currentYear by remember { mutableIntStateOf(dataSource.today.year) }

    val showDatePicker = remember { mutableStateOf(false) }

    LaunchedEffect(data.selectedDate.date) {
        val index = data.visibleDates.indexOfFirst { it.date.isEqual(data.selectedDate.date) }
        if (index >= 0) {
            coroutineScope.launch {
                val layoutInfo = lazyListState.layoutInfo
                val totalWidth = layoutInfo.viewportSize.width
                val itemWidth = 160
                val centerOffset = (totalWidth / 2) - (itemWidth / 2)

                lazyListState.animateScrollToItem(
                    index = index,
                    scrollOffset = -centerOffset
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        val todayIndex = data.visibleDates.indexOfFirst { it.isToday }
        if (todayIndex >= 0) {
            coroutineScope.launch {
                val layoutInfo = lazyListState.layoutInfo
                val totalWidth = layoutInfo.viewportSize.width
                val itemWidth = 160
                val centerOffset = (totalWidth / 2) - (itemWidth / 2)

                lazyListState.scrollToItem(
                    index = todayIndex,
                    scrollOffset = -centerOffset
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Header(
            data = data,
            onPrevClickListener = { startDate ->
                val newMonth = startDate.minusMonths(1)
                val lastDayOfNewMonth = newMonth.withDayOfMonth(newMonth.lengthOfMonth())
                data = dataSource.getMonthData(newMonth, lastDayOfNewMonth)
                currentMonth = newMonth.month
                currentYear = newMonth.year

                onDateClickListener(lastDayOfNewMonth)
            },
            onNextClickListener = { startDate ->
                val newMonth = startDate.plusMonths(1)
                val firstDayOfNewMonth = newMonth.withDayOfMonth(1)
                data = dataSource.getMonthData(newMonth, firstDayOfNewMonth)
                currentMonth = newMonth.month
                currentYear = newMonth.year

                onDateClickListener(firstDayOfNewMonth)
            },
            modifier = Modifier.padding(horizontal = 20.dp),
            onDatePickerClickListener = {
                showDatePicker.value = true
            }
        )
        Content(
            data = data,
            lazyListState = lazyListState,
        ) { date ->
            if (!date.isFutureDate) {
                if (date.date.month != currentMonth || date.date.year != currentYear) {
                    data = dataSource.getMonthData(date.date, date.date)
                    currentMonth = date.date.month
                    currentYear = date.date.year
                } else {
                    data = data.copy(
                        selectedDate = date,
                        visibleDates = data.visibleDates.map {
                            it.copy(
                                isSelected = it.date.isEqual(date.date)
                            )
                        }
                    )
                }
                onDateClickListener(date.date)
            }
        }

        // Date picker dialog
        if (showDatePicker.value) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    dateMillis?.let {
                        val date = Date(it)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val formattedDate = formatter.format(date)
                        val localDate = LocalDate.parse(
                            formattedDate,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy", INDONESIAN_LOCALE)
                        )
                        if (!localDate.isAfter(dataSource.today)) {
                            data = dataSource.getMonthData(localDate, localDate)
                            currentMonth = localDate.month
                            currentYear = localDate.year
                            onDateClickListener(localDate)
                        }
                    }
                },
                onDismiss = { showDatePicker.value = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Header(
    data: CalendarUiModel,
    onPrevClickListener: (LocalDate) -> Unit,
    onNextClickListener: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onDatePickerClickListener: () -> Unit
) {
    val nextMonth = data.selectedDate.date.plusMonths(1)
    val today = LocalDate.now()
    val isNextMonthDisabled = nextMonth.withDayOfMonth(1).isAfter(today)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Previous month",
            tint = colorResource(id = R.color.primary),
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    onPrevClickListener(data.selectedDate.date)
                }
        )
        Button(
            modifier = modifier
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Gray.copy(alpha = 0.5f)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.white),
            ),
            onClick = onDatePickerClickListener,
        ) {
            Text(
                text = data.selectedDate.date.format(
                    DateTimeFormatter.ofPattern("MMMM, yyyy", INDONESIAN_LOCALE)
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = colorResource(id = R.color.primary),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next month",
            tint = if (isNextMonthDisabled) {
                colorResource(id = R.color.primary).copy(alpha = 0.3f)
            } else {
                colorResource(id = R.color.primary)
            },
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (isNextMonthDisabled) {
                        Modifier
                    } else {
                        Modifier.clickable {
                            onNextClickListener(data.selectedDate.date)
                        }
                    }
                )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Content(
    data: CalendarUiModel,
    lazyListState: LazyListState,
    onDateClickListener: (CalendarUiModel.Date) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth(),
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(data.visibleDates) { date ->
            ContentItem(
                date = date,
                onClickListener = onDateClickListener
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContentItem(
    date: CalendarUiModel.Date,
    onClickListener: (CalendarUiModel.Date) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("DateItem_${date.date.dayOfMonth}")
            .shadow(
                elevation = 40.dp,
                shape = RoundedCornerShape(15.dp),
                spotColor = Color.Gray.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(15.dp))
            .background(colorResource(id = R.color.white))
    ) {
        Column(modifier = Modifier
            .width(60.dp)
            .then(
                if (date.isFutureDate) {
                    Modifier
                } else {
                    Modifier.noRippleEffect {
                        onClickListener(date)
                    }
                }
            )
            .background(
                color = if (date.isFutureDate) {
                    colorResource(R.color.white).copy(alpha = 0.5f)
                } else {
                    colorResource(R.color.white)
                },
                shape = RoundedCornerShape(15.dp)
            )
            .border(
                if (date.isSelected) (1.5).dp else (0.5).dp,
                if (date.isSelected) colorResource(R.color.primary) else colorResource(R.color.white),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(15.dp)),
            verticalArrangement = Arrangement.Center) {
            Text(
                text = date.day,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = 14.sp,
                color = if (date.isFutureDate) {
                    colorResource(id = R.color.primary).copy(alpha = 0.4f)
                } else {
                    colorResource(id = R.color.primary)
                }
            )
            Text(
                text = date.date.dayOfMonth.toString(),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = if (date.isFutureDate) {
                    colorResource(id = R.color.primary).copy(alpha = 0.4f)
                } else {
                    colorResource(id = R.color.primary)
                }
            )
        }
    }
}

class CalendarDataSource {

    val today: LocalDate
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return LocalDate.now()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getData(startDate: LocalDate = today, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfWeek = startDate.with(DayOfWeek.MONDAY)
        val endDayOfWeek = firstDayOfWeek.plusDays(7)
        val visibleDates = getDatesBetween(firstDayOfWeek, endDayOfWeek)
        return toUiModel(visibleDates, lastSelectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthData(date: LocalDate, lastSelectedDate: LocalDate): CalendarUiModel {
        val firstDayOfMonth = date.withDayOfMonth(1)
        val lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        val visibleDates = getDatesBetween(firstDayOfMonth, lastDayOfMonth.plusDays(1))
        return toUiModel(visibleDates, lastSelectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val numOfDays = ChronoUnit.DAYS.between(startDate, endDate)
        return Stream.iterate(startDate) { date ->
            date.plusDays(1)
        }
            .limit(numOfDays)
            .collect(Collectors.toList())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toUiModel(
        dateList: List<LocalDate>,
        lastSelectedDate: LocalDate
    ): CalendarUiModel {
        return CalendarUiModel(
            selectedDate = toItemUiModel(lastSelectedDate, true),
            visibleDates = dateList.map {
                toItemUiModel(it, it.isEqual(lastSelectedDate))
            },
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toItemUiModel(date: LocalDate, isSelectedDate: Boolean) = CalendarUiModel.Date(
        isSelected = isSelectedDate,
        isToday = date.isEqual(today),
        date = date,
    )

}

data class CalendarUiModel(
    val selectedDate: Date,
    val visibleDates: List<Date>,
) {
    data class Date(
        val date: LocalDate,
        val isSelected: Boolean,
        val isToday: Boolean
    ) {
        @RequiresApi(Build.VERSION_CODES.O)
        val day: String = date.format(DateTimeFormatter.ofPattern("E", INDONESIAN_LOCALE))

        @RequiresApi(Build.VERSION_CODES.O)
        val isFutureDate: Boolean = date.isAfter(LocalDate.now())
    }
}

private fun Modifier.noRippleEffect(
    onClick: () -> Unit
) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}