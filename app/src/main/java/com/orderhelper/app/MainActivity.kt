package com.orderhelper.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Typeface

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("filters", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
            setBackgroundColor(Color.rgb(8, 12, 18))
        }

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        box.addView(txt("🎯 ORDER HELPER", 28f, Color.rgb(120,255,50), true))
        box.addView(txt("Detecta • Vibra • Suena • Señala", 15f, Color.LTGRAY, false))

        status = txt("", 18f, Color.WHITE, true)
        status.setPadding(0, dp(20), 0, dp(10))
        box.addView(status)

        box.addView(label("PAGO MÍNIMO ($)"))
        val minPay = input(prefs.getFloat("minPay", 25f).toString())
        box.addView(minPay)

        box.addView(label("$/MILLA MÍNIMO"))
        val minPerMile = input(prefs.getFloat("minPerMile", 2f).toString())
        box.addView(minPerMile)

        box.addView(label("MILLAS MÁXIMAS"))
        val maxMiles = input(prefs.getFloat("maxMiles", 10f).toString())
        box.addView(maxMiles)

        space(box)

        val save = Button(this).apply {
            text = "💾 GUARDAR FILTROS"
            textSize = 17f
            setOnClickListener {
                prefs.edit()
                    .putFloat("minPay", minPay.text.toString().toFloatOrNull() ?: 25f)
                    .putFloat("minPerMile", minPerMile.text.toString().toFloatOrNull() ?: 2f)
                    .putFloat("maxMiles", maxMiles.text.toString().toFloatOrNull() ?: 10f)
                    .apply()
                Toast.makeText(this@MainActivity, "Filtros guardados ✅", Toast.LENGTH_SHORT).show()
            }
        }
        box.addView(save)

        space(box)

        val activate = Button(this).apply {
            text = "⚙️ ACTIVAR ORDER HELPER"
            textSize = 17f
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        box.addView(activate)

        space(box)

        val test = Button(this).apply {
            text = "📳🔊 PROBAR ALERTA"
            textSize = 16f
            setOnClickListener { testAlert() }
        }
        box.addView(test)

        box.addView(
            txt(
                "\nUSO\n\n" +
                "1. Guarda tus filtros.\n" +
                "2. Activa Order Helper en Accesibilidad.\n" +
                "3. Abre Spark Driver.\n" +
                "4. Cuando una oferta visible cumpla tus filtros, sonará, vibrará y aparecerá marcada en verde.\n" +
                "5. Tú tocas la oferta manualmente.",
                15f,
                Color.LTGRAY,
                false
            )
        )

        scroll.addView(box)
        root.addView(scroll)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()

        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""

        if (enabled.contains(packageName, true)) {
            status.text = "🟢 SNIPER ACTIVO"
            status.setTextColor(Color.rgb(120,255,50))
        } else {
            status.text = "⚪ SNIPER APAGADO"
            status.setTextColor(Color.LTGRAY)
        }
    }

    private fun testAlert() {
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

    private fun label(s: String) =
        txt(s, 13f, Color.rgb(120,255,50), true).apply {
            setPadding(0, dp(16), 0, dp(5))
        }

    private fun input(value: String) =
        EditText(this).apply {
            setText(value)
            textSize = 18f
            setTextColor(Color.WHITE)
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.rgb(28,34,44))
        }

    private fun txt(
        s: String,
        size: Float,
        color: Int,
        bold: Boolean
    ) = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun space(parent: LinearLayout) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(12))
        })
    }

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()
}
