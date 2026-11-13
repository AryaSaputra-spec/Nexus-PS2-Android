
package com.nexus.ps2android

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

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
        val subtitle = makeText("GAME FILE CHECKER 0.3", 16f)

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

        val checkButton = Button(this).apply {
            text = "PERIKSA FILE GAME"
            setOnClickListener {
                validateGameFiles()
            }
        }

        val engine = makeText(getEngineStatus(), 13f)

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(binButton)
        layout.addView(binStatus)
        layout.addView(cueButton)
        layout.addView(cueStatus)
        layout.addView(checkButton)
        layout.addView(gameStatus)
        layout.addView(engine)

        setContentView(layout)
    }

    private fun getFileName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "File dipilih"

        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = it.getString(index)
            }
        }

        return name
    }

    private fun checkGameFiles() {
        gameStatus.text = if (binUri != null && cueUri != null) {
            "BIN dan CUE sudah dipilih!\nTekan PERIKSA FILE GAME."
        } else {
            "Silakan pilih kedua file game."
        }
    }

    private fun validateGameFiles() {
        val bin = binUri
        val cue = cueUri

        if (bin == null || cue == null) {
            gameStatus.text = "ERROR: Pilih BIN dan CUE terlebih dahulu."
            return
        }

        try {
            val cueContent = contentResolver.openInputStream(cue)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw Exception("File CUE tidak dapat dibaca")

            val binName = getFileName(bin)
            val cueBinName = Regex(
                """FILE\s+"([^"]+)"""",
                RegexOption.IGNORE_CASE
            ).find(cueContent)?.groupValues?.get(1)

            if (cueBinName == null) {
                gameStatus.text = "ERROR: Format CUE tidak valid."
            } else if (!cueBinName.equals(binName, ignoreCase = true)) {
                gameStatus.text =
                    "FILE TIDAK COCOK!\nCUE: $cueBinName\nBIN: $binName"
            } else {
                val stream = contentResolver.openInputStream(bin)
                    ?: throw Exception("File BIN tidak dapat dibaca")

                stream.use {
                    val firstByte = it.read()
                    if (firstByte == -1) {
                        throw Exception("File BIN kosong")
                    }
                }

                gameStatus.text =
                    "FILE BERHASIL DIPERIKSA!\nBIN dan CUE cocok.\nSiap untuk tahap berikutnya."
            }

        } catch (e: Exception) {
            gameStatus.text = "PEMERIKSAAN GAGAL:\n${e.message}"
        }
    }
}
