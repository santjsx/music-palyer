package com.ipodmodern.audio.core.haptics

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

class WheelKinematicsCalculator(
    private val tickStepDegrees: Double = 15.0,
    private val frictionCoefficient: Double = 3.5 // Decay factor for inertial momentum
) {
    private val tickStepRadians: Double = Math.toRadians(tickStepDegrees)
    private var previousAngle: Double? = null
    private var accumulatedAngleForTick: Double = 0.0
    private var lastEventTimeNs: Long = 0L
    private var currentAngularVelocityRadPerSec: Double = 0.0

    /**
     * Resets tracking state when touch starts
     */
    fun reset(centerX: Float, centerY: Float, touchX: Float, touchY: Float, timestampNs: Long = System.nanoTime()) {
        previousAngle = calculateRawAngle(centerX, centerY, touchX, touchY)
        accumulatedAngleForTick = 0.0
        lastEventTimeNs = timestampNs
        currentAngularVelocityRadPerSec = 0.0
    }

    /**
     * Computes raw angle in [-PI, PI] relative to wheel center
     */
    fun calculateRawAngle(centerX: Float, centerY: Float, touchX: Float, touchY: Float): Double {
        val dx = touchX - centerX
        val dy = touchY - centerY
        return atan2(dy.toDouble(), dx.toDouble())
    }

    /**
     * Unwraps angular delta across the [-PI, PI] branch cut
     */
    fun unwrapDelta(currentAngle: Double, previousAngle: Double): Double {
        var delta = currentAngle - previousAngle
        while (delta > PI) delta -= 2.0 * PI
        while (delta < -PI) delta += 2.0 * PI
        return delta
    }

    /**
     * Processes a move event.
     * @return Number of 15-degree ticks crossed (positive = clockwise, negative = counter-clockwise)
     */
    fun onTouchMove(
        centerX: Float,
        centerY: Float,
        touchX: Float,
        touchY: Float,
        timestampNs: Long = System.nanoTime()
    ): Int {
        val currentAngle = calculateRawAngle(centerX, centerY, touchX, touchY)
        val prev = previousAngle ?: run {
            reset(centerX, centerY, touchX, touchY, timestampNs)
            return 0
        }

        val delta = unwrapDelta(currentAngle, prev)
        previousAngle = currentAngle

        // Calculate angular velocity
        val dtSec = (timestampNs - lastEventTimeNs).coerceAtLeast(1L) / 1_000_000_000.0
        lastEventTimeNs = timestampNs
        if (dtSec > 0.0 && dtSec < 0.25) {
            val instantaneousVelocity = delta / dtSec
            // Low-pass filter for smooth velocity estimation
            currentAngularVelocityRadPerSec = 0.7 * currentAngularVelocityRadPerSec + 0.3 * instantaneousVelocity
        }

        // Accumulate for 15-degree discrete tick thresholds
        accumulatedAngleForTick += delta
        val ticks = (accumulatedAngleForTick / tickStepRadians).toInt()
        if (ticks != 0) {
            accumulatedAngleForTick -= ticks * tickStepRadians
        }

        return ticks
    }

    /**
     * Gets current angular velocity in rad/sec
     */
    fun getAngularVelocity(): Double = currentAngularVelocityRadPerSec

    /**
     * Simulates inertial momentum decay for one time step dt
     * @return Number of ticks generated during inertia decay step
     */
    fun stepInertia(dtSec: Double): Int {
        if (kotlin.math.abs(currentAngularVelocityRadPerSec) < 0.1) {
            currentAngularVelocityRadPerSec = 0.0
            return 0
        }

        // Exponential deceleration: w(t) = w0 * exp(-friction * dt)
        val delta = currentAngularVelocityRadPerSec * dtSec
        currentAngularVelocityRadPerSec *= kotlin.math.exp(-frictionCoefficient * dtSec)

        accumulatedAngleForTick += delta
        val ticks = (accumulatedAngleForTick / tickStepRadians).toInt()
        if (ticks != 0) {
            accumulatedAngleForTick -= ticks * tickStepRadians
        }
        return ticks
    }

    fun isCoastComplete(): Boolean = kotlin.math.abs(currentAngularVelocityRadPerSec) < 0.05
}
