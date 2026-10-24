package com.example.model

enum class TemplateCategory(val displayNameAr: String, val displayNameEn: String) {
    ALL("الكل", "All"),
    RULED("مسطر", "Ruled"),
    GRID_DOT("شبكي ومنقط", "Grid & Dot"),
    CORNELL("كورنيل ودراسي", "Cornell & Study"),
    PLANNER("مخططات وتنظيم", "Planners"),
    TODO_LISTS("قوائم المهام (To-Do)", "To-Do & Tasks"),
    SCIENCE("علوم ومختبرات", "Science & Lab"),
    CREATIVE_ARTS("فن ورسم", "Arts & Sketch"),
    FINANCE_BUSINESS("مالية وأعمال", "Finance & Work"),
    HEALTH_WELLNESS("صحة وعافية", "Health & Wellness")
}

enum class TemplateType(
    val titleAr: String,
    val titleEn: String,
    val category: TemplateCategory,
    val defaultSpacingMm: Float,
    val descriptionAr: String,
    val descriptionEn: String
) {
    // Ruled Collection
    COLLEGE_RULED(
        "مسطر جامعي (7.1 مم)",
        "College Ruled (7.1mm)",
        TemplateCategory.RULED,
        7.1f,
        "تسطير قياسي للجامعات والمدارس مع خط هامش رأسي وردي.",
        "Standard academic ruling with vertical red margin guide."
    ),
    WIDE_RULED(
        "مسطر عريض (8.7 مم)",
        "Wide Ruled (8.7mm)",
        TemplateCategory.RULED,
        8.7f,
        "تسطير عريض ومريح لكتابة مريحة وخطوط اليد الكبيرة.",
        "Comfortable wide lines for handwriting and journaling."
    ),
    NARROW_RULED(
        "مسطر ضيق (6.35 مم)",
        "Narrow Ruled (6.35mm)",
        TemplateCategory.RULED,
        6.35f,
        "تسطير مدمج ومكثف لتدوين أكبر قدر من المعلومات.",
        "Compact line spacing for maximum content per page."
    ),
    FRENCH_RULED(
        "تسطير فرنسي (Séyès)",
        "French Ruled (Séyès)",
        TemplateCategory.RULED,
        8.0f,
        "شبكة خطوط متداخلة لتعلم وتحسين جودة الخط والكتابة.",
        "Classical French grid for handwriting mastery and calligraphy."
    ),
    MUSIC_STAFF(
        "نوتة موسيقية (5 أسطر)",
        "Music Manuscript Staff",
        TemplateCategory.RULED,
        8.0f,
        "مدرجات نوتة موسيقية قياسية للملحنين والموسيقيين.",
        "Standard 5-line musical staves for composition and practice."
    ),

    // Grid & Dot Collection
    DOT_GRID(
        "شبكة نقاط (5 مم)",
        "Dot Grid (5mm)",
        TemplateCategory.GRID_DOT,
        5.0f,
        "مثالي لبوليت جورنال، الرسم الهندسي، والتخطيط المرن.",
        "Ideal for bullet journaling, sketching, and flexible layouts."
    ),
    SQUARE_GRID(
        "مربعات قياسية (5 مم)",
        "Graph / Grid (5mm)",
        TemplateCategory.GRID_DOT,
        5.0f,
        "شبكة مربعات كلاسيكية للرياضيات، الحسابات والرسومات.",
        "Classic 5mm grid for mathematics, charts, and drafting."
    ),
    ENGINEERING_GRID(
        "شبكة هندسية دقيقة",
        "Engineering Grid",
        TemplateCategory.GRID_DOT,
        10.0f,
        "شبكة رئيسية 10 مم مع خطوط فرعية 2 مم للحسابات الهندسية.",
        "Major 10mm grid with fine 2mm subdivisions for engineers."
    ),
    ISOMETRIC(
        "شبكة مجسمة (Isometric)",
        "Isometric Grid (3D)",
        TemplateCategory.GRID_DOT,
        6.0f,
        "شبكة مثلثية مائلة 60 درجة لرسم المجسمات والمباني 3D.",
        "Triangular 60-degree grid for 3D sketching, game design, and CAD."
    ),

    // Cornell & Academic Study
    CORNELL_STANDARD(
        "ملاحظات كورنيل الكلاسيكية",
        "Cornell Notes Standard",
        TemplateCategory.CORNELL,
        7.1f,
        "نظام كورنيل الفعال: عمود إشارات، مساحة شرح، ومربع ملخص سفلي.",
        "Proven study layout: Cue column, notes body, and bottom summary."
    ),
    CORNELL_INVERTED(
        "كورنيل معكوس (لليمين)",
        "Cornell Notes Right-Cue",
        TemplateCategory.CORNELL,
        7.1f,
        "عمود الملاحظات على اليمين مخصص للكتابة باللغة العربية.",
        "Right-aligned cue column tailored for Arabic note-taking."
    ),
    VOCABULARY_INDEX(
        "سجل المفردات واللغات (Vocabulary)",
        "Vocabulary Study Index",
        TemplateCategory.CORNELL,
        7.0f,
        "جدول ثلاثي: الكلمة / المصطلح، النطق والمعنى، وجملة توضيحية.",
        "Tri-column sheet: Word, Pronunciation/Meaning, and Example."
    ),

    // Science & Lab Collection
    HEXAGONAL(
        "شبكة سداسية (كيمياء)",
        "Hexagonal Grid (Organic Chemistry)",
        TemplateCategory.SCIENCE,
        6.0f,
        "شبكة سداسية لرسم المركبات الكيميائية العضوية والجزيئات.",
        "Hexagonal tiling for drawing organic molecules, rings, and maps."
    ),
    SCIENCE_LAB_RECORD(
        "سجل تجارب علمية ومخبرية",
        "Science & Bioscience Lab Log",
        TemplateCategory.SCIENCE,
        7.0f,
        "نموذج علمي متكامل: اسم التجربة، الفرضية، جدول النتائج والمناقشة.",
        "Complete lab template: Objective, hypothesis, data table, and findings."
    ),

    // Creative Arts Collection (NEW)
    COMIC_STORYBOARD(
        "لوحة قصص مصورة (Comic Panels)",
        "Comic & Manga Storyboard",
        TemplateCategory.CREATIVE_ARTS,
        8.0f,
        "6 إطارات متوازنة للقصص المصورة مع هوامش للحوار والمؤثرات.",
        "6-panel layout with gutters for manga, comics, and video scenes."
    ),
    CALLIGRAPHY_SLANT(
        "تسطير خط وتدريب (Calligraphy 55°)",
        "Calligraphy & Lettering Guide",
        TemplateCategory.CREATIVE_ARTS,
        6.0f,
        "خطوط مائلة بزاوية 55 درجة مع مسارات الارتفاع والهبوط لكتابة رشيقة.",
        "55-degree slant lines with x-height and ascender/descender limits."
    ),
    PERSPECTIVE_GRID(
        "شبكة منظور معماري (2-Point Perspective)",
        "2-Point Perspective Grid",
        TemplateCategory.CREATIVE_ARTS,
        8.0f,
        "خطوط تلاشي لمنظور نقطتين لتصميم المباني والسيارات والديكور.",
        "Radiating two-point perspective guidelines for architectural sketching."
    ),
    GUITAR_CHORDS(
        "مخطط كوردات ونوتات جيتار",
        "Guitar Chords & Tablature",
        TemplateCategory.CREATIVE_ARTS,
        7.5f,
        "مربعات أوتار الجيتار مع أسطر التاب للموسيقيين والملحنين.",
        "Chord box diagrams with blank tablature staves for guitarists."
    ),

    // Business & Finance Collection (NEW)
    BUDGET_LEDGER(
        "دفتر الحسابات والميزانية (Ledger)",
        "Monthly Financial Ledger",
        TemplateCategory.FINANCE_BUSINESS,
        6.5f,
        "جدول محاسبي: التاريخ، البيان، التصنيف، الدخل، المصروف، والرصيد.",
        "Accounting register: Date, Description, Category, In, Out, Balance."
    ),
    MEETING_MINUTES(
        "محضر اجتماعات وقرارات العمل",
        "Executive Meeting Minutes",
        TemplateCategory.FINANCE_BUSINESS,
        6.8f,
        "توثيق الحاضرين، جدول الأعمال، ملخص النقاشات، والمهام الموكلة.",
        "Attendees, Agenda, Discussion key points, and Action items checklist."
    ),

    // Health & Wellness Collection (NEW)
    MEAL_PLANNER(
        "مخطط الوجبات وقائمة التسوق",
        "Weekly Meal Prep & Grocery",
        TemplateCategory.HEALTH_WELLNESS,
        6.0f,
        "تخطيط فطور وغداء وعشاء الأسبوع مع قائمة تسوق متكاملة.",
        "Monday-Sunday meal chart with categorized grocery checklist."
    ),
    WORKOUT_LOG(
        "سجل التمارين واللياقة البدنية",
        "Fitness & Workout Tracker",
        TemplateCategory.HEALTH_WELLNESS,
        6.2f,
        "تسجيل التمارين، المجموعات، التكرارات، الأوزان، ومعدل الكارديو.",
        "Strength & cardio log: Exercise, Sets, Reps, Weight, and Water intake."
    ),

    // Productivity & Planners Collection
    PLANNER_DAILY(
        "مخطط يومي بالساعات",
        "Daily Schedule & Priorities",
        TemplateCategory.PLANNER,
        6.5f,
        "جدول زمني من 06:00 إلى 22:00، أولويات اليوم، وملاحظات.",
        "Hourly agenda (06:00-22:00), top priorities, and notes section."
    ),
    PLANNER_WEEKLY(
        "مخطط أسبوعي للأهداف",
        "Weekly Horizon Planner",
        TemplateCategory.PLANNER,
        6.5f,
        "توزيع لجميع أيام الأسبوع مع قائمة مهام أسبوعية وأهداف رئيسية.",
        "7-day overview with weekly task checklist and goal tracker."
    ),
    HABIT_TRACKER(
        "مصفوفة تتبع العادات (31 يوم)",
        "Monthly Habit Tracker Matrix",
        TemplateCategory.PLANNER,
        5.5f,
        "جدول شهر كامل لتتبع 8 عادات يومية مع مؤشر إنجاز بصري.",
        "Full month grid to track up to 8 habits with daily checkboxes."
    ),

    // To-Do & Task Management Collection (NEW)
    TODO_DAILY_CHECKLIST(
        "قائمة مهام يومية بالساعات والأولويات",
        "Daily Hourly Tasks & Checklist",
        TemplateCategory.TODO_LISTS,
        6.5f,
        "جدول يومي متكامل: أهم 3 أولويات، جدول زمني بالساعات، ومربعات مهام للإنجاز.",
        "Full day layout: Top 3 priorities, hourly time blocks, task checkboxes, and notes."
    ),
    TODO_WEEKLY_PRIORITY(
        "مصفوفة المهام الأسبوعية (Weekly Horizon)",
        "Weekly Priority Task Spread",
        TemplateCategory.TODO_LISTS,
        6.0f,
        "تخطيط أسبوعي متكامل: مربعات مهام لكل يوم، أهداف الأسبوع، وقائمة متابعة الإنجاز.",
        "Weekly 7-day checklist spread with high-priority goals and milestone tracker."
    ),
    TODO_EISENHOWER_MATRIX(
        "مصفوفة أيزنهاور لتنظيم الأولويات",
        "Eisenhower 4-Quadrant Priority Matrix",
        TemplateCategory.TODO_LISTS,
        6.5f,
        "مصفوفة رباعية: عاجل ومهم (افعل الآن)، مهم وغير عاجل (خطط له)، عاجل وغير مهم (فوضه)، وغير عاجل وغير مهم (احذفه).",
        "4 Quadrants: Urgent & Important, Important/Not Urgent, Urgent/Not Important, and Eliminate."
    ),
    TODO_PROJECT_MILESTONES(
        "خريطة معالم ومراحل المشاريع",
        "Project Milestones & Deliverables",
        TemplateCategory.TODO_LISTS,
        7.0f,
        "تتبع مشاريع متكامل: المراحل الرئيسية، المواعيد النهائية، وقوائم التسليمات مع شريط الإنجاز.",
        "Project roadmap tracker with milestones, target dates, deliverable checklists, and status."
    );

    val isNew: Boolean
        get() = this in setOf(
            TODO_DAILY_CHECKLIST,
            TODO_WEEKLY_PRIORITY,
            TODO_EISENHOWER_MATRIX,
            TODO_PROJECT_MILESTONES,
            SCIENCE_LAB_RECORD,
            COMIC_STORYBOARD,
            CALLIGRAPHY_SLANT,
            PERSPECTIVE_GRID,
            GUITAR_CHORDS,
            BUDGET_LEDGER,
            MEETING_MINUTES,
            MEAL_PLANNER,
            WORKOUT_LOG,
            VOCABULARY_INDEX
        )
}
