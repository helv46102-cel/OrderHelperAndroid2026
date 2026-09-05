package com.orderhelper.app

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.view.*
import android.widget.*

class MainActivity : Activity() {

    private val green = Color.rgb(100, 255, 20)
    private val blue = Color.rgb(35, 145, 255)
    private val orange = Color.rgb(255, 145, 20)
    private val purple = Color.rgb(155, 70, 255)
    private val bg = Color.rgb(3, 8, 14)
    private val card = Color.rgb(8, 18, 28)

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var sniperSwitch: Switch
    private lateinit var sniperTitle: TextView
    private lateinit var minPay: EditText
    private lateinit var minMile: EditText
    private lateinit var maxMiles: EditText
    private lateinit var storeType: Button
    private lateinit var orderType: Button
    private lateinit var soundSwitch: Switch
    private lateinit var vibrationSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("filters", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(20))
        }

        val title = TextView(this).apply {
            text = "🎯  ORDER HELPER"
            textSize = 30f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        content.addView(title)

        val subtitle = TextView(this).apply {
            text = "Detecta • Vibra • Suena • Resalta"
            textSize = 15f
            setTextColor(Color.LTGRAY)
        }
        content.addView(subtitle)

        space(content, 18)

        val sniperCard = cardBox()

        val sniperRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val sniperTexts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        sniperTitle = TextView(this).apply {
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        }

        val sniperSub = TextView(this).apply {
            text = "Buscando órdenes que cumplan tus filtros..."
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        sniperTexts.addView(sniperTitle)
        sniperTexts.addView(sniperSub)

        sniperSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("sniperEnabled", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("sniperEnabled", checked).apply()
                updateSniper()
            }
        }

        sniperRow.addView(
            sniperTexts,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        sniperRow.addView(sniperSwitch)
        sniperCard.addView(sniperRow)
        content.addView(sniperCard)

        space(content, 12)

        val filters = cardBox()

        minPay = numberField(
            filters,
            "💲  PAGO MÍNIMO ($)",
            "Pago mínimo por orden",
            prefs.getFloat("minPay", 20f).toString(),
            green
        )

        minMile = numberField(
            filters,
            "💵  $/MILLA MÍNIMO",
            "Dólares por milla mínimo",
            prefs.getFloat("minPerMile", 2f).toString(),
            blue
        )

        maxMiles = numberField(
            filters,
            "📍  MILLAS MÁXIMAS",
            "Millas máximas por orden",
            prefs.getFloat("maxMiles", 8f).toString(),
            orange
        )

        storeType = selector(
            filters,
            "🏪  TIPO DE TIENDA",
            "¿Qué tiendas quieres permitir?",
            prefs.getString("storeType", "AMBOS") ?: "AMBOS",
            purple
        ) {
            choose(
                "Tipo de tienda",
                arrayOf("WALMART", "SAM'S CLUB", "AMBOS")
            ) { value ->
                storeType.text = value
                prefs.edit().putString("storeType", value).apply()
            }
        }

        orderType = selector(
            filters,
            "📋  TIPO DE ORDEN",
            "¿Qué tipo de órdenes quieres resaltar?",
            prefs.getString("orderType", "AMBOS") ?: "AMBOS",
            Color.rgb(255, 190, 20)
        ) {
            choose(
                "Tipo de orden",
                arrayOf("COMPRAR", "PICKUP", "AMBOS")
            ) { value ->
                orderType.text = value
                prefs.edit().putString("orderType", value).apply()
            }
        }

        selector(
            filters,
            "🏬  TIENDAS PERMITIDAS",
            "Selecciona los Walmart y Sam’s Club que quieres trabajar",
            "CONFIGURAR  ›",
            Color.rgb(255, 190, 20)
        ) {
            showStores()
        }

        content.addView(filters)

        space(content, 12)

        val alerts = cardBox()

        soundSwitch = switchRow(
            alerts,
            "🔊  SONIDO",
            "Reproducir alerta cuando encuentre una orden que cumpla",
            prefs.getBoolean("sound", true)
        )

        vibrationSwitch = switchRow(
            alerts,
            "📳  VIBRACIÓN",
            "Vibrar cuando encuentre una orden que cumpla",
            prefs.getBoolean("vibration", true)
        )

        content.addView(alerts)

        space(content, 14)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val test = actionButton(
            "🧪  PROBAR ALERTA",
            blue
        ) {
            testAlert()
        }

        val save = actionButton(
            "💾  GUARDAR FILTROS",
            green
        ) {
            saveFilters()
        }

        actions.addView(
            test,
            LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                marginEnd = dp(5)
            }
        )

        actions.addView(
            save,
            LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                marginStart = dp(5)
            }
        )

        content.addView(actions)

        space(content, 18)

        val access = Button(this).apply {
            text = "⚙️ PERMISO DE ACCESIBILIDAD"
            textSize = 14f
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(25, 35, 45), 14)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        content.addView(access)

        space(content, 22)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        listOf(
            "🎯\nFiltros",
            "🕘\nHistorial",
            "📊\nEstadísticas",
            "⚙️\nAjustes"
        ).forEachIndexed { i, text ->
            nav.addView(
                TextView(this).apply {
                    this.text = text
                    textSize = 13f
                    gravity = Gravity.CENTER
                    setTextColor(if (i == 0) green else Color.LTGRAY)
                    setPadding(4, 10, 4, 10)
                },
                LinearLayout.LayoutParams(0, dp(64), 1f)
            )
        }

        content.addView(nav)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
        updateSniper()
    }

    private fun updateSniper() {
        if (sniperSwitch.isChecked) {
            sniperTitle.text = "🟢 SNIPER ACTIVO"
            sniperTitle.setTextColor(green)
        } else {
            sniperTitle.text = "⚫ SNIPER APAGADO"
            sniperTitle.setTextColor(Color.LTGRAY)
        }
    }

    private fun saveFilters() {
        prefs.edit()
            .putBoolean("sniperEnabled", sniperSwitch.isChecked)
            .putFloat("minPay", minPay.text.toString().toFloatOrNull() ?: 20f)
            .putFloat("minPerMile", minMile.text.toString().toFloatOrNull() ?: 2f)
            .putFloat("maxMiles", maxMiles.text.toString().toFloatOrNull() ?: 8f)
            .putBoolean("sound", soundSwitch.isChecked)
            .putBoolean("vibration", vibrationSwitch.isChecked)
            .apply()

        Toast.makeText(this, "Filtros guardados ✅", Toast.LENGTH_SHORT).show()
    }

    private fun showStores() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }

        container.addView(sectionTitle("WALMART — SOLO NÚMERO"))

        val walmart = mutableListOf<EditText>()

        for (i in 1..10) {
            val e = EditText(this).apply {
                hint = "Walmart $i  (ej: 4557)"
                setText(prefs.getString("walmart$i", "") ?: "")
                inputType = InputType.TYPE_CLASS_NUMBER
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
            }
            walmart.add(e)
            container.addView(e)
        }

        container.addView(sectionTitle("SAM'S CLUB — SOLO NÚMERO"))

        val sams = mutableListOf<EditText>()

        for (i in 1..3) {
            val e = EditText(this).apply {
                hint = "Sam's Club $i"
                setText(prefs.getString("sams$i", "") ?: "")
                inputType = InputType.TYPE_CLASS_NUMBER
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
            }
            sams.add(e)
            container.addView(e)
        }

        val scroll = ScrollView(this)
        scroll.addView(container)

        AlertDialog.Builder(this)
            .setTitle("Tiendas permitidas")
            .setView(scroll)
            .setPositiveButton("GUARDAR") { _, _ ->
                val edit = prefs.edit()

                walmart.forEachIndexed { index, e ->
                    edit.putString(
                        "walmart${index + 1}",
                        e.text.toString().trim()
                    )
                }

                sams.forEachIndexed { index, e ->
                    edit.putString(
                        "sams${index + 1}",
                        e.text.toString().trim()
                    )
                }

                edit.apply()

                Toast.makeText(
                    this,
                    "Tiendas guardadas ✅",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun numberField(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        value: String,
        color: Int
    ): EditText {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 12, 8, 12)
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        textBox.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })

        textBox.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.LTGRAY)
        })

        val edit = EditText(this).apply {
            setText(value)
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(color)
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
            background = rounded(Color.rgb(4, 12, 20), 12)
        }

        row.addView(
            textBox,
            LinearLayout.LayoutParams(0, dp(62), 1f)
        )

        row.addView(
            edit,
            LinearLayout.LayoutParams(dp(130), dp(56))
        )

        parent.addView(row)
        divider(parent)

        return edit
    }

    private fun selector(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        value: String,
        color: Int,
        action: () -> Unit
    ): Button {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 12, 8, 12)
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        textBox.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })

        textBox.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.LTGRAY)
        })

        val button = Button(this).apply {
            text = value
            textSize = 14f
            setTextColor(color)
            background = rounded(Color.rgb(4, 12, 20), 12)
            setOnClickListener { action() }
        }

        row.addView(
            textBox,
            LinearLayout.LayoutParams(0, dp(70), 1f)
        )

        row.addView(
            button,
            LinearLayout.LayoutParams(dp(160), dp(56))
        )

        parent.addView(row)
        divider(parent)

        return button
    }

    private fun switchRow(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        checked: Boolean
    ): Switch {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 12, 8, 12)
        }

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        })

        texts.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.LTGRAY)
        })

        val sw = Switch(this).apply {
            isChecked = checked
        }

        row.addView(
            texts,
            LinearLayout.LayoutParams(0, dp(68), 1f)
        )
        row.addView(sw)

        parent.addView(row)

        return sw
    }

    private fun choose(
        title: String,
        options: Array<String>,
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which ->
                result(options[which])
            }
            .show()
    }

    private fun testAlert() {
        if (vibrationSwitch.isChecked) {
            try {
                val vibrator =
                    getSystemService(VIBRATOR_SERVICE) as Vibrator

                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        250,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } catch (_: Exception) {}
        }

        if (soundSwitch.isChecked) {
            try {
                ToneGenerator(
                    AudioManager.STREAM_NOTIFICATION,
                    100
                ).startTone(
                    ToneGenerator.TONE_PROP_BEEP2,
                    300
                )
            } catch (_: Exception) {}
        }
    }

    private fun actionButton(
        textValue: String,
        color: Int,
        action: () -> Unit
    ) = Button(this).apply {
        text = textValue
        textSize = 14f
        setTextColor(Color.WHITE)
        background = rounded(
            Color.argb(
                100,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            ),
            14
        )
        setOnClickListener { action() }
    }

    private fun cardBox() =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(card, 18)
        }

    private fun sectionTitle(value: String) =
        TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(green)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 16, 0, 6)
        }

    private fun divider(parent: LinearLayout) {
        parent.addView(View(this).apply {
            setBackgroundColor(Color.rgb(30, 42, 52))
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        ))
    }

    private fun rounded(color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(1, Color.rgb(35, 50, 62))
        }

    private fun space(parent: LinearLayout, value: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(
            1,
            dp(value)
        ))
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
