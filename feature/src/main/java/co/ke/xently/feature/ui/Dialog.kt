package co.ke.xently.feature.ui

import android.text.format.DateFormat
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.common.KENYA
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

@Composable
fun rememberDatePickerDialog(
    @StringRes title: Int,
    select: Date? = null,
    bounds: CalendarConstraints? = null,
    onDateSelected: (Date) -> Unit = {},
): MaterialDatePicker<Long> {
    val datePicker = remember {
        MaterialDatePicker.Builder.datePicker()
            .setSelection((select?.time
                ?: Date().time) + 24.hours.toLong(DurationUnit.MILLISECONDS))
            .setCalendarConstraints(bounds)
            .setTitleText(title)
            .build()
    }

    DisposableEffect(datePicker) {
        val listener = MaterialPickerOnPositiveButtonClickListener<Long> {
            if (it != null) onDateSelected(Date(it))
        }
        datePicker.addOnPositiveButtonClickListener(listener)
        onDispose {
            datePicker.removeOnPositiveButtonClickListener(listener)
        }
    }

    return datePicker
}

internal fun dateFromHourAndMinute(hour: Int, minute: Int, locale: Locale = KENYA) =
    SimpleDateFormat("HH:mm", locale).parse(
        "${String.format("%02d", hour)}:${String.format("%02d", minute)}")

@Composable
fun rememberTimePickerDialog(
    @StringRes title: Int,
    select: Date? = null,
    locale: Locale = KENYA,
    onTimeSelected: (Date) -> Unit = {},
): MaterialTimePicker {
    val context = LocalContext.current
    val timePicker = remember {
        val calender = GregorianCalendar.getInstance(locale).apply {
            time = select ?: Date()
        }
        MaterialTimePicker.Builder().setTimeFormat(
            if (DateFormat.is24HourFormat(context)) {
                TimeFormat.CLOCK_24H
            } else {
                TimeFormat.CLOCK_12H
            }
        ).setHour(calender.get(GregorianCalendar.HOUR_OF_DAY))
            .setMinute(calender.get(GregorianCalendar.MINUTE))
            .setTitleText(title)
            .build()
    }

    DisposableEffect(timePicker) {
        val listener = View.OnClickListener {
            dateFromHourAndMinute(timePicker.hour, timePicker.minute, locale)?.let {
                onTimeSelected(it)
            }
        }
        timePicker.addOnPositiveButtonClickListener(listener)
        onDispose {
            timePicker.removeOnPositiveButtonClickListener(listener)
        }
    }

    return timePicker
}