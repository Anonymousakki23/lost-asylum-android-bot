package com.lastasylum.automation

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var moduleSpinner: Spinner
    private lateinit var confidenceSeekBar: SeekBar
    private lateinit var pacingSeekBar: SeekBar
    private lateinit var confidenceValue: TextView
    private lateinit var pacingValue: TextView
    private lateinit var saveButton: Button

    companion object {
        const val PREFS_NAME = "lost_asylum_prefs"
        const val KEY_MODULE = "selected_module"
        const val KEY_CONFIDENCE = "confidence_threshold"
        const val KEY_PACING = "tap_pacing_ms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Automation Settings"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.white, null))
            setPadding(0, 0, 0, 32)
        }

        val moduleLabel = TextView(this).apply {
            text = "Active Module"
            setTextColor(resources.getColor(android.R.color.white, null))
        }

        moduleSpinner = Spinner(this)
        val modules = listOf(
            "auto_scavenge",
            "resource_farm",
            "cure_healing",
            "recon",
            "raids_rallies",
            "daily_quests"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modules)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moduleSpinner.adapter = adapter

        val confidenceLabel = TextView(this).apply {
            text = "Confidence Threshold"
            setTextColor(resources.getColor(android.R.color.white, null))
            setPadding(0, 24, 0, 0)
        }

        confidenceSeekBar = SeekBar(this).apply {
            max = 100
            progress = 85
        }

        confidenceValue = TextView(this).apply {
            text = "85%"
            setTextColor(resources.getColor(android.R.color.white, null))
        }

        confidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                confidenceValue.text = "$progress%"
            }

            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {}
        })

        val pacingLabel = TextView(this).apply {
            text = "Tap Pacing (ms)"
            setTextColor(resources.getColor(android.R.color.white, null))
            setPadding(0, 24, 0, 0)
        }

        pacingSeekBar = SeekBar(this).apply {
            max = 2000
            progress = 300
        }

        pacingValue = TextView(this).apply {
            text = "300ms"
            setTextColor(resources.getColor(android.R.color.white, null))
        }

        pacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                pacingValue.text = "${progress}ms"
            }

            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {}
        })

        saveButton = Button(this).apply {
            text = "Save"
            setTextColor(resources.getColor(android.R.color.white, null))
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
            setOnClickListener { saveSettings() }
        }

        layout.addView(title)
        layout.addView(moduleLabel)
        layout.addView(moduleSpinner)
        layout.addView(confidenceLabel)
        layout.addView(confidenceSeekBar)
        layout.addView(confidenceValue)
        layout.addView(pacingLabel)
        layout.addView(pacingSeekBar)
        layout.addView(pacingValue)
        layout.addView(saveButton)

        setContentView(layout)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedModule = prefs.getString(KEY_MODULE, "auto_scavenge")
        val savedConfidence = prefs.getInt(KEY_CONFIDENCE, 85)
        val savedPacing = prefs.getInt(KEY_PACING, 300)

        val modules = listOf(
            "auto_scavenge", "resource_farm", "cure_healing", "recon", "raids_rallies", "daily_quests"
        )
        val idx = modules.indexOf(savedModule)
        if (idx >= 0) moduleSpinner.setSelection(idx)

        confidenceSeekBar.progress = savedConfidence
        confidenceValue.text = "$savedConfidence%"

        pacingSeekBar.progress = savedPacing
        pacingValue.text = "${savedPacing}ms"
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_MODULE, moduleSpinner.selectedItem.toString())
        editor.putInt(KEY_CONFIDENCE, confidenceSeekBar.progress)
        editor.putInt(KEY_PACING, pacingSeekBar.progress)
        editor.apply()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
