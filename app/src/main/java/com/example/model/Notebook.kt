package com.example.model

data class Notebook(
    val id: Long = 0,
    val title: String,
    val description: String,
    val paperSize: PaperSize = PaperSize.A4,
    val pageCount: Int = 20,
    val coverConfig: CoverConfig = CoverConfig(),
    val templateConfig: TemplateConfig = TemplateConfig(),
    val includeCoverInPdf: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
