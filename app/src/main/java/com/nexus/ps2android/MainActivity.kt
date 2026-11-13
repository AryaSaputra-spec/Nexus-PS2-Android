
package com.nexus.ps2android

import android.os.Bundle
import android.view.Gravity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private var binUri: Uri? = null
    private var cueUri: Uri? = null

    private lateinit var binStatus: TextView
    private lateinit var cueStatus: TextView
    private lateinit var gameStatus: TextView

    private val prefs by lazy {
        getSharedPreferences("nexus_game_files", MODE_PRIVATE)
    }

    private val binPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                binUri = it
                keepFileAccess(it)
                saveGameFiles()
                binStatus.text = "BIN: ${getFileName(it)}"
                checkGameFiles()
            }
        }

    private val cuePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                cueUri = it
                keepFileAccess(it)
                saveGameFiles()
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
        val subtitle = makeText("GAME FILE MANAGER 0.4", 16f)

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

        restoreGameFiles()

        setContentView(layout)
    }

    private fun keepFileAccess(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            gameStatus.text =
                "Peringatan: Akses file tidak dapat disimpan permanen."
        }
    }

    private fun saveGameFiles() {
        prefs.edit()
            .putString("bin_uri", binUri?.toString())
            .putString("cue_uri", cueUri?.toString())
            .apply()
    }

    private fun restoreGameFiles() {
        val savedBin = prefs.getString("bin_uri", null)
        val savedCue = prefs.getString("cue_uri", null)

        binUri = savedBin?.let { Uri.parse(it) }
        cueUri = savedCue?.let { Uri.parse(it) }

        binStatus.text = binUri?.let {
            "BIN: ${getFileName(it)}"
        } ?: "BIN: Belum dipilih"

        cueStatus.text = cueUri?.let {
            "CUE: ${getFileName(it)}"
        } ?: "CUE: Belum dipilih"

        checkGameFiles()
    }

    private fun getFileName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "File dipilih"

        contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index =
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                if (index >= 0) {
                    name = cursor.getString(index) ?: name
                }
            }
        }

        return name
    }

    private fun checkGameFiles() {
        gameStatus.text =
            if (binUri != null && cueUri != null) {
                "BIN dan CUE tersimpan!\nTekan PERIKSA FILE GAME."
            } else {
                "Silakan pilih kedua file game."
            }
    }

    private fun validateGameFiles() {
        val bin = binUri
        val cue = cueUri

        if (bin == null || cue == null) {
            gameStatus.text =
                "ERROR: Pilih BIN dan CUE terlebih dahulu."
            return
        }

        try {
            val cueContent =
                contentResolver.openInputStream(cue)
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
                return
            }

            if (!cueBinName.equals(binName, ignoreCase = true)) {
                gameStatus.text =
                    "FILE TIDAK COCOK!\nCUE: $cueBinName\nBIN: $binName"
                return
            }

            contentResolver.openInputStream(bin)?.use {
                if (it.read() == -1) {
                    throw Exception("File BIN kosong")
                }
            } ?: throw Exception("File BIN tidak dapat dibaca")

            gameStatus.text =
                "FILE BERHASIL DIPERIKSA!\nBIN dan CUE cocok.\nAkses file berhasil."

        } catch (e: Exception) {
            gameStatus.text =
                "PEMERIKSAAN GAGAL:\n${e.message}"
        }
    }
}
