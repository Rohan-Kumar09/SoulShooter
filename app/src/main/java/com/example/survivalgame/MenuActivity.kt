package com.example.survivalgame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val sharedPrefFile = "com.example.android.sharedPrefFile"
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putInt("numberOfEnemies", 5) // default enemies
        editor.putInt("totalBullets", 5) // default bullets
        editor.apply()

        val startGameButton: Button = findViewById(R.id.startGameButton)
        startGameButton.setOnClickListener {
            // Navigate to the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            // Navigate to the SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val checkScoreButton: Button = findViewById(R.id.checkScoreButton)
        checkScoreButton.setOnClickListener {
            // Navigate to the ScoreActivity
            val intent = Intent(this, ScoreActivity::class.java)
            intent.putExtra("info", "show")
            startActivity(intent)
        }

        val quitButton: Button = findViewById(R.id.quitButton)
        quitButton.setOnClickListener {
            // Finish the activity to exit the app
            finish()
        }
    }
}
