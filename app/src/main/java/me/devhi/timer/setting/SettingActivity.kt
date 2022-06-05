package me.devhi.timer.setting

import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.CheckedTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.devhi.timer.*

class SettingActivity : AppCompatActivity() {
    private lateinit var soundCheckBox: CheckedTextView
    private lateinit var vibrateCheckBox: CheckedTextView
    private lateinit var screenCheckBox: CheckedTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.title = "설정"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        soundCheckBox = findViewById(R.id.option_sound)
        vibrateCheckBox = findViewById(R.id.option_vibrate)
        screenCheckBox = findViewById(R.id.option_screen)
        loadOptions()
        setClickListeners()
    }

    private fun setClickListeners() {
        soundCheckBox.setOnClickListener {
            val view = (it as CheckedTextView)
            view.isChecked = !view.isChecked
            saveToDataStore(soundKey, view.isChecked)
        }
        vibrateCheckBox.setOnClickListener {
            val view = (it as CheckedTextView)
            view.isChecked = !view.isChecked
            saveToDataStore(vibrateKey, view.isChecked)
        }
        screenCheckBox.setOnClickListener {
            val view = (it as CheckedTextView)
            view.isChecked = !view.isChecked
            saveToDataStore(screenKey, view.isChecked)
        }
    }


    private fun loadOptions() {
        lifecycleScope.launch {
            dataStore.data
                .collect { preferences ->
                    soundCheckBox.isChecked = preferences[soundKey] ?: false
                    vibrateCheckBox.isChecked = preferences[vibrateKey] ?: false
                    screenCheckBox.isChecked = preferences[screenKey] ?: false
                }
        }
    }

    private fun saveToDataStore(key: Preferences.Key<Boolean>, checked: Boolean) {
        lifecycleScope.launch {
            dataStore.edit { settings ->
                settings[key] = checked
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }
}