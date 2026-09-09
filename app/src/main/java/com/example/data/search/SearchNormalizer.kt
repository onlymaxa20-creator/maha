package com.example.data.search

import kotlin.math.min

/**
 * Intelligent Text Normalizer & Dictionary Resolver for Gagarin Go Marketplace.
 * Supports:
 * - Uzbek (Latin & Cyrillic), Russian, English
 * - Regional slangs, dialect terms & colloquial e-commerce jargons
 * - Transliteration & typo tolerance via Damerau-Levenshtein distance
 * - Semantic intent expansion
 */
object SearchNormalizer {

    // Cyrillic to Latin mapping for Uzbek/Russian phonetics
    private val cyrillicToLatinMap = mapOf(
        "а" to "a", "б" to "b", "в" to "v", "г" to "g", "д" to "d",
        "е" to "e", "ё" to "yo", "ж" to "j", "з" to "z", "и" to "i",
        "й" to "y", "к" to "k", "л" to "l", "м" to "m", "н" to "n",
        "о" to "o", "п" to "p", "р" to "r", "с" to "s", "т" to "t",
        "у" to "u", "ф" to "f", "х" to "x", "ц" to "ts", "ч" to "ch",
        "ш" to "sh", "щ" to "sh", "ъ" to "", "ы" to "i", "ь" to "",
        "э" to "e", "ю" to "yu", "я" to "ya", "ў" to "o'", "ғ" to "g'",
        "қ" to "q", "ҳ" to "h"
    )

    // Common regional slangs, typos and loanwords mapped to standard e-commerce keywords
    private val slangAndTypoDictionary = mapOf(
        // Footwear & Shoes
        "krosofka" to "krossovka",
        "krosovka" to "krossovka",
        "krossovka" to "krossovka",
        "krossovki" to "krossovka",
        "krassovka" to "krossovka",
        "krassovki" to "krossovka",
        "sneakers" to "krossovka",
        "sneaker" to "krossovka",
        "keta" to "krossovka",
        "keda" to "krossovka",
        "kedy" to "krossovka",
        "poyafzal" to "poyabzal",
        "poyabzal" to "poyabzal",
        "tufli" to "tufli",
        "tuflya" to "tufli",
        "tapochka" to "shippak",
        "shippak" to "shippak",
        "oyoqkiyim" to "poyabzal",
        "oyoq kiyim" to "poyabzal",
        "sapog" to "etik",
        "botinka" to "etik",

        // Phones & Electronics
        "tel" to "telefon",
        "telfon" to "telefon",
        "telifon" to "telefon",
        "telefoni" to "telefon",
        "telefon" to "telefon",
        "smartfon" to "smartfon",
        "smartfone" to "smartfon",
        "ayfon" to "iphone",
        "aifon" to "iphone",
        "iphon" to "iphone",
        "iphone" to "iphone",
        "samsun" to "samsung",
        "sumsung" to "samsung",
        "redmi" to "redmi",
        "xiaomi" to "xiaomi",
        "radmi" to "redmi",
        "zaryadka" to "quvvatlagich",
        "zaryadchik" to "quvvatlagich",
        "naushnik" to "quloqchin",
        "naushniki" to "quloqchin",
        "quloqchin" to "quloqchin",
        "airpod" to "airpods",
        "ayrpods" to "airpods",

        // Computers & Tech
        "nout" to "noutbuk",
        "notbuk" to "noutbuk",
        "noutbuk" to "noutbuk",
        "noutbook" to "noutbuk",
        "laptop" to "noutbuk",
        "komp" to "kompyuter",
        "kompyuter" to "kompyuter",
        "planshet" to "planshet",
        "plenshet" to "planshet",

        // Clothing & Fashion
        "svitshot" to "kofta",
        "sviter" to "kofta",
        "hudi" to "kofta",
        "hoodie" to "kofta",
        "tolstovka" to "kofta",
        "futbolka" to "futbolka",
        "mayka" to "futbolka",
        "ko'ylak" to "ko'ylak",
        "koylak" to "ko'ylak",
        "rubashka" to "ko'ylak",
        "kurtka" to "kurtka",
        "palto" to "kurtka",
        "vetrovka" to "kurtka",
        "jaket" to "kurtka",
        "shim" to "shim",
        "jinsi" to "jinsi",
        "djinsi" to "jinsi",
        "bryuk" to "shim",
        "bryuki" to "shim",
        "trixo" to "sport kiyim",
        "sportivka" to "sport kiyim",
        "kostyum" to "kostyum",

        // Tools, Auto & Appliances
        "moyka" to "avtomoyka",
        "avtomoyka" to "avtomoyka",
        "kerxer" to "avtomoyka",
        "karcher" to "avtomoyka",
        "yuvish" to "avtomoyka",
        "changyutgich" to "changyutgich",
        "pilesos" to "changyutgich",
        "pilisos" to "changyutgich",
        "muzlatgich" to "muzlatgich",
        "xolodilnik" to "muzlatgich",
        "konditsioner" to "konditsioner",
        "konditsaner" to "konditsioner",
        "kandisaner" to "konditsioner",
        "dazmol" to "dazmol",
        "utyug" to "dazmol",

        // Household, Accessories & Food
        "soat" to "soat",
        "smartwatch" to "soat",
        "chasi" to "soat",
        "parfyum" to "atir",
        "atir" to "atir",
        "duxi" to "atir",
        "sumka" to "sumka",
        "ryukzak" to "sumka",
        "rukzak" to "sumka",
        "kartoshka" to "kartoshka",
        "piyoz" to "piyoz",
        "olma" to "olma",
        "non" to "non",
        "sut" to "sut",
        "gosht" to "go'sht",
        "go'sht" to "go'sht",
        "shashlik" to "shashlik",
        "somsa" to "somsa",
        "osh" to "palov",
        "palov" to "palov",
        "pitsa" to "pizza",
        "pizza" to "pizza",
        "lavash" to "lavash",
        "burger" to "burger"
    )

