package com.orderhelper.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class OfferAssistService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var overlay: View? = null
    private var lastKey = ""
    private var lastTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != "com.walmart.sparkdriver") {
            removeOverlay()
            return
        }

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ scan() }, 180)
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    private fun scan() {
        val root = rootInActiveWindow ?: return

        val prefs = getSharedPreferences("filters", MODE_PRIVATE)
        val minPay = prefs.getFloat("minPay", 25f)
        val minPerMile = prefs.getFloat("minPerMile", 2f)
        val maxMiles = prefs.getFloat("maxMiles", 10f)

        val offers = mutableListOf<Offer>()
        walk(root, offers)

        val best = offers
            .filter {
                it.pay >= minPay &&
                it.miles > 0 &&
                it.miles <= maxMiles &&
                (it.pay / it.miles) >= minPerMile
            }
            .maxByOrNull { it.pay / it.miles }

        if (best == null) {
            removeOverlay()
            return
        }

        showOverlay(best)

        val key = "${best.pay}-${best.miles}"
        val now = System.currentTimeMillis()

        if (key != lastKey || now - lastTime > 20000) {
            lastKey = key
            lastTime = now
            alert()
        }
    }

    private fun walk(
        node: AccessibilityNodeInfo,
        out: MutableList<Offer>
    ) {
        val text = node.text?.toString().orEmpty()

        if (text.contains("$")) {
            candidate(node)?.let { out.add(it) }
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { walk(it, out) }
        }
    }

    private fun candidate(start: AccessibilityNodeInfo): Offer? {
        var n: AccessibilityNodeInfo? = start

        repeat(6) {
            val node = n ?: return@repeat
            val all = collect(node)

            val pay = Regex("""\$\s*(\d+(?:[.,]\d{1,2})?)""")
                .find(all)
                ?.groupValues
                ?.getOrNull(1)
                ?.replace(",", ".")
                ?.toFloatOrNull()

            val miles = Regex(
                """(\d+(?:[.,]\d+)?)\s*(?:mi|millas?)\b""",
                RegexOption.IGNORE_CASE
            )
                .find(all)
                ?.groupValues
                ?.getOrNull(1)
                ?.replace(",", ".")
                ?.toFloatOrNull()

            if (pay != null && miles != null && miles > 0f) {
                val r = Rect()
                node.getBoundsInScreen(r)

                if (r.width() > 200 && r.height() > 70) {
                    return Offer(pay, miles, r)
                }
            }

            n = node.parent
        }

        return null
    }

    private fun collect(
        node: AccessibilityNodeInfo,
        depth: Int = 0
    ): String {
        if (depth > 6) return ""

        val b = StringBuilder()

        node.text?.toString()?.let { b.append(it).append(" ") }
        node.contentDescription?.toString()?.let { b.append(it).append(" ") }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                b.append(collect(it, depth + 1))
            }
        }

        return b.toString()
    }

    private fun showOverlay(o: Offer) {
        removeOverlay()

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val view = TextView(this).apply {
            text = "✓ MEJOR OFERTA  $${"%.2f".format(o.pay)}  ${"%.1f".format(o.miles)} mi"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(dp(6), dp(4), dp(6), 0)

            background = GradientDrawable().apply {
                setColor(Color.argb(35, 80, 255, 40))
                setStroke(dp(4), Color.rgb(80,255,40))
                cornerRadius = dp(12).toFloat()
            }
        }

        val p = WindowManager.LayoutParams(
            o.bounds.width(),
            o.bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = o.bounds.left
            y = o.bounds.top
        }

        try {
            wm.addView(view, p)
            overlay = view
        } catch (_: Exception) {}
    }

    private fun removeOverlay() {
        val v = overlay ?: return

        try {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.removeView(v)
        } catch (_: Exception) {}

        overlay = null
    }

    private fun alert() {
        try {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            v.vibrate(
                VibrationEffect.createOneShot(
                    250,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } catch (_: Exception) {}

        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                .startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
        } catch (_: Exception) {}
    }

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    data class Offer(
        val pay: Float,
        val miles: Float,
        val bounds: Rect
    )
}
