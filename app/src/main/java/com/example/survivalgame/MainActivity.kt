package com.example.survivalgame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.atan2


class MainActivity : AppCompatActivity() {
    private lateinit var gameCanvas: Canvas // Canvas for drawing
    private lateinit var gameImageView: ImageView // ImageView to display the game
    private lateinit var gameBitmap: Bitmap // Bitmap for the game display
    private lateinit var enemyBitmap: Bitmap // Bitmap for the enemy
    private lateinit var bulletBitmap: Bitmap // Bitmap for the bullet
    private lateinit var reloadingButton: Button // Button for reloading
    private lateinit var movementJoystick: JoystickView // Joystick for movement
    private lateinit var player: Player // Player object
    private lateinit var enemyCountDisplay: TextView // TextView to display enemy count
    private lateinit var bulletCountDisplay: TextView // TextView to display bullet count

    private var numberOfEnemies: Int = 0 // number of enemies to spawn
    private var totalBullets: Int = 0  // Total number of bullets

    private val shootAnimationTime = 400L // Time in milliseconds for the shoot animation
    private val reloadAnimationTime = 500L // Time in milliseconds for the reload animation
    private val shootingDelay = 500L  // Time in milliseconds between shots

    private var enemiesKilled = 0 // number of enemies killed
    private var bulletsShot = 0  // to keep track of number of bullets shot
    private var lastShotTime = 0L  // Timestamp of the last shot (limits fire rate)

//    private val playerPixelSize = 128 // size of the player picture
    private val enemyPixelSize = 128 // size of the enemy picture
    private val enemies = mutableListOf<Enemy>() // list of enemies
    private val bullets = mutableListOf<Bullet>() // list of bullets

    private val sharedPrefFile = "com.example.android.sharedPrefFile"

    // Player's active location (X, Y), used for bullet shooting, and enemy chasing
    private var playerX = 0f
    private var playerY = 0f

    private var isGameOver = false

    private val handler = Handler(Looper.getMainLooper()) // Handler to run the update loop
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateGameDisplay()  // Continuously update game display
            handler.postDelayed(this, 16)  // Run this every 16ms (~60 FPS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        numberOfEnemies = sharedPreferences.getInt("numberOfEnemies", 5)
        totalBullets = sharedPreferences.getInt("totalBullets", 5)

        bulletBitmap = BitmapFactory.decodeResource(resources, R.drawable.bullet) // bullet Image as a bitmap
        enemyBitmap = BitmapFactory.decodeResource(resources, R.drawable.enemy) // enemy Image as a bitmap
        enemyCountDisplay = findViewById(R.id.enemiesLeftView) // TextView to display enemy count
        bulletCountDisplay = findViewById(R.id.bulletCount) // TextView to display bullet count

        bulletCountDisplay.text = "$totalBullets" // set display to number of bullets
        enemyCountDisplay.text = "$numberOfEnemies" // set display to number of enemies

        // Initialize gameBitmap and gameCanvas
        val displayMetrics = resources.displayMetrics
        // 4 bytes per pixel, ARGB
        gameBitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
        gameCanvas = Canvas(gameBitmap) // Canvas for drawing on the bitmap

        gameImageView = findViewById(R.id.gameImageView)
        gameImageView.setImageBitmap(gameBitmap) // Set up the ImageView


        spawnEnemies() // Spawn enemies at random positions

        // center the player and bullet position
        playerX = gameCanvas.width / 2f
        playerY = gameCanvas.height / 2f

        player = Player(playerX, playerY, resources)

        movementJoystick = findViewById(R.id.movingJoystickView)
        movementJoystick.onJoystickMoved = { x, y ->

            // add boundary checks later
            player.update(calculateAngle(x, y))

            // Update the player position based on the joystick movement
            playerX = player.getX()
            playerY = player.getY()

            if (x != 0f || y != 0f) {
                player.update(calculateAngle(x, y))
                player.setAnimation("walk")
            } else {
                player.setAnimation("idle")
            }
        }

        reloadingButton = findViewById(R.id.reloadButton)
        reloadingButton.setOnClickListener {
            player.setAnimation("recharge", false)
            Handler(Looper.getMainLooper()).postDelayed({
                player.setAnimation("idle")
                bulletsShot = 0
                bulletCountDisplay.text = "$totalBullets"
            }, reloadAnimationTime) // Adjust this delay to match the shot animation duration
        }

