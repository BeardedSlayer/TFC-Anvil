package com.example.tfcanvilcalc

enum class Action(val title: String, val value: Int) {
    SKIP("Пропуск", 0),
    LIGHT_HIT("Слабо ударить", -3),
    HIT("Ударить", -6),
    HEAVY_HIT("Сильно ударить", -9),
    DRAW("Протянуть", -15),
    PUNCH("Штамповать", 2),
    BEND("Изогнуть", 7),
    SHRINK("Обжать", 13),
    UPSET("Усадить", 16);

    companion object {
        fun getAllActions() = values().toList()
    }
} 