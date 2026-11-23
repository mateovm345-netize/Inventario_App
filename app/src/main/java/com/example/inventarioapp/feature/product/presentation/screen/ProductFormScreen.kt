package com.example.inventarioapp.feature.product.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.inventarioapp.feature.product.presentation.section.ProductFormSection
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductFormViewModel

/** La Screen orquesta estado, estructura Scaffold y salida del formulario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    vm: ProductFormViewModel,
    productId: String?,
    onDone: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { vm.load(productId) }
    LaunchedEffect(ui.saved) { if (ui.saved) onDone() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (productId == null) {
                            stringResource(R.string.nuevo_producto)
                        } else {
                            stringResource(R.string.editar_producto)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDone,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ProductFormSection(
                ui = ui,
                onName = vm::updateName,
                onDescription = vm::updateDescription,
                onPrice = vm::updatePrice,
                onQuantity = vm::updateQuantity,
                onSave = vm::save
            )
        }
    }
}
