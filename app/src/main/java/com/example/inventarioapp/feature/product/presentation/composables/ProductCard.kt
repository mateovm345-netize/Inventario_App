package com.example.inventarioapp.feature.product.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.inventarioapp.feature.product.domain.model.Product
import java.text.NumberFormat
import java.util.Locale

/** Componente visual reutilizable: recibe datos y emite eventos. */
@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onAskDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).apply {
            maximumFractionDigits = 0
        }
    }
    val lowStock = product.quantity <= 5

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                SyncStatusPill(pendingSync = product.pendingSync)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(product.priceCop),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = if (lowStock) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${product.quantity} uds.",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (lowStock) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_product_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.editar))
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onAskDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_product_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusPill(pendingSync: Boolean) {
    val containerColor = if (pendingSync) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (pendingSync) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val icon = if (pendingSync) Icons.Outlined.CloudUpload else Icons.Outlined.CloudDone
    val label = if (pendingSync) {
        stringResource(R.string.pendiente_sincronizar)
    } else {
        stringResource(R.string.sincronizado)
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}
