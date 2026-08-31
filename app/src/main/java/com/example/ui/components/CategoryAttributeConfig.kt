package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSubtle

/**
 * Category-specific attribute configuration model
 */
data class CategoryAttributeGroup(
    val categoryKey: String,
    val defaultUnit: String,
    val recommendedUnits: List<String>,
    val sizeLabel: String? = null,
    val presetSizes: List<String> = emptyList(),
    val colorOptions: List<String> = emptyList(),
    val materialOrTypeLabel: String? = null,
    val presetMaterialsOrTypes: List<String> = emptyList(),
    val extraOptionLabel: String? = null,
    val presetExtraOptions: List<String> = emptyList(),
    val helperTip: String = ""
)

/**
 * Parsed specs from product description
 */
data class ParsedProductSpecs(
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val material: String? = null,
    val extra: String? = null,
    val cleanDescription: String = ""
)

/**
 * Returns tailored attribute rules according to category name or icon key.
 */
fun getCategoryAttributeGroup(categoryName: String, iconKey: String = ""): CategoryAttributeGroup {
    val lowerName = categoryName.lowercase().trim()
    val lowerKey = iconKey.lowercase().trim()

    return when {
        // 1. Poyabzallar (Shoes / Footwear)
        lowerName.contains("poyabzal") || lowerName.contains("oyoq") || lowerName.contains("krossovka") ||
                lowerName.contains("tufli") || lowerName.contains("etik") || lowerKey.contains("shoe") -> {
            CategoryAttributeGroup(
                categoryKey = "shoes",
                defaultUnit = "juft",
                recommendedUnits = listOf("juft", "dona", "to‘plam"),
                sizeLabel = "Oyoq kiyim o‘lchamlari (Razmer)",
                presetSizes = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"),
                colorOptions = listOf("Qora", "Oq", "Jigarrang", "Ko‘k", "Kulrang", "Qizil", "Yashil", "Bej"),
                materialOrTypeLabel = "Ustki material turi",
                presetMaterialsOrTypes = listOf("Tabiiy charm (Koja)", "Ekokoja", "Zamsh", "Tekstil / To‘r", "Rezina", "Kombinatsiyalangan"),
                extraOptionLabel = "Mavsum / Jins",
                presetExtraOptions = listOf("Erkaklar uchun", "Ayollar uchun", "Bolalar uchun", "Yozgi", "Qishki (Mo‘ynali)", "Bahor / Kuz"),
                helperTip = "Mavjud oyoq kiyim o‘lchamlari va ranglarini belgilang. Xaridorlar tanlash imkoniga ega bo‘ladilar."
            )
        }

        // 2. Oziq-ovqat va Ichimliklar (Food & Groceries)
        lowerName.contains("oziq") || lowerName.contains("ovqat") || lowerName.contains("meva") ||
                lowerName.contains("sabzavot") || lowerName.contains("go'sht") || lowerName.contains("gosht") ||
                lowerName.contains("ichimlik") || lowerName.contains("non") || lowerKey.contains("food") -> {
            CategoryAttributeGroup(
                categoryKey = "food",
                defaultUnit = "kg",
                recommendedUnits = listOf("kg", "gramm", "dona", "litr", "paket", "quti", "bog‘", "baklashka"),
                sizeLabel = "Qadoq / Vazn me’yori",
                presetSizes = listOf("1 kg", "500 gr", "250 gr", "100 gr", "1 litr", "1.5 litr", "5 litr", "1 dona", "1 quti", "1 qop (50kg)"),
                colorOptions = emptyList(),
                materialOrTypeLabel = "Mahsulot xususiyati / Turi",
                presetMaterialsOrTypes = listOf("Yangi uzilgan", "Mahalliy fermer (Organik)", "Halol sertifikatli", "Muzlatilgan", "Quritilgan", "Konservalangan"),
                extraOptionLabel = "Kelib chiqishi / Saqlash",
                presetExtraOptions = listOf("Mirzacho‘l / Gagarin", "Samarqand", "Toshkent", "Farg‘ona vodiysi", "Import", "Yaroqlilik: 3 kun", "Yaroqlilik: 1 oy", "Yaroqlilik: 6 oy"),
                helperTip = "Mahsulotning o‘lchov birligini (kg, gramm yoki litr) hamda yangiligini ko‘rsating."
            )
        }

        // 3. Kiyim-kechak (Clothing & Fashion)
        lowerName.contains("kiyim") || lowerName.contains("kechak") || lowerName.contains("libos") ||
                lowerName.contains("ko‘ylak") || lowerName.contains("shim") || lowerName.contains("kurtka") || lowerKey.contains("clothing") -> {
            CategoryAttributeGroup(
                categoryKey = "clothing",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "juft", "to‘plam"),
                sizeLabel = "Kiyim o‘lchamlari (Razmer)",
                presetSizes = listOf("XS", "S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL"),
                colorOptions = listOf("Qora", "Oq", "Kulrang", "Ko‘k", "To‘q ko‘k", "Qizil", "Yashil", "Bej", "Pushti"),
                materialOrTypeLabel = "Mato tarkibi",
                presetMaterialsOrTypes = listOf("100% Paxta (Xlopok)", "Ipak", "Jun (Sherst)", "Jinsi (Denim)", "Trikotaj", "Zig‘ir (Lyuon)", "Sintetika / Elastan"),
                extraOptionLabel = "Jins / Mavsum",
                presetExtraOptions = listOf("Erkaklar", "Ayollar", "Bolalar", "Yozgi", "Qishki", "Bahor/Kuz (Demi-sezon)"),
                helperTip = "Kiyim razmerlari (S, M, L, XL...) va mato turini tanlang."
            )
        }

        // 4. Smartfonlar va Gadjetlar (Smartphones & Gadgets)
        lowerName.contains("smartfon") || lowerName.contains("gadjet") || lowerName.contains("telefon") ||
                lowerName.contains("naushnik") || lowerName.contains("soat") || lowerKey.contains("smartphone") -> {
            CategoryAttributeGroup(
                categoryKey = "smartphones",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "komplekt"),
                sizeLabel = "Xotira hajmi (RAM / ROM)",
                presetSizes = listOf("4/64 GB", "6/128 GB", "8/128 GB", "8/256 GB", "12/256 GB", "12/512 GB", "16/512 GB", "1 TB"),
                colorOptions = listOf("Midnight Black", "Space Gray", "Silver", "Gold", "Deep Blue", "Titanium Gray", "Green", "White"),
                materialOrTypeLabel = "Holati va Kafolati",
                presetMaterialsOrTypes = listOf("Yangi qutida (Muhrlangan)", "1 yil rasmiy kafolat", "IMEI ro‘yxatidan o‘tgan", "Ideal holatda (A+)"),
                extraOptionLabel = "Komplektatsiyasi",
                presetExtraOptions = listOf("To‘liq quti + Zaryadlovchi", "Kabel + G‘ilof", "Himoya oynasi sovg‘a"),
                helperTip = "Smartfon xotira sig‘imi, rangi va kafolatini belgilang."
            )
        }

        // 5. Kompyuterlar va Noutbuklar (Computers & Laptops)
        lowerName.contains("kompyuter") || lowerName.contains("noutbuk") || lowerName.contains("laptop") ||
                lowerName.contains("monitor") || lowerKey.contains("computer") -> {
            CategoryAttributeGroup(
                categoryKey = "computers",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "komplekt"),
                sizeLabel = "Operativ va Doimiy xotira (RAM / SSD)",
                presetSizes = listOf("8GB / 256GB SSD", "8GB / 512GB SSD", "16GB / 512GB SSD", "16GB / 1TB SSD", "32GB / 1TB SSD", "64GB / 2TB SSD"),
                colorOptions = listOf("Space Gray", "Silver", "Qora", "Oq", "Ko‘k"),
                materialOrTypeLabel = "Protsessor (CPU)",
                presetMaterialsOrTypes = listOf("Intel Core i3", "Intel Core i5", "Intel Core i7", "Intel Core i9", "AMD Ryzen 5", "AMD Ryzen 7", "Apple M2", "Apple M3"),
                extraOptionLabel = "Kafolat va Holati",
                presetExtraOptions = listOf("1 yil rasmiy kafolat", "2 yil kafolat", "Yangi muhrlangan", "Ideal holatda"),
                helperTip = "Noutbuk yoki kompyuterning protsessor va xotira parametrlarini kiriting."
            )
        }

        // 6. Maishiy texnika (Appliances)
        lowerName.contains("maishiy") || lowerName.contains("texnika") || lowerName.contains("televizor") ||
                lowerName.contains("muzlatgich") || lowerName.contains("konditsioner") || lowerKey.contains("appliance") -> {
            CategoryAttributeGroup(
                categoryKey = "appliances",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "komplekt"),
                sizeLabel = "Hajmi / Sig‘imi / Diagonali",
                presetSizes = listOf("32 dyuym", "43 dyuym (4K)", "55 dyuym (4K)", "65 dyuym", "6 kg sig‘im", "7 kg sig‘im", "8 kg sig‘im", "300 litr", "400 litr"),
                colorOptions = listOf("Kumushrang (Inox)", "Oq", "Qora", "To‘q kulrang"),
                materialOrTypeLabel = "Xususiyatlari",
                presetMaterialsOrTypes = listOf("Invertor motor (Tejamkor)", "A+++ energiya sinfi", "Smart TV (Android)", "No Frost tizimi", "Wi-Fi boshqaruv"),
                extraOptionLabel = "Kafolat muddati",
                presetExtraOptions = listOf("3 yil rasmiy kafolat", "1 yil kafolat", "Bepul o‘rnatib berish"),
                helperTip = "Kafolat va energiya tejamkorlik parametrlarini belgilang."
            )
        }

        // 7. Go‘zallik va Parvarish (Beauty & Cosmetics)
        lowerName.contains("go‘zallik") || lowerName.contains("gozallik") || lowerName.contains("parvarish") ||
                lowerName.contains("atir") || lowerName.contains("krem") || lowerKey.contains("beauty") -> {
            CategoryAttributeGroup(
                categoryKey = "beauty",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "ml", "gramm", "to‘plam"),
                sizeLabel = "Hajmi / Og‘irligi",
                presetSizes = listOf("30 ml", "50 ml", "100 ml", "150 ml", "200 ml", "500 ml", "100 gr", "250 gr"),
                colorOptions = listOf("Shaffof", "Och bej", "Tabiiy pushti", "Qizil", "Jigarrang"),
                materialOrTypeLabel = "Teri va Soch turi",
                presetMaterialsOrTypes = listOf("Barcha teri turlariga", "Quruq teri uchun", "Yog‘li teri uchun", "Ta’sirchan teri", "100% Organik / Tabiiy"),
                extraOptionLabel = "Ishlab chiqaruvchi mamlakat",
                presetExtraOptions = listOf("Fransiya", "Koreya", "Turkiya", "O‘zbekiston", "BAA (Dubay)", "Germaniya"),
                helperTip = "Kosmetika va atirlarning millilitr (ml) hajmi va mos keluvchi teri turini belgilang."
            )
        }

        // 8. Salomatlik va Dorixona (Health & Pharmacy)
        lowerName.contains("salomatlik") || lowerName.contains("dorixona") || lowerName.contains("vitamin") || lowerKey.contains("health") -> {
            CategoryAttributeGroup(
                categoryKey = "health",
                defaultUnit = "quti",
                recommendedUnits = listOf("quti", "dona", "flakon", "paket"),
                sizeLabel = "Qadoqdagi soni / Dozasi",
                presetSizes = listOf("10 dona", "20 dona", "30 kapsula", "60 tabletka", "90 kapsula", "100 ml", "200 ml"),
                colorOptions = emptyList(),
                materialOrTypeLabel = "Ishlab chiqaruvchi",
                presetMaterialsOrTypes = listOf("Mahalliy (O‘zbekiston)", "Germaniya", "Turkiya", "AQSh", "Hindiston", "Rossiya"),
                extraOptionLabel = "Saqlash sharoiti",
                presetExtraOptions = listOf("Xona haroratida", "Salqin qorong‘i joyda", "Muzlatgichda (+2...+8°C)"),
                helperTip = "Tabletkalar soni, dozasi va saqlash sharoitini ko‘rsating."
            )
        }

        // 9. Uy-ro‘zg‘or va Oshxona (Home & Kitchen)
        lowerName.contains("uy") || lowerName.contains("ro‘zg‘or") || lowerName.contains("rozgor") ||
                lowerName.contains("oshxona") || lowerName.contains("mebel") || lowerKey.contains("home") -> {
            CategoryAttributeGroup(
                categoryKey = "home",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "to‘plam", "juft", "metr"),
                sizeLabel = "O‘lchami / Sig‘imi",
                presetSizes = listOf("2 litr", "3.5 litr", "5 litr", "160x200 sm", "180x200 sm", "200x220 sm", "6 kishilik (18 dona)", "12 kishilik (24 dona)"),
                colorOptions = listOf("Oq", "Kulrang", "Jigarrang", "Qora", "Bej", "Oltinrang", "Zanglamas po‘lat"),
                materialOrTypeLabel = "Asosiy material",
                presetMaterialsOrTypes = listOf("Zanglamas po‘lat (Nerjaveyka)", "Granit / Marmar qoplamali", "Keramika & Farfor", "Yog‘och (Tabiiy)", "Shisha", "Tefal"),
                extraOptionLabel = "Xususiyati",
                presetExtraOptions = listOf("Idish yuvish mashinasiga mos", "Induksion plitalarga mos", "Bakteriyalarga qarshi"),
                helperTip = "Idishlar va uy jihozlarining materiali va sig‘imini belgilang."
            )
        }

        // 10. Bolalar mahsulotlari (Kids & Toys)
        lowerName.contains("bola") || lowerName.contains("o‘yinchoq") || lowerName.contains("chaqaloq") || lowerKey.contains("kid") -> {
            CategoryAttributeGroup(
                categoryKey = "kids",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "to‘plam", "paket", "quti"),
                sizeLabel = "Yosh toifasi / O‘lchami",
                presetSizes = listOf("0-6 oy", "6-12 oy", "1-3 yosh", "3-6 yosh", "6-10 yosh", "Taglik: 3-razmer", "Taglik: 4-razmer", "Taglik: 5-razmer"),
                colorOptions = listOf("Moviy", "Pushti", "Sariq", "Yashil", "Oq", "Rang-barang"),
                materialOrTypeLabel = "Xavfsizlik va Material",
                presetMaterialsOrTypes = listOf("BPA Free (Zararsiz)", "100% Organik paxta", "Ekologik yog‘och", "Yumshoq gipoallergen", "Mustahkam plastik"),
                extraOptionLabel = "Ishlab chiqaruvchi",
                presetExtraOptions = listOf("Turkiya", "O‘zbekiston", "Polsha", "Rossiya", "Xitoy (Original)"),
                helperTip = "Bolalar tovarlari uchun tavsiya etilgan yosh chegarasini belgilang."
            )
        }

        // 11. Avtomobil jihozlari (Auto accessories)
        lowerName.contains("avto") || lowerName.contains("mashina") || lowerKey.contains("auto") -> {
            CategoryAttributeGroup(
                categoryKey = "auto",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "komplekt", "litr", "juft"),
                sizeLabel = "Mos keluvchi avtomobil modeli",
                presetSizes = listOf("Universal", "Cobalt", "Gentra / Lacetti", "Nexia 3", "Spark", "Tracker 2 / Onix", "Monza", "Kia / Hyundai"),
                colorOptions = listOf("Qora", "Kulrang", "Jigarrang", "Bej", "Qizil chiziqli"),
                materialOrTypeLabel = "Moy qovushqoqligi / Material",
                presetMaterialsOrTypes = listOf("5W-30 Sintetika", "5W-40 Sintetika", "10W-40 Yarim sintetik", "Ekokoja (Chexollar)", "Eva poliklar", "Original GM ehtiyot qism"),
                extraOptionLabel = "Hajmi / Kafolat",
                presetExtraOptions = listOf("1 litr", "4 litr", "5 litr", "6 oy kafolat", "1 yil kafolat"),
                helperTip = "Avtomobil aksessuarlari uchun mos model (Cobalt, Gentra...) va moy parametrlarini belgilang."
            )
        }

        // 12. Sport va Dam olish (Sport & Leisure)
        lowerName.contains("sport") || lowerName.contains("fitnes") || lowerName.contains("dam") || lowerKey.contains("sport") -> {
            CategoryAttributeGroup(
                categoryKey = "sport",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "juft", "komplekt"),
                sizeLabel = "O‘lcham / Vazn (kg)",
                presetSizes = listOf("S", "M", "L", "XL", "2XL", "2.5 kg", "5 kg", "10 kg", "15 kg", "20 kg", "Universal"),
                colorOptions = listOf("Qora", "Qizil", "Ko‘k", "Kulrang", "Yashil", "To‘q sariq"),
                materialOrTypeLabel = "Sport turi / Yo‘nalishi",
                presetMaterialsOrTypes = listOf("Futbol", "Fitnes va Gimnastika", "Boks va Yakkakurash", "Yugurish va Atletika", "Turizm va Lager"),
                extraOptionLabel = "Sifati / Material",
                presetExtraOptions = listOf("Professional daraja", "Havaskorlar uchun", "Suv o‘tkazmaydigan", "Mustahkam po‘lat"),
                helperTip = "Sport anjomlarining og‘irligi (kg) yoki kiyim o‘lchamini tanlang."
            )
        }

        // 13. Qurilish va Ta’mirlash (Tools & Construction)
        lowerName.contains("qurilish") || lowerName.contains("ta’mirlash") || lowerName.contains("tamirlash") ||
                lowerName.contains("asbob") || lowerKey.contains("tool") -> {
            CategoryAttributeGroup(
                categoryKey = "tools",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "metr", "m²", "kg", "qop", "rulon", "litr"),
                sizeLabel = "Quvvat / Uzunlik / Hajm",
                presetSizes = listOf("12V Akkumulyator", "20V Akkumulyator", "650W", "1200W", "2000W", "5 metr", "10 metr", "50 metr", "Qop (50 kg)"),
                colorOptions = emptyList(),
                materialOrTypeLabel = "Brend va Turi",
                presetMaterialsOrTypes = listOf("Bosch", "Total", "Crown", "Ingco", "DeWalt", "Knauf", "Professional sifat"),
                extraOptionLabel = "Kafolati",
                presetExtraOptions = listOf("1 yil kafolat", "6 oy kafolat", "Original litsenziyali"),
                helperTip = "Qurilish materiallari uchun o‘lchov birligi (metr, m², qop) yoki asbob quvvatini belgilang."
            )
        }

        // 14. Kantselyariya va Kitoblar (Books & Stationery)
        lowerName.contains("kitob") || lowerName.contains("kantselyariya") || lowerName.contains("daftar") || lowerKey.contains("book") -> {
            CategoryAttributeGroup(
                categoryKey = "books",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "to‘plam", "quti", "paket"),
                sizeLabel = "Varaq soni / Format",
                presetSizes = listOf("A4 format", "A5 format", "12 varaq", "48 varaq", "96 varaq", "200+ bet", "400+ bet"),
                colorOptions = emptyList(),
                materialOrTypeLabel = "Muqova va Til",
                presetMaterialsOrTypes = listOf("Qattiq muqova", "Yumshoq muqova", "O‘zbekcha (Lotin)", "O‘zbekcha (Kirill)", "Ruscha", "Inglizcha"),
                extraOptionLabel = "Janr / Yo‘nalish",
                presetExtraOptions = listOf("Badiiy adabiyot", "Psixologiya va Rivojlanish", "Darslik va O‘quv qo‘llanma", "Biznes va Moliya", "Bolalar kitoblari"),
                helperTip = "Kitobning tili, muqova turi va varaqlar sonini tanlang."
            )
        }

        // 15. Zargarlik va Aksessuarlar (Jewelry & Watches)
        lowerName.contains("zargar") || lowerName.contains("soat") || lowerName.contains("uzuk") ||
                lowerName.contains("oltin") || lowerName.contains("kumush") || lowerKey.contains("jewelry") -> {
            CategoryAttributeGroup(
                categoryKey = "jewelry",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "juft", "gramm"),
                sizeLabel = "Uzuk o‘lchami / Diametri",
                presetSizes = listOf("16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "21", "Universal"),
                colorOptions = listOf("Oltinrang (Sariq)", "Oq oltin / Kumushrang", "Qizil oltin", "Qora", "Jigarrang"),
                materialOrTypeLabel = "Metall va Proba",
                presetMaterialsOrTypes = listOf("Oltin 585-proba", "Kumush 925-proba", "Titan", "Zanglamas po‘lat", "Tabiiy charm", "Sirkoniy toshli", "Brilliant toshli"),
                extraOptionLabel = "Sertifikat va Kafolat",
                presetExtraOptions = listOf("Rasmiy proba tamg‘asi bilan", "1 yil kafolat", "Sovg‘abop qutida"),
                helperTip = "Taqinchoqning probasi, uzuk razmeri va qimmatbaho toshlarini belgilang."
            )
        }

        // Default generic
        else -> {
            CategoryAttributeGroup(
                categoryKey = "general",
                defaultUnit = "dona",
                recommendedUnits = listOf("dona", "kg", "litr", "juft", "to‘plam", "metr"),
                sizeLabel = "O‘lcham / Variant",
                presetSizes = listOf("Standart", "S", "M", "L", "XL", "1 kg", "1 litr"),
                colorOptions = listOf("Qora", "Oq", "Kulrang", "Ko‘k", "Qizil", "Yashil"),
                materialOrTypeLabel = "Xususiyati",
                presetMaterialsOrTypes = listOf("Original", "Premium sifat", "Mahalliy ishlab chiqarish"),
                extraOptionLabel = "Qo‘shimcha",
                presetExtraOptions = listOf("Kafolat bilan", "Yangi qutida"),
                helperTip = "Mahsulotning o‘lchami, rangi va o‘lchov birligini belgilang."
            )
        }
    }
}

