package com.example.survivalgame

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScoreActivity : AppCompatActivity() {
    private val sharedPrefFile = "com.example.android.sharedPrefFile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val resetButton: Button = findViewById(R.id.resetButton)
        val winView: TextView = findViewById(R.id.winView)
        val lostView: TextView = findViewById(R.id.lostView)

        val received = intent.getStringExtra("info")
        when (received) {
            "lost" -> { // lost
                winView.visibility = View.INVISIBLE
                resetButton.visibility = View.INVISIBLE
            }
            "won" -> { // won
                lostView.visibility = View.INVISIBLE
                resetButton.visibility = View.INVISIBLE
            }
            "show" -> {
                winView.visibility = View.INVISIBLE
                lostView.visibility = View.INVISIBLE

                resetButton.setOnClickListener{
                    val timesWon: TextView = findViewById(R.id.timesWon)
                    val timesLost: TextView = findViewById(R.id.timesLost)
                    timesWon.text = "0"
                    timesLost.text = "0"
                    val editor = sharedPreferences.edit()
                    editor.putInt("timesWon", 0) // default
                    editor.putInt("timesLost", 0) // default
                    editor.apply()
                }
            }
            else -> {
                finish()
            }
        }

        val timesWonView: TextView = findViewById(R.id.timesWon)
        val timesLostView: TextView = findViewById(R.id.timesLost)

        val timesWon = sharedPreferences.getInt("timesWon", 0)
        timesWonView.text = "$timesWon"
        val timesLost = sharedPreferences.getInt("timesLost", 0)
        timesLostView.text = "$timesLost"


        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}