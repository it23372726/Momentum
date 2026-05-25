package com.example.projectpbd.presentation.transactions.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategoryType

data class CategoryVisuals(
    val icon: ImageVector,
    val color: Color
)

object CategoryVisualMapper {
    fun getVisuals(category: Category): CategoryVisuals {
        val defaultVisuals = when (category.type) {
            CategoryType.EXPENSE -> getExpenseVisuals(category.name.lowercase())
            CategoryType.INCOME -> getIncomeVisuals(category.name.lowercase())
        }
        
        val icon = category.iconKey?.let { mapIcon(it) } ?: defaultVisuals.icon
        val color = category.colorKey?.let { 
            try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { defaultVisuals.color }
        } ?: defaultVisuals.color
        
        return CategoryVisuals(icon, color)
    }

    private fun mapIcon(key: String): ImageVector {
        return when (key) {
            "shopping_cart" -> Icons.Default.ShoppingCart
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "home" -> Icons.Default.Home
            "star" -> Icons.Default.Star
            "payments" -> Icons.Default.Payments
            "work" -> Icons.Default.Work
            "school" -> Icons.Default.School
            "medical_services" -> Icons.Default.MedicalServices
            "flight" -> Icons.Default.Flight
            "sports_esports" -> Icons.Default.SportsEsports
            "laptop" -> Icons.Default.Laptop
            "business_center" -> Icons.Default.BusinessCenter
            "redeem" -> Icons.Default.Redeem
            else -> Icons.Default.Category
        }
    }

    private fun getExpenseVisuals(name: String): CategoryVisuals {
        return when {
            name.contains("food") || name.contains("restaurant") || name.contains("eat") || name.contains("dining") -> 
                CategoryVisuals(Icons.Default.Restaurant, Color(0xFFFF7043))
            name.contains("transport") || name.contains("car") || name.contains("taxi") || name.contains("fuel") -> 
                CategoryVisuals(Icons.Default.DirectionsCar, Color(0xFF42A5F5))
            name.contains("shopping") || name.contains("grocery") || name.contains("store") -> 
                CategoryVisuals(Icons.Default.ShoppingBag, Color(0xFFAB47BC))
            name.contains("bill") || name.contains("utility") || name.contains("rent") || name.contains("tax") -> 
                CategoryVisuals(Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF66BB6A))
            name.contains("health") || name.contains("medical") || name.contains("doctor") || name.contains("pharmacy") -> 
                CategoryVisuals(Icons.Default.MedicalServices, Color(0xFFEF5350))
            name.contains("entertainment") || name.contains("movie") || name.contains("game") || name.contains("music") -> 
                CategoryVisuals(Icons.Default.SportsEsports, Color(0xFFFFCA28))
            name.contains("education") || name.contains("school") || name.contains("book") || name.contains("course") -> 
                CategoryVisuals(Icons.Default.School, Color(0xFF5C6BC0))
            name.contains("travel") || name.contains("flight") || name.contains("hotel") || name.contains("trip") -> 
                CategoryVisuals(Icons.Default.Flight, Color(0xFF26A69A))
            name.contains("home") || name.contains("furniture") || name.contains("maintenance") -> 
                CategoryVisuals(Icons.Default.Home, Color(0xFF78909C))
            else -> CategoryVisuals(Icons.Default.Category, Color(0xFF9E9E9E))
        }
    }

    private fun getIncomeVisuals(name: String): CategoryVisuals {
        return when {
            name.contains("salary") || name.contains("wage") || name.contains("work") || name.contains("pay") -> 
                CategoryVisuals(Icons.Default.Work, Color(0xFF43A047))
            name.contains("freelance") || name.contains("project") || name.contains("gig") -> 
                CategoryVisuals(Icons.Default.Laptop, Color(0xFF0288D1))
            name.contains("business") || name.contains("profit") || name.contains("sales") -> 
                CategoryVisuals(Icons.Default.BusinessCenter, Color(0xFF5E35B1))
            name.contains("crypto") || name.contains("bitcoin") || name.contains("eth") -> 
                CategoryVisuals(Icons.Default.CurrencyBitcoin, Color(0xFFFFB300))
            name.contains("investment") || name.contains("stock") || name.contains("dividend") || name.contains("trading") -> 
                CategoryVisuals(Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00ACC1))
            name.contains("gift") || name.contains("present") || name.contains("reward") -> 
                CategoryVisuals(Icons.Default.Redeem, Color(0xFFD81B60))
            name.contains("interest") || name.contains("bank") || name.contains("savings") -> 
                CategoryVisuals(Icons.Default.AccountBalance, Color(0xFF7CB342))
            else -> CategoryVisuals(Icons.Default.Payments, Color(0xFF757575))
        }
    }
}
