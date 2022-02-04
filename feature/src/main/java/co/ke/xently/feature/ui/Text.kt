package co.ke.xently.feature.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties


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
fun <T> AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    error: String = "",
    suggestions: List<T> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    label: @Composable (() -> Unit)? = null,
    suggestionItemContent: @Composable ((T) -> Unit),
) {
    var query by remember(value) { mutableStateOf(value) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = query,
                singleLine = singleLine,
                isError = isError,
                onValueChange = {
                    onValueChange(it)
                    query = it
                    showDropdownMenu = query.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                label = label,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                trailingIcon = {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
            )
            if (isError) {
                TextFieldErrorText(error = error, modifier = Modifier.fillMaxWidth())
            }
        }
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