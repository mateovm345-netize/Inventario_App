package com.example.inventarioapp.feature.product.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType

/** Campo de texto sin conocimiento del ViewModel. */
@Composable
fun ProductFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
