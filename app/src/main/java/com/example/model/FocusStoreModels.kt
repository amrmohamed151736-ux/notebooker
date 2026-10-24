package com.example.model

enum class StoreCategory(val labelAr: String) {
    COLLECTIBLES("المقتنيات النادرة"),
    TIMER_SKINS("أشكال المؤقت"),
    SOUND_PACKS("حزم المؤثرات الصوتية"),
    THEMES("الثيمات والخلفيات"),
    THEME_STUDIO("استوديو الثيمات"),
    STATS_CARDS("لوحة الإحصائيات"),
    BADGES_TITLES("الشارات والألقاب")
}

enum class ItemRarity(val labelAr: String, val colorHex: Long) {
    COMMON("عادي", 0xFF64748B),
    UNCOMMON("غير شائع", 0xFF059669),
    RARE("نادر", 0xFF2563EB),
    EPIC("ملحمي", 0xFF9333EA),
    LEGENDARY("أسطوري", 0xFFD97706)
}

enum class TimerSkinType(val id: String, val titleAr: String, val descriptionAr: String) {
    CIRCULAR("SKIN_CIRCULAR", "مؤقت دائري حديث", "حلقة تقدم متدفقة ومؤشرات زمنية ناعمة"),
    DIGITAL("SKIN_DIGITAL", "مؤقت رقمي واضح", "أرقام شاشات LED عريضة وعالية المقروئية"),
    FLIP("SKIN_FLIP", "أرقام متقلبة Flip Clock", "بطاقات أرقام ميكانيكية بتقليب ثلاثي الأبعاد"),
    CLASSIC("SKIN_CLASSIC", "ساعة كلاسيكية", "ميناء ساعة عقارب بنقاط دقيقة وأسلوب عريق"),
    BAR("SKIN_BAR", "مؤشر شريطي أفقي", "شريط طاقة أفقي ممتد مع نسب مئوية فورية"),
    SCI_FI("SKIN_SCI_FI", "لوحة تحكم مستقبلية", "واجهة خيال علمي HUD بقراءات هندسية رنانة"),
    MINIMAL("SKIN_MINIMAL", "مؤقت بسيط مصغر", "تصميم ناعم ومضغوط وموفر للمساحة")
}

data class StoreItem(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val category: StoreCategory,
    val price: Int,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val iconKey: String = "DEFAULT",
    val unlockConditionAr: String? = null,
    val isDefaultOwned: Boolean = false
)

