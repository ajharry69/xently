package co.ke.xently.feature.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun stringRes(@StringRes string: Int, @StringRes vararg strings: Int): String {
    return stringResource(string, *strings.map {
        stringResource(it)
    }.toTypedArray())
}

@Composable
fun TextFieldErrorText(error: String, modifier: Modifier = Modifier) {
    Text(
        error,
        modifier = modifier.padding(start = 16.dp, end = 12.dp),
        color = MaterialTheme.colors.error,
        style = MaterialTheme.typography.caption,
    )
}

@Composable
fun XentlyTextField(
    modifier: Modifier,
    value: TextFieldValue,
    singleLine: Boolean = true,
    isError: Boolean = false,
    error: String = "",
    helpText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions(),
    onValueChange: (TextFieldValue) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            singleLine = singleLine,
            isError = isError,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
        )
        if (isError) {
            TextFieldErrorText(error, Modifier.fillMaxWidth())
        } else if (!helpText.isNullOrBlank()) {
            Text(
                helpText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = .6f),
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun <T> AutoCompleteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    error: String = "",
    suggestions: List<T> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    label: String? = null,
    suggestionItemContent: @Composable ((T) -> Unit),
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        XentlyTextField(
            value = value,
            label = label,
            error = error,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (!it.isFocused) {
                        showDropdownMenu = false
                    }
                },
            onValueChange = {
                onValueChange(it)
                showDropdownMenu = it.text.isNotBlank()
            },
            trailingIcon = {
                IconButton(onClick = { showDropdownMenu = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
        )
        DropdownMenu(
            expanded = showDropdownMenu && suggestions.isNotEmpty(),
            onDismissRequest = {
                showDropdownMenu = false
            },
            modifier = Modifier.fillMaxWidth(),
            properties = PopupProperties(focusable = false),
        ) {
            suggestions.forEach {
                DropdownMenuItem(
                    modifier = Modifier.padding(vertical = 8.dp),
                    content = {
                        suggestionItemContent(it)
                    },
                    onClick = {
                        onOptionSelected(it)
                        showDropdownMenu = false
                    },
                )
            }
        }
    }
}