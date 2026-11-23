package com.example.inventarioapp.feature.product.presentation.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R

/** Modal solo visual. La operación real se decide en la Screen. */
@Composable
fun ProductDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.eliminar_producto_titulo)) },
        text = { Text(stringResource(R.string.eliminar_producto_mensaje)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("confirm_delete_button")
            ) {
                Text(stringResource(R.string.eliminar))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("cancel_delete_button")
            ) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