object StoreCatalog {
    val items: List<StoreItem> = listOf(
        // --- 1. Rare Collectibles ---
        StoreItem(
            id = "COL_ROBOT",
            titleAr = "روبوت التركيز الذكي (Aura-Bot)",
            descriptionAr = "مساعد رقمي صغير يراقب جلساتك ويبعث طاقة إيجابية دائمة أثناء إنجاز المهام الصعبة.",
            category = StoreCategory.COLLECTIBLES,
            price = 45,
            rarity = ItemRarity.UNCOMMON,
            iconKey = "ROBOT"
        ),
        StoreItem(
            id = "COL_CRYSTAL",
            titleAr = "بلورة الكوارتز السديمية",
            descriptionAr = "حجر مضيء مصقول بضوء بنفسجي يعكس صفاء الذهن والقدرة على التركيز لمدد متواصلة.",
            category = StoreCategory.COLLECTIBLES,
            price = 80,
            rarity = ItemRarity.RARE,
            iconKey = "CRYSTAL"
        ),
        StoreItem(
            id = "COL_SPACE_HELMET",
            titleAr = "خوذة المستكشف الكونية",
            descriptionAr = "رمز لعزل الضوضاء والانغماس في أعماق التعلم كأنك تطفو في الفضاء الخارجي الهادئ.",
            category = StoreCategory.COLLECTIBLES,
            price = 120,
            rarity = ItemRarity.EPIC,
            iconKey = "SPACE"
        ),
        StoreItem(
            id = "COL_CHARM",
            titleAr = "تميمة الحكمة الأكاديمية",
            descriptionAr = "ميدالية برونزية تقليدية تمنحك إحساساً مستمراً بالارتباط بالدفاتر والمكتبات القديمة.",
            category = StoreCategory.COLLECTIBLES,
            price = 50,
            rarity = ItemRarity.COMMON,
            iconKey = "CHARM"
        ),
        StoreItem(
            id = "COL_SHIELD",
            titleAr = "درع الصمود الفولاذي",
            descriptionAr = "رمز لعدم الاستسلام ومقاومة المشتتات والعودة الدائمة إلى جلسات العمل.",
            category = StoreCategory.COLLECTIBLES,
            price = 90,
            rarity = ItemRarity.RARE,
            iconKey = "SHIELD"
        ),
        StoreItem(
            id = "COL_HOURGLASS",
            titleAr = "ساعة الرمل الأسطورية",
            descriptionAr = "أندر المقتنيات في المتجر، تحتوي على رمال ذهبية نقية تدل على سيطرتك المطلقة على وقتك.",
            category = StoreCategory.COLLECTIBLES,
            price = 200,
            rarity = ItemRarity.LEGENDARY,
            iconKey = "HOURGLASS"
        ),

        // --- 2. Timer Skins ---
        StoreItem(
            id = "SKIN_CIRCULAR",
            titleAr = "مؤقت دائري حديث",
            descriptionAr = "المظهر الافتراضي الأنيق بحلقة تقدم سلسة ولمسات لونية حيوية.",
            category = StoreCategory.TIMER_SKINS,
            price = 0,
            rarity = ItemRarity.COMMON,
            isDefaultOwned = true
        ),
        StoreItem(
            id = "SKIN_DIGITAL",
            titleAr = "مؤقت رقمي واضح",
            descriptionAr = "شاشة أرقام عملاقة تسهل متابعة الوقت المتبقي بنظرة سريعة من مسافة بعيدة.",
            category = StoreCategory.TIMER_SKINS,
            price = 30,
            rarity = ItemRarity.UNCOMMON
        ),
        StoreItem(
            id = "SKIN_FLIP",
            titleAr = "مؤقت أرقام مقلوبة (Retro Flip)",
            descriptionAr = "محاكاة واقعية للوحات الساعات الميكانيكية الشهيرة التي تتقلب كل دقيقة.",
            category = StoreCategory.TIMER_SKINS,
            price = 45,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "SKIN_CLASSIC",
            titleAr = "مؤقت ساعة كلاسيكية",
            descriptionAr = "ساعة معصم عريقة بعقارب ومؤشرات ذهبية دقيقة لمحبي الطابع التناظري.",
            category = StoreCategory.TIMER_SKINS,
            price = 60,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "SKIN_BAR",
            titleAr = "مؤشر شريطي أفقي",
            descriptionAr = "شريط طاقة متدرج يعرض نسبة الإنجاز والزمن بنقاء وانسيابية تامة.",
            category = StoreCategory.TIMER_SKINS,
            price = 35,
            rarity = ItemRarity.UNCOMMON
        ),
        StoreItem(
            id = "SKIN_SCI_FI",
            titleAr = "لوحة تحكم مستقبلية (Sci-Fi HUD)",
            descriptionAr = "تصميم سيبراني مستقبلي بزوايا نيون ومؤشرات شبيهة بقمرة مركبة فضاء.",
            category = StoreCategory.TIMER_SKINS,
            price = 90,
            rarity = ItemRarity.EPIC
        ),
        StoreItem(
            id = "SKIN_MINIMAL",
            titleAr = "مؤقت بسيط مصغر",
            descriptionAr = "تصميم مدمج ونظيف جداً يركز فقط على الوقت لتقليل أي تشتيت بصري.",
            category = StoreCategory.TIMER_SKINS,
            price = 25,
            rarity = ItemRarity.COMMON
        ),

        // --- 3. Sound Effect Packs ---
        StoreItem(
            id = "SND_ZEN_BELL",
            titleAr = "حزمة أجراس التأمل (Zen Bells)",
            descriptionAr = "نغمات هارمونية نقية تشبه صدى أوعية التبت الهادئة لبدء ونهاية الجلسات.",
            category = StoreCategory.SOUND_PACKS,
            price = 0,
            rarity = ItemRarity.COMMON,
            isDefaultOwned = true
        ),
        StoreItem(
            id = "SND_NATURE",
            titleAr = "حزمة الطبيعة والمطر",
            descriptionAr = "ترددات مهدئة وقطرات مطر ونبضات استرخاء تساعد على التركيز العميق.",
            category = StoreCategory.SOUND_PACKS,
            price = 35,
            rarity = ItemRarity.UNCOMMON
        ),
        StoreItem(
            id = "SND_POINTS_CHIME",
            titleAr = "حزمة رنين المكافآت الذهبي",
            descriptionAr = "أصوات رنانة مبهجة تعزف نغمة تصاعدية مع كل نقطة تضاف إلى محفظتك.",
            category = StoreCategory.SOUND_PACKS,
            price = 40,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "SND_CYBER",
            titleAr = "حزمة النبضات السيبرانية",
            descriptionAr = "مؤثرات إلكترونية وموجات سنثسايزر حديثة لمحبي أجواء الإنتاجية التقنية.",
            category = StoreCategory.SOUND_PACKS,
            price = 55,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "SND_FANFARE",
            titleAr = "أبواق النصر والبطولة (Hero Fanfare)",
            descriptionAr = "نغمة احتفالية ترحيبية تصدح عند إكمال الجلسة وفتح إنجاز جديد.",
            category = StoreCategory.SOUND_PACKS,
            price = 70,
            rarity = ItemRarity.EPIC
        ),

        // --- 4. Pre-built Themes & Wallpapers ---
        StoreItem(
            id = "THM_DARK_SLATE",
            titleAr = "ثيم السليت الليلي (Dark Slate)",
            descriptionAr = "درجات الرمادي الداكن المطفأ مع لمسات زرقاء سماوية لراحة العين في الظلام.",
            category = StoreCategory.THEMES,
            price = 0,
            rarity = ItemRarity.COMMON,
            isDefaultOwned = true
        ),
        StoreItem(
            id = "THM_WARM_PAPER",
            titleAr = "ثيم الورق الدافئ (Paper Warmth)",
            descriptionAr = "ألوان العاج والورق العتيق مع حبر كهرماني مريح يماثل دفاتر المذكرات الفاخرة.",
            category = StoreCategory.THEMES,
            price = 40,
            rarity = ItemRarity.UNCOMMON
        ),
        StoreItem(
            id = "THM_FOREST",
            titleAr = "ثيم الزمرد النباتي (Emerald Forest)",
            descriptionAr = "طبيعة خضراء منعشة تعزز الشعور بالانتعاش والنشاط وتجديد الطاقة.",
            category = StoreCategory.THEMES,
            price = 50,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "THM_CYBERPUNK",
            titleAr = "ثيم النيون السيبراني (Neon Cyberpunk)",
            descriptionAr = "أسود فاحم مع وردي نيون وأزرق مشع للمبرمجين ومصممي المستقبل.",
            category = StoreCategory.THEMES,
            price = 85,
            rarity = ItemRarity.EPIC
        ),
        StoreItem(
            id = "THM_COSMIC",
            titleAr = "ثيم السديم الكوني (Cosmic Nebula)",
            descriptionAr = "مزيج بنفسجي ملكي مع ومضات نجمية ذهبية لشغف الإبداع اللامحدود.",
            category = StoreCategory.THEMES,
            price = 110,
            rarity = ItemRarity.LEGENDARY
        ),

        // --- 5. Badges & Titles ---
        StoreItem(
            id = "TITLE_NOVICE",
            titleAr = "لقب: مبتدئ التركيز",
            descriptionAr = "خطوتك الأولى في بناء مسار الإنضباط اليومي.",
            category = StoreCategory.BADGES_TITLES,
            price = 0,
            rarity = ItemRarity.COMMON,
            isDefaultOwned = true
        ),
        StoreItem(
            id = "TITLE_HABIT",
            titleAr = "لقب: صانع العادات",
            descriptionAr = "يُمنح لمن حول جلسات العمل إلى عادة يومية راسخة.",
            category = StoreCategory.BADGES_TITLES,
            price = 30,
            rarity = ItemRarity.UNCOMMON
        ),
        StoreItem(
            id = "TITLE_DISCIPLINE",
            titleAr = "لقب: محترف الانضباط",
            descriptionAr = "شهادة استحقاق لإكمال الجلسات بتركيز نقي دون مقاطعات.",
            category = StoreCategory.BADGES_TITLES,
            price = 60,
            rarity = ItemRarity.RARE
        ),
        StoreItem(
            id = "TITLE_ARCHITECT",
            titleAr = "لقب: مهندس التركيز",
            descriptionAr = "دلالة على تنظيم متقدم وإتقان لأدوات وإحصائيات العمل الأكاديمي.",
            category = StoreCategory.BADGES_TITLES,
            price = 95,
            rarity = ItemRarity.EPIC
        ),
        StoreItem(
            id = "TITLE_LEGEND",
            titleAr = "لقب: أسطورة الاستمرارية",
            descriptionAr = "أعلى ألقاب المتجر فخامة، يبرهن على التزام أسطوري لا ينقطع.",
            category = StoreCategory.BADGES_TITLES,
            price = 150,
            rarity = ItemRarity.LEGENDARY
        )
    )
}

