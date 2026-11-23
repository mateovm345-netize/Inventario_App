package com.example.inventarioapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.inventarioapp.core.auth.authStateFlow
import com.example.inventarioapp.core.auth.signOutUser
import com.example.inventarioapp.feature.product.presentation.screen.AuthScreen
import com.example.inventarioapp.feature.product.presentation.screen.ProductFormScreen
import com.example.inventarioapp.feature.product.presentation.screen.ProductListScreen
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductFormViewModel
import com.example.inventarioapp.feature.product.presentation.viewmodel.ProductListViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable

@Serializable
data object ProductListRoute : NavKey

@Serializable
data class ProductFormRoute(val id: String? = null) : NavKey

@Composable
fun AppNavigation() {
    val auth = remember { Firebase.auth }
    var authVersion by remember { mutableStateOf(0) }
    var isOfflineMode by remember { mutableStateOf(false) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null && !isOfflineMode) {
        AuthScreen(
            onAuthSuccess = {
                isOfflineMode = false
                authVersion++
            },
            onOfflineMode = {
                isOfflineMode = true
                authVersion++
            }
        )
    } else {
        val email = user?.email ?: if (isOfflineMode) "Modo Offline (Sin sesión)" else "invitado@inventario.app"
        val keyId = user?.uid ?: "offline_user"
        key(keyId, authVersion) {
            AuthenticatedInventoryNavigation(
                userEmail = email,
                onExitToAuth = {
                    isOfflineMode = false
                    authVersion++
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedInventoryNavigation(
    userEmail: String?,
    onExitToAuth: () -> Unit
) {
    val backStack = rememberNavBackStack(ProductListRoute)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<ProductListRoute> {
                val vm: ProductListViewModel = hiltViewModel()
                ProductListScreen(
                    vm = vm,
                    userEmail = userEmail,
                    onCreate = { backStack.add(ProductFormRoute()) },
                    onEdit = { id -> backStack.add(ProductFormRoute(id)) },
                    onSignOut = {
                        signOutUser(
                            credentialManager = credentialManager,
                            scope = scope,
                            onSignOutComplete = {
                                onExitToAuth()
                            }
                        )
                    }
                )
            }
            entry<ProductFormRoute> { route ->
                val vm: ProductFormViewModel = hiltViewModel()
                ProductFormScreen(
                    vm = vm,
                    productId = route.id,
                    onDone = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
