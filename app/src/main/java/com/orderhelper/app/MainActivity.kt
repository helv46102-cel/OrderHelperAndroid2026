package com.orderhelper.app

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Order Helper"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val message = TextView(this).apply {
            text = "Aplicación instalada correctamente"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
        }

        layout.addView(title)
        layout.addView(message)

        setContentView(layout)
    }
}
