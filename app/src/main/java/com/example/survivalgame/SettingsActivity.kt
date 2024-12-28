package com.example.survivalgame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val sharedPrefFile = "com.example.android.sharedPrefFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)

        val numberOfEnemies: EditText = findViewById(R.id.numberOfEnemies)
        val numberOfBullets: EditText = findViewById(R.id.numberOfBullets)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val numberOfEnemiesValue = numberOfEnemies.text.toString().toIntOrNull()
            val numberOfBulletsValue = numberOfBullets.text.toString().toIntOrNull()

            if (numberOfEnemiesValue != null && numberOfBulletsValue != null) {
                val editor = preferences.edit()
                editor.putInt("numberOfEnemies", numberOfEnemiesValue)
                editor.putInt("totalBullets", numberOfBulletsValue)
                editor.apply()
                finish()
            }
            finish()
        }
    }
}