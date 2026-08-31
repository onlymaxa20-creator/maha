package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBurgundy
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray

data class CategoryStyleInfo(
    val emoji: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val bgTint: Color,
    val iconColor: Color,
    val badgeTag: String? = null,
    val subcategories: List<String> = emptyList()
)

fun getCategoryStyleInfo(iconKey: String, name: String = ""): CategoryStyleInfo {
    val lowerName = name.lowercase()
    val lowerKey = iconKey.lowercase()

    return when {
        // 1. Smartfonlar & Gadjetlar (Electronics, Phones, Gadgets)
        lowerKey.contains("smart") || lowerKey.contains("phone") || lowerKey.contains("gadget") || lowerKey.contains("electron") ||
                lowerName.contains("smartfon") || lowerName.contains("telefon") || lowerName.contains("gadjet") || lowerName.contains("naushnik") -> {
            CategoryStyleInfo(
                emoji = "📱",
                icon = Icons.Filled.PhoneAndroid,
                gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)), // Deep Indigo to Violet
                bgTint = Color(0xFFEEF2FF),
                iconColor = Color(0xFF4F46E5),
                badgeTag = "XIT",
                subcategories = listOf("Smartfonlar", "Naushniklar", "Aqlli soatlar", "Powerbank", "G‘iloflar")
            )
        }

        // 2. Kompyuterlar & Noutbuklar (Computers, Laptops, Accessories)
        lowerKey.contains("comput") || lowerKey.contains("laptop") || lowerKey.contains("pc") ||
                lowerName.contains("kompyuter") || lowerName.contains("noutbuk") || lowerName.contains("monitor") || lowerName.contains("klaviatura") -> {
            CategoryStyleInfo(
                emoji = "💻",
                icon = Icons.Filled.Tv,
                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF3B82F6)), // Sky to Royal Blue
                bgTint = Color(0xFFF0F9FF),
                iconColor = Color(0xFF0284C7),
                badgeTag = "TOP",
                subcategories = listOf("Noutbuklar", "Monitorlar", "Klaviatura & Sichqoncha", "Printerlar", "Fleshkalar")
            )
        }

        // 3. Maishiy texnika (Appliances, TV, Fridge, Vacuum)
        lowerKey.contains("appliance") || lowerKey.contains("tech") || lowerName.contains("maishiy") ||
                lowerName.contains("texnika") || lowerName.contains("televizor") || lowerName.contains("muzlatgich") || lowerName.contains("changyutgich") -> {
            CategoryStyleInfo(
                emoji = "📺",
                icon = Icons.Filled.Tv,
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8)), // Royal Blue to Sky
                bgTint = Color(0xFFEFF6FF),
                iconColor = Color(0xFF2563EB),
                badgeTag = "TOP",
                subcategories = listOf("Televizorlar", "Muzlatgichlar", "Kir yuvish mashinasi", "Changyutgichlar", "Konditsionerlar")
            )
        }

        // 4. Oziq-ovqat va Ichimliklar (Groceries, Food, Fresh fruits, beverages)
        lowerKey.contains("food") || lowerKey.contains("groc") || lowerName.contains("oziq") || lowerName.contains("ovqat") ||
                lowerName.contains("meva") || lowerName.contains("sabzavot") || lowerName.contains("go'sht") || lowerName.contains("gosht") -> {
            CategoryStyleInfo(
                emoji = "🍏",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFF16A34A), Color(0xFF65A30D)), // Fresh Green to Lime
                bgTint = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A),
                badgeTag = "YANGI",
                subcategories = listOf("Meva & Sabzavot", "Go‘sht & Parranda", "Sut mahsulotlari", "Choy & Qahva", "Shirinliklar")
            )
        }

        // 5. Kiyim-kechak (Fashion, Clothing, Dresses)
        lowerKey.contains("cloth") || lowerKey.contains("fashion") || lowerName.contains("kiyim") || lowerName.contains("moda") ||
                lowerName.contains("libos") || lowerName.contains("ko‘ylak") || lowerName.contains("kurtka") -> {
            CategoryStyleInfo(
                emoji = "👗",
                icon = Icons.Filled.Checkroom,
                gradientColors = listOf(Color(0xFFC026D3), Color(0xFFE11D48)), // Magenta to Rose
                bgTint = Color(0xFFFDF4FF),
                iconColor = Color(0xFFC026D3),
                badgeTag = "TREND",
                subcategories = listOf("Erkaklar kiyimi", "Ayollar kiyimi", "Bolalar kiyimi", "Kurtkalar", "Sport kiyimlari")
            )
        }

        // 6. Poyabzallar (Shoes, Sneakers)
        lowerKey.contains("shoe") || lowerKey.contains("footwear") || lowerName.contains("poyabzal") ||
                lowerName.contains("krossovka") || lowerName.contains("tufli") || lowerName.contains("etik") -> {
            CategoryStyleInfo(
                emoji = "👟",
                icon = Icons.Filled.ShoppingBag,
                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF06B6D4)), // Ocean Blue to Cyan
                bgTint = Color(0xFFF0F9FF),
                iconColor = Color(0xFF0284C7),
                badgeTag = "MASHHUR",
                subcategories = listOf("Krossovkalar", "Klassik poyabzal", "Mavsumiy etiklar", "Shippaklar", "Bolalar poyabzali")
            )
        }

        // 7. Go‘zallik & Parvarish (Beauty, Cosmetics, Perfumes)
        lowerKey.contains("beauty") || lowerKey.contains("cosmetic") || lowerName.contains("go‘zallik") ||
                lowerName.contains("gozallik") || lowerName.contains("parvarish") || lowerName.contains("parfyum") || lowerName.contains("atir") || lowerName.contains("krem") -> {
            CategoryStyleInfo(
                emoji = "💄",
                icon = Icons.Filled.Spa,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFFB7185)), // Ruby to Soft Rose
                bgTint = Color(0xFFFFF1F2),
                iconColor = Color(0xFFE11D48),
                badgeTag = "TAVSIYA",
                subcategories = listOf("Parfyumeriya", "Pardoz buyumlari", "Soch parvarishi", "Yuz kremlari", "Tana parvarishi")
            )
        }

        // 8. Salomatlik & Dorixona (Health, Pharmacy, Vitamins)
        lowerKey.contains("health") || lowerKey.contains("pharm") || lowerName.contains("salomatlik") ||
                lowerName.contains("dorixona") || lowerName.contains("dori") || lowerName.contains("vitamin") -> {
            CategoryStyleInfo(
                emoji = "💊",
                icon = Icons.Filled.LocalPharmacy,
                gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)), // Emerald to Mint
                bgTint = Color(0xFFECFDF5),
                iconColor = Color(0xFF059669),
                subcategories = listOf("Vitaminlar", "Tibbiy texnika", "Gigiyena vositalari", "Tonometrlar", "Ortopediya")
            )
        }

        // 9. Uy-ro‘zg‘or & Oshxona (Home, Kitchen, Living)
        lowerKey.contains("home") || lowerKey.contains("kitchen") || lowerName.contains("uy") ||
                lowerName.contains("ro‘zg‘or") || lowerName.contains("rozgor") || lowerName.contains("oshxona") || lowerName.contains("mebel") || lowerName.contains("idish") -> {
            CategoryStyleInfo(
                emoji = "🛋️",
                icon = Icons.Filled.Home,
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFF59E0B)), // Warm Amber to Gold
                bgTint = Color(0xFFFFFBEB),
                iconColor = Color(0xFFD97706),
                subcategories = listOf("Idish-tovoqlar", "Mebellar", "To‘shak & Yostiqlar", "Oshxona anjomlari", "Yoritgichlar")
            )
        }

        // 10. Bolalar mahsulotlari (Kids, Toys, Baby)
        lowerKey.contains("kid") || lowerKey.contains("baby") || lowerKey.contains("toy") ||
                lowerName.contains("bola") || lowerName.contains("o‘yinchoq") || lowerName.contains("oyinchoq") || lowerName.contains("chaqaloq") -> {
            CategoryStyleInfo(
                emoji = "🧸",
                icon = Icons.Filled.ChildCare,
                gradientColors = listOf(Color(0xFFEA580C), Color(0xFFF97316)), // Sunset Orange
                bgTint = Color(0xFFFFF7ED),
                iconColor = Color(0xFFEA580C),
                subcategories = listOf("O‘yinchoqlar", "Bolalar aravachalari", "Tagliklar", "Chaqaloq kiyimlari", "Bolalar ovqati")
            )
        }

        // 11. Avtomobil jihozlari (Auto, Car accessories, Oils)
        lowerKey.contains("auto") || lowerKey.contains("car") || lowerName.contains("avto") ||
                lowerName.contains("mashina") || lowerName.contains("ehtiyot") -> {
            CategoryStyleInfo(
                emoji = "🚘",
                icon = Icons.Filled.DirectionsCar,
                gradientColors = listOf(Color(0xFF334155), Color(0xFF64748B)), // Titanium Slate
                bgTint = Color(0xFFF8FAFC),
                iconColor = Color(0xFF334155),
                subcategories = listOf("Avtoaksessuarlar", "Motor moylari", "Videoregistratorlar", "Ehtiyot qismlar", "Avtokimyo")
            )
        }

        // 12. Sport & Dam olish (Sport, Fitness, Gym)
        lowerKey.contains("sport") || lowerKey.contains("fit") || lowerName.contains("sport") ||
                lowerName.contains("dam olish") || lowerName.contains("mashq") || lowerName.contains("futbol") || lowerName.contains("fitnes") -> {
            CategoryStyleInfo(
                emoji = "⚽",
                icon = Icons.Filled.FitnessCenter,
                gradientColors = listOf(Color(0xFFDC2626), Color(0xFFEF4444)), // Crimson Red
                bgTint = Color(0xFFFEF2F2),
                iconColor = Color(0xFFDC2626),
                subcategories = listOf("Trenajyorlar", "Sport kiyimlari", "To‘plar & Anjomlar", "Chodirlar & Lager", "Velosipedlar")
            )
        }

        // 13. Qurilish & Ta’mirlash (Tools, Construction)
        lowerKey.contains("tool") || lowerKey.contains("build") || lowerKey.contains("construct") ||
                lowerName.contains("qurilish") || lowerName.contains("ta’mirlash") || lowerName.contains("tamirlash") || lowerName.contains("asbob") -> {
            CategoryStyleInfo(
                emoji = "🛠️",
                icon = Icons.Filled.Build,
                gradientColors = listOf(Color(0xFFCA8A04), Color(0xFFEAB308)), // Yellow Gold
                bgTint = Color(0xFFFEFCE8),
                iconColor = Color(0xFFCA8A04),
                subcategories = listOf("Elektr asboblar", "Santexnika", "Qo‘l asboblari", "Bo‘yoq & Lak", "Yoritish moslamalari")
            )
        }

        // 14. Kantselyariya & Kitoblar (Books, Stationery, Office)
        lowerKey.contains("book") || lowerKey.contains("station") || lowerName.contains("kitob") ||
                lowerName.contains("kantselyariya") || lowerName.contains("daftar") || lowerName.contains("qalam") || lowerName.contains("ofis") -> {
            CategoryStyleInfo(
                emoji = "📚",
                icon = Icons.Filled.MenuBook,
                gradientColors = listOf(Color(0xFF4338CA), Color(0xFF6366F1)), // Deep Indigo
                bgTint = Color(0xFFEEF2FF),
                iconColor = Color(0xFF4338CA),
                subcategories = listOf("Badiiy kitoblar", "O‘quv adabiyotlari", "Daftar & Bloknot", "Qalam & Ruchkalar", "Ofis jihozlari")
            )
        }

        // 15. Zargarlik & Aksessuarlar (Jewelry, Watches, Accessories)
        lowerKey.contains("jewel") || lowerKey.contains("watch") || lowerName.contains("zargar") ||
                lowerName.contains("soat") || lowerName.contains("uzuk") || lowerName.contains("oltin") || lowerName.contains("kumush") || lowerName.contains("aksessuar") -> {
            CategoryStyleInfo(
                emoji = "💎",
                icon = Icons.Filled.Diamond,
                gradientColors = listOf(Color(0xFF9333EA), Color(0xFFC084FC)), // Amethyst Purple
                bgTint = Color(0xFFFAF5FF),
                iconColor = Color(0xFF9333EA),
                badgeTag = "VIP",
                subcategories = listOf("Qo‘l soatlari", "Zargarlik buyumlari", "Sumkalar & Hamyonlar", "Ko‘zoynaklar", "Kamar & Galstuk")
            )
        }

        // 16. Hayvonlar uchun tovarlar (Pets, Zoo)
        lowerKey.contains("pet") || lowerKey.contains("zoo") || lowerName.contains("hayvon") ||
                lowerName.contains("mushuk") || lowerName.contains("it") -> {
            CategoryStyleInfo(
                emoji = "🐾",
                icon = Icons.Filled.Pets,
                gradientColors = listOf(Color(0xFFB45309), Color(0xFFD97706)), // Cinnamon
                bgTint = Color(0xFFFFFBEB),
                iconColor = Color(0xFFB45309),
                subcategories = listOf("Itlar uchun ozuqa", "Mushuklar uchun ozuqa", "Parvarish & Gigiyena", "Katak & Uychalar", "O‘yinchoqlar")
            )
        }

        // 17. Bog‘dorchilik va Tomorqa (Garden, Plants, Flowers)
        lowerKey.contains("garden") || lowerKey.contains("plant") || lowerName.contains("bog‘") ||
                lowerName.contains("bog") || lowerName.contains("tomorqa") || lowerName.contains("gul") || lowerName.contains("ko‘chat") -> {
            CategoryStyleInfo(
                emoji = "🌷",
                icon = Icons.Filled.Spa,
                gradientColors = listOf(Color(0xFF059669), Color(0xFF34D399)), // Emerald to Mint
                bgTint = Color(0xFFECFDF5),
                iconColor = Color(0xFF059669),
                subcategories = listOf("Gullar & Ko‘chatlar", "Urug‘lar", "Bog‘ asboblari", "Tuvaklar", "Sug‘orish tizimlari")
            )
        }

        // 18. Hunarmandchilik va Xobbi (Hobby, Crafts, Art)
        lowerKey.contains("hobby") || lowerKey.contains("craft") || lowerKey.contains("art") ||
                lowerName.contains("hunar") || lowerName.contains("xobbi") || lowerName.contains("rasm") || lowerName.contains("musiqa") -> {
            CategoryStyleInfo(
                emoji = "🎨",
                icon = Icons.Filled.AutoAwesome,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7)), // Purple to Violet
                bgTint = Color(0xFFF5F3FF),
                iconColor = Color(0xFF7C3AED),
                subcategories = listOf("Rasm chizish", "Tikish & To‘qish", "Musiqa asboblari", "Modellashtirish", "Stol o‘yinlari")
            )
        }

        // 19. Aqlli uy va Xavfsizlik (Smart home, Security, Cameras)
        lowerKey.contains("smart_home") || lowerKey.contains("smarthome") || lowerKey.contains("security") ||
                lowerName.contains("aqlli uy") || lowerName.contains("xavfsizlik") || lowerName.contains("kamera") || lowerName.contains("datchik") -> {
            CategoryStyleInfo(
                emoji = "🔒",
                icon = Icons.Filled.Home,
                gradientColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)), // Teal
                bgTint = Color(0xFFF0FDFA),
                iconColor = Color(0xFF0F766E),
                badgeTag = "YANGI",
                subcategories = listOf("Videokuzatuv", "Aqlli datchiklar", "Elektron qulflar", "Domofonlar", "Aqlli rozetkalar")
            )
        }

        // 20. Barchasi / All Products
        lowerKey.contains("all") || lowerName.contains("barcha") -> {
            CategoryStyleInfo(
                emoji = "🌟",
                icon = Icons.Filled.GridView,
                gradientColors = listOf(Color(0xFF7E22CE), Color(0xFFA855F7)), // Uzum Royal Purple
                bgTint = Color(0xFFF3E8FF),
                iconColor = Color(0xFF7E22CE),
                subcategories = listOf("Aksiyalar", "Yangi mahsulotlar", "Chegirmalar", "Xit tovarlar")
            )
        }

        // Food Categories Specific Styles
        lowerKey.contains("pizza") || lowerName.contains("pitsa") -> {
            CategoryStyleInfo(
                emoji = "🍕",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFF97316)),
                bgTint = Color(0xFFFFF1F2),
                iconColor = Color(0xFFE11D48)
            )
        }
        lowerKey.contains("burger") || lowerName.contains("burger") -> {
            CategoryStyleInfo(
                emoji = "🍔",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFEA580C)),
                bgTint = Color(0xFFFFFBEB),
                iconColor = Color(0xFFD97706)
            )
        }
        lowerKey.contains("lavash") || lowerName.contains("lavash") || lowerName.contains("donar") -> {
            CategoryStyleInfo(
                emoji = "🌯",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFF16A34A), Color(0xFFEAB308)),
                bgTint = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A)
            )
        }
        lowerKey.contains("milliy") || lowerName.contains("milliy") || lowerName.contains("osh") || lowerName.contains("palov") || lowerName.contains("manti") || lowerName.contains("lagmon") -> {
            CategoryStyleInfo(
                emoji = "🍲",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFB45309), Color(0xFFEA580C)),
                bgTint = Color(0xFFFFFBEB),
                iconColor = Color(0xFFB45309)
            )
        }
        lowerKey.contains("shashlik") || lowerName.contains("shashlik") || lowerName.contains("kabob") -> {
            CategoryStyleInfo(
                emoji = "🍢",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D)),
                bgTint = Color(0xFFFEF2F2),
                iconColor = Color(0xFFDC2626)
            )
        }
        lowerKey.contains("tovuq") || lowerName.contains("tovuq") || lowerName.contains("gril") -> {
            CategoryStyleInfo(
                emoji = "🍗",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFEA580C), Color(0xFFF59E0B)),
                bgTint = Color(0xFFFFF7ED),
                iconColor = Color(0xFFEA580C)
            )
        }
        lowerKey.contains("somsa") || lowerName.contains("somsa") || lowerName.contains("pirojki") -> {
            CategoryStyleInfo(
                emoji = "🥟",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFCA8A04), Color(0xFFD97706)),
                bgTint = Color(0xFFFEFCE8),
                iconColor = Color(0xFFCA8A04)
            )
        }
        lowerKey.contains("sushi") || lowerName.contains("sushi") || lowerName.contains("roll") -> {
            CategoryStyleInfo(
                emoji = "🍣",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFF06B6D4)),
                bgTint = Color(0xFFFFF1F2),
                iconColor = Color(0xFFE11D48)
            )
        }
        lowerKey.contains("desert") || lowerName.contains("shirinlik") || lowerName.contains("desert") || lowerName.contains("tort") -> {
            CategoryStyleInfo(
                emoji = "🍰",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFDB2777), Color(0xFFF472B6)),
                bgTint = Color(0xFFFDF2F8),
                iconColor = Color(0xFFDB2777)
            )
        }
        lowerKey.contains("non") || lowerName.contains("non") || lowerName.contains("tandir") -> {
            CategoryStyleInfo(
                emoji = "🥖",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                bgTint = Color(0xFFFFFBEB),
                iconColor = Color(0xFFD97706)
            )
        }
        lowerKey.contains("ichimlik") || lowerName.contains("ichimlik") || lowerName.contains("kofe") || lowerName.contains("sharbat") || lowerName.contains("choy") -> {
            CategoryStyleInfo(
                emoji = "🥤",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF06B6D4)),
                bgTint = Color(0xFFF0F9FF),
                iconColor = Color(0xFF0284C7)
            )
        }
        lowerKey.contains("salat") || lowerName.contains("salat") || lowerName.contains("gazak") -> {
            CategoryStyleInfo(
                emoji = "🥗",
                icon = Icons.Filled.Fastfood,
                gradientColors = listOf(Color(0xFF16A34A), Color(0xFF84CC16)),
                bgTint = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A)
            )
        }

        // Default Fallback
        else -> {
            CategoryStyleInfo(
                emoji = "🎁",
                icon = Icons.Filled.Widgets,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                bgTint = Color(0xFFF5F3FF),
                iconColor = Color(0xFF6366F1)
            )
        }
    }
}