/**
 * Parses existing description into structured attributes and clean description text.
 */
fun parseCategorySpecs(description: String): ParsedProductSpecs {
    if (description.isBlank()) return ParsedProductSpecs()

    val lines = description.lines()
    val sizes = mutableListOf<String>()
    val colors = mutableListOf<String>()
    var material: String? = null
    var extra: String? = null
    val cleanLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("O‘lchamlar:") || trimmed.startsWith("Razmer:") || trimmed.startsWith("O'lcham:") || trimmed.startsWith("Vazn:") || trimmed.startsWith("Xotira:")) {
            val value = trimmed.substringAfter(":").trim()
            sizes.addAll(value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        } else if (trimmed.startsWith("Ranglar:") || trimmed.startsWith("Rangi:") || trimmed.startsWith("Rang:")) {
            val value = trimmed.substringAfter(":").trim()
            colors.addAll(value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        } else if (trimmed.startsWith("Material:") || trimmed.startsWith("Turi:") || trimmed.startsWith("Protsessor:") || trimmed.startsWith("Tarkibi:")) {
            material = trimmed.substringAfter(":").trim()
        } else if (trimmed.startsWith("Kafolat:") || trimmed.startsWith("Mavsum:") || trimmed.startsWith("Xususiyati:") || trimmed.startsWith("Model:")) {
            extra = trimmed.substringAfter(":").trim()
        } else {
            cleanLines.add(line)
        }
    }

    return ParsedProductSpecs(
        sizes = sizes.distinct(),
        colors = colors.distinct(),
        material = material,
        extra = extra,
        cleanDescription = cleanLines.joinToString("\n").trim()
    )
}

