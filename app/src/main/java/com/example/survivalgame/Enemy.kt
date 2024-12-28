package com.example.survivalgame

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import kotlin.math.pow
import kotlin.math.sqrt

data class Enemy(var x: Float, var y: Float, val radius: Float, val bitmap: Bitmap) {
    private val speed = 4f  // Speed of the enemy
    private val stopDistance = 128f
    private var killed = false

    // Function to draw the enemy on the canvas
    fun draw(canvas: Canvas) {
        // Adjust for drawing the enemy so the center of the bitmap is at (x, y)
        canvas.drawBitmap(bitmap, x - bitmap.width / 2f, y - bitmap.height / 2f, null)
    }

    // Function to update the enemy's position, moving towards the player
    @SuppressLint("ClickableViewAccessibility")
    fun chasePlayer(playerX: Float, playerY: Float, player: Player, movementJoystick: JoystickView) {
        // Calculate the direction vector from the enemy to the player
        val dx = playerX - x
        val dy = playerY - y

        // Calculate the distance between the enemy and the player
        val distance = sqrt(dx.toDouble().pow(2.0) + dy.toDouble().pow(2.0)).toFloat()

        if (distance > stopDistance && !killed){
//            if (distance != 0f) {
            val normalizedDx = dx / distance
            val normalizedDy = dy / distance

            // Move the enemy towards the player by a set speed
            x += normalizedDx * speed
            y += normalizedDy * speed
//            }
        } else {
            // If the enemy is close enough to the player, stop moving
            // define attack here.
            player.setAnimation("dead", false)
            killed = true
            // disable Movement joystick, player's dead
            movementJoystick.setOnTouchListener {_, _ -> true}
        }
    }

    fun isKilled() : Boolean {
        return killed
    }
}
