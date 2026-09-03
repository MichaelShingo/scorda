package com.example.scorda.data.database.entities

import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes

enum class BrushFamilyType(
    val uiLabel: String,
    val defaultName: String
) {
    PRESSURE_PEN("Pen", "Pressure Pen"),
    MARKER("Marker", "Marker"),
    HIGHLIGHTER("Highlighter", "Highlighter"),
    DASHED_LINE("Dashed", "Dashed Line");

    fun toInkBrushFamily(): BrushFamily {
        return when (this) {
            PRESSURE_PEN -> StockBrushes.pressurePen()
            MARKER -> StockBrushes.marker()
            HIGHLIGHTER -> StockBrushes.highlighter()
            DASHED_LINE -> StockBrushes.dashedLine()
        }
    }

    companion object {
        fun fromString(value: String?): BrushFamilyType {
            if (value == null) return PRESSURE_PEN
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.name.replace("_", "").equals(value.replace("_", ""), ignoreCase = true)
            } ?: PRESSURE_PEN
        }
    }
}
