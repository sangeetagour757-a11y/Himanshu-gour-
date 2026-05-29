package com.example

import com.example.util.MathEvaluator
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin
import java.lang.Math.toRadians

/**
 * Unit tests verifying local MathEvaluator calculations.
 */
class ExampleUnitTest {
    @Test
    fun testBasicArithmetic() {
        assertEquals(17.0, MathEvaluator.evaluate("7 + 5 * 2"), 0.0001)
        assertEquals(24.0, MathEvaluator.evaluate("(7 + 5) * 2"), 0.0001)
        assertEquals(3.5, MathEvaluator.evaluate("7 / 2"), 0.0001)
        assertEquals(1.0, MathEvaluator.evaluate("7 % 3"), 0.0001)
    }

    @Test
    fun testNegativeNumbers() {
        assertEquals(-3.0, MathEvaluator.evaluate("-3"), 0.0001)
        assertEquals(10.0, MathEvaluator.evaluate("15 + -5"), 0.0001)
        assertEquals(-20.0, MathEvaluator.evaluate("-2 * 10"), 0.0001)
    }

    @Test
    fun testExponentiation() {
        assertEquals(8.0, MathEvaluator.evaluate("2 ^ 3"), 0.0001)
        assertEquals(100.0, MathEvaluator.evaluate("10 ^ 2"), 0.0001)
        assertEquals(16.0, MathEvaluator.evaluate("2 ^ (1 + 3)"), 0.0001)
    }

    @Test
    fun testScientificFunctionsDegrees() {
        assertEquals(1.0, MathEvaluator.evaluate("sin(90)", useDegrees = true), 0.0001)
        assertEquals(0.5, MathEvaluator.evaluate("sin(30)", useDegrees = true), 0.0001)
        assertEquals(0.5, MathEvaluator.evaluate("cos(60)", useDegrees = true), 0.0001)
        assertEquals(1.0, MathEvaluator.evaluate("tan(45)", useDegrees = true), 0.0001)
        assertEquals(2.0, MathEvaluator.evaluate("log(100)"), 0.0001)
    }

    @Test
    fun testScientificFunctionsRadians() {
        assertEquals(sin(1.0), MathEvaluator.evaluate("sin(1)", useDegrees = false), 0.0001)
    }

    @Test
    fun testConstants() {
        assertEquals(Math.PI, MathEvaluator.evaluate("pi"), 0.0001)
        assertEquals(Math.E, MathEvaluator.evaluate("e"), 0.0001)
    }
}
