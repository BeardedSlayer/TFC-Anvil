package com.example.tfcanvilcalc.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.tfcanvilcalc.Action
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Internal data class for calculator components
data class CalcComponent(
    val name: String,
    val minPercent: Double,
    val maxPercent: Double,
    val count: Int,
    val percent: Double
)

// Extension functions for JSON conversion
fun List<CalcComponent>.toJson(gson: Gson = Gson()): String = gson.toJson(this)

fun String.toCalcComponents(gson: Gson = Gson()): List<CalcComponent> {
    return try {
        val type = object : TypeToken<List<CalcComponent>>() {}.type
        gson.fromJson(this, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

@Entity(tableName = "saved_results")
data class SavedResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val targetNumber: Int,
    val actions: List<Action>,
    val solution: List<Action>,
    val folderId: Int? = null,
    // Calculator fields - nullable for backward compatibility
    val calcTotalUnits: Int? = null,
    val calcMaxPerItem: Int? = null,
    val calcAutoPickEnabled: Boolean? = null,
    val calcComponentsJson: String? = null
)

class ActionListConverter {
    @TypeConverter
    fun fromJson(json: String): List<Action> {
        val type = object : TypeToken<List<Action>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toJson(actions: List<Action>): String {
        return Gson().toJson(actions)
    }
} 