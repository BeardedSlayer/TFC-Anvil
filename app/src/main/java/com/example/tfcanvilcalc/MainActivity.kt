package com.example.tfcanvilcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Folder
import androidx.compose.foundation.pager.HorizontalPager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Science
import com.example.tfcanvilcalc.ui.alloy.AlloyMixerScreen
import com.example.tfcanvilcalc.data.toCalcComponents
import com.example.tfcanvilcalc.ui.theme.AppTheme
import com.example.tfcanvilcalc.ui.theme.ThemeMode
import com.example.tfcanvilcalc.ui.theme.ThemeViewModel
import com.example.tfcanvilcalc.ui.theme.ThemeSettings
import com.example.tfcanvilcalc.ui.components.PrimaryButton
import com.example.tfcanvilcalc.ui.components.PrimaryButtonSlot
import com.example.tfcanvilcalc.data.ThemePreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import android.content.Context
import kotlin.math.abs
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi

enum class MainTab { HOME, SAVED, SEARCH, ALLOY, SETTINGS }

// Extension property for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme preferences
        val themePreferences = ThemePreferences(dataStore)
        
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeViewModel: ThemeViewModel = viewModel { ThemeViewModel(themePreferences) }
            
            val state by mainViewModel.state.collectAsState()
            val themeMode by themeViewModel.themeMode.collectAsState()

            AppTheme(themeMode = themeMode) {
                MainRootScreen(
                    state = state,
                    viewModel = mainViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}


@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val cornerRadius = 16.dp

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) surface.copy(alpha = 0.3f) else surface.copy(alpha = 0.1f),
            contentColor = if (enabled) primary else primary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if(enabled) 4.dp else 0.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


