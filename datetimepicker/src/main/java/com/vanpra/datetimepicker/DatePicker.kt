package com.vanpra.datetimepicker


import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.layout.ColumnScope.gravity
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ChevronLeft
import androidx.ui.material.icons.filled.ChevronRight
import androidx.ui.material.ripple.ripple
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight.Companion.W400
import androidx.ui.text.font.FontWeight.Companion.W500
import androidx.ui.text.font.FontWeight.Companion.W600
import androidx.ui.text.font.FontWeight.Companion.W700
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import androidx.ui.util.fastForEach
import androidx.ui.util.fastForEachIndexed
import java.time.LocalDate
import java.time.YearMonth

/**
 * @brief A date picker dialog
 *
 * @param showing mutable state which controls if the dialog is showing to the user
 * @param initialDate The time to be shown to the user when the dialog is first shown.
 * Defaults to the current date if this is not set
 * @param onComplete callback with a LocalDate object when the user completes their input
 */
@Composable
fun DatePicker(
    showing: MutableState<Boolean>,
    initialDate: LocalDate = LocalDate.now(),
    onCancel: () -> Unit,
    onComplete: (LocalDate) -> Unit
) {
    val currentDate = remember { initialDate }
    val selectedDate = state { currentDate }

    if (showing.value) {
        ThemedDialog(onCloseRequest = { showing.value = false }) {
            Column(Modifier.drawBackground(MaterialTheme.colors.background)) {
                DialogTitle("Select a date", Modifier.padding(top = 16.dp, bottom = 16.dp))
                DatePickerLayout(currentDate = currentDate, selectedDate = selectedDate)
                ButtonLayout(
                    confirmText = "Ok",
                    onConfirm = {
                        showing.value = false
                        onComplete(selectedDate.value)
                    },
                    onCancel = {
                        showing.value = false
                        onCancel()
                    })
            }
        }
    }
}

@Composable
internal fun DatePickerLayout(
    modifier: Modifier = Modifier,
    selectedDate: MutableState<LocalDate>,
    currentDate: LocalDate
) {
    Column(modifier.drawBackground(MaterialTheme.colors.background)) {
        DateTitle(selectedDate)
        ViewPager(Modifier.drawBackground(Color.Transparent)) {
            val newDate = remember(index) {
                currentDate.plusMonths(index.toLong())
            }
            val dates = remember(newDate) { getDates(newDate) }
            val yearMonth = remember(newDate) { newDate.yearMonth }

            Column {
                MonthTitle(this@ViewPager, newDate.month.fullLocalName, newDate.year.toString())
                DaysTitle()
                DateLayout(dates, yearMonth, selectedDate)
            }
        }
    }
}

