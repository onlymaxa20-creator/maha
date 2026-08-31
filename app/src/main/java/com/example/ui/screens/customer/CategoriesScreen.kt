package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.CategoryListItemCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBackground
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.PrimaryBurgundyLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.viewmodels.StoreViewModel

enum class CategorySortOption(val title: String) {
    RECOMMENDED("Tavsiya etilganlar"),
    PRICE_LOW_HIGH("Arzonidan qimmatga"),
    PRICE_HIGH_LOW("Qimmatidan arzonga"),
    NEWEST("Yangi qo‘shilganlar"),
    POPULAR("Mashhurligi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    storeViewModel: StoreViewModel,
    onProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by storeViewModel.categories.collectAsState()
    val allProducts by storeViewModel.products.collectAsState()
    val favoriteProducts by storeViewModel.favoriteProducts.collectAsState()
    val favIds = remember(favoriteProducts) { favoriteProducts.map { it.id }.toSet() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var sortOption by remember { mutableStateOf(CategorySortOption.RECOMMENDED) }
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState()

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.find { it.id == selectedCategoryId }
    }

    val displayedProducts = remember(allProducts, selectedCategoryId, searchQuery, sortOption) {
        var list = allProducts.filter { it.isAvailable }
        if (selectedCategoryId != null) {
            list = list.filter { it.categoryId == selectedCategoryId }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        when (sortOption) {
            CategorySortOption.RECOMMENDED -> list
            CategorySortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            CategorySortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            CategorySortOption.NEWEST -> list.sortedByDescending { it.createdAt }
            CategorySortOption.POPULAR -> list.sortedByDescending { it.discountPercent }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCategory != null) selectedCategory.name else "Kategoriyalar",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                },
                navigationIcon = {
                    if (selectedCategoryId != null) {
                        IconButton(onClick = { selectedCategoryId = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Orqaga", tint = DarkText)
                        }
                    }
                },
                actions = {
                    if (selectedCategoryId != null) {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Saralash", tint = PrimaryBurgundy)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
        ) {
            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (selectedCategoryId != null) "${selectedCategory?.name ?: ""} bo‘yicha qidirish..." else "Kategoriya nomi bo‘yicha qidirish...",
                        fontSize = 13.sp,
                        color = SlateGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Qidiruv",
                        tint = PrimaryBurgundy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Tozalash", tint = SlateGray, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBurgundy,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("categories_search_input")
            )

            // CATEGORY LISTING VIEW (When no category is selected)
            if (selectedCategoryId == null) {
                if (filteredCategories.isEmpty()) {
                    EmptyStateView(
                        title = "Kategoriya topilmadi",
                        description = "Boshqa so‘z bilan qidirib ko‘ring",
                        icon = Icons.Outlined.Category,
                        actionButtonText = "Tozalash",
                        onActionClick = { searchQuery = "" }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Barcha toifalar (${filteredCategories.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Jami ${allProducts.count { it.isAvailable }} ta tovar",
                                    fontSize = 12.sp,
                                    color = SlateGray
                                )
                            }
                        }

                        items(filteredCategories, key = { it.id }) { cat ->
                            val count = allProducts.count { it.categoryId == cat.id && it.isAvailable }
                            CategoryListItemCard(
                                category = cat,
                                productCount = count,
                                onClick = {
                                    selectedCategoryId = cat.id
                                    searchQuery = ""
                                },
                                onSubcategoryClick = { sub ->
                                    selectedCategoryId = cat.id
                                    searchQuery = sub
                                }
                            )
                        }
                    }
                }
            } else {
                // PRODUCTS IN SELECTED CATEGORY VIEW (2-column responsive grid)
                if (selectedCategory != null) {
                    val catStyle = com.example.ui.components.getCategoryStyleInfo(selectedCategory.iconKey, selectedCategory.name)
                    if (catStyle.subcategories.isNotEmpty()) {
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Surface(
                                    color = if (searchQuery.isEmpty()) PrimaryBurgundy else CardSurface,
                                    shape = RoundedCornerShape(18.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (searchQuery.isEmpty()) PrimaryBurgundy else BorderColor),
                                    modifier = Modifier.clickable { searchQuery = "" }
                                ) {
                                    Text(
                                        text = "Barchasi",
                                        fontSize = 12.sp,
                                        fontWeight = if (searchQuery.isEmpty()) FontWeight.Bold else FontWeight.Medium,
                                        color = if (searchQuery.isEmpty()) Color.White else DarkText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            items(catStyle.subcategories) { sub ->
                                val isSelected = searchQuery.equals(sub, ignoreCase = true)
                                Surface(
                                    color = if (isSelected) PrimaryBurgundy else CardSurface,
                                    shape = RoundedCornerShape(18.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBurgundy else BorderColor),
                                    modifier = Modifier.clickable {
                                        searchQuery = if (isSelected) "" else sub
                                    }
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else DarkText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (displayedProducts.isEmpty()) {
                    EmptyStateView(
                        title = "Ushbu toifada tovarlar yo‘q",
                        description = if (searchQuery.isNotEmpty()) "\"$searchQuery\" bo‘yicha tovar topilmadi" else "Tez orada yangi mahsulotlar joylashtiriladi",
                        icon = Icons.Outlined.Inventory2,
                        actionButtonText = if (searchQuery.isNotEmpty()) "Filtrni tozalash" else "Barcha kategoriyalar",
                        onActionClick = {
                            if (searchQuery.isNotEmpty()) {
                                searchQuery = ""
                            } else {
                                selectedCategoryId = null
                            }
                        }
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = favIds.contains(product.id),
                                onProductClick = { onProductClick(product) },
                                onToggleFavorite = { storeViewModel.toggleFavorite(product.id) },
                                onAddToCart = { storeViewModel.addToCart(product.id, 1) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Sort Selection Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState,
            containerColor = CardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Saralash",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(14.dp))

                CategorySortOption.entries.forEach { option ->
                    val isSelected = sortOption == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                sortOption = option
                                showSortSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryBurgundy else DarkText
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = PrimaryBurgundy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
