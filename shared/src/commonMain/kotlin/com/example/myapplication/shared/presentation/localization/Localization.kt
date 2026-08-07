package com.example.myapplication.shared.presentation.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.myapplication.shared.features.settings.model.AppLanguage

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun String.tr(): String {
    val language = LocalAppLanguage.current
    if (language == AppLanguage.HINDI) {
        return HindiTranslations[this] ?: this
    }
    return this
}

val HindiTranslations: Map<String, String> = mapOf(
    // Navigation & Toolbar Titles
    "Warehouse Management System" to "वेयरहाउस प्रबंधन प्रणाली",
    "Dashboard" to "डैशबोर्ड",
    "Shuttle Management" to "शटल प्रबंधन",
    "Operator Console" to "ऑपरेटर कंसोल",
    "Reports & Analytics" to "रिपोर्ट और विश्लेषण",
    "Diagnostics" to "डायग्नोस्टिक्स",
    "Maintenance" to "रखरखाव",
    "System Settings" to "सिस्टम सेटिंग्स",
    "User Management" to "उपयोगकर्ता प्रबंधन",
    "Overview" to "अवलोकन",
    "Shuttle" to "शटल",
    "Operator" to "ऑपरेटर",
    "Reports" to "रिपोर्ट्स",
    "Settings" to "सेटिंग्स",
    "Users" to "उपयोगकर्ता",

    // Common Buttons & Labels
    "Refresh" to "ताज़ा करें",
    "Connect" to "कनेक्ट करें",
    "Disconnect" to "डिसकनेक्ट करें",
    "Active" to "सक्रिय",
    "DISABLED" to "अक्षम",
    "AVAILABLE" to "उपलब्ध",
    "CONNECTED" to "कनेक्टेड",
    "DISCONNECTED" to "डिसकनेक्टेड",
    "ONLINE" to "ऑनलाइन",
    "OFFLINE" to "ऑफ़लाइन",
    "Edit" to "संपादित करें",
    "Save" to "सहेजें",
    "Save Changes" to "बदलाव सहेजें",
    "Cancel" to "रद्द करें",
    "Logout" to "लॉग आउट",
    "Submit" to "जमा करें",
    "Export PDF" to "पीडीएफ निर्यात करें",
    "Export CSV" to "सीएसवी निर्यात करें",
    "Export JSON" to "जेसन निर्यात करें",
    "Download" to "डाउनलोड",
    "Search" to "खोजें",
    "Filter" to "फ़िल्टर",
    "Back" to "वापस",
    "Delete" to "हटाएं",
    "Unregister" to "रद्द करें",
    "Register" to "पंजीकृत करें",
    "Status" to "स्थिति",
    "Actions" to "कार्रवाइयां",

    // Dashboard Cards & Widgets
    "System Overview & Operational Metrics" to "सिस्टम अवलोकन और परिचालन मेट्रिक्स",
    "Total Shuttles" to "कुल शटल",
    "Online Shuttles" to "ऑनलाइन शटल",
    "Offline Shuttles" to "ऑफ़लाइन शटल",
    "System Health" to "सिस्टम स्वास्थ्य",
    "Operational Status" to "परिचालन स्थिति",
    "Active Shuttle Connection" to "सक्रिय शटल कनेक्शन",
    "Recent Activity & Alerts" to "हाल की गतिविधियां और अलर्ट",
    "Quick Navigation" to "त्वरित नेविगेशन",
    "Telemetry Data" to "टेलीमेट्री डेटा",
    "No Active Shuttle" to "कोई सक्रिय शटल नहीं",
    "Connected to" to "से जुड़ा हुआ",

    // Settings Navigation & Sections
    "General Settings" to "सामान्य सेटिंग्स",
    "Communication Settings" to "संचार सेटिंग्स",
    "Reports & Backup" to "रिपोर्ट और बैकअप",
    "System & Security" to "सिस्टम और सुरक्षा",
    "Appearance" to "उपस्थिति",
    "Theme" to "थीम",
    "Language" to "भाषा",
    "Light Theme" to "लाइट थीम",
    "Dark Theme" to "डार्क थीम",
    "Font Size" to "फ़ॉन्ट आकार",
    "Medium (Default)" to "मध्यम (डिफ़ॉल्ट)",
    "Small" to "छोटा",
    "Large" to "बड़ा",
    "English (US)" to "अंग्रेज़ी (English)",
    "Hindi (हिंदी)" to "हिंदी (Hindi)",
    "Communication Mode" to "संचार मोड",
    "MQTT Broker Address" to "एमक्विटीटी ब्रोकर पता",
    "MQTT Port" to "एमक्विटीटी पोर्ट",
    "Client ID" to "क्लाइंट आईडी",
    "Allowed Shuttle IPs" to "अनुमत शटल आईपी",
    "WebSocket Port" to "वेबसोकेट पोर्ट",
    "Keep Alive Interval" to "कीप अलाइव अंतराल",
    "Automatic Backup" to "स्वचालित बैकअप",
    "Backup Location" to "बैकअप स्थान",
    "Report Export Format" to "रिपोर्ट निर्यात प्रारूप",

    // Shuttle Management & Reports
    "Registered Shuttles" to "पंजीकृत शटल",
    "Discovered Shuttles" to "खोजे गए शटल",
    "Select Data Source" to "डेटा स्रोत चुनें",
    "All Shuttles" to "सभी शटल",
    "Individual Shuttles" to "व्यक्तिगत शटल",
    "INDIVIDUAL SHUTTLES" to "व्यक्तिगत शटल",
    "Aggregated reports and analytics across all operational shuttles" to "सभी परिचालन शटल का संचयी रिपोर्ट और विश्लेषण",
    "Choose a shuttle to view its reports, or select All Shuttles for aggregated data." to "रिपोर्ट देखने के लिए शटल चुनें, या संचयी डेटा के लिए सभी शटल चुनें।",
    "No individual online shuttles active. Select 'All Shuttles' above for system reports." to "कोई व्यक्तिगत ऑनलाइन शटल सक्रिय नहीं है। सिस्टम रिपोर्ट के लिए ऊपर 'सभी शटल' चुनें।",

    // Operator Console & Diagnostics & Maintenance
    "Manual Controls" to "मैन्युअल नियंत्रण",
    "Auto Move" to "ऑटो मूव",
    "Lift Up" to "लिफ्ट ऊपर",
    "Lift Down" to "लिफ्ट नीचे",
    "Move Forward" to "आगे बढ़ें",
    "Move Backward" to "पीछे हटें",
    "Emergency Stop" to "आपातकालीन रोक",
    "Battery Level" to "बैटरी स्तर",
    "Position" to "स्थिति",
    "Speed" to "गति",
    "Temperature" to "तापमान",
    "Diagnostic Tests" to "डायग्नोस्टिक परीक्षण",
    "Run Full Diagnostics" to "पूर्ण डायग्नोस्टिक्स चलाएं",
    "Sensor Calibration" to "सेंसर अंशांकन",
    "Motor Test" to "मोटर परीक्षण",
    "Battery Health" to "बैटरी स्वास्थ्य",
    "Maintenance Schedule" to "रखरखाव अनुसूची",
    "Perform Maintenance" to "रखरखाव करें",
    "Log History" to "लॉग इतिहास",

    // User Management & Login
    "User Accounts" to "उपयोगकर्ता खाते",
    "Add User" to "उपयोगकर्ता जोड़ें",
    "Role" to "भूमिका",
    "Admin" to "एडमिन",
    "Operator" to "ऑपरेटर",
    "Technician" to "तकनीशियन",
    "Supervisor" to "पर्यवेक्षक",
    "Full Name" to "पूरा नाम",
    "Email" to "ईमेल",
    "Username" to "उपयोगकर्ता नाम",
    "Password" to "पासवर्ड",
    "Login" to "लॉगिन",
    "Welcome Back" to "पुनः स्वागत है",
    "Sign in to Warehouse Management System" to "वेयरहाउस प्रबंधन प्रणाली में साइन इन करें"
)
