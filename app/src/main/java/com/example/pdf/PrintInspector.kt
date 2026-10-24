package com.example.pdf

import com.example.model.Notebook
import com.example.model.PaperSize
import kotlin.math.roundToInt

data class PrintAuditReport(
    val spineThicknessMm: Float,
    val estimatedWeightGrams: Float,
    val recommendedBinding: String,
    val bindingDescriptionAr: String,
    val warnings: List<String>,
    val tips: List<String>,
    val passesSafeMargins: Boolean,
    val inkSavingsPercent: Int
)

object PrintInspector {

    /**
     * Caliper thickness per sheet (2 pages) in millimeters according to GSM
     */
    fun getSheetCaliperMm(paperGsm: Int): Float = when {
        paperGsm <= 70 -> 0.088f
        paperGsm <= 80 -> 0.102f
        paperGsm <= 100 -> 0.128f
        else -> 0.160f // 120+ GSM
    }

    /**
     * Conducts a comprehensive pre-flight printing and binding audit.
     */
    fun auditNotebook(notebook: Notebook): PrintAuditReport {
        val pageCount = notebook.pageCount
        val sheets = maxOf(1, (pageCount + 1) / 2)
        val gsm = notebook.templateConfig.paperGsm
        val sheetThickness = getSheetCaliperMm(gsm)

        // Spine thickness: sheets * thickness + cardstock covers allowance (0.7mm)
        val spineThicknessMm = ((sheets * sheetThickness + 0.70f) * 10f).roundToInt() / 10f

        // Paper weight calculation (A4 = 1/16 m2 = 0.0625 m2 per sheet)
        val sheetAreaM2 = when (notebook.paperSize) {
            PaperSize.A4 -> 0.06237f
            PaperSize.A5 -> 0.03108f
            PaperSize.B5 -> 0.04395f
            PaperSize.LETTER -> 0.06029f
        }
        val paperWeightGrams = sheets * sheetAreaM2 * gsm
        val coverWeightGrams = 2 * sheetAreaM2 * 250f // 250 GSM cover
        val totalWeight = ((paperWeightGrams + coverWeightGrams) * 10f).roundToInt() / 10f

        val warnings = mutableListOf<String>()
        val tips = mutableListOf<String>()

        val bindingType = notebook.templateConfig.bindingType
        val marginMm = notebook.templateConfig.marginMm

        // Margin Safety
        val passesMargins = marginMm >= 8.0f
        if (marginMm < 10.0f) {
            warnings.add("الهامش الحالي (${marginMm} مم) ضيق جداً لمرحلة قص الأطراف والطباعة المنزلية. يوصى بـ 12-15 مم على الأقل.")
        }

        // Binding Checks
        val recommendedBinding: String
        val bindingDescriptionAr: String

        when {
            pageCount <= 40 -> {
                recommendedBinding = "SADDLE_STITCH"
                bindingDescriptionAr = "تدبيس سرجي في المنتصف (Saddle Stitch) — اقتصادي وسهل التصفح التام 180 درجة."
                if (bindingType == "PERFECT_BOUND") {
                    warnings.add("عدد الصفحات ($pageCount) قليل جداً للتجليد الحراري بالكعب المربع؛ يفضل أن يكون 60 صفحة فأكثر لثبات الغراء.")
                }
            }
            pageCount in 41..160 -> {
                recommendedBinding = "SPIRAL"
                bindingDescriptionAr = "سلك حلزوني مزدوج (Wire-O / Spiral) — متين وعملي للدراسة والمكاتب والفتح الكامل 360 درجة."
                if (bindingType == "SADDLE_STITCH" && pageCount > 60) {
                    warnings.add("التدبيس في المنتصف لـ $pageCount صفحة قد يؤدي لانتفاخ الكعب وعدم انغلاق الدفتر بسلاسة.")
                }
                if (marginMm < 14.0f) {
                    warnings.add("عند التجليد بالسلك الحلزوني، يوصى بهامش داخلي 14-16 مم لتجنب ثقب خطوط الكتابة.")
                }
            }
            else -> {
                recommendedBinding = "PERFECT_BOUND"
                bindingDescriptionAr = "تجليد حراري بكعب مربع مقوى (Perfect Bound) — مظهر كتاب فخم واحترافي مناسب للمجلدات الكبيرة."
                if (bindingType == "SPIRAL" && pageCount > 220) {
                    tips.add("لسماكة $spineThicknessMm مم، ستحتاج حلقة سلك حلزوني بقطر كبير (16-20 مم).")
                }
            }
        }

        // Eco-ink Savings
        val inkSavings = if (notebook.templateConfig.inkSavingMode) 55 else 0
        if (notebook.templateConfig.inkSavingMode) {
            tips.add("وضع توفير الحبر مفعل: تم تقليل كثافة خطوط الشبكة بنسبة 55% لتوفير الحبر وحماية رأس الطباعة.")
        } else {
            tips.add("يمكنك تفعيل 'وضع توفير الحبر' لتقليل استهلاك الحبر بنسبة 55% عند الطباعة التجريبية.")
        }

        // Paper GSM recommendation
        if (gsm == 70) {
            tips.add("ورق 70 جم مناسب للكتابة بقلم الرصاص أو الجاف الخفيف؛ قد تظهر أقلام الحبر السائل من الخلف (Bleed-through).")
        } else if (gsm >= 100) {
            tips.add("ورق $gsm جم ممتاز لأقلام الحبر السائل وألوان الماركر دون شفافية للظهر.")
        }

        return PrintAuditReport(
            spineThicknessMm = spineThicknessMm,
            estimatedWeightGrams = totalWeight,
            recommendedBinding = recommendedBinding,
            bindingDescriptionAr = bindingDescriptionAr,
            warnings = warnings,
            tips = tips,
            passesSafeMargins = passesMargins,
            inkSavingsPercent = inkSavings
        )
    }
}
