package com.ipodmodern.audio

import com.ipodmodern.audio.core.haptics.WheelKinematicsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class WheelKinematicsCalculatorTest {

    @Test
    fun testRawAngleCalculations() {
        val calc = WheelKinematicsCalculator()
        val centerX = 100f
        val centerY = 100f

        // Right (0 rad)
        val angleRight = calc.calculateRawAngle(centerX, centerY, 200f, 100f)
        assertEquals(0.0, angleRight, 0.001)

        // Down (PI/2 rad)
        val angleDown = calc.calculateRawAngle(centerX, centerY, 100f, 200f)
        assertEquals(PI / 2.0, angleDown, 0.001)

        // Up (-PI/2 rad)
        val angleUp = calc.calculateRawAngle(centerX, centerY, 100f, 0f)
        assertEquals(-PI / 2.0, angleUp, 0.001)
    }

    @Test
    fun testPhaseUnwrappingAcrossBranchCut() {
        val calc = WheelKinematicsCalculator()

        // Rotating clockwise across the -PI / +PI boundary: e.g. from 3.10 rad to -3.10 rad
        val prevAngle = 3.10
        val currentAngle = -3.10
        val unwrappedDelta = calc.unwrapDelta(currentAngle, prevAngle)

        // Delta should be positive (clockwise rotation ~ 0.083 rad), not negative ~ -6.20 rad
        assertTrue("Delta across branch cut should be positive", unwrappedDelta > 0.0)
        assertEquals((2.0 * PI) - 6.20, unwrappedDelta, 0.001)
    }

    @Test
    fun test15DegreeTickThresholds() {
        val calc = WheelKinematicsCalculator(tickStepDegrees = 15.0)
        val centerX = 100f
        val centerY = 100f

        calc.reset(centerX, centerY, 200f, 100f) // Start at 0 deg

        // Move to 16 degrees (approx x = 100 + 100*cos(16°), y = 100 + 100*sin(16°))
        val rad16 = Math.toRadians(16.0)
        val touchX = 100f + 100f * kotlin.math.cos(rad16).toFloat()
        val touchY = 100f + 100f * kotlin.math.sin(rad16).toFloat()

        val ticks = calc.onTouchMove(centerX, centerY, touchX, touchY)
        assertEquals(1, ticks)
    }

    @Test
    fun testInertialMomentumDecay() {
        val calc = WheelKinematicsCalculator(frictionCoefficient = 3.0)
        calc.reset(100f, 100f, 200f, 100f)

        // Spin fast
        val rad1 = Math.toRadians(45.0)
        calc.onTouchMove(100f, 100f, 100f + 100f * kotlin.math.cos(rad1).toFloat(), 100f + 100f * kotlin.math.sin(rad1).toFloat(), 10_000_000L)

        // Coast
        var totalCoastTicks = 0
        for (i in 0 until 50) {
            val ticks = calc.stepInertia(0.02) // 20ms frame
            totalCoastTicks += kotlin.math.abs(ticks)
            if (calc.isCoastComplete()) break
        }

        assertTrue("Inertia should complete coasting", calc.isCoastComplete())
    }
}
