package com.example.tfcanvilcalc.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.tfcanvilcalc.Action
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "saved_results")
data class SavedResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val targetNumber: Int,
    val actions: List<Action>,
    val solution: List<Action>,
    val folderId: Int? = null
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