/**
 * Clean white category visual badge with colorful product logo inside.
 */
@Composable
fun CategoryVisualBadge(
    style: CategoryStyleInfo,
    size: Dp = 56.dp,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(),
        label = "badge_scale"
    )

    val borderColor = if (isSelected) style.gradientColors.first() else Color(0xFFE5E7EB)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(animatedScale)
            .shadow(
                elevation = if (isSelected) 5.dp else 1.5.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isSelected) style.gradientColors.first().copy(alpha = 0.25f) else Color(0x1A000000),
                ambientColor = Color(0x0D000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        // High-definition colorful real product emoji / 3D symbol on white canvas
        Text(
            text = style.emoji,
            fontSize = (size.value * 0.52f).sp,
            textAlign = TextAlign.Center
        )

        // Tag badge if available (e.g. "XIT", "YANGI", "TOP")
        if (style.badgeTag != null && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(style.bgTint)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = style.badgeTag,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    color = style.gradientColors.first()
                )
            }
        }

        // Active Checkmark Indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(style.gradientColors.first()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

/**
 * Uzum Market / Gagarin Go style category card:
 * High-vibrancy 3D product visual badge + clean bold category title.
 */
@Composable
fun UzumCategoryCard(
    label: String,
    iconKey: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = getCategoryStyleInfo(iconKey, label)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(76.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag("category_card_${label.lowercase().replace(" ", "_")}")
    ) {
        CategoryVisualBadge(
            style = style,
            size = 58.dp,
            isSelected = isSelected
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Category Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) style.gradientColors.first() else DarkText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
    }
}