data class AchievementItem(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val rewardPoints: Int,
    val conditionType: String,
    val targetValue: Int
)

object AchievementCatalog {
    val achievements: List<AchievementItem> = listOf(
        AchievementItem(
            id = "ACH_FIRST_SESSION",
            titleAr = "الشرارة الأولى",
            descriptionAr = "إكمال أول جلسة تركيز كاملة وناجحة.",
            rewardPoints = 15,
            conditionType = "COMPLETED_SESSIONS",
            targetValue = 1
        ),
        AchievementItem(
            id = "ACH_10_SESSIONS",
            titleAr = "عشرية الإنجاز",
            descriptionAr = "إتمام 10 جلسات تركيز مسجلة في السجل.",
            rewardPoints = 40,
            conditionType = "COMPLETED_SESSIONS",
            targetValue = 10
        ),
        AchievementItem(
            id = "ACH_100_MINUTES",
            titleAr = "نادي المئة دقيقة",
            descriptionAr = "الوصول إلى 100 دقيقة تركيز فعلية ومحتسبة.",
            rewardPoints = 30,
            conditionType = "TOTAL_MINUTES",
            targetValue = 100
        ),
        AchievementItem(
            id = "ACH_500_MINUTES",
            titleAr = "عملاق الإنتاجية",
            descriptionAr = "تخطي 500 دقيقة تركيز في التطبيق.",
            rewardPoints = 75,
            conditionType = "TOTAL_MINUTES",
            targetValue = 500
        ),
        AchievementItem(
            id = "ACH_BUY_COLLECTIBLE",
            titleAr = "جامع التحف",
            descriptionAr = "اقتناء أول تحفة نادرة من موسوعة المتجر.",
            rewardPoints = 25,
            conditionType = "COLLECTIBLES_OWNED",
            targetValue = 1
        ),
        AchievementItem(
            id = "ACH_CUSTOM_THEME",
            titleAr = "المصمم المبدع",
            descriptionAr = "ابتكار وحفظ أول ثيم مخصص في استوديو الثيمات.",
            rewardPoints = 20,
            conditionType = "THEMES_CREATED",
            targetValue = 1
        )
    )
}
