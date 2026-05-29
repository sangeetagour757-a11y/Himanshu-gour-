package com.example.util

import kotlin.math.*

object MathEvaluator {
    fun evaluate(expression: String, useDegrees: Boolean = true): Double {
        // Sanitize string (replace visual math symbols with parser friendly equivalents)
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < sanitized.length) sanitized[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < sanitized.length) throw RuntimeException("Unexpected character: " + ch.toChar())
                return x
            }

            // expression = term | expression `+` term | expression `-` term
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            // term = factor | term `*` factor | term `/` factor | term `%` factor
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else if (eat('%'.code)) x %= parseFactor() // modulo
                    else return x
                }
            }

            // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    val strValue = sanitized.substring(startPos, pos)
                    x = strValue.toDoubleOrNull() ?: throw RuntimeException("Invalid number format: $strValue")
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions & constants
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = sanitized.substring(startPos, pos)
                    if (func == "pi") {
                        x = Math.PI
                    } else if (func == "e") {
                        x = Math.E
                    } else {
                        // All functions require an expression as factor (either parenthetically or directly next)
                        val arg = parseFactor()
                        x = when (func) {
                            "sin" -> if (useDegrees) sin(Math.toRadians(arg)) else sin(arg)
                            "cos" -> if (useDegrees) cos(Math.toRadians(arg)) else cos(arg)
                            "tan" -> if (useDegrees) tan(Math.toRadians(arg)) else tan(arg)
                            "sqrt" -> {
                                if (arg < 0) throw RuntimeException("Square root of a negative number")
                                sqrt(arg)
                            }
                            "log" -> {
                                if (arg <= 0) throw RuntimeException("Log of non-positive number")
                                log10(arg)
                            }
                            "ln" -> {
                                if (arg <= 0) throw RuntimeException("Natural log of non-positive number")
                                ln(arg)
                            }
                            "abs" -> abs(arg)
                            "exp" -> exp(arg)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected character index $pos: '" + ch.toChar() + "'")
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                return x
            }
        }.parse()
    }
}
