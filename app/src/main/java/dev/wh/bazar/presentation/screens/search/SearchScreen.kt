package dev.wh.bazar.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.wh.bazar.presentation.screens.home.ProductCard
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    viewModel: SearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (uiState.searchingProducts) {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.List, "Filtros")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar productos o tiendas...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                singleLine = true
            )

            TabRow(selectedTabIndex = if (uiState.searchingProducts) 0 else 1) {
                Tab(
                    selected = uiState.searchingProducts,
                    onClick = { viewModel.switchToProducts() },
                    text = { Text("Productos") }
                )
                Tab(
                    selected = !uiState.searchingProducts,
                    onClick = { viewModel.switchToStores() },
                    text = { Text("Tiendas") }
                )
            }

            if (showFilters && uiState.searchingProducts) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.availableCategories) { category ->
                            FilterChip(
                                selected = if (category == "Todos") {
                                    uiState.selectedCategory == null
                                } else {
                                    uiState.selectedCategory == category
                                },
                                onClick = { viewModel.onCategorySelected(category) },
                                label = { Text(category) }
                            )
                        }
                    }

                    Text(
                        text = "Rango de precio",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    var minPriceText by remember { mutableStateOf("") }
                    var maxPriceText by remember { mutableStateOf("") }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minPriceText,
                            onValueChange = {
                                minPriceText = it
                                val minPrice = it.toDoubleOrNull()
                                val maxPrice = maxPriceText.toDoubleOrNull()
                                viewModel.onPriceRangeChanged(minPrice, maxPrice)
                            },
                            label = { Text("Min") },
                            placeholder = { Text("$0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxPriceText,
                            onValueChange = {
                                maxPriceText = it
                                val minPrice = minPriceText.toDoubleOrNull()
                                val maxPrice = it.toDoubleOrNull()
                                viewModel.onPriceRangeChanged(minPrice, maxPrice)
                            },
                            label = { Text("Max") },
                            placeholder = { Text("$999") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "Filtros adicionales",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.onlyInStock,
                            onClick = { viewModel.onStockFilterChanged(!uiState.onlyInStock) },
                            label = { Text("Solo disponibles") }
                        )
                        FilterChip(
                            selected = uiState.onlyWithDiscount,
                            onClick = { viewModel.onDiscountFilterChanged(!uiState.onlyWithDiscount) },
                            label = { Text("Con descuento") }
                        )
                    }

                    Text(
                        text = "Ordenar por",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.sortBy == "price_asc",
                            onClick = { viewModel.onSortByChanged("price_asc") },
                            label = { Text("Precio ↑") }
                        )
                        FilterChip(
                            selected = uiState.sortBy == "price_desc",
                            onClick = { viewModel.onSortByChanged("price_desc") },
                            label = { Text("Precio ↓") }
                        )
                        FilterChip(
                            selected = uiState.sortBy == "rating",
                            onClick = { viewModel.onSortByChanged("rating") },
                            label = { Text("Rating") }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Button(
                        onClick = {
                            minPriceText = ""
                            maxPriceText = ""
                            viewModel.clearFilters()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.searchingProducts) {
                if (uiState.products.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.products.chunked(2)) { rowProducts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowProducts.forEach { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = { onNavigateToProduct(product.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowProducts.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.stores.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron tiendas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.stores) { store ->
                            StoreCard(store = store, onClick = { /* TODO */ })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreCard(
    store: dev.wh.bazar.data.model.Store,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = store.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "${store.rating} ⭐",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
