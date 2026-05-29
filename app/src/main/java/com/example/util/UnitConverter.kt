package com.example.util

object UnitConverter {
    enum class Category {
        LENGTH, MASS, TEMPERATURE, AREA, SPEED, CURRENCY
    }

    data class UnitOption(val name: String, val abbrev: String)

    val categories = listOf(
        Category.LENGTH,
        Category.MASS,
        Category.TEMPERATURE,
        Category.AREA,
        Category.SPEED,
        Category.CURRENCY
    )

    fun getUnits(category: Category): List<UnitOption> = when (category) {
        Category.LENGTH -> listOf(
            UnitOption("Meters", "m"),
            UnitOption("Kilometers", "km"),
            UnitOption("Centimeters", "cm"),
            UnitOption("Millimeters", "mm"),
            UnitOption("Miles", "mi"),
            UnitOption("Yards", "yd"),
            UnitOption("Feet", "ft"),
            UnitOption("Inches", "in")
        )
        Category.MASS -> listOf(
            UnitOption("Kilograms", "kg"),
            UnitOption("Grams", "g"),
            UnitOption("Milligrams", "mg"),
            UnitOption("Pounds", "lbs"),
            UnitOption("Ounces", "oz")
        )
        Category.TEMPERATURE -> listOf(
            UnitOption("Celsius", "°C"),
            UnitOption("Fahrenheit", "°F"),
            UnitOption("Kelvin", "K")
        )
        Category.AREA -> listOf(
            UnitOption("Square Meters", "m²"),
            UnitOption("Square Kilometers", "km²"),
            UnitOption("Square Miles", "mi²"),
            UnitOption("Acres", "ac"),
            UnitOption("Hectares", "ha")
        )
        Category.SPEED -> listOf(
            UnitOption("Meters/Second", "m/s"),
            UnitOption("Kilometers/Hour", "km/h"),
            UnitOption("Miles/Hour", "mph"),
            UnitOption("Knots", "kt")
        )
        Category.CURRENCY -> listOf(
            UnitOption("US Dollar", "USD"),
            UnitOption("Euro", "EUR"),
            UnitOption("British Pound", "GBP"),
            UnitOption("Japanese Yen", "JPY"),
            UnitOption("Canadian Dollar", "CAD"),
            UnitOption("Australian Dollar", "AUD"),
            UnitOption("Indian Rupee", "INR"),
            UnitOption("Chinese Yuan", "CNY")
        )
    }

    fun convert(value: Double, fromUnit: String, toUnit: String, category: Category): Double {
        if (fromUnit == toUnit) return value

        return when (category) {
            Category.LENGTH -> {
                // Convert to base unit: meters
                val valueInMeters = when (fromUnit) {
                    "m" -> value
                    "km" -> value * 1000.0
                    "cm" -> value / 100.0
                    "mm" -> value / 1000.0
                    "mi" -> value * 1609.344
                    "yd" -> value * 0.9144
                    "ft" -> value * 0.3048
                    "in" -> value * 0.0254
                    else -> value
                }
                // Convert from meters to target unit
                when (toUnit) {
                    "m" -> valueInMeters
                    "km" -> valueInMeters / 1000.0
                    "cm" -> valueInMeters * 100.0
                    "mm" -> valueInMeters * 1000.0
                    "mi" -> valueInMeters / 1609.344
                    "yd" -> valueInMeters / 0.9144
                    "ft" -> valueInMeters / 0.3048
                    "in" -> valueInMeters / 0.0254
                    else -> valueInMeters
                }
            }
            Category.MASS -> {
                // Convert to base unit: grams (g)
                val valueInGrams = when (fromUnit) {
                    "kg" -> value * 1000.0
                    "g" -> value
                    "mg" -> value / 1000.0
                    "lbs" -> value * 453.59237
                    "oz" -> value * 28.349523125
                    else -> value
                }
                // Convert from grams to target unit
                when (toUnit) {
                    "kg" -> valueInGrams / 1000.0
                    "g" -> valueInGrams
                    "mg" -> valueInGrams * 1000.0
                    "lbs" -> valueInGrams / 453.59237
                    "oz" -> valueInGrams / 28.349523125
                    else -> valueInGrams
                }
            }
            Category.TEMPERATURE -> {
                // Convert to base unit: Celsius
                val valueInCelsius = when (fromUnit) {
                    "°C" -> value
                    "°F" -> (value - 32.0) * 5.0 / 9.0
                    "K" -> value - 273.15
                    else -> value
                }
                // Convert from Celsius to target unit
                when (toUnit) {
                    "°C" -> valueInCelsius
                    "°F" -> (valueInCelsius * 9.0 / 5.0) + 32.0
                    "K" -> valueInCelsius + 273.15
                    else -> valueInCelsius
                }
            }
            Category.AREA -> {
                // Convert to base unit: square meters (m²)
                val valueInSqM = when (fromUnit) {
                    "m²" -> value
                    "km²" -> value * 1_000_000.0
                    "mi²" -> value * 2_589_988.110336
                    "ac" -> value * 4046.8564224
                    "ha" -> value * 10_000.0
                    else -> value
                }
                // Convert from square meters to target unit
                when (toUnit) {
                    "m²" -> valueInSqM
                    "km²" -> valueInSqM / 1_000_000.0
                    "mi²" -> valueInSqM / 2_589_988.110336
                    "ac" -> valueInSqM / 4046.8564224
                    "ha" -> valueInSqM / 10_000.0
                    else -> valueInSqM
                }
            }
            Category.SPEED -> {
                // Convert to base unit: m/s
                val valueInMps = when (fromUnit) {
                    "m/s" -> value
                    "km/h" -> value / 3.6
                    "mph" -> value * 0.44704
                    "kt" -> value * 0.514444
                    else -> value
                }
                // Convert from m/s to target unit
                when (toUnit) {
                    "m/s" -> valueInMps
                    "km/h" -> valueInMps * 3.6
                    "mph" -> valueInMps / 0.44704
                    "kt" -> valueInMps / 0.514444
                    else -> valueInMps
                }
            }
            Category.CURRENCY -> {
                // Convert to base unit: USD
                val valueInUsd = when (fromUnit) {
                    "USD" -> value
                    "EUR" -> value / 0.92
                    "GBP" -> value / 0.78
                    "JPY" -> value / 155.0
                    "CAD" -> value / 1.36
                    "AUD" -> value / 1.50
                    "INR" -> value / 83.3
                    "CNY" -> value / 7.25
                    else -> value
                }
                // Convert from USD to target unit
                when (toUnit) {
                    "USD" -> valueInUsd
                    "EUR" -> valueInUsd * 0.92
                    "GBP" -> valueInUsd * 0.78
                    "JPY" -> valueInUsd * 155.0
                    "CAD" -> valueInUsd * 1.36
                    "AUD" -> valueInUsd * 1.50
                    "INR" -> valueInUsd * 83.3
                    "CNY" -> valueInUsd * 7.25
                    else -> valueInUsd
                }
            }
        }
    }
}
