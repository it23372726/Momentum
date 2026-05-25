package com.example.projectpbd.domain.model

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val flag: String
)

object CurrencyRegistry {
    val currencies = listOf(
        CurrencyInfo("LKR", "Rs", "Sri Lankan Rupee", "Sri Lanka", "🇱🇰"),
        CurrencyInfo("USD", "$", "US Dollar", "United States", "🇺🇸"),
        CurrencyInfo("EUR", "€", "Euro", "European Union", "🇪🇺"),
        CurrencyInfo("GBP", "£", "British Pound", "United Kingdom", "🇬🇧"),
        CurrencyInfo("JPY", "¥", "Japanese Yen", "Japan", "🇯🇵"),
        CurrencyInfo("INR", "₹", "Indian Rupee", "India", "🇮🇳"),
        CurrencyInfo("AUD", "A$", "Australian Dollar", "Australia", "🇦🇺"),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", "Canada", "🇨🇦"),
        CurrencyInfo("CHF", "Fr", "Swiss Franc", "Switzerland", "🇨🇭"),
        CurrencyInfo("CNY", "¥", "Chinese Yuan", "China", "🇨🇳"),
        CurrencyInfo("AED", "د.إ", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
        CurrencyInfo("SGD", "S$", "Singapore Dollar", "Singapore", "🇸🇬"),
        CurrencyInfo("NZD", "NZ$", "New Zealand Dollar", "New Zealand", "🇳🇿"),
        CurrencyInfo("HKD", "HK$", "Hong Kong Dollar", "Hong Kong", "🇭🇰"),
        CurrencyInfo("KRW", "₩", "South Korean Won", "South Korea", "🇰🇷"),
        CurrencyInfo("TRY", "₺", "Turkish Lira", "Turkey", "🇹🇷"),
        CurrencyInfo("RUB", "₽", "Russian Ruble", "Russia", "🇷🇺"),
        CurrencyInfo("BRL", "R$", "Brazilian Real", "Brazil", "🇧🇷"),
        CurrencyInfo("ZAR", "R", "South African Rand", "South Africa", "🇿🇦"),
        CurrencyInfo("MXN", "$", "Mexican Peso", "Mexico", "🇲🇽"),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", "Malaysia", "🇲🇾"),
        CurrencyInfo("IDR", "Rp", "Indonesian Rupiah", "Indonesia", "🇮🇩"),
        CurrencyInfo("THB", "฿", "Thai Baht", "Thailand", "🇹🇭"),
        CurrencyInfo("PHP", "₱", "Philippine Peso", "Philippines", "🇵🇭"),
        CurrencyInfo("VND", "₫", "Vietnamese Dong", "Vietnam", "🇻🇳"),
        CurrencyInfo("SAR", "﷼", "Saudi Riyal", "Saudi Arabia", "🇸🇦"),
        CurrencyInfo("QAR", "﷼", "Qatari Rial", "Qatar", "🇶🇦"),
        CurrencyInfo("KWD", "د.ك", "Kuwaiti Dinar", "Kuwait", "🇰🇼"),
        CurrencyInfo("BHD", ".د.ب", "Bahraini Dinar", "Bahrain", "🇧🇭"),
        CurrencyInfo("OMR", "﷼", "Omani Rial", "Oman", "🇴🇲"),
        CurrencyInfo("ILS", "₪", "Israeli New Shekel", "Israel", "🇮🇱"),
        CurrencyInfo("SEK", "kr", "Swedish Krona", "Sweden", "🇸🇪"),
        CurrencyInfo("NOK", "kr", "Norwegian Krone", "Norway", "🇳🇴"),
        CurrencyInfo("DKK", "kr", "Danish Krone", "Denmark", "🇩🇰"),
        CurrencyInfo("PLN", "zł", "Polish Zloty", "Poland", "🇵🇱"),
        CurrencyInfo("HUF", "Ft", "Hungarian Forint", "Hungary", "🇭🇺"),
        CurrencyInfo("CZK", "Kč", "Czech Koruna", "Czech Republic", "🇨🇿"),
        CurrencyInfo("PKR", "Rs", "Pakistani Rupee", "Pakistan", "🇵🇰"),
        CurrencyInfo("BDT", "৳", "Bangladeshi Taka", "Bangladesh", "🇧🇩"),
        CurrencyInfo("EGP", "E£", "Egyptian Pound", "Egypt", "🇪🇬"),
        CurrencyInfo("NGN", "₦", "Nigerian Naira", "Nigeria", "🇳🇬"),
        CurrencyInfo("KES", "KSh", "Kenyan Shilling", "Kenya", "🇰🇪"),
        CurrencyInfo("USDT", "₮", "Tether", "Crypto", "🪙"),
        CurrencyInfo("BTC", "₿", "Bitcoin", "Crypto", "₿"),
        CurrencyInfo("ETH", "Ξ", "Ethereum", "Crypto", "Ξ")
    )

    fun getByCode(code: String): CurrencyInfo {
        return currencies.find { it.code == code } ?: currencies.first()
    }
}
