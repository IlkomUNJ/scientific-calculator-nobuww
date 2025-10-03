package com.example.myapplication

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import kotlin.math.*

private val priority: Map<Char, Int> = mapOf(
    '^' to 4,
    'x' to 3,
    '*' to 3,
    '/' to 3,
    '+' to 2,
    '-' to 2
)

val functions = setOf(
    "sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "log", "ln"
)

private fun factorial(n: Int): BigInteger {
    require(n >= 0) { "Factorial is not defined for negative numbers." }
    if (n == 0) return BigInteger.ONE
    var result = BigInteger.ONE
    for (i in 2..n) {
        result = result.multiply(BigInteger.valueOf(i.toLong()))
    }
    return result
}

fun tokenize(expr: String): List<String> {
    val regex = Regex("""([0-9.]+|[a-wyzA-WYZ]+|[\^*x/+\-()!%])""")
    val tokens = regex.findAll(expr).map { it.value }.toList()
    val processed = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        if (token == "-") {
            val isFirstToken = i == 0
            val isAfterOperator = if (i > 0) tokens[i - 1] in setOf("x", "*", "-", "+", "^", "/", "(") else false

            if ((isFirstToken || isAfterOperator) && i + 1 < tokens.size && tokens[i + 1].toBigDecimalOrNull() != null) {
                processed.add("-${tokens[i + 1]}")
                i++
            } else {
                processed.add(token)
            }
        } else {
            processed.add(token)
        }
        i++
    }
    return processed
}

fun shuntingYard(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val stack = ArrayDeque<String>()

    for (token in tokens) {
        if (token.toBigDecimalOrNull() != null) {
            output.add(token)
        } else if (token in functions) {
            stack.addLast(token)
        } else if (token == "!" || token == "%") {
            output.add(token)
        } else if (token == "(") {
            stack.addLast(token)
        } else if (token == ")") {
            while (stack.isNotEmpty() && stack.last() != "(") {
                output.add(stack.removeLast())
            }
            if (stack.isNotEmpty()) stack.removeLast()
            if (stack.isNotEmpty() && stack.last() in functions) {
                output.add(stack.removeLast())
            }
        } else {
            while (stack.isNotEmpty() && stack.last() != "(" &&
                (priority[stack.last().first()] ?: 0) >= (priority[token.first()] ?: 0) &&
                token != "^"
            ) {
                output.add(stack.removeLast())
            }
            stack.addLast(token)
        }
    }

    output.addAll(stack.reversed())
    return output
}

fun evalRpn(rpnTokens: List<String>): String {
    val stack = ArrayDeque<BigDecimal>()
    val mc = MathContext.DECIMAL64

    for (token in rpnTokens) {
        val number = token.toBigDecimalOrNull()
        if (number != null) {
            stack.addLast(number)
            continue
        }

        if (stack.isEmpty()) return "Error"
        val op1 = stack.removeLast()
        val op2 = if (token in "+-x*/^" && stack.isNotEmpty()) stack.removeLast() else BigDecimal.ZERO

        val result = when (token) {
            "+" -> op2.add(op1)
            "-" -> op2.subtract(op1)
            "*" -> op2.multiply(op1)
            "x" -> op2.multiply(op1)
            "/" -> if (op1 != BigDecimal.ZERO) op2.divide(op1, mc) else return "Error"
            "^" -> op2.pow(op1.toInt(), mc)
            "!" -> factorial(op1.toInt()).toBigDecimal()
            "%" -> op1.divide(BigDecimal(100), mc)
            "sqrt" -> op1.toDouble().let { if (it < 0) return "Error" else sqrt(it) }.toBigDecimal()
            "sin" -> sin(Math.toRadians(op1.toDouble())).toBigDecimal()
            "cos" -> cos(Math.toRadians(op1.toDouble())).toBigDecimal()
            "tan" -> tan(Math.toRadians(op1.toDouble())).toBigDecimal()
            "asin" -> Math.toDegrees(asin(op1.toDouble())).toBigDecimal()
            "acos" -> Math.toDegrees(acos(op1.toDouble())).toBigDecimal()
            "atan" -> Math.toDegrees(atan(op1.toDouble())).toBigDecimal()
            "log" -> log10(op1.toDouble()).toBigDecimal()
            "ln" -> ln(op1.toDouble()).toBigDecimal()
            else -> return "Error"
        }
        stack.addLast(result)
    }

    if (stack.isEmpty()) return "0"
    return stack.first().stripTrailingZeros().toPlainString()
}

fun evaluate(expr: String): String {
    if (expr.toBigDecimalOrNull() != null) {
        return expr
    }

    var tokens = tokenize(expr)

    if (tokens.size >= 2 && tokens.last()[0] in priority.keys && tokens[tokens.size - 2].toBigDecimalOrNull() != null) {
        tokens = tokens.dropLast(1)
    }

    return try {
        val rpnTokens = shuntingYard(tokens)
        evalRpn(rpnTokens)
    } catch (e: Exception) {
        "Error"
    }
}