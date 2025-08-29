package com.example.tfcanvilcalc.ui.alloy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfcanvilcalc.MainViewModel
import com.example.tfcanvilcalc.data.CalcComponent
import kotlinx.coroutines.launch
import kotlin.math.*

// Константы
private const val MB_PER_INGOT = 144
private const val CRUCIBLE_CAP_MB = 3000
private const val SAFE_MAX_INGOTS_PER_BATCH = CRUCIBLE_CAP_MB / 144 // = 20

// Модели данных
data class AlloyRange(
    val id: Long,
    val name: String,
    val minPercent: Double,
    val maxPercent: Double
)

data class BatchItem(
    val name: String,
    val ingots: Int
) {
    val mb: Int = ingots * MB_PER_INGOT
    fun getPercent(totalIngots: Int): Double = if (totalIngots > 0) 100.0 * ingots / totalIngots else 0.0
}

data class BatchPick(
    val P: Int,
    val items: List<BatchItem>
) {
    val totalMB: Int = P * MB_PER_INGOT
}

data class PlanResult(
    val baseBatch: BatchPick,
    val fullBatches: Int,
    val remainderBatch: BatchPick?,
    val suggestions: List<Int>
) {
    val totalIngots: Int = fullBatches * baseBatch.P + (remainderBatch?.P ?: 0)
    val totalMB: Int = totalIngots * MB_PER_INGOT
}

// Утилиты
private fun sanitizePercent(value: Double): Double = value.coerceIn(0.0, 100.0)

private fun fmt1(value: Double): String = "%.1f".format(value)

// Алгоритм подбора партии для заданного размера P
private fun pickBatchForP(ranges: List<AlloyRange>, P: Int): BatchPick? {
    if (ranges.isEmpty() || P <= 0) return null
    
    // Вычисляем границы для каждого компонента
    val bounds = ranges.map { range ->
        val minN = ceil(range.minPercent * P / 100.0).toInt()
        val maxN = floor(range.maxPercent * P / 100.0).toInt()
        Triple(range.name, minN, maxN)
    }
    
    // Проверяем возможность решения
    val sumMin = bounds.sumOf { it.second }
    val sumMax = bounds.sumOf { it.third }
    
    if (sumMin > P || sumMax < P) return null
    
    // Бэктрекинг для поиска решения
    val result = IntArray(ranges.size)
    
    fun backtrack(index: Int, remaining: Int): Boolean {
        if (index == ranges.size) {
            return remaining == 0
        }
        
        val (_, minN, maxN) = bounds[index]
        for (n in minN..min(maxN, remaining)) {
            result[index] = n
            if (backtrack(index + 1, remaining - n)) {
                return true
            }
        }
        return false
    }
    
    return if (backtrack(0, P)) {
        val items = ranges.mapIndexed { index, range ->
            BatchItem(range.name, result[index])
        }
        BatchPick(P, items)
    } else null
}

// Автоподбор оптимального размера партии
private fun planAlloyAuto(ranges: List<AlloyRange>, totalIngots: Int): PlanResult? {
    if (ranges.isEmpty() || totalIngots <= 0) return null
    
    // Ищем базовую партию от максимального размера к минимальному
    var baseBatch: BatchPick? = null
    for (P in SAFE_MAX_INGOTS_PER_BATCH downTo 1) {
        baseBatch = pickBatchForP(ranges, P)
        if (baseBatch != null) break
    }
    
    if (baseBatch == null) return null
    
    val K = totalIngots / baseBatch.P
    val R = totalIngots % baseBatch.P
    
    val remainderBatch = if (R > 0) pickBatchForP(ranges, R) else null
    val suggestions = if (R > 0 && remainderBatch == null) {
        suggestClosestTotals(ranges, totalIngots, baseBatch.P)
    } else emptyList()
    
    return PlanResult(baseBatch, K, remainderBatch, suggestions)
}

