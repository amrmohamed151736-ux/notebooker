package com.example.data.backup

import android.content.Context
import com.example.model.CoverConfig
import com.example.model.CoverStyle
import com.example.model.Notebook
import com.example.model.PaperSize
import com.example.model.PunchHoleType
import com.example.model.TemplateConfig
import com.example.model.TemplateType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    fun exportNotebooksToJson(notebooks: List<Notebook>): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        root.put("app", "NoteForge")

        val array = JSONArray()
        for (book in notebooks) {
            val bookObj = JSONObject()
            bookObj.put("title", book.title)
            bookObj.put("description", book.description)
            bookObj.put("paperSize", book.paperSize.name)
            bookObj.put("pageCount", book.pageCount)
            bookObj.put("includeCoverInPdf", book.includeCoverInPdf)

            // Cover
            val coverObj = JSONObject()
            coverObj.put("themeStyle", book.coverConfig.themeStyle.name)
            coverObj.put("primaryColorHex", book.coverConfig.primaryColorHex)
            coverObj.put("accentColorHex", book.coverConfig.accentColorHex)
            coverObj.put("title", book.coverConfig.title)
            coverObj.put("subtitle", book.coverConfig.subtitle)
            coverObj.put("author", book.coverConfig.author)
            coverObj.put("editionLabel", book.coverConfig.editionLabel)
            coverObj.put("spineText", book.coverConfig.spineText)
            coverObj.put("cornerTabs", book.coverConfig.cornerTabs)
            coverObj.put("foilEffect", book.coverConfig.foilEffect)
            coverObj.put("coverBadge", book.coverConfig.coverBadge)
            coverObj.put("proceduralPattern", book.coverConfig.proceduralPattern)
            bookObj.put("coverConfig", coverObj)

            // Template
            val tplObj = JSONObject()
            tplObj.put("templateType", book.templateConfig.templateType.name)
            tplObj.put("spacingMm", book.templateConfig.spacingMm.toDouble())
            tplObj.put("marginMm", book.templateConfig.marginMm.toDouble())
            tplObj.put("strokeColorHex", book.templateConfig.strokeColorHex)
            tplObj.put("accentColorHex", book.templateConfig.accentColorHex)
            tplObj.put("strokeWidthPt", book.templateConfig.strokeWidthPt.toDouble())
            tplObj.put("punchHoles", book.templateConfig.punchHoles.name)
            tplObj.put("showHeader", book.templateConfig.showHeader)
            tplObj.put("headerTitle", book.templateConfig.headerTitle)
            tplObj.put("showPageNumber", book.templateConfig.showPageNumber)
            tplObj.put("inkSavingMode", book.templateConfig.inkSavingMode)
            tplObj.put("paperGsm", book.templateConfig.paperGsm)
            tplObj.put("bindingType", book.templateConfig.bindingType)
            bookObj.put("templateConfig", tplObj)

            array.put(bookObj)
        }
        root.put("notebooks", array)

        return root.toString(2)
    }

    fun importNotebooksFromJson(jsonString: String): List<Notebook> {
        val result = mutableListOf<Notebook>()
        try {
            val root = JSONObject(jsonString)
            val array = root.optJSONArray("notebooks") ?: return emptyList()

            for (i in 0 until array.length()) {
                val bookObj = array.getJSONObject(i)
                val title = bookObj.optString("title", "نوت بوك مستورد")
                val desc = bookObj.optString("description", "")
                val paperSizeName = bookObj.optString("paperSize", "A4")
                val paperSize = try { PaperSize.valueOf(paperSizeName) } catch (_: Exception) { PaperSize.A4 }
                val pageCount = bookObj.optInt("pageCount", 20)
                val includeCover = bookObj.optBoolean("includeCoverInPdf", true)

                // Cover
                val coverObj = bookObj.optJSONObject("coverConfig")
                val coverConfig = if (coverObj != null) {
                    val styleName = coverObj.optString("themeStyle", "LEATHER_BOUND")
                    val style = try { CoverStyle.valueOf(styleName) } catch (_: Exception) { CoverStyle.LEATHER_BOUND }
                    CoverConfig(
                        themeStyle = style,
                        primaryColorHex = coverObj.optString("primaryColorHex", "#1E293B"),
                        accentColorHex = coverObj.optString("accentColorHex", "#D97706"),
                        title = coverObj.optString("title", title),
                        subtitle = coverObj.optString("subtitle", "NoteForge Edition"),
                        author = coverObj.optString("author", "الباحث / المالك"),
                        editionLabel = coverObj.optString("editionLabel", "VOL. 2026"),
                        spineText = coverObj.optString("spineText", "NOTELAB • VOL. 1"),
                        cornerTabs = coverObj.optString("cornerTabs", "BRASS_GOLD"),
                        foilEffect = coverObj.optString("foilEffect", "GOLD_FOIL"),
                        coverBadge = coverObj.optString("coverBadge", "LAUREL"),
                        proceduralPattern = coverObj.optString("proceduralPattern", "CELESTIAL")
                    )
                } else CoverConfig()

                // Template
                val tplObj = bookObj.optJSONObject("templateConfig")
                val templateConfig = if (tplObj != null) {
                    val typeName = tplObj.optString("templateType", "COLLEGE_RULED")
                    val type = try { TemplateType.valueOf(typeName) } catch (_: Exception) { TemplateType.COLLEGE_RULED }
                    val holesName = tplObj.optString("punchHoles", "NONE")
                    val holes = try { PunchHoleType.valueOf(holesName) } catch (_: Exception) { PunchHoleType.NONE }

                    TemplateConfig(
                        templateType = type,
                        spacingMm = tplObj.optDouble("spacingMm", 7.1).toFloat(),
                        marginMm = tplObj.optDouble("marginMm", 15.0).toFloat(),
                        strokeColorHex = tplObj.optString("strokeColorHex", "#94A3B8"),
                        accentColorHex = tplObj.optString("accentColorHex", "#F43F5E"),
                        strokeWidthPt = tplObj.optDouble("strokeWidthPt", 0.5).toFloat(),
                        punchHoles = holes,
                        showHeader = tplObj.optBoolean("showHeader", true),
                        headerTitle = tplObj.optString("headerTitle", "Date: _______   Page: ___"),
                        showPageNumber = tplObj.optBoolean("showPageNumber", true),
                        inkSavingMode = tplObj.optBoolean("inkSavingMode", false),
                        paperGsm = tplObj.optInt("paperGsm", 80),
                        bindingType = tplObj.optString("bindingType", "SPIRAL")
                    )
                } else TemplateConfig()

                result.add(
                    Notebook(
                        id = 0, // Auto-assign when inserted
                        title = title,
                        description = desc,
                        paperSize = paperSize,
                        pageCount = pageCount,
                        coverConfig = coverConfig,
                        templateConfig = templateConfig,
                        includeCoverInPdf = includeCover,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun writeBackupToFile(context: Context, jsonString: String): File {
        val dir = File(context.cacheDir, "noteforge_backups")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "noteforge_backup_${System.currentTimeMillis()}.noteforge.json")
        FileOutputStream(file).use { fos ->
            fos.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        return file
    }
}
