package com.example.survivalgame

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import kotlin.math.cos
import kotlin.math.sin

class Bullet(
    private var bitmap: Bitmap,
    private var x: Float, // Initial X position
    private var y: Float, // Initial Y position
    private var angle: Float // Angle of shooting
) {
    private val speed = 50f // Speed of the bullet

    // Calculate direction vector based on angle
    private val vx = cos(Math.toRadians(angle.toDouble())).toFloat()
    private val vy = sin(Math.toRadians(angle.toDouble())).toFloat()

    // Update the bullet's position based on its angle
    fun update() {
        // Calculate the new position based on the angle (speed * cos/sin gives the direction)
        x += speed * vx
        y += speed * vy
    }

    fun checkCollisionWithEnemy(enemy: Enemy): Boolean {
        // Calculate previous position (A) based on speed and direction
        val xPrev = x - speed * vx
        val yPrev = y - speed * vy

        // Vector from previous bullet position to current position
        val dx = x - xPrev
        val dy = y - yPrev

        // Enemy hit box (rectangular area)
        val enemyLeft = enemy.x
        val enemyRight = enemy.x + 128
        val enemyTop = enemy.y
        val enemyBottom = enemy.y + 128

        // Check collision with all four sides of the enemy's rectangle
        return checkLineIntersectionWithRectangle(xPrev, yPrev, dx, dy, enemyLeft, enemyTop, enemyRight, enemyBottom)
    }

    private fun checkLineIntersectionWithRectangle(xPrev: Float, yPrev: Float, dx: Float, dy: Float,
                                                   enemyLeft: Float, enemyTop: Float, enemyRight: Float, enemyBottom: Float): Boolean {
        // Check intersection with each of the four sides of the rectangle
        // We will check line-segment intersections between the bullet's path and each side of the rectangle.

        // Left side of the rectangle (x = enemyLeft)
        if (checkLineIntersection(xPrev, yPrev, dx, dy, enemyLeft, enemyTop, enemyLeft, enemyBottom)) return true

        // Right side of the rectangle (x = enemyRight)
        if (checkLineIntersection(xPrev, yPrev, dx, dy, enemyRight, enemyTop, enemyRight, enemyBottom)) return true

        // Top side of the rectangle (y = enemyTop)
        if (checkLineIntersection(xPrev, yPrev, dx, dy, enemyLeft, enemyTop, enemyRight, enemyTop)) return true

        // Bottom side of the rectangle (y = enemyBottom)
        if (checkLineIntersection(xPrev, yPrev, dx, dy, enemyLeft, enemyBottom, enemyRight, enemyBottom)) return true

        return false
    }

    private fun checkLineIntersection(x1: Float, y1: Float, dx: Float, dy: Float,
                                      x2: Float, y2: Float, x3: Float, y3: Float): Boolean {
        // Check if the line segment (x1, y1) -> (x1+dx, y1+dy) intersects the line segment (x2, y2) -> (x3, y3)

        val den = (x1 - (x1 + dx)) * (y2 - y3) - (y1 - (y1 + dy)) * (x2 - x3)

        if (den == 0f) return false  // Parallel lines (no intersection)

        val t1 = ((x1 - x2) * (y2 - y3) - (y1 - y2) * (x2 - x3)) / den
        val t2 = ((x1 - x2) * (y1 - (y1 + dy)) - (y1 - y2) * (x1 - (x1 + dx))) / den

        // If 0 <= t1 <= 1 and 0 <= t2 <= 1, the lines intersect within the segments
        return t1 in 0f..1f && t2 in 0f..1f
    }


    // Check if the bullet is off-screen
    fun isOffScreen(screenWidth: Int, screenHeight: Int): Boolean {
        return x > screenWidth || y > screenHeight || x < 0 || y < 0
    }

    fun draw(canvas: Canvas) {
        val matrix = Matrix()
        matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f) // Center the bullet
        matrix.postRotate(angle + 90) // Rotate to match the shooting direction
        matrix.postTranslate(x, y) // Position bullet at its current location

        // Draw the rotated bullet on the canvas
        canvas.drawBitmap(bitmap, matrix, null)
    }
}