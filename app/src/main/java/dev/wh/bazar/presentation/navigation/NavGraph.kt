package dev.wh.bazar.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.wh.bazar.presentation.screens.auth.AuthViewModel
import dev.wh.bazar.presentation.screens.auth.LoginScreen
import dev.wh.bazar.presentation.screens.auth.SignUpScreen
import dev.wh.bazar.presentation.screens.cart.CartScreen
import dev.wh.bazar.presentation.screens.cart.CartViewModel
import dev.wh.bazar.presentation.screens.cart.CartViewModelFactory
import dev.wh.bazar.presentation.screens.home.HomeScreen
import dev.wh.bazar.presentation.screens.home.HomeViewModel
import dev.wh.bazar.presentation.screens.home.HomeViewModelFactory
import dev.wh.bazar.presentation.screens.orders.OrdersScreen
import dev.wh.bazar.presentation.screens.orders.OrdersViewModel
import dev.wh.bazar.presentation.screens.product.ProductDetailScreen
import dev.wh.bazar.presentation.screens.product.ProductDetailViewModel
import dev.wh.bazar.presentation.screens.product.ProductDetailViewModelFactory
import dev.wh.bazar.presentation.screens.search.SearchScreen
import dev.wh.bazar.presentation.screens.search.SearchViewModel
import dev.wh.bazar.presentation.screens.preferences.PreferencesScreen
import dev.wh.bazar.presentation.screens.profile.ProfileScreen
import dev.wh.bazar.presentation.screens.profile.ProfileViewModel
import dev.wh.bazar.presentation.screens.profile.ProfileViewModelFactory
import dev.wh.bazar.presentation.screens.seller.AddProductScreen
import dev.wh.bazar.presentation.screens.seller.AddProductViewModel
import dev.wh.bazar.presentation.screens.seller.AddProductViewModelFactory
import dev.wh.bazar.presentation.screens.seller.CreateStoreScreen
import dev.wh.bazar.presentation.screens.seller.CreateStoreViewModel
import dev.wh.bazar.presentation.screens.seller.CreateStoreViewModelFactory
import dev.wh.bazar.presentation.screens.seller.EditProductScreen
import dev.wh.bazar.presentation.screens.seller.EditProductViewModel
import dev.wh.bazar.presentation.screens.seller.EditProductViewModelFactory
import dev.wh.bazar.presentation.screens.seller.SellerDashboardScreen
import dev.wh.bazar.presentation.screens.seller.SellerViewModel
import dev.wh.bazar.presentation.screens.checkout.CheckoutScreen
import dev.wh.bazar.presentation.screens.checkout.CheckoutViewModel
import dev.wh.bazar.presentation.screens.checkout.CheckoutViewModelFactory

@Composable
fun BazarNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            val authViewModel: AuthViewModel = viewModel()
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(context.applicationContext as android.app.Application)
            )
            HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                onNavigateToSeller = {
                    navController.navigate(Screen.SellerDashboard.route)
                },
                onNavigateToCreateStore = {
                    navController.navigate(Screen.CreateStore.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                viewModel = homeViewModel
            )
        }

        // Search Screen
        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = viewModel()
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                viewModel = searchViewModel
            )
        }

        // Product Detail Screen
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val productDetailViewModel: ProductDetailViewModel = viewModel(
                factory = ProductDetailViewModelFactory(context.applicationContext as android.app.Application)
            )
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                viewModel = productDetailViewModel
            )
        }

        // Cart Screen
        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModelFactory(context.applicationContext as android.app.Application)
            )
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                viewModel = cartViewModel
            )
        }

        // Orders Screen
        composable(Screen.Orders.route) {
            val ordersViewModel: OrdersViewModel = viewModel()
            OrdersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                viewModel = ordersViewModel
            )
        }

        // Seller Dashboard
        composable(Screen.SellerDashboard.route) { backStackEntry ->
            val sellerViewModel: SellerViewModel = viewModel()

            // Recargar cuando volvemos de otra pantalla
            LaunchedEffect(backStackEntry) {
                sellerViewModel.loadStoreAndProducts()
            }

            SellerDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                },
                onNavigateToCreateStore = {
                    navController.navigate(Screen.CreateStore.route)
                },
                viewModel = sellerViewModel
            )
        }

        // Create Store Screen
        composable(Screen.CreateStore.route) {
            val createStoreViewModel: CreateStoreViewModel = viewModel(
                factory = CreateStoreViewModelFactory(context.applicationContext as android.app.Application)
            )
            CreateStoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = createStoreViewModel
            )
        }

        // Add Product Screen
        composable(Screen.AddProduct.route) {
            val addProductViewModel: AddProductViewModel = viewModel(
                factory = AddProductViewModelFactory(context.applicationContext as android.app.Application)
            )
            AddProductScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = addProductViewModel
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(context.applicationContext as android.app.Application)
            )
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPreferences = {
                    navController.navigate(Screen.Preferences.route)
                },
                viewModel = profileViewModel
            )
        }

        // Preferences Screen
        composable(Screen.Preferences.route) {
            PreferencesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Checkout Screen
        composable(Screen.Checkout.route) {
            val checkoutViewModel: CheckoutViewModel = viewModel(
                factory = CheckoutViewModelFactory(context.applicationContext as android.app.Application)
            )
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderCreated = {
                    // Navegar a Orders después de crear la orden
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                viewModel = checkoutViewModel
            )
        }

        // Edit Product Screen
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val editProductViewModel: EditProductViewModel = viewModel(
                factory = EditProductViewModelFactory(
                    context.applicationContext as android.app.Application,
                    productId
                )
            )
            EditProductScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = editProductViewModel
            )
        }
    }
}
