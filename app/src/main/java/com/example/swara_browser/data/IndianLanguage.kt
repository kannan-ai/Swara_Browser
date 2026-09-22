package com.example.swara_browser.data

enum class IndianLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val searchPlaceholder: String,
    val settingsTitle: String,
    val quickAccessHeader: String,
    val privateModeLabel: String
) {
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        searchPlaceholder = "Search or type URL...",
        settingsTitle = "Browser Settings",
        quickAccessHeader = "Quick Access - Popular Indian Brands",
        privateModeLabel = "Private Incognito Mode Active"
    ),
    HINDI(
        code = "hi",
        nativeName = "हिंदी",
        englishName = "Hindi",
        searchPlaceholder = "खोजें या वेब पता दर्ज करें...",
        settingsTitle = "ब्राउज़र सेटिंग्स",
        quickAccessHeader = "त्वरित पहुंच - लोकप्रिय भारतीय ब्रांड",
        privateModeLabel = "निजी गोपनीयता मोड सक्षम"
    ),
    TAMIL(
        code = "ta",
        nativeName = "தமிழ்",
        englishName = "Tamil",
        searchPlaceholder = "தேடுங்கள் அல்லது முகவரியை தட்டச்சு செய்க...",
        settingsTitle = "உலாவி அமைப்புகள்",
        quickAccessHeader = "விரைவு அணுகல் - இந்திய பிராண்டுகள்",
        privateModeLabel = "தனியுரிமை முறை இயக்கப்பட்டது"
    ),
    TELUGU(
        code = "te",
        nativeName = "తెలుగు",
        englishName = "Telugu",
        searchPlaceholder = "శోధించండి లేదా చిరునామా నమోదు చేయండి...",
        settingsTitle = "బ్రౌజర్ సెట్టింగ్‌లు",
        quickAccessHeader = "త్వరిత యాక్సెస్ - భారతీయ బ్రాండ్‌లు",
        privateModeLabel = "గోప్యతా మోడ్ సక్రియం చేయబడింది"
    ),
    MARATHI(
        code = "mr",
        nativeName = "मराठी",
        englishName = "Marathi",
        searchPlaceholder = "शोधा किंवा वेब पत्ता टाका...",
        settingsTitle = "ब्राउझर सेटिंग्ज",
        quickAccessHeader = "जलद प्रवेश - लोकप्रिय भारतीय ब्रँड",
        privateModeLabel = "गोपनीयता मोड सक्षम"
    ),
    BENGALI(
        code = "bn",
        nativeName = "বাংলা",
        englishName = "Bengali",
        searchPlaceholder = "অনুসন্ধান করুন বা ইউআরএল লিখুন...",
        settingsTitle = "ব্রাউজার সেটিংস",
        quickAccessHeader = "দ্রুত অ্যাক্সেস - ভারতীয় ব্র্যান্ড",
        privateModeLabel = "গোপনীয়তা মোড চালু"
    ),
    KANNADA(
        code = "kn",
        nativeName = "ಕನ್ನಡ",
        englishName = "Kannada",
        searchPlaceholder = "ಹುಡುಕಿ ಅಥವಾ ವೆಬ್ ವಿಳಾಸ ನಮೂದಿಸಿ...",
        settingsTitle = "ಬ್ರೌಸರ್ ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
        quickAccessHeader = "ತ್ವರಿತ ಪ್ರವೇಶ - ಪ್ರಸಿದ್ಧ ಭಾರತೀಯ ಬ್ರ್ಯಾಂಡ್‌ಗಳು",
        privateModeLabel = "ಖಾಸಗಿ ಮೋಡ್ ಸಕ್ರಿಯಗೊಳಿಸಲಾಗಿದೆ"
    ),
    GUJARATI(
        code = "gu",
        nativeName = "ગુજરાતી",
        englishName = "Gujarati",
        searchPlaceholder = "શોધો અથવા વેબ સરનામું દાખલ કરો...",
        settingsTitle = "બ્રાઉઝર સેટિંગ્સ",
        quickAccessHeader = "ઝડપી પ્રવેશ - લોકપ્રિય ભારતીય બ્રાન્ડ્સ",
        privateModeLabel = "ગોપનીયતા મોડ સક્ષમ"
    ),
    MALAYALAM(
        code = "ml",
        nativeName = "മലയാളം",
        englishName = "Malayalam",
        searchPlaceholder = "തിരയുക അല്ലെങ്കിൽ വെബ് വിലാസം നൽകുക...",
        settingsTitle = "ബ്രൗസർ ക്രമീകരണങ്ങൾ",
        quickAccessHeader = "ദ്രുത പ്രവേശനം - ജനപ്രിയ ഇന്ത്യൻ ബ്രാൻഡുകൾ",
        privateModeLabel = "സ്വകാര്യതാ മോഡ് സജീവമാക്കി"
    ),
    PUNJABI(
        code = "pa",
        nativeName = "ਪੰਜਾਬੀ",
        englishName = "Punjabi",
        searchPlaceholder = "ਖੋਜੋ ਜਾਂ ਵੈੱਬ ਪਤਾ ਦਰਜ ਕਰੋ...",
        settingsTitle = "ਬ੍ਰਾਊਜ਼ਰ ਸੈਟਿੰਗਾਂ",
        quickAccessHeader = "ਤੁਰੰਤ ਪਹੁੰਚ - ਭਾਰਤੀ ਬ੍ਰਾਂਡ",
        privateModeLabel = "ਗੋਪਨੀਯਤਾ ਮੋਡ ਚਾਲੂ"
    )
}
