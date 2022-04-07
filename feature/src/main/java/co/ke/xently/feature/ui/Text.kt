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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import co.ke.xently.feature.R

@Composable
fun stringRes(@StringRes string: Int, @StringRes vararg args: Int): String {
    return stringResource(string, *args.map {
        stringResource(it)
    }.toTypedArray())
}

val DefaultKeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)

@Composable
fun TextInputLayout(
    modifier: Modifier,
    value: TextFieldValue,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    isError: Boolean = false,
    error: String = "",
    helpText: String? = null,
    keyboardOptions: KeyboardOptions = DefaultKeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions(),
    onValueChange: (TextFieldValue) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        val description = stringResource(R.string.text_field_content_description, label ?: "")
            .removeSuffix("*")
            .trimStart()
        TextField(
            value = value,
            readOnly = readOnly,
            singleLine = singleLine,
            isError = isError,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = description },
        )
        if (isError) {
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = VIEW_SPACE, end = 12.dp),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
            )
        } else if (!helpText.isNullOrBlank()) {
            Text(
                text = helpText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = VIEW_SPACE, end = 12.dp),
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
    helpText: String? = null,
    suggestions: List<T> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    label: String? = null,
    wasSuggestionPicked: (Boolean) -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
    suggestionItemContent: @Composable ((T) -> Unit),
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var wasSuggestionSelected by remember { mutableStateOf(false) }

    SideEffect {
        wasSuggestionPicked.invoke(wasSuggestionSelected)
    }

    Box(modifier = modifier) {
        TextInputLayout(
            value = value,
            label = label,
            error = error,
            isError = isError,
            helpText = helpText,
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
                wasSuggestionSelected = false
                onValueChange(it)
                showDropdownMenu = it.text.isNotBlank()
            },
            trailingIcon = trailingIcon ?: {
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
                        wasSuggestionSelected = true
                    },
                )
            }
        }
    }
}