/**
 * Builds formatted combined description text.
 */
fun buildCombinedDescription(
    cleanDesc: String,
    selectedSizes: List<String>,
    selectedColors: List<String>,
    selectedMaterial: String,
    selectedExtra: String
): String {
    val builder = StringBuilder()

    if (selectedSizes.isNotEmpty()) {
        builder.append("O‘lchamlar: ").append(selectedSizes.joinToString(", ")).append("\n")
    }
    if (selectedColors.isNotEmpty()) {
        builder.append("Ranglar: ").append(selectedColors.joinToString(", ")).append("\n")
    }
    if (selectedMaterial.isNotBlank()) {
        builder.append("Material: ").append(selectedMaterial.trim()).append("\n")
    }
    if (selectedExtra.isNotBlank()) {
        builder.append("Xususiyati: ").append(selectedExtra.trim()).append("\n")
    }

    if (builder.isNotEmpty() && cleanDesc.isNotBlank()) {
        builder.append("\n").append(cleanDesc.trim())
    } else if (cleanDesc.isNotBlank()) {
        builder.append(cleanDesc.trim())
    }

    return builder.toString().trim()
}

/**
 * Interactive Category-Specific Attribute Editor for adding/editing products.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryAttributeEditor(
    categoryName: String,
    currentUnit: String,
    onUnitChange: (String) -> Unit,
    selectedSizes: List<String>,
    onSizesChange: (List<String>) -> Unit,
    selectedColors: List<String>,
    onColorsChange: (List<String>) -> Unit,
    selectedMaterial: String,
    onMaterialChange: (String) -> Unit,
    selectedExtra: String,
    onExtraChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = remember(categoryName) { getCategoryAttributeGroup(categoryName) }
    var customSizeInput by remember { mutableStateOf("") }
    var showCustomSizeInput by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$categoryName parametrari",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                }

                Surface(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "MOSLASHTIRILGAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = config.helperTip,
                fontSize = 11.5.sp,
                color = SlateGray,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. O'LCHOV BIRLIGI (UNIT) CHIPS
            Text(
                text = "Tavsiya etilgan o‘lchov birligi:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                config.recommendedUnits.forEach { u ->
                    val isSelected = currentUnit.equals(u, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryBlue else CardSurface,
                        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                        modifier = Modifier.clickable { onUnitChange(u) }
                    ) {
                        Text(
                            text = u,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else SecondaryNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 2. SIZES / WEIGHTS / MEMORY CHIPS
            if (config.presetSizes.isNotEmpty() && config.sizeLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Straighten,
                            contentDescription = null,
                            tint = SecondaryNavy,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = config.sizeLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryNavy
                        )
                    }

                    if (selectedSizes.isNotEmpty()) {
                        Text(
                            text = "${selectedSizes.size} ta tanlandi",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    config.presetSizes.forEach { sizeOption ->
                        val isSelected = selectedSizes.contains(sizeOption)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SuccessGreen else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) SuccessGreen else BorderColor),
                            modifier = Modifier.clickable {
                                val updated = if (isSelected) {
                                    selectedSizes - sizeOption
                                } else {
                                    selectedSizes + sizeOption
                                }
                                onSizesChange(updated)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = sizeOption,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else SecondaryNavy
                                )
                            }
                        }
                    }

                    // Add Custom size button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.clickable { showCustomSizeInput = !showCustomSizeInput }
                    ) {
                        Text(
                            text = if (showCustomSizeInput) "✕ Yopish" else "+ Boshqa o‘lcham",
                            fontSize = 11.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                if (showCustomSizeInput) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customSizeInput,
                            onValueChange = { customSizeInput = it },
                            placeholder = { Text("Masalan: 47 yoki 2.5 kg", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                if (customSizeInput.isNotBlank()) {
                                    onSizesChange(selectedSizes + customSizeInput.trim())
                                    customSizeInput = ""
                                    showCustomSizeInput = false
                                }
                            }
                        ) {
                            Text(
                                text = "Qo‘shish",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // 3. COLOR SELECTION CHIPS
            if (config.colorOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = SecondaryNavy,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mavjud ranglar:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryNavy
                        )
                    }

                    if (selectedColors.isNotEmpty()) {
                        Text(
                            text = "${selectedColors.size} ta rang",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    config.colorOptions.forEach { colorName ->
                        val isSelected = selectedColors.contains(colorName)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF4338CA) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF4338CA) else BorderColor),
                            modifier = Modifier.clickable {
                                val updated = if (isSelected) {
                                    selectedColors - colorName
                                } else {
                                    selectedColors + colorName
                                }
                                onColorsChange(updated)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = colorName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else SecondaryNavy
                                )
                            }
                        }
                    }
                }
            }

            // 4. MATERIAL / TYPE CHIPS
            if (config.presetMaterialsOrTypes.isNotEmpty() && config.materialOrTypeLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = config.materialOrTypeLabel,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    config.presetMaterialsOrTypes.forEach { mat ->
                        val isSelected = selectedMaterial == mat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF0F766E) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else BorderColor),
                            modifier = Modifier.clickable {
                                onMaterialChange(if (isSelected) "" else mat)
                            }
                        ) {
                            Text(
                                text = mat,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 5. EXTRA / SEASON / WARRANTY CHIPS
            if (config.presetExtraOptions.isNotEmpty() && config.extraOptionLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = config.extraOptionLabel,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    config.presetExtraOptions.forEach { opt ->
                        val isSelected = selectedExtra == opt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF9333EA) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF9333EA) else BorderColor),
                            modifier = Modifier.clickable {
                                onExtraChange(if (isSelected) "" else opt)
                            }
                        ) {
                            Text(
                                text = opt,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SecondaryNavy,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive variant selector (e.g. shoe sizes, kg/unit weight, colors) on ProductDetailScreen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductVariantSelectorView(
    specs: ParsedProductSpecs,
    unit: String,
    selectedSize: String?,
    onSizeSelect: (String) -> Unit,
    selectedColor: String?,
    onColorSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (specs.sizes.isEmpty() && specs.colors.isEmpty()) return

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Sizes / Weights Selector
            if (specs.sizes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "O‘lcham / Variantni tanlang:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                    if (selectedSize != null) {
                        Text(
                            text = "Tanlandi: $selectedSize",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    specs.sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlue else SurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                            modifier = Modifier.clickable { onSizeSelect(size) }
                        ) {
                            Text(
                                text = size,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else DarkText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Colors Selector
            if (specs.colors.isNotEmpty()) {
                if (specs.sizes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rangini tanlang:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryNavy
                    )
                    if (selectedColor != null) {
                        Text(
                            text = "Tanlandi: $selectedColor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    specs.colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlue else SurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                            modifier = Modifier.clickable { onColorSelect(color) }
                        ) {
                            Text(
                                text = color,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else DarkText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean specs table for ProductDetailScreen (e.g. material, warranty, category attributes).
 */
@Composable
fun ProductSpecsCardView(
    specs: ParsedProductSpecs,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    if (specs.material == null && specs.extra == null && specs.sizes.isEmpty() && specs.colors.isEmpty()) return

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Mahsulot xususiyatlari",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (specs.material != null) {
                SpecRowItem(label = "Material / Turi", value = specs.material)
            }
            if (specs.extra != null) {
                SpecRowItem(label = "Xususiyati / Kafolat", value = specs.extra)
            }
            if (specs.sizes.isNotEmpty()) {
                SpecRowItem(label = "Mavjud o‘lchamlar", value = specs.sizes.joinToString(", "))
            }
            if (specs.colors.isNotEmpty()) {
                SpecRowItem(label = "Mavjud ranglar", value = specs.colors.joinToString(", "))
            }
            SpecRowItem(label = "Toifa", value = categoryName)
        }
    }
}

@Composable
private fun SpecRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = SlateGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText,
            modifier = Modifier.weight(1.3f)
        )
    }
}
