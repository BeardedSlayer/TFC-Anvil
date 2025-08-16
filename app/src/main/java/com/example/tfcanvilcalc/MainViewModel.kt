package com.example.tfcanvilcalc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfcanvilcalc.data.AppDatabase
import com.example.tfcanvilcalc.data.SavedResultEntity
import com.example.tfcanvilcalc.data.FolderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainState(
    val targetNumber: String = "",
    val selectedActions: List<Action> = emptyList(),
    val solution: List<Action> = emptyList(),
    val errorMessage: String? = null,
    val savedResults: List<SavedResult> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isMenuVisible: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.savedResultDao()
    private val folderDao = database.folderDao()
    
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    init {
        viewModelScope.launch {
            dao.getAllResults().collect { results ->
                _state.update { it.copy(
                    savedResults = results.map { entity ->
                        SavedResult(
                            id = entity.id,
                            name = entity.name,
                            targetNumber = entity.targetNumber,
                            actions = entity.actions,
                            solution = entity.solution,
                            folderId = entity.folderId
                        )
                    }
                )}
            }
        }
        
        viewModelScope.launch {
            folderDao.getAllFolders().collect { folders ->
                _state.update { it.copy(
                    folders = folders.map { entity ->
                        Folder(
                            id = entity.id,
                            name = entity.name,
                            createdAt = entity.createdAt
                        )
                    }
                )}
            }
        }
    }

    fun onTargetNumberChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("\\d+"))) {
            _state.update { it.copy(targetNumber = value) }
        }
    }

    fun onActionSelected(action: Action) {
        _state.update { currentState ->
            if (currentState.selectedActions.size < 3) {
                currentState.copy(selectedActions = currentState.selectedActions + action)
            } else {
                currentState
            }
        }
    }

    fun replaceActionAtIndex(index: Int, newAction: Action) {
        _state.update { currentState ->
            val mutableList = currentState.selectedActions.toMutableList()
            if (index in mutableList.indices) {
                mutableList[index] = newAction
                currentState.copy(selectedActions = mutableList)
            } else if (index == mutableList.size && mutableList.size < 3) {
                currentState.copy(selectedActions = mutableList + newAction)
            } else {
                currentState
            }
        }
    }

    fun onActionRemoved(index: Int) {
        _state.update { currentState ->
            val mutableList = currentState.selectedActions.toMutableList()
            if (index in mutableList.indices) {
                mutableList.removeAt(index)
                currentState.copy(selectedActions = mutableList)
            } else {
                currentState
            }
        }
    }

    private fun abs(value: Int): Int = if (value < 0) -value else value

    fun findSolution() {
        val targetNumber = _state.value.targetNumber.toIntOrNull() ?: return
        val lastThreeActions = _state.value.selectedActions

        if (lastThreeActions.size != 3) {
            _state.update { it.copy(errorMessage = "Выберите три действия") }
            return
        }

        // Выводим отладочную информацию
        val debugInfo = StringBuilder()
        debugInfo.append("Целевое число: $targetNumber\n")
        debugInfo.append("Последние три действия: ${lastThreeActions.joinToString { "${it.title}(${it.value})" }}\n")

        // Получаем все положительные действия (ковка)
        val positiveActions = Action.getAllActions()
            .filter { it.value > 0 }
            .sortedByDescending { it.value }

        debugInfo.append("\nДоступные действия ковки: ${positiveActions.joinToString { "${it.title}(${it.value})" }}\n")

        // Вычисляем сумму последних трех действий
        val lastThreeSum = lastThreeActions.sumOf { it.value }
        debugInfo.append("Сумма последних трех действий: $lastThreeSum\n")

        // Нам нужно найти комбинацию положительных действий, которая даст (targetNumber - lastThreeSum)
        val neededSum = targetNumber - lastThreeSum
        debugInfo.append("Необходимая сумма от ковки: $neededSum\n")

        // Создаем карту для хранения путей к каждой сумме
        data class PathInfo(val actions: List<Action>)
        val paths = mutableMapOf<Int, PathInfo>()
        paths[0] = PathInfo(emptyList())

        // Ищем все возможные комбинации положительных действий
        var currentSums = setOf(0)
        var maxIterations = 10
        var iteration = 0

        while (iteration < maxIterations && !paths.containsKey(neededSum)) {
            iteration++
            val newSums = mutableSetOf<Int>()

            for (currentSum in currentSums) {
                for (action in positiveActions) {
                    val newSum = currentSum + action.value
                    if (newSum <= neededSum + 5) { // Даем небольшой запас
                        val currentPath = paths[currentSum]!!.actions
                        val newPath = currentPath + action
                        if (!paths.containsKey(newSum) || paths[newSum]!!.actions.size > newPath.size) {
                            paths[newSum] = PathInfo(newPath)
                            newSums.add(newSum)
                            
                            debugInfo.append("Найден путь к $newSum: ${newPath.joinToString { "${it.title}(${it.value})" }}\n")
                        }
                    }
                }
            }

            if (newSums.isEmpty()) break
            currentSums = newSums
        }

        // Проверяем, нашли ли мы нужную сумму
        if (paths.containsKey(neededSum)) {
            val forgingPath = paths[neededSum]!!.actions
            val fullPath = forgingPath + lastThreeActions
            
            debugInfo.append("\nНайдено решение!\n")
            debugInfo.append("Действия ковки: ${forgingPath.joinToString { "${it.title}(${it.value})" }}\n")
            debugInfo.append("Последние действия: ${lastThreeActions.joinToString { "${it.title}(${it.value})" }}\n")
            debugInfo.append("Полный путь: ${fullPath.joinToString { "${it.title}(${it.value})" }}\n")
            
            var sum = 0
            debugInfo.append("\nПроверка решения:\n")
            for (action in fullPath) {
                sum += action.value
                debugInfo.append("После ${action.title}(${action.value}): $sum\n")
            }
            
            _state.update { 
                it.copy(
                    solution = fullPath,
                    errorMessage = null
                )
            }
        } else {
            debugInfo.append("\nРешение не найдено\n")
            debugInfo.append("Проверенные суммы:\n")
            paths.entries
                .sortedBy { it.key }
                .forEach { (sum, pathInfo) ->
                    debugInfo.append("$sum: ${pathInfo.actions.joinToString { "${it.title}(${it.value})" }}\n")
                }
            
            _state.update { 
                it.copy(
                    solution = emptyList(),
                    errorMessage = "Решение не найдено\n\nОтладочная информация:\n$debugInfo"
                )
            }
        }
    }

    fun saveCurrentResult() {
        val currentState = _state.value
        if (currentState.solution.isNotEmpty()) {
            viewModelScope.launch {
                val result = SavedResultEntity(
                    name = "Результат ${currentState.savedResults.size + 1}",
                    targetNumber = currentState.targetNumber.toInt(),
                    actions = currentState.selectedActions,
                    solution = currentState.solution,
                    folderId = null
                )
                dao.insertResult(result)
            }
        }
    }

    fun deleteResult(result: SavedResult) {
        viewModelScope.launch {
            dao.deleteResult(SavedResultEntity(
                id = result.id,
                name = result.name,
                targetNumber = result.targetNumber,
                actions = result.actions,
                solution = result.solution,
                folderId = result.folderId
            ))
        }
    }

    fun updateResultName(result: SavedResult, newName: String) {
        viewModelScope.launch {
            dao.updateResultName(result.id, newName)
        }
    }
    
    fun createFolder(folderName: String) {
        viewModelScope.launch {
            val folder = FolderEntity(name = folderName)
            folderDao.insertFolder(folder)
        }
    }
    
    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            // Сначала перемещаем все результаты из папки в корень
            val resultsInFolder = _state.value.savedResults.filter { it.folderId == folder.id }
            resultsInFolder.forEach { result ->
                dao.moveResultToFolder(result.id, null)
            }
            // Затем удаляем папку
            folderDao.deleteFolder(FolderEntity(
                id = folder.id,
                name = folder.name,
                createdAt = folder.createdAt
            ))
        }
    }
    
    fun moveResultToFolder(result: SavedResult, folderId: Int?) {
        viewModelScope.launch {
            // Добавляем проверку, чтобы избежать ненужных обновлений
            if (result.folderId != folderId) {
                println("🔄 Moving result ${result.name} from folder ${result.folderId} to folder $folderId")
            dao.moveResultToFolder(result.id, folderId)
            } else {
                println("⏭️ Result ${result.name} is already in folder $folderId, skipping move")
            }
        }
    }

    fun reset() {
        _state.update {
            it.copy(
                targetNumber = "",
                selectedActions = emptyList(),
                solution = emptyList(),
                errorMessage = null
            )
        }
    }

    fun toggleMenu() {
        _state.update { it.copy(isMenuVisible = !it.isMenuVisible) }
    }
} 