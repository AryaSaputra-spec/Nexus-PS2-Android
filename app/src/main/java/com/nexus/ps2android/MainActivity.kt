
package com.nexus.ps2android

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    external fun getEngineStatus(): String

    companion object {
        init {
            System.loadLibrary("nexus_engine")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFF101820.toInt())
        }

        val title = TextView(this).apply {
            text = "NEXUS PS2 ANDROID"
            textSize = 26f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = getEngineStatus()
            textSize = 16f
            setTextColor(0xFF00FF99.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
        }

        layout.addView(title)
        layout.addView(status)

        setContentView(layout)
    }
}