// Планирование с фиксированным размером партии
private fun planAlloyFixed(ranges: List<AlloyRange>, totalIngots: Int, fixedP: Int): PlanResult? {
    if (ranges.isEmpty() || totalIngots <= 0 || fixedP <= 0) return null
    
    val baseBatch = pickBatchForP(ranges, fixedP) ?: return null
    
    val K = totalIngots / baseBatch.P
    val R = totalIngots % baseBatch.P
    
    val remainderBatch = if (R > 0) pickBatchForP(ranges, R) else null
    val suggestions = if (R > 0 && remainderBatch == null) {
        suggestClosestTotals(ranges, totalIngots, baseBatch.P)
    } else emptyList()
    
    return PlanResult(baseBatch, K, remainderBatch, suggestions)
}

// Предложение ближайших количеств
private fun suggestClosestTotals(
    ranges: List<AlloyRange>,
    totalIngots: Int,
    baseP: Int,
    searchRadius: Int = 10
): List<Int> {
    val suggestions = mutableListOf<Pair<Int, Int>>() // (total, distance)
    
    for (d in 1..searchRadius) {
        // Проверяем totalIngots + d
        val higher = totalIngots + d
        val higherR = higher % baseP
        if (higherR == 0 || pickBatchForP(ranges, higherR) != null) {
            suggestions.add(higher to d)
        }
        
        // Проверяем totalIngots - d
        val lower = totalIngots - d
        if (lower > 0) {
            val lowerR = lower % baseP
            if (lowerR == 0 || pickBatchForP(ranges, lowerR) != null) {
                suggestions.add(lower to d)
            }
        }
    }
    
    return suggestions
        .sortedBy { it.second }
        .take(6)
        .map { it.first }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlloyMixerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        AlloyMixerContent(viewModel = viewModel, modifier = Modifier)
    }
}

