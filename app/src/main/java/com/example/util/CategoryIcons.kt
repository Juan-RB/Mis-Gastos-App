package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {

    val AVAILABLE_ICONS = listOf(
        "restaurant" to Icons.Default.Restaurant,
        "fastfood" to Icons.Default.Fastfood,
        "local_cafe" to Icons.Default.LocalCafe,
        "directions_car" to Icons.Default.DirectionsCar,
        "local_gas_station" to Icons.Default.LocalGasStation,
        "home" to Icons.Default.Home,
        "medical_services" to Icons.Default.MedicalServices,
        "sports_esports" to Icons.Default.SportsEsports,
        "movie" to Icons.Default.Movie,
        "shopping_bag" to Icons.Default.ShoppingBag,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "school" to Icons.Default.School,
        "fitness_center" to Icons.Default.FitnessCenter,
        "pets" to Icons.Default.Pets,
        "flight" to Icons.Default.Flight,
        "work" to Icons.Default.Work,
        "receipt" to Icons.AutoMirrored.Filled.ReceiptLong,
        "payments" to Icons.Default.Payments,
        "savings" to Icons.Default.Savings
    )

    val AVAILABLE_COLORS = listOf(
        0xFF4DA2FFL, // Blue (#4da2ff)
        0xFF55DB9CL, // Green (#55db9c)
        0xFFE9CCFFL, // Lavender (#e9ccff)
        0xFFFB4903L, // Orange (#fb4903)
        0xFFFFD731L, // Yellow (#ffd731)
        0xFF5C4ADEL  // Violet (#5c4ade)
    )

    fun getIcon(name: String): ImageVector {
        return AVAILABLE_ICONS.firstOrNull { it.first.equals(name, ignoreCase = true) }?.second
            ?: Icons.Default.Category
    }

    fun getColor(colorHex: Long): Color {
        return Color(colorHex)
    }
}
