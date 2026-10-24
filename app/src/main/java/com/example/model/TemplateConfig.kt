package com.example.model

data class TemplateConfig(
    val templateType: TemplateType = TemplateType.COLLEGE_RULED,
    val spacingMm: Float = 7.1f,
    val marginMm: Float = 15f,
    val strokeColorHex: String = "#94A3B8",
    val accentColorHex: String = "#F43F5E",
    val strokeWidthPt: Float = 0.5f,
    val punchHoles: PunchHoleType = PunchHoleType.NONE,
    val showHeader: Boolean = true,
    val headerTitle: String = "Date: _______   Page: ___",
    val showPageNumber: Boolean = true,
    val cornellCueWidthRatio: Float = 0.28f,
    val cornellSummaryHeightRatio: Float = 0.18f,
    val showGridMinorLines: Boolean = true,
    val customNotesTitle: String = "NOTEBOOK SHEET",
    val isDualPageSpread: Boolean = false,
    val showCropMarks: Boolean = false,
    val inkSavingMode: Boolean = false,
    val paperGsm: Int = 80,
    val bindingType: String = "SPIRAL"
)

data class CoverConfig(
    val themeStyle: CoverStyle = CoverStyle.LEATHER_BOUND,
    val primaryColorHex: String = "#1E293B",
    val accentColorHex: String = "#D97706",
    val title: String = "دفتر الملاحظات والأفكار",
    val subtitle: String = "NoteForge Studio Edition",
    val author: String = "الباحث / المالك",
    val editionLabel: String = "VOL. 2026",
    val spineText: String = "NOTELAB • VOL. 1",
    val aiIllustrationBase64: String? = null,
    val cornerTabs: String = "BRASS_GOLD", // NONE, BRASS_GOLD, SILVER, ROSE_GOLD
    val foilEffect: String = "GOLD_FOIL", // NONE, GOLD_FOIL, SILVER_FOIL, ROSE_FOIL
    val coverBadge: String = "LAUREL", // NONE, LAUREL, ORBIT, ATOM, CREST, STAR
    val ribbonColorHex: String = "#D97706",
    val proceduralPattern: String = "CELESTIAL" // NONE, CELESTIAL, SACRED_GEOMETRY, BOTANICAL, ART_DECO, BLUEPRINT, TOPOGRAPHY, LEATHER_GRAIN, MARBLED
)