@Composable
private fun AlloyMixerContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Initialization gate to prevent crashes during rapid transitions
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield()
        ready = true
    }
    if (!ready) return
    
    var components by remember { mutableStateOf<List<AlloyRange>>(emptyList()) }
    var nextId by remember { mutableStateOf(1L) }
    
    // Поля ввода
    var metalName by remember { mutableStateOf("") }
    var minPercent by remember { mutableStateOf("") }
    var maxPercent by remember { mutableStateOf("") }
    
    // Параметры партии
    var autoPickBatch by remember { mutableStateOf(true) }
    var fixedBatchSize by remember { mutableStateOf("20") }
    var totalIngots by remember { mutableStateOf("") }
    
    // Результаты
    var planResult by remember { mutableStateOf<PlanResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Редактирование
    var editingComponent by remember { mutableStateOf<AlloyRange?>(null) }
    
    // Сохранение
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    
    // Фокус
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    
    // Вычисления
    val sumMin = components.sumOf { it.minPercent }
    val sumMax = components.sumOf { it.maxPercent }
    val rangesValid = sumMin <= 100.0 && sumMax >= 100.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        item(key = "header") {
            Text(
                text = "Конструктор сплавов",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        
        // Поля ввода компонента
        item(key = "input-fields") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Добавить компонент",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = metalName,
                        onValueChange = { metalName = it },
                        label = { Text("Металл") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minPercent,
                            onValueChange = { minPercent = it },
                            label = { Text("Мин %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        OutlinedTextField(
                            value = maxPercent,
                            onValueChange = { maxPercent = it },
                            label = { Text("Макс %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    
                    // TODO: Можно заменить на NeonButton для консистентности стиля
                    Button(
                        onClick = {
                            val name = metalName.trim()
                            val min = minPercent.toDoubleOrNull()
                            val max = maxPercent.toDoubleOrNull()
                            
                            if (name.isBlank()) {
                                errorMessage = "Введите название металла"
                                return@Button
                            }
                            
                            if (min == null || max == null) {
                                errorMessage = "Введите корректные проценты"
                                return@Button
                            }
                            
                            val sanitizedMin = sanitizePercent(min)
                            val sanitizedMax = sanitizePercent(max)
                            
                            if (sanitizedMin > sanitizedMax) {
                                errorMessage = "Минимальный процент не может быть больше максимального"
                                return@Button
                            }
                            
                            if (editingComponent != null) {
                                components = components.map { comp ->
                                    if (comp.id == editingComponent!!.id) {
                                        comp.copy(name = name, minPercent = sanitizedMin, maxPercent = sanitizedMax)
                                    } else comp
                                }
                                editingComponent = null
                            } else {
                                components = components + AlloyRange(nextId++, name, sanitizedMin, sanitizedMax)
                            }
                            
                            // Очистка полей
                            metalName = ""
                            minPercent = ""
                            maxPercent = ""
                            errorMessage = null
                            planResult = null
                            
                            // Фокус на поле металла
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            if (editingComponent != null) "Сохранить изменения" else "Добавить компонент",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    if (editingComponent != null) {
                        // TODO: Можно заменить на NeonButton для консистентности стиля
                        Button(
                            onClick = {
                                editingComponent = null
                                metalName = ""
                                minPercent = ""
                                maxPercent = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Отменить редактирование", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
        
        // Подсказка по суммам
        if (components.isNotEmpty()) {
            item(key = "totals-hint") {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (rangesValid) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Σмин: ${fmt1(sumMin)}%, Σмакс: ${fmt1(sumMax)}%",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!rangesValid) {
                            Text(
                                text = "Требуется: Σмин ≤ 100% ≤ Σмакс",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        // Список компонентов
        if (components.isNotEmpty()) {
            items(components, key = { comp -> "alloy_component_${comp.id}" }) { component ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = component.name,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${fmt1(component.minPercent)}% - ${fmt1(component.maxPercent)}%",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Row {
                            IconButton(
                                onClick = {
                                    editingComponent = component
                                    metalName = component.name
                                    minPercent = component.minPercent.toString()
                                    maxPercent = component.maxPercent.toString()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Редактировать",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    components = components.filter { it.id != component.id }
                                    planResult = null
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Параметры партии
        if (components.isNotEmpty()) {
            item(key = "batch-params") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Параметры партии",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = autoPickBatch,
                                onCheckedChange = { autoPickBatch = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Автоподбор размера партии (до $SAFE_MAX_INGOTS_PER_BATCH слитков)",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (!autoPickBatch) {
                            OutlinedTextField(
                                value = fixedBatchSize,
                                onValueChange = { fixedBatchSize = it },
                                label = { Text("Размер партии (слитков)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        
                        OutlinedTextField(
                            value = totalIngots,
                            onValueChange = { totalIngots = it },
                            label = { Text("Нужно слитков сплава") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        // TODO: Можно заменить на NeonButton для консистентности стиля
                        Button(
                            onClick = {
                                val total = totalIngots.toIntOrNull()
                                
                                if (total == null || total < 1) {
                                    errorMessage = "Введите корректное количество слитков (≥ 1)"
                                    return@Button
                                }
                                
                                if (!rangesValid) {
                                    errorMessage = "Исправьте диапазоны компонентов"
                                    return@Button
                                }
                                
                                val result = if (autoPickBatch) {
                                    planAlloyAuto(components, total)
                                } else {
                                    val fixedP = fixedBatchSize.toIntOrNull()
                                    if (fixedP == null || fixedP < 1 || fixedP > SAFE_MAX_INGOTS_PER_BATCH) {
                                        errorMessage = "Размер партии должен быть от 1 до $SAFE_MAX_INGOTS_PER_BATCH"
                                        return@Button
                                    }
                                    planAlloyFixed(components, total, fixedP)
                                }
                                
                                if (result == null) {
                                    errorMessage = "Не удалось найти решение для данных диапазонов"
                                    planResult = null
                                } else {
                                    planResult = result
                                    errorMessage = null
                                }
                            },
                            enabled = rangesValid,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Рассчитать", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
        
        // Ошибки
        errorMessage?.let { message ->
            item(key = "error") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Сообщение о сохранении
        saveMessage?.let { message ->
            item(key = "save-message") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Результаты
        planResult?.let { result ->
            // Состав одной партии
            item(key = "batch-composition") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Состав одной партии: ${result.baseBatch.P} слитков (~${result.baseBatch.totalMB} mB)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        result.baseBatch.items.forEach { item ->
                            val percent = item.getPercent(result.baseBatch.P)
                            Text(
                                text = "${item.name}: ${item.ingots} слитков (${fmt1(percent)}%, ${item.mb} mB)",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // План на заказ
            item(key = "order-plan") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "План на заказ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (result.fullBatches > 0) {
                            Text(
                                text = "Полных партий: ${result.fullBatches} × ${result.baseBatch.P} = ${result.fullBatches * result.baseBatch.P} слитков",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        result.remainderBatch?.let { remainder ->
                            Text(
                                text = "Мини-партия: ${remainder.P} слитков (~${remainder.totalMB} mB)",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            remainder.items.forEach { item ->
                                val percent = item.getPercent(remainder.P)
                                Text(
                                    text = "  ${item.name}: ${item.ingots} слитков (${fmt1(percent)}%, ${item.mb} mB)",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        Text(
                            text = "Итого: ${result.totalIngots} слитков, ${result.totalMB} mB",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Кнопка сохранения
            item(key = "save-button") {
                // TODO: Можно заменить на NeonButton для консистентности стиля
                Button(
                    onClick = {
                        saveName = "Сплав "
                        showSaveDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Сохранить",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить результат", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
            
            // Предложения ближайших количеств
            if (result.suggestions.isNotEmpty()) {
                item(key = "suggestions") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Подойдут также количества",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = result.suggestions.joinToString(", ") { "$it слитков" },
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        
        // Отступ снизу
        item(key = "bottom-spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Автофокус при открытии - безопасный вызов после инициализации модификатора
    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield() // Гарантирует, что модификатор focusRequester уже применён
        try {
            focusRequester.requestFocus()
        } catch (e: IllegalStateException) {
            // FocusRequester not initialized - ignore safely
        }
    }
    
    // Диалог сохранения
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Сохранить результат", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (saveName.isNotBlank() && planResult != null) {
                            coroutineScope.launch {
                                try {
                                    val calcComponents = components.mapIndexed { index, range ->
                                        val batchItem = planResult!!.baseBatch.items.find { it.name == range.name }
                                        CalcComponent(
                                            name = range.name,
                                            minPercent = range.minPercent,
                                            maxPercent = range.maxPercent,
                                            count = batchItem?.ingots ?: 0,
                                            percent = batchItem?.getPercent(planResult!!.baseBatch.P) ?: 0.0
                                        )
                                    }
                                    
                                    viewModel.saveCalculatorResult(
                                        name = saveName,
                                        totalUnits = totalIngots.toIntOrNull() ?: 0,
                                        maxPerItem = if (autoPickBatch) SAFE_MAX_INGOTS_PER_BATCH else fixedBatchSize.toIntOrNull() ?: 20,
                                        autoPickEnabled = autoPickBatch,
                                        components = calcComponents
                                    )
                                    
                                    saveMessage = "Результат успешно сохранён!"
                                    showSaveDialog = false
                                } catch (e: Exception) {
                                    saveMessage = "Ошибка сохранения: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = saveName.isNotBlank()
                ) {
                    Text("Сохранить", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
    
    // Сообщение о сохранении
    saveMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            saveMessage = null
        }
    }
}

/*
TODO: Тестовые сценарии для проверки:

1. Пример из задачи:
   - 4 металла с диапазонами: 10–15, 10–15, 50–55, 20–25
   - Автоподбор партии и расчёт на 16 слитков
   - Проверить точные проценты и целые слитки

2. Большое количество компонентов:
   - Создать 8–10 компонентов с небольшими диапазонами
   - Проверить что Σмин ≤ 100 ≤ Σмакс
   - Убедиться что скролл работает корректно

3. Проверить что объёмы всегда кратны 144 mB
*/