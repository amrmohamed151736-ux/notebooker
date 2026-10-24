package com.example.model

enum class CoverStyle(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String
) {
    LEATHER_BOUND(
        "جلد كلاسيكي فاخر",
        "Classic Debossed Leather",
        "مظهر جلدي فخم مع خياطة محيطية وحروف ذهبية غائرة.",
        "Rich bookbound leather texture with perimeter stitching & gold deboss."
    ),
    LINEN_BOOKCLOTH(
        "قماش الكتان المجلد",
        "Linen Bookcloth Weave",
        "ملمس قماشي راقٍ يشبه كتب الفنون والروايات الفاخرة.",
        "Textured woven cloth finish inspired by bespoke hardcovers."
    ),
    KRAFT_NATURAL(
        "ورق كرافت ياباني طبيعي",
        "Raw Natural Kraft",
        "ورق كرافت بيئي عتيق مع لمسات تصميم يابانية نقية.",
        "Warm organic recycled kraft cardstock with minimalist stamps."
    ),
    MINIMAL_MODERN(
        "حداثي بسيط ومحدد",
        "Modern Minimalist",
        "ألوان باستيل هادئة مع خطوط طباعية نقية وبادج مركزي.",
        "Clean muted pastel or stark contrast with refined typography."
    ),
    SCIENCE_BLUEPRINT(
        "مخطط تقني وهندسي",
        "Scientific & Blueprint",
        "خلفية زرقاء تقنية مع خطوط شبكية ورسوم بيانية دقيقة.",
        "Deep technical blueprint cyan with architectural drafting lines."
    ),
    BOTANICAL_ELEGANT(
        "نباتي وطبيعي أنيق",
        "Botanical & Organic",
        "زخارف أوراق وشعارات طبيعية مع تدرجات زيتية وترابية.",
        "Serene organic earth tones with floral leaf framing."
    ),
    MARBLE_VINTAGE(
        "رخامي عتيق (كشكول كلاسيك)",
        "Vintage Composition",
        "نمط الرخام الكلاسيكي الشهير لدفاتر الملاحظات العريقة.",
        "Iconic black-and-white marbled composition notebook motif."
    ),
    TERRAZZO_MODERN(
        "موزاييك تيرازو فني",
        "Modern Terrazzo Stone",
        "نقوش حجر التيرازو العصرية بألوان حيوية متناسقة.",
        "Playful speckled terrazzo stone pattern with gold foil accents."
    ),
    GEOMETRIC_ART(
        "أشكال هندسية معاصرة",
        "Geometric Abstract",
        "تكوينات فنية متدرجة بأشكال هندسية متوازنة وعصرية.",
        "Dynamic interlocking abstract polygons and vibrant gradients."
    )
}
