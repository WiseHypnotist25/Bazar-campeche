package dev.wh.bazar.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Search : Screen("search")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    object StoreDetail : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(orderId: String) = "order/$orderId"
    }
    object Profile : Screen("profile")
    object Preferences : Screen("preferences")
    object CreateStore : Screen("seller/create_store")
    object SellerDashboard : Screen("seller/dashboard")
    object AddProduct : Screen("seller/add_product")
    object EditProduct : Screen("seller/edit_product/{productId}") {
        fun createRoute(productId: String) = "seller/edit_product/$productId"
    }
}
