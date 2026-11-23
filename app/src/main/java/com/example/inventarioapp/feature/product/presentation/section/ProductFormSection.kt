package com.example.inventarioapp.feature.product.presentation.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.inventarioapp.feature.product.presentation.composables.ProductFormField
import com.example.inventarioapp.feature.product.presentation.state.ProductFormUiState

/** Sección de formulario: solo recibe UiState y callbacks. */
@Composable
fun ProductFormSection(
    ui: ProductFormUiState,
    onName: (String) -> Unit,
    onDescription: (String) -> Unit,
    onPrice: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProductFormField(
                label = stringResource(R.string.nombre),
                value = ui.name,
                onValueChange = onName,
                testTag = "field_name"
            )
            ProductFormField(
                label = stringResource(R.string.descripcion),
                value = ui.description,
                onValueChange = onDescription,
                testTag = "field_description",
                singleLine = false,
                minLines = 2
            )
            ProductFormField(
                label = stringResource(R.string.precio_cop),
                value = ui.price,
                onValueChange = onPrice,
                testTag = "field_price",
                keyboardType = KeyboardType.Number
            )
            ProductFormField(
                label = stringResource(R.string.cantidad),
                value = ui.quantity,
                onValueChange = onQuantity,
                testTag = "field_quantity",
                keyboardType = KeyboardType.Number
            )

            ui.error?.let { errorMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onSave,
                enabled = !ui.saving,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("save_product_button")
            ) {
                if (ui.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.guardando))
                } else {
                    Text(stringResource(R.string.guardar))
                }
            }
        }
    }
}
