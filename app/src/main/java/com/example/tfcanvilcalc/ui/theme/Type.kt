package com.example.tfcanvilcalc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.content.Context
import java.io.File
import com.example.tfcanvilcalc.SavedResult
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Инициализация необходимых директорий для хранения данных.
 * Вызывать при старте приложения.
 */
fun initAppDirectories(context: Context) {
    File(context.filesDir, "logs").mkdirs()
    File(context.filesDir, "user_results").mkdirs()
}

/**
 * Очищает все файлы в указанной директории.
 * Не удаляет саму директорию.
 */
fun clearDirectory(directory: File) {
    if (directory.exists() && directory.isDirectory) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }
}

/**
 * Очищает кэш приложения (context.cacheDir).
 */
fun clearAppCache(context: Context) {
    clearDirectory(context.cacheDir)
}

/**
 * Очищает все логи (директория logs).
 */
fun clearLogs(context: Context) {
    val logDir = File(context.filesDir, "logs")
    clearDirectory(logDir)
}

/**
 * Удаляет только старые логи (старше 7 дней) из директории logs.
 */
fun clearOldLogs(context: Context) {
    val logDir = File(context.filesDir, "logs")
    val now = System.currentTimeMillis()
    logDir.listFiles()?.forEach { file ->
        // Удалять логи старше 7 дней
        if (file.isFile && now - file.lastModified() > 7 * 24 * 60 * 60 * 1000) {
            file.delete()
        }
    }
}

/**
 * Сохраняет пользовательский результат в защищённую директорию.
 * @param fileName Имя файла (без путей)
 * @param data Данные для сохранения
 */
fun saveUserResult(context: Context, fileName: String, data: ByteArray) {
    val userResultsDir = File(context.filesDir, "user_results")
    if (!userResultsDir.exists()) userResultsDir.mkdirs()
    val file = File(userResultsDir, fileName)
    file.writeBytes(data)
}

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)