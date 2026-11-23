package com.example.inventarioapp.feature.product.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.inventarioapp.feature.product.presentation.composables.ProductDeleteDialog
import com.example.inventarioapp.feature.product.presentation.section.ProductListSection
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductListViewModel

/** Pantalla contenedora: conecta ViewModel, secciones y navegación. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    vm: ProductListViewModel,
    userEmail: String?,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.inventario_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!userEmail.isNullOrBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { vm.sync() },
                        enabled = !ui.isSyncing,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sincronizar))
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = stringResource(R.string.cerrar_sesion)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.agregar_producto)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (ui.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(visible = ui.message != null) {
                ui.message?.let { message ->
                    Surface(
                        color = if (ui.isErrorMessage) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (ui.isErrorMessage) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.clearMessage() },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.cancelar),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            ProductListSection(
                products = ui.filteredProducts,
                allCount = ui.products.size,
                totalUnits = ui.totalUnits,
                pendingCount = ui.pendingCount,
                searchQuery = ui.searchQuery,
                activeFilter = ui.filter,
                onSearchQueryChange = vm::updateSearchQuery,
                onFilterChange = vm::updateFilter,
                onEdit = onEdit,
                onAskDelete = { pendingDeleteId = it }
            )
        }
    }

    if (pendingDeleteId != null) {
        ProductDeleteDialog(
            onConfirm = {
                pendingDeleteId?.let(vm::delete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }
}
