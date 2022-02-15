package co.ke.xently.products.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.ke.xently.data.Attribute
import co.ke.xently.data.Brand
import co.ke.xently.data.MeasurementUnit
import co.ke.xently.feature.ui.*


@Composable
fun measurementUnitTextField(
    unit: String,
    error: String,
    clearField: Boolean,
    suggestions: List<MeasurementUnit>,
    onQueryChanged: (String) -> Unit,
): TextFieldValue {
    var value by remember(unit, clearField) {
        mutableStateOf(TextFieldValue(unit))
    }
    var isError by remember { mutableStateOf(error.isNotBlank()) }
    AutoCompleteTextField(
        modifier = VerticalLayoutModifier,
        value = value,
        isError = isError,
        error = error,
        label = stringResource(R.string.fsp_product_detail_unit_label),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        onValueChange = {
            value = it
            isError = false
            onQueryChanged(it.text)
        },
        onOptionSelected = {
            value = TextFieldValue(it.name)
        },
        suggestions = suggestions,
    ) {
        Text(it.toString(), style = MaterialTheme.typography.body1)
    }
    return value
}

@Composable
fun productNameTextField(name: String, error: String, clearField: Boolean): TextFieldValue {
    var value by remember(name, clearField) {
        mutableStateOf(TextFieldValue(name))
    }
    var isError by remember { mutableStateOf(error.isNotBlank()) }
    TextInputLayout(
        modifier = VerticalLayoutModifier,
        value = value,
        isError = isError,
        error = error,
        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next),
        onValueChange = {
            value = it
            isError = false
        },
        label = stringResource(R.string.fsp_product_detail_name_label),
    )
    return value
}

@Composable
fun numberTextField(
    number: Number,
    error: String,
    clearField: Boolean,
    @StringRes label: Int,
): TextFieldValue {
    var value by remember(number, clearField) {
        mutableStateOf(TextFieldValue(number.toString()))
    }
    var isError by remember { mutableStateOf(error.isNotBlank()) }
    TextInputLayout(
        modifier = VerticalLayoutModifier,
        value = value,
        isError = isError,
        error = error,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next),
        onValueChange = {
            value = it
            isError = false
        },
        label = stringResource(label),
    )
    return value
}

@Composable
fun productBrandsView(
    clearFields: Boolean,
    suggestions: List<Brand>,
    onQueryChanged: (String) -> Unit,
): SnapshotStateList<Brand> {
    val brands = remember(clearFields) { mutableStateListOf<Brand>() }
    var brandQuery by remember(clearFields) { mutableStateOf(TextFieldValue("")) }

    var showAddBrandIcon by remember { mutableStateOf(false) }
    val addBrand: (Brand) -> Unit = {
        brands.add(0, it)
        brandQuery = TextFieldValue() // Reset search
    }
    AutoCompleteTextField(
        modifier = VerticalLayoutModifier,
        value = brandQuery,
        label = stringResource(R.string.fsp_product_detail_brand_query_label),
        helpText = if (showAddBrandIcon) {
            stringResource(R.string.fsp_product_detail_brand_query_help_text)
        } else {
            null
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        onValueChange = {
            brandQuery = it
            onQueryChanged(it.text)
        },
        trailingIcon = if (showAddBrandIcon) {
            {
                IconButton(onClick = { addBrand(Brand(name = brandQuery.text.trim())) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        } else {
            null
        },
        onOptionSelected = addBrand,
        wasSuggestionPicked = {
            showAddBrandIcon = !it && brandQuery.text.isNotBlank()
        },
        suggestions = suggestions,
    ) {
        Text(it.toString(), style = MaterialTheme.typography.body1)
    }
    if (brands.isNotEmpty()) {
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            stringRes(R.string.fsp_product_detail_brands_title),
            style = MaterialTheme.typography.h5,
            modifier = VerticalLayoutModifier,
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        ChipGroup(
            modifier = VerticalLayoutModifier,
            isSingleLine = false, chipItems = brands,
        ) { i, b ->
            Chip(b.toString()) {
                brands.removeAt(i)
            }
        }
    }
    return brands
}

@Composable
fun productAttributesView(
    clearFields: Boolean,
    suggestions: List<Attribute>,
    onQueryChanged: (AttributeQuery) -> Unit,
): SnapshotStateList<Attribute> {
    val attributes = remember(clearFields) { mutableStateListOf<Attribute>() }
    var attributeNameQuery by remember(clearFields) { mutableStateOf(TextFieldValue("")) }
    var attributeValueQuery by remember(clearFields) { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(attributeNameQuery, attributeValueQuery) {
        onQueryChanged(AttributeQuery(attributeNameQuery.text, attributeValueQuery.text))
    }
    MultipleTextFieldRow(VerticalLayoutModifier) { fieldModifier ->
        AutoCompleteTextField(
            modifier = fieldModifier,
            value = attributeNameQuery,
            label = stringResource(R.string.fsp_product_detail_attribute_name_query_label),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            onValueChange = {
                attributeNameQuery = it
            },
            onOptionSelected = {
                attributeNameQuery = TextFieldValue(it.name)
            },
            suggestions = suggestions,
        ) {
            Text(it.name, style = MaterialTheme.typography.body1)
        }

        var showAddAttributeValueIcon by remember { mutableStateOf(false) }
        val addAttributeValue: (Attribute) -> Unit = {
            // Only override name if the attr.value was added without an attr.name
            attributes.add(0,
                it.copy(name = it.name.ifBlank { attributeNameQuery.text.trim() }))
            attributeValueQuery = TextFieldValue() // Reset search
        }
        // TODO: Show checkbox to enable reusing previously added attribute name when `attributeNameQuery` is blank
        AutoCompleteTextField(
            modifier = fieldModifier,
            value = attributeValueQuery,
            label = stringResource(R.string.fsp_product_detail_attribute_value_query_label),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            onValueChange = {
                attributeValueQuery = it
            },
            trailingIcon = if (showAddAttributeValueIcon) {
                {
                    IconButton(onClick = {
                        addAttributeValue(Attribute(value = attributeValueQuery.text.trim()))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            } else {
                null
            },
            onOptionSelected = addAttributeValue,
            wasSuggestionPicked = {
                showAddAttributeValueIcon =
                    !it && attributeValueQuery.text.isNotBlank() && attributeNameQuery.text.isNotBlank()
            },
            suggestions = suggestions,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(it.name, style = MaterialTheme.typography.body1)
                Text(it.value, style = MaterialTheme.typography.subtitle1)
            }
        }
    }
    if (attributes.isNotEmpty()) {
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            stringRes(R.string.fsp_product_detail_attributes_title),
            style = MaterialTheme.typography.h5,
            modifier = VerticalLayoutModifier,
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        ChipGroup(
            modifier = VerticalLayoutModifier,
            isSingleLine = false, chipItems = attributes,
        ) { i, b ->
            Chip(b.toString()) {
                attributes.removeAt(i)
            }
        }
    }
    return attributes
}