@Composable
fun BottomNavigationBar(
    selectedTab: MainTab,
    onHomeClick: () -> Unit,
    onSavedClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAlloyClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
            .navigationBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == MainTab.HOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onSavedClick) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = "Saved Results",
                        tint = if (selectedTab == MainTab.SAVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (selectedTab == MainTab.SEARCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onAlloyClick) {
                    Icon(
                        Icons.Filled.Science,
                        contentDescription = "Alloy Mixer",
                        tint = if (selectedTab == MainTab.ALLOY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (selectedTab == MainTab.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Индикатор страниц
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val isSelected = when (index) {
                        0 -> selectedTab == MainTab.HOME
                        1 -> selectedTab == MainTab.SAVED
                        2 -> selectedTab == MainTab.SEARCH
                        3 -> selectedTab == MainTab.ALLOY
                        4 -> selectedTab == MainTab.SETTINGS
                        else -> false
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (isSelected) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    state: MainState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween // делим на две зоны: контент и кнопки
                ) {
                    // --- Верхняя часть: ввод и результат ---
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Целевое число
                        OutlinedTextField(
                            value = state.targetNumber,
                            onValueChange = { viewModel.onTargetNumberChanged(it) },
                            label = { Text("Целевое число", color = MaterialTheme.colorScheme.onSurface) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            "Выберите последние действия:",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Кнопки "Ввод 1-3"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val actions = state.selectedActions
                            repeat(3) { index ->
                                var showDropdown by remember { mutableStateOf(false) }
                                val action = actions.getOrNull(index)

                                Box(modifier = Modifier.weight(1f)) {
                                    PrimaryButtonSlot(
                                        onClick = { showDropdown = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            if (action != null) "(${action.value})" else "${index + 1}",
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Visible,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showDropdown,
                                        onDismissRequest = { showDropdown = false }
                                    ) {
                                        Action.getAllActions().forEach { actionItem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        "${actionItem.title} (${actionItem.value})",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.replaceActionAtIndex(index, actionItem)
                                                    showDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Кнопка Найти решение
                        PrimaryButton(
                            onClick = { viewModel.findSolution() },
                            enabled = state.selectedActions.size == 3 && state.targetNumber.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            text = "Найти решение"
                        )

                        // Ошибка
                        if (state.errorMessage != null) {
                            Text(
                                state.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Результат
                        if (state.solution.isNotEmpty()) {
                            Text(
                                "Решение найдено:",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Последовательность действий
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Последовательность:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    state.solution.forEachIndexed { idx, act ->
                                        Text(
                                            "${idx + 1}. ${act.title} (${act.value})",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                // Количество каждого действия
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Количество действий:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val mainActions = state.solution.dropLast(3)
                                    val lastThreeActions = state.solution.takeLast(3)
                                    mainActions
                                        .groupBy { it.value }
                                        .entries
                                        .sortedByDescending { it.key }
                                        .forEach { (value, actionsGroup) ->
                                            if (actionsGroup.isNotEmpty()) {
                                                Text("${actionsGroup.size}-($value)",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    if (mainActions.isNotEmpty() && lastThreeActions.isNotEmpty()) {
                                        Text(
                                            "Последние действия:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    state.selectedActions.forEach { selectedAction ->
                                        val count = lastThreeActions.count { it.value == selectedAction.value }
                                        if (count > 0) {
                                            Text("$count-(${selectedAction.value})",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Text(
                                "Итоговая сумма: ${state.solution.sumOf { it.value }}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // --- Нижняя часть: кнопки ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            onClick = { viewModel.saveCurrentResult() },
                            enabled = state.solution.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            text = "Сохранить"
                        )
                        PrimaryButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.weight(1f),
                            text = "Сбросить"
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun SavedResultsMenu(
    results: List<SavedResult>,
    folders: List<Folder>,
    onDelete: (SavedResult) -> Unit,
    onMoveToFolder: (SavedResult, Int?) -> Unit,
    onEditName: (SavedResult, String) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    selectedResult: SavedResult?,
    onResultSelect: (SavedResult?) -> Unit,
    showCreateFolderDialog: Boolean,
    onShowCreateFolderDialogChange: (Boolean) -> Unit,
    newFolderName: String,
    onNewFolderNameChange: (String) -> Unit,
    selectedFolder: Folder?,
    onSelectedFolderChange: (Folder?) -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    var editingResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingName by remember { mutableStateOf("") }
    var resultToDelete by remember { mutableStateOf<SavedResult?>(null) }
    var moveTargetResult by remember { mutableStateOf<SavedResult?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    
    // Visibility-based state reset for keep-alive scenarios
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            // Close all local dialog/selection states when screen becomes invisible
            editingResult = null
            editingName = ""
            resultToDelete = null
            moveTargetResult = null
            folderToDelete = null
            // Note: External states are managed by parent component
        }
    }
    
    // Early return if not visible (for keep-alive scenarios)
    if (!isVisible) return
    
    // Guaranteed state reset on screen hide/change
    DisposableEffect(Unit) {
        onDispose {
            editingResult = null
            editingName = ""
            resultToDelete = null
            moveTargetResult = null
            folderToDelete = null
            onResultSelect(null)
            onShowCreateFolderDialogChange(false)
            onNewFolderNameChange("")
            onSelectedFolderChange(null)
        }
    }
    
    // Defer heavy UI until data stabilization
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield()
        ready = true
    }
    if (!ready) {
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Загрузка…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    // Авто-сброс "висящих" стейтов при изменении входных списков
    LaunchedEffect(results, folders) {
        // Enhanced reset for data changes (combines both soft reset and hanging state cleanup)
        editingResult = null
        editingName = ""
        resultToDelete = null
        moveTargetResult = null
        folderToDelete = null
        onResultSelect(null)
        onShowCreateFolderDialogChange(false)
        onNewFolderNameChange("")
        onSelectedFolderChange(null)
        
        // Also check for orphaned states
        if (selectedResult?.let { sel -> results.none { it.id == sel.id } } == true) {
            onResultSelect(null)
        }
    }
    
    // Делаем "безопасные" копии списков, чтобы избежать ConcurrentModification
    val safeResults = remember(results) { results.toList() }
    val safeFolders = remember(folders) { folders.toList() }

    // Диагностика дубликатов ID
    LaunchedEffect(safeResults) {
        val ids = mutableSetOf<Int>()
        safeResults.forEach {
            if (!ids.add(it.id)) {
                println("DUPLICATE RESULT ID: ${it.id}")
            }
        }
    }
    LaunchedEffect(safeFolders) {
        val ids = mutableSetOf<Int>()
        safeFolders.forEach {
            if (!ids.add(it.id)) {
                println("DUPLICATE FOLDER ID: ${it.id}")
            }
        }
    }

    // Безопасное вычисление resultsToShow без мутации исходных коллекций
    val resultsToShow = remember(safeResults, selectedFolder) {
        if (selectedFolder != null) {
            safeResults.filter { it.folderId == selectedFolder.id }
        } else {
            safeResults.filter { it.folderId == null }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Заголовок
        item(key = "sr_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (selectedFolder != null) "Папка: ${selectedFolder.name}" else "Сохранённые результаты",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (selectedFolder == null) {
                    NeonButton(
                        onClick = {
                            onShowCreateFolderDialogChange(true)
                            onNewFolderNameChange("")
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Создать папку", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    TextButton(onClick = { onSelectedFolderChange(null) }) {
                        Text("Назад", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item(key = "sr_header_spacer") {
            Spacer(Modifier.height(16.dp))
        }

        // Список папок
        if (selectedFolder == null && safeFolders.isNotEmpty()) {
            item(key = "sr_folders_title") {
                Text("Папки:", color = MaterialTheme.colorScheme.primary)
            }
            
            item(key = "sr_folders_title_spacer") {
                Spacer(Modifier.height(8.dp))
            }
            
            items(safeFolders, key = { folder -> "sr_folder_${folder.id}" }) { folder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        val current = safeFolders.firstOrNull { it.id == folder.id } ?: return@Card
                        onFolderClick(current) 
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(folder.name, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { 
                            val current = safeFolders.firstOrNull { it.id == folder.id } ?: return@IconButton
                            folderToDelete = current 
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            
            item(key = "sr_folders_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }

        // Результаты
        if (resultsToShow.isEmpty()) {
            item(key = "sr_no_results") {
                Text("Нет сохранённых результатов", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            items(resultsToShow, key = { res -> "sr_result_${res.id}" }) { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val current = safeResults.firstOrNull { it.id == result.id } ?: return@Card
                        onResultSelect(current)
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(result.name, color = MaterialTheme.colorScheme.primary)
                            Row {
                                IconButton(onClick = {
                                    val current = safeResults.firstOrNull { it.id == result.id } ?: return@IconButton
                                    editingResult = current
                                    editingName = current.name
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    val current = safeResults.firstOrNull { it.id == result.id } ?: return@IconButton
                                    moveTargetResult = current
                                }) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    val current = safeResults.firstOrNull { it.id == result.id } ?: return@IconButton
                                    resultToDelete = current
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Text("Целевое число: ${result.targetNumber}", color = MaterialTheme.colorScheme.onSurface)
                        
                        // Краткая сводка для калькулятора
                        if (result.calcTotalUnits != null) {
                            Text("Партия: ${result.calcTotalUnits} слитков", 
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Кнопка закрыть
        item(key = "sr_close_button") {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Закрыть", color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Диалог подтверждения удаления результата
    val resultToDeleteCurrent = remember(resultToDelete, safeResults) {
        safeResults.firstOrNull { it.id == resultToDelete?.id }
    }
    if (resultToDeleteCurrent != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Подтверждение удаления", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Удалить «${resultToDeleteCurrent.name}»?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(resultToDeleteCurrent)
                    resultToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    } else if (resultToDelete != null) {
        resultToDelete = null
    }

    // Диалог подтверждения удаления папки
    val folderToDeleteCurrent = remember(folderToDelete, safeFolders) {
        safeFolders.firstOrNull { it.id == folderToDelete?.id }
    }
    if (folderToDeleteCurrent != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Подтверждение удаления", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Удалить папку «${folderToDeleteCurrent.name}»?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteFolder(folderToDeleteCurrent)
                    folderToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    } else if (folderToDelete != null) {
        folderToDelete = null
    }

    // Диалог редактирования результата
    val editingCurrent = remember(editingResult, safeResults) {
        safeResults.firstOrNull { it.id == editingResult?.id }
    }
    if (editingCurrent != null) {
        AlertDialog(
            onDismissRequest = { editingResult = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Изменить название", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Название", color = MaterialTheme.colorScheme.onSurface) },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditName(editingCurrent, editingName)
                    editingResult = null
                }) { Text("Сохранить", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { editingResult = null }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    } else if (editingResult != null) {
        editingResult = null
    }

    // Диалог перемещения в папку с LazyColumn и ограничением высоты
    val moveTargetCurrent = remember(moveTargetResult, safeResults) {
        safeResults.firstOrNull { it.id == moveTargetResult?.id }
    }
    if (moveTargetCurrent != null) {
        AlertDialog(
            onDismissRequest = { moveTargetResult = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Переместить в папку", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    if (safeFolders.isEmpty()) {
                        Text("Нет доступных папок", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                        ) {
                            items(safeFolders, key = { folder -> "sr_dialog_folder_${folder.id}" }) { folder ->
                                TextButton(
                                    onClick = {
                                        onMoveToFolder(moveTargetCurrent, folder.id)
                                        moveTargetResult = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        folder.name, 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            onMoveToFolder(moveTargetCurrent, null)
                            moveTargetResult = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Без папки", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { moveTargetResult = null }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    } else if (moveTargetResult != null) {
        moveTargetResult = null
    }

    // Диалог создания папки
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                onShowCreateFolderDialogChange(false)
                onNewFolderNameChange("")
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Создать папку", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = onNewFolderNameChange,
                    label = { Text("Название папки", color = MaterialTheme.colorScheme.onSurface) },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank()) {
                        onCreateFolder(newFolderName)
                        onShowCreateFolderDialogChange(false)
                        onNewFolderNameChange("")
                    }
                }) { Text("Создать", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onShowCreateFolderDialogChange(false)
                    onNewFolderNameChange("")
                }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}







@Composable
fun SearchScreen(
    results: List<SavedResult>,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onEditName: (SavedResult, String) -> Unit,
    onDelete: (SavedResult) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingName by remember { mutableStateOf("") }
    var resultToDelete by remember { mutableStateOf<SavedResult?>(null) } // подтверждение удаления
    
    // Visibility-based state reset for keep-alive scenarios
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            // Close all local dialog/selection states when screen becomes invisible
            selectedResult = null
            editingResult = null
            editingName = ""
            resultToDelete = null
            // Optional: reset search query
            // searchQuery = ""
        }
    }
    
    // Early return if not visible (for keep-alive scenarios)
    if (!isVisible) return
    
    // Guaranteed state reset on screen hide/change
    DisposableEffect(Unit) {
        onDispose {
            // Close all local dialog/selection states
            selectedResult = null
            editingResult = null
            editingName = ""
            resultToDelete = null
            // Optional: reset search query (uncomment if needed)
            // searchQuery = ""
        }
    }
    
    // Defer heavy UI until data stabilization
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.yield()
        ready = true
    }
    if (!ready) {
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Загрузка…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    // Делаем "безопасные" копии списков
    val safeResults = remember(results) { results.toList() }
    val safeFolders = remember(folders) { folders.toList() }
    
    // Диагностика дубликатов ID
    LaunchedEffect(safeResults) {
        val ids = mutableSetOf<Int>()
        safeResults.forEach {
            if (!ids.add(it.id)) {
                println("DUPLICATE RESULT ID: ${it.id}")
            }
        }
    }
    LaunchedEffect(safeFolders) {
        val ids = mutableSetOf<Int>()
        safeFolders.forEach {
            if (!ids.add(it.id)) {
                println("DUPLICATE FOLDER ID: ${it.id}")
            }
        }
    }
    
    // Сброс "висящих" стейтов при изменении входов
    LaunchedEffect(safeResults, safeFolders) {
        if (selectedResult?.let { sel -> safeResults.none { it.id == sel.id } } == true) {
            selectedResult = null
        }
        if (editingResult?.let { er -> safeResults.none { it.id == er.id } } == true) {
            editingResult = null
        }
        if (resultToDelete?.let { dr -> safeResults.none { it.id == dr.id } } == true) {
            resultToDelete = null
        }
    }

    val filteredResults = if (searchQuery.isBlank()) emptyList()
    else safeResults.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Введите название результата", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                Text("Введите название для поиска", color = MaterialTheme.colorScheme.onSurface)
            } else if (filteredResults.isEmpty()) {
                Text("Результаты не найдены", color = MaterialTheme.colorScheme.onSurface)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredResults, key = { res -> "search_result_${res.id}" }) { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val current = safeResults.firstOrNull { it.id == result.id } ?: return@Card
                                selectedResult = current
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(result.name, color = MaterialTheme.colorScheme.primary)
                                    Row {
                                        IconButton(onClick = {
                                            val current = safeResults.firstOrNull { it.id == result.id } ?: return@IconButton
                                            editingResult = current
                                            editingName = current.name
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            val current = safeResults.firstOrNull { it.id == result.id } ?: return@IconButton
                                            resultToDelete = current
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                Text("Целевое число: ${result.targetNumber}", color = MaterialTheme.colorScheme.onSurface)
                                if (result.folderId != null) {
                                    val folderName = safeFolders.find { it.id == result.folderId }?.name ?: "Неизвестно"
                                    Text("В папке: $folderName", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Закрыть", color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Диалог удаления
    val toDelete = remember(resultToDelete, safeResults) {
        safeResults.firstOrNull { it.id == resultToDelete?.id }
    }
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Подтверждение удаления", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Вы действительно хотите удалить \"${toDelete.name}\"?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(toDelete)
                    resultToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    } else if (resultToDelete != null) {
        resultToDelete = null
    }

    // Диалог просмотра результата
    val selected = remember(selectedResult, safeResults) {
        safeResults.firstOrNull { it.id == selectedResult?.id }
    }
    if (selected != null) {
        AlertDialog(
            onDismissRequest = { selectedResult = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(selected.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Целевое число: ${selected.targetNumber}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    
                    // Если это результат калькулятора сплавов
                    if (selected.calcComponentsJson != null) {
                        Text("Состав партии (из сохранения):", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        selected.calcTotalUnits?.let { total ->
                            Text("Партия: $total слитков", color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        val calcComponents = selected.calcComponentsJson!!.toCalcComponents()
                        calcComponents.forEach { component ->
                            Text("${component.name}: ${component.count} ед. (${"%.1f".format(component.percent)}%)", color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        selected.calcMaxPerItem?.let { maxPer ->
                            Text("Макс. на партию: $maxPer", color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        selected.calcAutoPickEnabled?.let { autoPick ->
                            Text("Автоподбор: ${if (autoPick) "Да" else "Нет"}", color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        // Разделитель если есть также старые поля
                        if (selected.actions.isNotEmpty() || selected.solution.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Оригинальные поля (если есть)
                    if (selected.actions.isNotEmpty()) {
                        Text("Последние действия:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        selected.actions.forEach { Text("• ${it.title} (${it.value})", color = MaterialTheme.colorScheme.onSurface) }
                    }
                    if (selected.solution.isNotEmpty()) {
                        Text("Решение:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        selected.solution.forEachIndexed { i, a -> Text("${i + 1}. ${a.title} (${a.value})", color = MaterialTheme.colorScheme.onSurface) }
                        Text("Итоговая сумма: ${selected.solution.sumOf { it.value }}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedResult = null }) { Text("Закрыть", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    } else if (selectedResult != null) {
        selectedResult = null
    }

    // Диалог редактирования
    val editing = remember(editingResult, safeResults) {
        safeResults.firstOrNull { it.id == editingResult?.id }
    }
    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editingResult = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Изменить название", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Название", color = MaterialTheme.colorScheme.onSurface) },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditName(editing, editingName)
                    editingResult = null
                }) { Text("Сохранить", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { editingResult = null }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    } else if (editingResult != null) {
        editingResult = null
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainRootScreen(
    state: MainState,
    viewModel: MainViewModel,
    themeViewModel: ThemeViewModel
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedResult by remember { mutableStateOf<SavedResult?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    // Главная логика переключения — быстрый jump через среднее
    LaunchedEffect(currentTab) {
        val currentPage = pagerState.currentPage
        val targetPage = when (currentTab) {
            MainTab.HOME -> 0
            MainTab.SAVED -> 1
            MainTab.SEARCH -> 2
            MainTab.ALLOY -> 3
            MainTab.SETTINGS -> 4
        }
        if (currentPage == targetPage) return@LaunchedEffect

        val distance = abs(currentPage - targetPage)
        if (distance == 1) {
            // Соседний — плавная анимация
            pagerState.animateScrollToPage(targetPage, animationSpec = tween(300))
        } else {
            // Через одну ("jump" через центр) — моментальный переход
            pagerState.scrollToPage(targetPage)
        }
    }

    // Синхронизация тек. страницы и таба
    LaunchedEffect(pagerState.currentPage) {
        currentTab = when (pagerState.currentPage) {
            0 -> MainTab.HOME
            1 -> MainTab.SAVED
            2 -> MainTab.SEARCH
            3 -> MainTab.ALLOY
            4 -> MainTab.SETTINGS
            else -> MainTab.HOME
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentTab,
                onHomeClick = { 
                    // Reset external states before switching
                    selectedResult = null
                    showCreateFolderDialog = false
                    newFolderName = ""
                    selectedFolder = null
                    currentTab = MainTab.HOME 
                },
                onSavedClick = { 
                    // Reset external states before switching
                    selectedResult = null
                    showCreateFolderDialog = false
                    newFolderName = ""
                    selectedFolder = null
                    currentTab = MainTab.SAVED 
                },
                onSearchClick = { 
                    // Reset external states before switching
                    selectedResult = null
                    showCreateFolderDialog = false
                    newFolderName = ""
                    selectedFolder = null
                    currentTab = MainTab.SEARCH 
                },
                onAlloyClick = { 
                    // Reset external states before switching
                    selectedResult = null
                    showCreateFolderDialog = false
                    newFolderName = ""
                    selectedFolder = null
                    currentTab = MainTab.ALLOY 
                },
                onSettingsClick = { 
                    // Reset external states before switching
                    selectedResult = null
                    showCreateFolderDialog = false
                    newFolderName = ""
                    selectedFolder = null
                    currentTab = MainTab.SETTINGS 
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 16.dp
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                when (page) {

                    // --- Главное меню ---
                    0 -> MainScreen(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )

                    // --- Хранилище ---
                    1 -> SavedResultsMenu(
                        results = state.savedResults,
                        folders = state.folders,
                        onDelete = { viewModel.deleteResult(it) },
                        onMoveToFolder = { result, folderId ->
                            viewModel.moveResultToFolder(result, folderId)
                        },
                        onEditName = { result, newName ->
                            viewModel.updateResultName(result, newName)
                        },
                        onFolderClick = { folder -> selectedFolder = folder },
                        onCreateFolder = { folderName ->
                            viewModel.createFolder(folderName)
                        },
                        onDeleteFolder = { folder ->
                            viewModel.deleteFolder(folder)
                        },
                        selectedResult = selectedResult,
                        onResultSelect = { result -> selectedResult = result },
                        showCreateFolderDialog = showCreateFolderDialog,
                        onShowCreateFolderDialogChange = { showCreateFolderDialog = it },
                        newFolderName = newFolderName,
                        onNewFolderNameChange = { newFolderName = it },
                        selectedFolder = selectedFolder,
                        onSelectedFolderChange = { selectedFolder = it },
                        onDismiss = { currentTab = MainTab.HOME },
                        isVisible = currentTab == MainTab.SAVED,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )

                    // --- Поиск ---
                    2 -> SearchScreen(
                        results = state.savedResults,
                        folders = state.folders,
                        onDismiss = { currentTab = MainTab.HOME },
                        onEditName = { result, newName ->
                            viewModel.updateResultName(result, newName)
                        },
                        onDelete = { viewModel.deleteResult(it) },
                        isVisible = currentTab == MainTab.SEARCH,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )

                    // --- Конструктор сплавов ---
                    3 -> AlloyMixerScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                    
                    // --- Настройки ---
                    4 -> ThemeSettings(
                        themeViewModel = themeViewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}