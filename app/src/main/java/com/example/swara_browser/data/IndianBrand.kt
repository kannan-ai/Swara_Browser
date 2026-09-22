package com.example.swara_browser.data

import androidx.compose.ui.graphics.Color

data class IndianBrand(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val accentColor: Color,
    val initialLetter: String
) {
    val faviconUrl: String
        get() = "https://www.google.com/s2/favicons?domain=$url&sz=128"

    companion object {
        val topIndianBrands = listOf(
            IndianBrand(
                id = "flipkart",
                name = "Flipkart",
                url = "https://www.flipkart.com",
                category = "Shopping",
                accentColor = Color(0xFF2874F0),
                initialLetter = "F"
            ),
            IndianBrand(
                id = "amazon_in",
                name = "Amazon IN",
                url = "https://www.amazon.in",
                category = "Shopping",
                accentColor = Color(0xFFFF9900),
                initialLetter = "A"
            ),
            IndianBrand(
                id = "irctc",
                name = "IRCTC Rail",
                url = "https://www.irctc.co.in",
                category = "Travel",
                accentColor = Color(0xFF20488A),
                initialLetter = "I"
            ),
            IndianBrand(
                id = "cricbuzz",
                name = "Cricbuzz",
                url = "https://www.cricbuzz.com",
                category = "Sports",
                accentColor = Color(0xFF009270),
                initialLetter = "C"
            ),
            IndianBrand(
                id = "swiggy",
                name = "Swiggy",
                url = "https://www.swiggy.com",
                category = "Food",
                accentColor = Color(0xFFFC8019),
                initialLetter = "S"
            ),
            IndianBrand(
                id = "zomato",
                name = "Zomato",
                url = "https://www.zomato.com",
                category = "Food",
                accentColor = Color(0xFFE23744),
                initialLetter = "Z"
            ),
            IndianBrand(
                id = "meesho",
                name = "Meesho",
                url = "https://www.meesho.com",
                category = "Shopping",
                accentColor = Color(0xFF911F5B),
                initialLetter = "M"
            ),
            IndianBrand(
                id = "hotstar",
                name = "Disney+ Hotstar",
                url = "https://www.hotstar.com",
                category = "Entertainment",
                accentColor = Color(0xFF131A28),
                initialLetter = "H"
            ),
            IndianBrand(
                id = "times_of_india",
                name = "Times of India",
                url = "https://timesofindia.indiatimes.com",
                category = "News",
                accentColor = Color(0xFFC8102E),
                initialLetter = "T"
            ),
            IndianBrand(
                id = "ndtv",
                name = "NDTV News",
                url = "https://www.ndtv.com",
                category = "News",
                accentColor = Color(0xFFDA291C),
                initialLetter = "N"
            ),
            IndianBrand(
                id = "paytm",
                name = "Paytm",
                url = "https://paytm.com",
                category = "Finance",
                accentColor = Color(0xFF00BAF2),
                initialLetter = "P"
            ),
            IndianBrand(
                id = "jiocinema",
                name = "JioCinema",
                url = "https://www.jiocinema.com",
                category = "Entertainment",
                accentColor = Color(0xFFE10078),
                initialLetter = "J"
            ),
            IndianBrand(
                id = "bookmyshow",
                name = "BookMyShow",
                url = "https://in.bookmyshow.com",
                category = "Entertainment",
                accentColor = Color(0xFFC02C39),
                initialLetter = "B"
            )
        )
    }
}