/**
 * Category Chip for Filters & Search bars with mini colorful product logo.
 */
@Composable
fun CategoryChip(
    category: CategoryEntity?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = getCategoryStyleInfo(category?.iconKey ?: "all", label)
    val bgColor = if (isSelected) style.gradientColors.first() else CardSurface
    val contentColor = if (isSelected) Color.White else SecondaryNavy
    val borderColor = if (isSelected) style.gradientColors.first() else BorderColor

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = style.emoji,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * Full Category Item Card for CategoriesScreen.
 * Displays large colorful product logo badge, title, product count, and navigation arrow.
 */
@Composable
fun CategoryListItemCard(
    category: CategoryEntity,
    productCount: Int,
    onClick: () -> Unit,
    onSubcategoryClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val style = getCategoryStyleInfo(category.iconKey, category.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .testTag("category_list_item_${category.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Vibrant product logo badge
                    CategoryVisualBadge(
                        style = style,
                        size = 48.dp,
                        isSelected = false
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = DarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = style.bgTint,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "$productCount ta mahsulot",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = style.gradientColors.first(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (style.badgeTag != null) {
                                Text(
                                    text = "• ${style.badgeTag}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = style.gradientColors.first()
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(style.bgTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = style.gradientColors.first(),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Subcategories chips if present
            if (style.subcategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    style.subcategories.take(3).forEach { sub ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                if (onSubcategoryClick != null) {
                                    onSubcategoryClick(sub)
                                } else {
                                    onClick()
                                }
                            }
                        ) {
                            Text(
                                text = sub,
                                fontSize = 10.5.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (style.subcategories.size > 3) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${style.subcategories.size - 3}",
                                fontSize = 10.5.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Food Category Card for Gagarin Food section.
 */
@Composable
fun FoodCategoryVisualCard(
    name: String,
    iconEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = getCategoryStyleInfo(iconEmoji, name)
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFEA580C) else BorderColor,
        label = "food_cat_border"
    )

    Surface(
        color = if (isSelected) Color(0xFFFFF7ED) else CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.8.dp else 1.dp, animatedBorder),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("food_cat_${name.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, if (isSelected) Color(0xFFEA580C).copy(alpha = 0.5f) else Color(0xFFE5E7EB), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (iconEmoji.isNotBlank()) iconEmoji else style.emoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name,
                color = if (isSelected) Color(0xFFEA580C) else SecondaryNavy,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}
