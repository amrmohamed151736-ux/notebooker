package com.example.model

/**
 * Normalized point representing coordinates in document-space (0.0f .. 1.0f).
 * Independent of screen resolution, DPI, orientation, and zoom level.
 */
data class NormalizedPoint(
    val x: Float,
    val y: Float
)

/**
 * Single digital ink stroke stored in document-space coordinates.
 */
data class DrawingStroke(
    val points: List<NormalizedPoint>,
    val colorHex: String = "#0F172A",
    val strokeWidthPt: Float = 2.5f,
    val isHighlighter: Boolean = false,
    val alpha: Float = if (isHighlighter) 0.35f else 1.0f
)

/**
 * Document page model holding rich text and digital ink annotations.
 */
data class NotebookPage(
    val pageNumber: Int,
    val title: String = "",
    val textContent: String = "",
    val strokes: List<DrawingStroke> = emptyList(),
    val undoStack: List<DrawingStroke> = emptyList()
)

enum class EditorToolMode(val labelAr: String) {
    TEXT("كتابة نصوص"),
    PEN("قلم الحبر"),
    HIGHLIGHTER("قلم التمييز"),
    ERASER("ممحاة")
}