        handler.post(updateRunnable)
    }

    private fun updateGameDisplay() {
        // Clear canvas before redrawing
        gameCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        var enemyKilled = false

        // Update and draw each bullet
        val iterator = bullets.iterator()
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            bullet.update()  // Update bullet's position based on its angle

            // Check for collisions with enemies
            val enemyIterator = enemies.iterator()
            while (enemyIterator.hasNext()) {
                val enemy = enemyIterator.next()
                if (bullet.checkCollisionWithEnemy(enemy)) {
                    // Handle enemy hit, remove enemy
                    enemyIterator.remove()  // Remove enemy if hit
                    enemyKilled = true
                    ++enemiesKilled
                    (numberOfEnemies - enemiesKilled).toString().also { enemyCountDisplay.text = it }
                    break
                }
            }

            // remove the bullet if it's offscreen or hits an enemy
            if (bullet.isOffScreen(gameCanvas.width, gameCanvas.height) or enemyKilled) {
                iterator.remove()  // Remove the bullet if off-screen
            } else {
                bullet.draw(gameCanvas)  // Draw bullet onto the canvas
            }
        }

        // player won
        if (enemiesKilled == numberOfEnemies && !isGameOver) {
            isGameOver = true

            val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
            var won = sharedPreferences.getInt("timesWon", 0)
            won += 1
            val editor = sharedPreferences.edit()
            editor.putInt("timesWon", won) // Store the updated value
            editor.apply()

            val intent = Intent(this, ScoreActivity::class.java)
            intent.putExtra("info", "won")
            startActivity(intent)
            finish()
        }

        player.draw(gameCanvas)

        enemies.forEach { enemy ->
            enemy.draw(gameCanvas)
            enemy.chasePlayer(playerX, playerY, player, movementJoystick) // give enemy player location
            // if enemy killed a player then end the game, show score, player lost
            if (enemy.isKilled() && !isGameOver) {
                isGameOver = true

                val sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
                var lost = sharedPreferences.getInt("timesLost", 0)
                lost += 1
                val editor = sharedPreferences.edit()
                editor.putInt("timesLost", lost) // Store the updated value
                editor.apply()

                val intent = Intent(this, ScoreActivity::class.java)
                intent.putExtra("info", "lost")

                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(intent)
                    finish()
                }, 1000) // Delay for 1 second (1000 milliseconds)
            }
        }

        // Update the ImageView with the latest bitmap
        gameImageView.invalidate() // Trigger a redraw of the gameImageView every 16ms (60 FPS)
    }

    private fun spawnEnemies() {
        // You can randomly spawn enemies around the screen or in fixed positions
        for (i in 0 until numberOfEnemies) {
            var x = (Math.random() * (gameCanvas.width - enemyPixelSize)).toFloat()
            var y = (Math.random() * (gameCanvas.height - enemyPixelSize)).toFloat()
            val z = (Math.random() * 2).toInt() // Random value 0 or 1

            // spawn the enemies in fixed spots places around the parameter
            if (z == 1) {
                x = -200f
            } else {
                y = -200f
            }
            enemies.add(Enemy(x, y, (enemyPixelSize / 2).toFloat(), enemyBitmap))
        }
    }


//    // On Touch Shooting Mechanism
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                // Calculate angle from player to touch point
                val angle = calculateAngle(touchX - playerX, touchY - playerY)

                if (System.currentTimeMillis() - lastShotTime > shootingDelay) {
                    if (bulletsShot < totalBullets) {
                        // Fire bullet toward the touch point
                        fireBullet(playerX, playerY, angle)
                        lastShotTime = System.currentTimeMillis()
                        player.setAnimation("shot")
                        bulletsShot++
                        (totalBullets - bulletsShot).toString().also { bulletCountDisplay.text = it } // display the remaining bullet count

                        Handler(Looper.getMainLooper()).postDelayed({
                            player.setAnimation("idle")
                        }, shootAnimationTime) // Adjust this delay to match the shot animation duration
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // Reset Animation when touch is released
                Handler(Looper.getMainLooper()).postDelayed({
                    player.setAnimation("idle")
                }, shootAnimationTime) // Adjust this delay to match the shot animation duration

            }
        }
        return super.onTouchEvent(event)
    }


    private fun fireBullet(x: Float, y: Float, angle: Float) {
        bullets.add(Bullet(bulletBitmap, x, y, angle))
    }

    private fun calculateAngle(x: Float, y: Float): Float {
        val angle = atan2(y.toDouble(), x.toDouble())
        return Math.toDegrees(angle).toFloat()
    }

    override fun onPause() {
        super.onPause()
        // Stop the update loop to conserve resources when the activity is not visible
        handler.removeCallbacks(updateRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Restart the update loop when the activity becomes visible again
        handler.post(updateRunnable)
    }
}