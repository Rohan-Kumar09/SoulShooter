package com.example.survivalgame

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import kotlin.math.cos
import kotlin.math.sin

class Player(
    private var x: Float, // Initial X position (centered)
    private var y: Float, // Initial X position (centered)
    resources: Resources // Resources to load images
) {
    private var speed: Float = 20f // Player's movement speed

    private lateinit var spriteSheet: Bitmap
    private var totalFrames = 0
    private var currentFrame = 0
    private var runningAnimation = "idle" // Stores the active animation state of player
    private val shootFrameDelay: Long = 100 // Delay between frames (in milliseconds)
    private val reloadFrameDelay: Long = 15 // Delay between frames (in milliseconds)
    private val handler = Handler(Looper.getMainLooper())
    private var animationRunning = false // Flag to check if animation is running
    private var facingRight = true

    // Maps sprite sheet animations to their bitmap and frame count
    private val spriteSheets: Map<String, Bitmap> = mapOf(
        "attack" to BitmapFactory.decodeResource(resources, R.drawable.attack),
        "idle" to BitmapFactory.decodeResource(resources, R.drawable.idle),
        "walk" to BitmapFactory.decodeResource(resources, R.drawable.walk),
        "shot" to BitmapFactory.decodeResource(resources, R.drawable.shot),
        "recharge" to BitmapFactory.decodeResource(resources, R.drawable.recharge),
        "hurt" to BitmapFactory.decodeResource(resources, R.drawable.hurt),
        "dead" to BitmapFactory.decodeResource(resources, R.drawable.dead)
    )

    private val numberOfFrames: Map<String, Int> = mapOf(
        "attack" to 3,
        "idle" to 6,
        "walk" to 10,
        "shot" to 4,
        "recharge" to 17,
        "hurt" to 5,
        "dead" to 5
    )


    // Draw the player on the canvas
    fun draw(canvas: Canvas) {
        // Get the correct sprite sheet and total number of frames
        spriteSheet = spriteSheets[runningAnimation] ?: return
        totalFrames = numberOfFrames[runningAnimation] ?: return

        // Dynamically calculate the frame width and height based on sprite sheet
        val frameWidth = spriteSheet.width / totalFrames
        val frameHeight = spriteSheet.height // Assuming one row of frames

        // Extract the current frame from the sprite sheet
        val x = currentFrame * frameWidth
        val y = 0 // Assuming frames are in a single row

        // Crop the sprite sheet to get the current frame
        val frame = Bitmap.createBitmap(spriteSheet, x, y, frameWidth, frameHeight)

        // Create a transformation matrix to position the sprite at (x, y)
        val matrix = Matrix().apply {
            // Flip horizontally if facing left
            if (!facingRight) {
                preScale(-1f, 1f)
                postTranslate(frame.width.toFloat(), 0f) // Offset to flip around the correct axis
            }
            postTranslate(-frame.width / 2f, -frame.height / 2f) // Center the player
            postTranslate(this@Player.x, this@Player.y) // Set the player position
        }

        // Draw the frame on the canvas
        canvas.drawBitmap(frame, matrix, null)
    }

    fun update(angle: Float) {
        val deltaX = speed * cos(Math.toRadians(angle.toDouble())).toFloat()
        val deltaY = speed * sin(Math.toRadians(angle.toDouble())).toFloat()

        // Update position
        x += deltaX
        y += deltaY

        // Update facing direction based on horizontal movement
        if (deltaX > 0) {
            facingRight = true
        } else if (deltaX < 0) {
            facingRight = false
        }
    }

    fun getX() = x
    fun getY() = y

    // Change the animation state and start the animation
    fun setAnimation(newAnimation: String, infinite: Boolean = true) {
        if (runningAnimation != newAnimation) { // if animation already running, do nothing
            runningAnimation = newAnimation
            currentFrame = 0 // Reset to the first frame of the new animation
            if (animationRunning) {
                handler.removeCallbacksAndMessages(null) // Stop current animation if it’s running
            }
            startFrameAnimation(newAnimation, infinite)  // Start the frame animation loop
        }
    }

    // Handle the frame-by-frame animation update
    private fun startFrameAnimation(animation: String, infinite: Boolean) {
        // Runnable to update the frame every `frameDelay`
        val runnable = object : Runnable {
            override fun run() {
                // Move to the next frame
                currentFrame = (currentFrame + 1) % totalFrames

                if (!infinite && currentFrame == totalFrames - 1) {
                    animationRunning = false
                    return
                } // only play the animation once if not infinite

                if (animation == "recharge"){
                    handler.postDelayed(this, reloadFrameDelay)
                } else {
                    handler.postDelayed(this, shootFrameDelay) // Repeat the animation
                }
            }
        }

        // Start the animation loop
        handler.post(runnable)
        animationRunning = true
    }
}