    // Semantic Synonyms Groupings (Category and concept linkage)
    private val synonymGroups = mapOf(
        "krossovka" to listOf("krossovka", "poyabzal", "oyoq kiyim", "sneakers", "keta", "sport"),
        "telefon" to listOf("telefon", "smartfon", "iphone", "samsung", "redmi", "xiaomi", "mobil"),
        "noutbuk" to listOf("noutbuk", "laptop", "kompyuter", "asus", "hp", "lenovo", "macbook"),
        "avtomoyka" to listOf("avtomoyka", "moyka", "karcher", "yuvish apparati", "suv sepkich"),
        "kofta" to listOf("kofta", "svitshot", "sviter", "hudi", "kiyim", "ustki kiyim"),
        "kurtka" to listOf("kurtka", "palto", "vetrovka", "issiq kiyim", "qishki"),
        "changyutgich" to listOf("changyutgich", "pilesos", "tozalagich", "maishiy texnika"),
        "atir" to listOf("atir", "parfyum", "duxi", "hushbo'y", "tualet suvi"),
        "quloqchin" to listOf("quloqchin", "naushnik", "airpods", "bluetooth naushnik")
    )

    /**
     * Transliterates Cyrillic text to Latin phonetic representation.
     */
    fun cyrillicToLatin(input: String): String {
        val lower = input.lowercase()
        val sb = StringBuilder()
        var i = 0
        while (i < lower.length) {
            val ch = lower[i].toString()
            if (cyrillicToLatinMap.containsKey(ch)) {
                sb.append(cyrillicToLatinMap[ch])
            } else {
                sb.append(ch)
            }
            i++
        }
        return sb.toString()
    }

    /**
     * Cleans, transliterates and normalizes tokens from a raw user search query.
     */
    fun normalize(rawQuery: String): NormalizedQueryResult {
        val trimmed = rawQuery.trim().lowercase()
        if (trimmed.isEmpty()) {
            return NormalizedQueryResult("", "", emptyList(), null)
        }

        // 1. Transliterate Cyrillic to Latin
        val latinQuery = cyrillicToLatin(trimmed)
            .replace("[^a-zA-Z0-9\\s']".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        val tokens = latinQuery.split(" ").filter { it.isNotBlank() }
        val correctedTokens = mutableListOf<String>()
        var didYouMeanQuery: String? = null
        val expandedSynonyms = mutableSetOf<String>()

        var hasCorrection = false

        for (token in tokens) {
            // Direct dictionary lookup
            val mapped = slangAndTypoDictionary[token]
            if (mapped != null) {
                correctedTokens.add(mapped)
                if (mapped != token) hasCorrection = true
                synonymGroups[mapped]?.let { expandedSynonyms.addAll(it) }
            } else {
                // Fuzzy match against dictionary keys using Levenshtein distance
                val fuzzyMatch = findClosestMatch(token)
                if (fuzzyMatch != null) {
                    val resolved = slangAndTypoDictionary[fuzzyMatch] ?: fuzzyMatch
                    correctedTokens.add(resolved)
                    hasCorrection = true
                    synonymGroups[resolved]?.let { expandedSynonyms.addAll(it) }
                } else {
                    correctedTokens.add(token)
                    expandedSynonyms.add(token)
                }
            }
        }

        val canonicalQuery = correctedTokens.joinToString(" ")
        if (hasCorrection && canonicalQuery.isNotBlank() && canonicalQuery != trimmed) {
            didYouMeanQuery = canonicalQuery
        }

        return NormalizedQueryResult(
            originalQuery = trimmed,
            canonicalQuery = canonicalQuery,
            synonyms = expandedSynonyms.toList(),
            didYouMean = didYouMeanQuery
        )
    }

    /**
     * Damerau-Levenshtein distance to calculate typo similarity between words.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
                // Transposition
                if (i > 1 && j > 1 && s1[i - 1] == s2[j - 2] && s1[i - 2] == s2[j - 1]) {
                    dp[i][j] = min(dp[i][j], dp[i - 2][j - 2] + cost)
                }
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun findClosestMatch(token: String): String? {
        if (token.length < 3) return null
        var bestMatch: String? = null
        var minDistance = Int.MAX_VALUE

        val maxAllowedDistance = if (token.length <= 4) 1 else 2

        for (dictWord in slangAndTypoDictionary.keys) {
            val dist = levenshteinDistance(token, dictWord)
            if (dist <= maxAllowedDistance && dist < minDistance) {
                minDistance = dist
                bestMatch = dictWord
            }
        }
        return bestMatch
    }
}

data class NormalizedQueryResult(
    val originalQuery: String,
    val canonicalQuery: String,
    val synonyms: List<String>,
    val didYouMean: String?
)
