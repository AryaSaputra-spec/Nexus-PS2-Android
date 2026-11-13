
package com.nexus.ps2android

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var binUri: Uri? = null
    private var cueUri: Uri? = null

    private lateinit var binStatus: TextView
    private lateinit var cueStatus: TextView
    private lateinit var gameStatus: TextView

    private val binPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                binUri = it
                binStatus.text = "BIN: ${getFileName(it)}"
                checkGameFiles()
            }
        }

    private val cuePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                cueUri = it
                cueStatus.text = "CUE: ${getFileName(it)}"
                checkGameFiles()
            }
        }

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
            setPadding(30, 30, 30, 30)
            setBackgroundColor(0xFF101820.toInt())
        }

        fun makeText(value: String, size: Float): TextView {
            return TextView(this).apply {
                text = value
                textSize = size
                gravity = Gravity.CENTER
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(8, 15, 8, 15)
            }
        }

        val title = makeText("NEXUS PS2 ANDROID", 26f)

        val subtitle = makeText("GAME FILE SELECTOR 0.2", 16f)

        binStatus = makeText("BIN: Belum dipilih", 14f)
        cueStatus = makeText("CUE: Belum dipilih", 14f)
        gameStatus = makeText("Belum ada game dipilih", 16f)

        val binButton = Button(this).apply {
            text = "PILIH GAME BIN"
            setOnClickListener {
                binPicker.launch(arrayOf("*/*"))
            }
        }

        val cueButton = Button(this).apply {
            text = "PILIH FILE CUE"
            setOnClickListener {
                cuePicker.launch(arrayOf("*/*"))
            }
        }

        val engine = makeText(getEngineStatus(), 13f)

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(binButton)
        layout.addView(binStatus)
        layout.addView(cueButton)
        layout.addView(cueStatus)
        layout.addView(gameStatus)
        layout.addView(engine)

        setContentView(layout)
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "File dipilih"
    }

    private fun checkGameFiles() {
        gameStatus.text = if (binUri != null && cueUri != null) {
            "BIN dan CUE sudah dipilih!\nSiap untuk tahap pemeriksaan game."
        } else {
            "Silakan pilih kedua file game."
        }
    }
}
