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
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.foundation.pager.HorizontalPager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import kotlin.math.abs
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi

enum class MainTab { HOME, SAVED, SEARCH }

@Composable
fun DraggableResultCard(
    result: SavedResult,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSelect: () -> Unit,
    onMoveToFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2D3A)
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Цель: ${result.targetNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onMoveToFolder) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = "Переместить в папку",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Удалить",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            var isDarkTheme by remember { mutableStateOf(true) }

            MainRootScreen(
                state = state,
                viewModel = viewModel,
                isDarkTheme = isDarkTheme
            )
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF181A20))
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
                        tint = if (selectedTab == MainTab.HOME) Color(0xFFB388FF) else Color.Gray
                    )
                }
                IconButton(onClick = onSavedClick) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = "Saved Results",
                        tint = if (selectedTab == MainTab.SAVED) Color(0xFFB388FF) else Color.Gray
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (selectedTab == MainTab.SEARCH) Color(0xFFB388FF) else Color.Gray
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
                repeat(3) { index ->
                    val isSelected = when (index) {
                        0 -> selectedTab == MainTab.HOME
                        1 -> selectedTab == MainTab.SAVED
                        2 -> selectedTab == MainTab.SEARCH
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
                                color = if (isSelected) Color(0xFFB388FF) else Color.Gray.copy(alpha = 0.5f),
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
    isDarkTheme: Boolean,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
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
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(0.dp)
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
                            label = { Text("Целевое число", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
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
                                    NeonButton(
                                        onClick = { showDropdown = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(min = 80.dp)
                                            .height(56.dp)
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            if (action != null) "(${action.value})" else "${index + 1}",
                                            fontSize = 15.sp,
                                            color = Color.White,
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
                        NeonButton(
                            onClick = { viewModel.findSolution() },
                            enabled = state.selectedActions.size == 3 && state.targetNumber.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Найти решение", color = Color.White)
                        }

                        // Ошибка
                        if (state.errorMessage != null) {
                            Text(
                                state.errorMessage ?: "",
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
                                            color = Color.White,
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
                                                    color = Color.White,
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
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Text(
                                "Итоговая сумма: ${state.solution.sumOf { it.value }}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
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
                        NeonButton(
                            onClick = { viewModel.saveCurrentResult() },
                            enabled = state.solution.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Сохранить", color = Color.White, textAlign = TextAlign.Center)
                        }
                        NeonButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Сбросить", color = Color.White, textAlign = TextAlign.Center)
                        }
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
    modifier: Modifier = Modifier
) {
    var editingResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingName by remember { mutableStateOf("") }
    var resultToDelete by remember { mutableStateOf<SavedResult?>(null) }
    var moveTargetResult by remember { mutableStateOf<SavedResult?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) } // для подтверждения удаления папки

    Column(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Заголовок ---
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
                    Icon(Icons.Default.Add, contentDescription = "Создать папку", tint = Color.White)
                }
            } else {
                TextButton(onClick = { onSelectedFolderChange(null) }) {
                    Text("Назад", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Список папок
        if (selectedFolder == null && folders.isNotEmpty()) {
            Text("Папки:", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            folders.forEach { folder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onFolderClick(folder) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D3A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(folder.name, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { folderToDelete = folder }) { // теперь с подтверждением
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Результаты
        val resultsToShow = if (selectedFolder != null) {
            results.filter { it.folderId == selectedFolder.id }
        } else {
            results.filter { it.folderId == null }
        }

        if (resultsToShow.isEmpty()) {
            Text("Нет сохранённых результатов", color = Color.White)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(resultsToShow) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onResultSelect(result) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D3A))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(result.name, color = MaterialTheme.colorScheme.primary)
                                Row {
                                    IconButton(onClick = {
                                        editingResult = result
                                        editingName = result.name
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { moveTargetResult = result }) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { resultToDelete = result }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Text("Целевое число: ${result.targetNumber}", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Закрыть", color = MaterialTheme.colorScheme.primary)
        }
    }

    // --- Диалог подтверждения удаления результата ---
    if (resultToDelete != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            containerColor = Color.Black,
            title = { Text("Подтверждение удаления", color = Color.White) },
            text = { Text("Удалить «${resultToDelete?.name}»?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    resultToDelete?.let { onDelete(it) }
                    resultToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Отмена", color = Color.White) }
            }
        )
    }

    // --- Диалог подтверждения удаления папки ---
    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            containerColor = Color.Black,
            title = { Text("Подтверждение удаления", color = Color.White) },
            text = { Text("Удалить папку «${folderToDelete?.name}»?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    folderToDelete?.let { onDeleteFolder(it) }
                    folderToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }

    // --- Диалог редактирования результата ---
    if (editingResult != null) {
        AlertDialog(
            onDismissRequest = { editingResult = null },
            containerColor = Color.Black,
            title = { Text("Изменить название", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Название", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingResult?.let { onEditName(it, editingName) }
                    editingResult = null
                }) { Text("Сохранить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { editingResult = null }) { Text("Отмена", color = Color.White) }
            }
        )
    }

    // --- Диалог перемещения в папку ---
    if (moveTargetResult != null) {
        AlertDialog(
            onDismissRequest = { moveTargetResult = null },
            containerColor = Color.Black,
            title = { Text("Переместить в папку", color = Color.White) },
            text = {
                Column {
                    if (folders.isEmpty()) {
                        Text("Нет доступных папок", color = Color.Gray)
                    } else {
                        folders.forEach { folder ->
                            TextButton(onClick = {
                                moveTargetResult?.let {
                                    onMoveToFolder(it, folder.id)
                                }
                                moveTargetResult = null
                            }) {
                                Text(folder.name, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        moveTargetResult?.let {
                            onMoveToFolder(it, null)
                        }
                        moveTargetResult = null
                    }) {
                        Text("Без папки", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { moveTargetResult = null }) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }

    // --- Диалог создания папки ---
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                onShowCreateFolderDialogChange(false)
                onNewFolderNameChange("")
            },
            containerColor = Color.Black,
            title = { Text("Создать папку", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = onNewFolderNameChange,
                    label = { Text("Название папки", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
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
                }) { Text("Отмена", color = Color.White) }
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingResult by remember { mutableStateOf<SavedResult?>(null) }
    var editingName by remember { mutableStateOf("") }
    var resultToDelete by remember { mutableStateOf<SavedResult?>(null) } // подтверждение удаления

    val filteredResults = if (searchQuery.isBlank()) emptyList()
    else results.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Введите название результата", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                Text("Введите название для поиска", color = Color.White)
            } else if (filteredResults.isEmpty()) {
                Text("Результаты не найдены", color = Color.White)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredResults) { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedResult = result },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D3A))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(result.name, color = MaterialTheme.colorScheme.primary)
                                    Row {
                                        IconButton(onClick = {
                                            editingResult = result
                                            editingName = result.name
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { resultToDelete = result }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                Text("Целевое число: ${result.targetNumber}", color = Color.White)
                                if (result.folderId != null) {
                                    val folderName = folders.find { it.id == result.folderId }?.name ?: "Неизвестно"
                                    Text("В папке: $folderName", color = Color.White, style = MaterialTheme.typography.bodySmall)
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
    if (resultToDelete != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            containerColor = Color.Black,
            title = { Text("Подтверждение удаления", color = Color.White) },
            text = { Text("Вы действительно хотите удалить \"${resultToDelete?.name}\"?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    resultToDelete?.let { onDelete(it) }
                    resultToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Отмена", color = Color.White) }
            }
        )
    }

    // Диалог просмотра результата
    if (selectedResult != null) {
        AlertDialog(
            onDismissRequest = { selectedResult = null },
            containerColor = Color.Black,
            title = { Text(selectedResult!!.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Целевое число: ${selectedResult!!.targetNumber}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    Text("Последние действия:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    selectedResult!!.actions.forEach { Text("• ${it.title} (${it.value})", color = Color.White) }
                    Text("Решение:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    selectedResult!!.solution.forEachIndexed { i, a -> Text("${i + 1}. ${a.title} (${a.value})", color = Color.White) }
                    Text("Итоговая сумма: ${selectedResult!!.solution.sumOf { it.value }}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedResult = null }) { Text("Закрыть", color = Color.White) }
            }
        )
    }

    // Диалог редактирования
    if (editingResult != null) {
        AlertDialog(
            onDismissRequest = { editingResult = null },
            containerColor = Color.Black,
            title = { Text("Изменить название", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Название", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingResult?.let { onEditName(it, editingName) }
                    editingResult = null
                }) { Text("Сохранить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { editingResult = null }) { Text("Отмена", color = Color.White) }
            }
        )
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainRootScreen(
    state: MainState,
    viewModel: MainViewModel,
    isDarkTheme: Boolean
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedResult by remember { mutableStateOf<SavedResult?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    // Главная логика переключения — быстрый jump через среднее
    LaunchedEffect(currentTab) {
        val currentPage = pagerState.currentPage
        val targetPage = when (currentTab) {
            MainTab.HOME -> 0
            MainTab.SAVED -> 1
            MainTab.SEARCH -> 2
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
            else -> MainTab.HOME
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentTab,
                onHomeClick = { currentTab = MainTab.HOME },
                onSavedClick = { currentTab = MainTab.SAVED },
                onSearchClick = { currentTab = MainTab.SEARCH }
            )
        },
        containerColor = Color.Black
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
                        isDarkTheme = isDarkTheme,
                        onHomeClick = { currentTab = MainTab.HOME },
                        onSearchClick = { currentTab = MainTab.SEARCH },
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
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }
        }
    }
}