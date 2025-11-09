package dev.wh.bazar.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wh.bazar.data.model.Product
import dev.wh.bazar.data.model.Store
import dev.wh.bazar.data.repository.ProductRepository
import dev.wh.bazar.data.repository.StoreRepository
import dev.wh.bazar.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val products: List<Product> = emptyList(),
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val searchingProducts: Boolean = true,
    val selectedCategory: String? = null,
    val availableCategories: List<String> = listOf("Todos", "Electrónica", "Ropa", "Hogar", "Alimentos", "Deportes", "Otros"),
    val sortBy: String = "rating",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val onlyInStock: Boolean = false,
    val onlyWithDiscount: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val storeRepository: StoreRepository = StoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                // Siempre realizar búsqueda, incluso con query vacío
                performSearch(query)
            }
            .launchIn(viewModelScope)

        // Cargar todos los productos al iniciar
        viewModelScope.launch {
            performSearch("")
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun switchToProducts() {
        _uiState.update { it.copy(searchingProducts = true) }
        performSearch(_uiState.value.searchQuery)
    }

    fun switchToStores() {
        _uiState.update { it.copy(searchingProducts = false) }
        performSearch(_uiState.value.searchQuery)
    }

    fun onCategorySelected(category: String) {
        val newCategory = if (category == "Todos") null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }
        performSearch(_uiState.value.searchQuery)
    }

    fun onSortByChanged(sortBy: String) {
        _uiState.update { it.copy(sortBy = sortBy) }
        performSearch(_uiState.value.searchQuery)
    }

    fun onPriceRangeChanged(minPrice: Double?, maxPrice: Double?) {
        _uiState.update { it.copy(minPrice = minPrice, maxPrice = maxPrice) }
        performSearch(_uiState.value.searchQuery)
    }

    fun onStockFilterChanged(onlyInStock: Boolean) {
        _uiState.update { it.copy(onlyInStock = onlyInStock) }
        performSearch(_uiState.value.searchQuery)
    }

    fun onDiscountFilterChanged(onlyWithDiscount: Boolean) {
        _uiState.update { it.copy(onlyWithDiscount = onlyWithDiscount) }
        performSearch(_uiState.value.searchQuery)
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                minPrice = null,
                maxPrice = null,
                onlyInStock = false,
                onlyWithDiscount = false,
                sortBy = "rating"
            )
        }
        performSearch(_uiState.value.searchQuery)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (_uiState.value.searchingProducts) {
                when (val result = productRepository.searchProducts(query)) {
                    is Resource.Success -> {
                        var products = result.data ?: emptyList()

                        // Aplicar filtro de categoría
                        val selectedCategory = _uiState.value.selectedCategory
                        if (selectedCategory != null) {
                            products = products.filter { it.category == selectedCategory }
                        }

                        // Aplicar filtro de rango de precio
                        val minPrice = _uiState.value.minPrice
                        val maxPrice = _uiState.value.maxPrice
                        if (minPrice != null) {
                            products = products.filter { it.finalPrice >= minPrice }
                        }
                        if (maxPrice != null) {
                            products = products.filter { it.finalPrice <= maxPrice }
                        }

                        // Aplicar filtro de stock
                        if (_uiState.value.onlyInStock) {
                            products = products.filter { it.stock > 0 }
                        }

                        // Aplicar filtro de descuento
                        if (_uiState.value.onlyWithDiscount) {
                            products = products.filter { it.hasDiscount }
                        }

                        // Aplicar ordenamiento
                        products = when (_uiState.value.sortBy) {
                            "price_asc" -> products.sortedBy { it.finalPrice }
                            "price_desc" -> products.sortedByDescending { it.finalPrice }
                            "rating" -> products.sortedByDescending { it.rating }
                            else -> products
                        }

                        _uiState.update {
                            it.copy(
                                products = products,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    else -> {}
                }
            } else {
                when (val result = storeRepository.searchStores(query)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                stores = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    else -> {}
                }
            }
        }
    }
}
