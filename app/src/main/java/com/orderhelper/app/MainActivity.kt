package com.orderhelper.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(8, 12, 18))
        }

        val title = TextView(this).apply {
            text = "ORDER HELPER V2"
            textSize = 30f
            setTextColor(Color.rgb(120, 255, 50))
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = "\nPRUEBA CORRECTA ✅"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(status)
        setContentView(root)
    }
}
