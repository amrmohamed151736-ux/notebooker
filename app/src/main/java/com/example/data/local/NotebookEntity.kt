package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateConfig
import com.example.model.TemplateType

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val paperSize: String,
    val pageCount: Int,
    // Template config fields
    val templateType: String,
    val spacingMm: Float,
    val marginMm: Float,
    val strokeColorHex: String,
    val accentColorHex: String,
    val strokeWidthPt: Float,
    val punchHoles: String,
    val showHeader: Boolean,
    val headerTitle: String,
    val showPageNumber: Boolean,
    val cornellCueWidthRatio: Float,
    val cornellSummaryHeightRatio: Float,
    val isDualPageSpread: Boolean = false,
    val showCropMarks: Boolean = false,
    // Cover config fields
    val coverThemeStyle: String,
    val coverPrimaryColorHex: String,
    val coverAccentColorHex: String,
    val coverTitle: String,
    val coverSubtitle: String,
    val coverAuthor: String,
    val coverEditionLabel: String,
    val coverSpineText: String,
    val coverAiIllustration: String?,
    val cornerTabs: String = "BRASS_GOLD",
    val foilEffect: String = "GOLD_FOIL",
    val coverBadge: String = "LAUREL",
    val ribbonColorHex: String = "#D97706",
    val proceduralPattern: String = "CELESTIAL",
    val includeCoverInPdf: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Notebook {
        val resolvedPaperSize = try {
            PaperSize.valueOf(paperSize)
        } catch (_: Exception) {
            PaperSize.A4
        }
        val resolvedTemplateType = try {
            TemplateType.valueOf(templateType)
        } catch (_: Exception) {
            TemplateType.COLLEGE_RULED
        }
        val resolvedPunchHoles = try {
            PunchHoleType.valueOf(punchHoles)
        } catch (_: Exception) {
            PunchHoleType.NONE
        }
        val resolvedCoverStyle = try {
            CoverStyle.valueOf(coverThemeStyle)
        } catch (_: Exception) {
            CoverStyle.LEATHER_BOUND
        }

        return Notebook(
            id = id,
            title = title,
            description = description,
            paperSize = resolvedPaperSize,
            pageCount = pageCount,
            coverConfig = CoverConfig(
                themeStyle = resolvedCoverStyle,
                primaryColorHex = coverPrimaryColorHex,
                accentColorHex = coverAccentColorHex,
                title = coverTitle,
                subtitle = coverSubtitle,
                author = coverAuthor,
                editionLabel = coverEditionLabel,
                spineText = coverSpineText,
                aiIllustrationBase64 = coverAiIllustration,
                cornerTabs = cornerTabs,
                foilEffect = foilEffect,
                coverBadge = coverBadge,
                ribbonColorHex = ribbonColorHex,
                proceduralPattern = proceduralPattern
            ),
            templateConfig = TemplateConfig(
                templateType = resolvedTemplateType,
                spacingMm = spacingMm,
                marginMm = marginMm,
                strokeColorHex = strokeColorHex,
                accentColorHex = accentColorHex,
                strokeWidthPt = strokeWidthPt,
                punchHoles = resolvedPunchHoles,
                showHeader = showHeader,
                headerTitle = headerTitle,
                showPageNumber = showPageNumber,
                cornellCueWidthRatio = cornellCueWidthRatio,
                cornellSummaryHeightRatio = cornellSummaryHeightRatio,
                isDualPageSpread = isDualPageSpread,
                showCropMarks = showCropMarks
            ),
            includeCoverInPdf = includeCoverInPdf,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(notebook: Notebook): NotebookEntity {
            return NotebookEntity(
                id = notebook.id,
                title = notebook.title,
                description = notebook.description,
                paperSize = notebook.paperSize.name,
                pageCount = notebook.pageCount,
                templateType = notebook.templateConfig.templateType.name,
                spacingMm = notebook.templateConfig.spacingMm,
                marginMm = notebook.templateConfig.marginMm,
                strokeColorHex = notebook.templateConfig.strokeColorHex,
                accentColorHex = notebook.templateConfig.accentColorHex,
                strokeWidthPt = notebook.templateConfig.strokeWidthPt,
                punchHoles = notebook.templateConfig.punchHoles.name,
                showHeader = notebook.templateConfig.showHeader,
                headerTitle = notebook.templateConfig.headerTitle,
                showPageNumber = notebook.templateConfig.showPageNumber,
                cornellCueWidthRatio = notebook.templateConfig.cornellCueWidthRatio,
                cornellSummaryHeightRatio = notebook.templateConfig.cornellSummaryHeightRatio,
                isDualPageSpread = notebook.templateConfig.isDualPageSpread,
                showCropMarks = notebook.templateConfig.showCropMarks,
                coverThemeStyle = notebook.coverConfig.themeStyle.name,
                coverPrimaryColorHex = notebook.coverConfig.primaryColorHex,
                coverAccentColorHex = notebook.coverConfig.accentColorHex,
                coverTitle = notebook.coverConfig.title,
                coverSubtitle = notebook.coverConfig.subtitle,
                coverAuthor = notebook.coverConfig.author,
                coverEditionLabel = notebook.coverConfig.editionLabel,
                coverSpineText = notebook.coverConfig.spineText,
                coverAiIllustration = notebook.coverConfig.aiIllustrationBase64,
                cornerTabs = notebook.coverConfig.cornerTabs,
                foilEffect = notebook.coverConfig.foilEffect,
                coverBadge = notebook.coverConfig.coverBadge,
                ribbonColorHex = notebook.coverConfig.ribbonColorHex,
                proceduralPattern = notebook.coverConfig.proceduralPattern,
                includeCoverInPdf = notebook.includeCoverInPdf,
                createdAt = notebook.createdAt,
                updatedAt = notebook.updatedAt
            )
        }
    }
}
