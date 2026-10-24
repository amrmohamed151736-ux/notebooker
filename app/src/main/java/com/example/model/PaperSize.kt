package com.example.model

enum class PaperSize(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float,
    val widthPt: Int,
    val heightPt: Int
) {
    A4("A4 (210 × 297 mm)", 210f, 297f, 595, 842),
    A5("A5 (148 × 210 mm)", 148f, 210f, 420, 595),
    LETTER("US Letter (8.5 × 11 in)", 215.9f, 279.4f, 612, 792),
    B5("B5 (176 × 250 mm)", 176f, 250f, 499, 709)
}

enum class PunchHoleType(val displayName: String, val count: Int) {
    NONE("None (Plain Edge)", 0),
    TWO_HOLE("2-Hole (Standard)", 2),
    THREE_HOLE("3-Hole (US Ring Binder)", 3),
    FOUR_HOLE("4-Hole (ISO 838)", 4)
}
