package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.SecondaryNavy
import com.example.ui.viewmodels.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    storeViewModel: StoreViewModel,
    onProductClick: (ProductEntity) -> Unit,
    onExploreProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteProducts by storeViewModel.favoriteProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sevimlilar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (favoriteProducts.isEmpty()) {
            EmptyStateView(
                title = "Sevimlilar ro‘yxati hozircha bo‘sh.",
                description = "O‘zingizga yoqqan mahsulotlarni yurakcha tugmasi orqali bu yerga saqlab qo‘yishingiz mumkin.",
                icon = Icons.Outlined.FavoriteBorder,
                actionButtonText = "Mahsulotlarni ko‘rish",
                onActionClick = onExploreProducts,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(innerPadding)
                    .testTag("favorites_grid")
            ) {
                items(favoriteProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = true,
                        onProductClick = { onProductClick(product) },
                        onAddToCart = {
                            storeViewModel.addToCart(product.id, 1)
                        },
                        onToggleFavorite = {
                            storeViewModel.toggleFavorite(product.id)
                        }
                    )
                }
            }
        }
    }
}