@Composable
private fun DateLayout(
    month: List<Int>,
    yearMonth: YearMonth,
    selected: MutableState<LocalDate>
) {
    val check = remember(selected.value, yearMonth) {
        selected.value.monthValue == yearMonth.monthValue
                && selected.value.year == yearMonth.year
    }

    val textStyle = TextStyle(fontSize = 12.sp, fontWeight = W400)
    val boxSize = 35.dp
    val boxSizePx = with(DensityAmbient.current) { boxSize.toIntPx() }

    val verticalSpacing = 40
    val maxRows = 5
    val layoutHeight = maxRows * boxSizePx + (verticalSpacing * (maxRows - 1))

    Layout(
        children = {
            month.fastForEach {
                if (it != -1) {
                    var selectedModifier: Modifier = Modifier
                    var textColor: Color = MaterialTheme.colors.onBackground

                    if (check && selected.value.dayOfMonth == it) {
                        selectedModifier = Modifier.drawBackground(
                            MaterialTheme.colors.primary.copy(0.7f),
                            CircleShape
                        )
                        textColor = Color.White
                    }

                    Text(
                        it.toString(),
                        modifier = Modifier.size(boxSize)
                            .clickable(onClick = {
                                selected.value =
                                    LocalDate.of(
                                        yearMonth.year,
                                        yearMonth.month,
                                        it
                                    )
                            }, indication = null)
                            .plus(selectedModifier)
                            .wrapContentSize(Alignment.Center),
                        style = textStyle,
                        color = textColor
                    )
                } else {
                    Box(Modifier.size(boxSize))
                }
            }
        },
        modifier = Modifier.padding(
            top = 8.dp,
            bottom = 8.dp,
            start = 12.dp,
            end = 12.dp
        )
            .fillMaxWidth()
            .gravity(Alignment.CenterHorizontally)
    ) { measurables, constraints, _ ->
        val horizontalSpacing = (constraints.maxWidth - (boxSizePx * 7)) / 6

        layout(constraints.maxWidth, layoutHeight) {
            measurables
                .map {
                    it.measure(
                        Constraints(
                            maxHeight = boxSizePx,
                            maxWidth = boxSizePx
                        )
                    )
                }
                .fastForEachIndexed { index, it ->
                    it.place(
                        x = (index % 7) * (boxSizePx + horizontalSpacing),
                        y = (index / 7) * (boxSizePx + verticalSpacing)
                    )
                }
        }
    }
}

@Composable
private fun DaysTitle() {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            .fillMaxWidth()
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").fastForEach {
            Text(
                it,
                modifier = Modifier.drawOpacity(0.8f),
                style = TextStyle(fontSize = 14.sp, fontWeight = W600),
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

@Composable
private fun MonthTitle(scope: ViewPagerScope, month: String, year: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Box(
            Modifier.clip(CircleShape).clickable(
                    onClick = { scope.previous() },
                    enabled = true
                )
        ) {
            Image(
                Icons.Default.ChevronLeft,
                modifier = Modifier.padding(start = 24.dp)
                    .wrapContentWidth(Alignment.Start),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
            )
        }

        Text(
            "$month $year",
            modifier = Modifier.weight(3f).wrapContentSize(Alignment.Center),
            style = TextStyle(fontSize = 16.sp, fontWeight = W500),
            color = MaterialTheme.colors.onBackground
        )

        Box(
            Modifier.clip(CircleShape)
                .clickable(
                    onClick = { scope.next() },
                    enabled = true
                )
        ) {
            Image(
                Icons.Default.ChevronRight,
                modifier = Modifier.padding(end = 24.dp)
                    .wrapContentWidth(Alignment.End),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
            )
        }
    }
}

@Composable
private fun DateTitle(selected: MutableState<LocalDate>) {
    val month = selected.value.month.shortLocalName
    val day = selected.value.dayOfWeek.shortLocalName

    Box(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = selected.value.year.toString(),
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.drawOpacity(0.8f).padding(bottom = 2.dp),
                style = TextStyle(fontSize = 18.sp, fontWeight = W700)
            )
            Text(
                text = "$day, $month ${selected.value.dayOfMonth}",
                color = MaterialTheme.colors.onPrimary,
                style = TextStyle(fontSize = 26.sp, fontWeight = W700)
            )
        }
    }
}

private fun getDates(date: LocalDate): List<Int> {
    val dates = mutableListOf<Int>()

    val firstDate = LocalDate.of(date.year, date.monthValue, 1)
    val firstDay = firstDate.dayOfWeek.value - 1
    val numDays = date.month.length(firstDate.isLeapYear)

    for (x in 0 until firstDay) {
        dates.add(-1)
    }

    for (x in 1..(7 - firstDay)) {
        dates.add(x)
    }


    for (x in 0 until 3) {
        for (y in 1..7) {
            dates.add(((7 - firstDay) + 7 * x + y))
        }
    }

    if ((7 - firstDay) + 21 != numDays) {
        for (x in ((7 - firstDay) + 22)..numDays) {
            dates.add(x)
        }
    }

    return dates
}