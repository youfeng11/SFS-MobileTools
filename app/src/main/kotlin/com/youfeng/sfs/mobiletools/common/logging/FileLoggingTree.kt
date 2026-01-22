package com.youfeng.sfs.mobiletools.common.logging

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path
import okio.buffer
import okio.sink
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLoggingTree @Inject constructor(
    @ApplicationContext context: Context
) : Timber.DebugTree() {

    private val fs = FileSystem.SYSTEM

    private val logFile = File(context.getExternalFilesDir(null), "logs").apply {
            mkdirs()
        }.toOkioPath() / "app_log_${System.currentTimeMillis()}.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val maxLogAgeMs = 7 * 24 * 60 * 60 * 1000L  // 保留 7 天

    init {
        cleanOldLogs()
    }

    @SuppressLint("LogNotTimber")
    private fun cleanOldLogs() = CoroutineScope(Dispatchers.IO).launch {
        val now = System.currentTimeMillis()
        val logDir = logFile.parent ?: return@launch
        fs.list(logDir).forEach { path ->
            val metadata = fs.metadata(path)
            if (metadata.isRegularFile && now - (metadata.lastModifiedAtMillis ?: now) > maxLogAgeMs) {
                val deleted = fs.delete(path)
                Log.i(
                    "FileLoggingTree",
                    "自动清理旧日志: ${path.name}"
                )
            }
        }
    }

    private fun createTag(): String {
        val stackTrace = Throwable().stackTrace

        // 跳过 Timber 内部类 & 当前 Tree 类
        for (element in stackTrace) {
            val className = element.className

            if (!className.startsWith("timber.log.")
                && className != this::class.java.name
            ) {
                val simpleName = className.substringAfterLast('.')
                return "$simpleName:${element.lineNumber}"
            }
        }
        return "Unknown"
    }


    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val realTag = tag ?: createTag()
            val logTime = dateFormat.format(Date())
            val priorityStr = when (priority) {
                Log.VERBOSE -> "V"; Log.DEBUG -> "D"; Log.INFO -> "I"
                Log.WARN -> "W"; Log.ERROR -> "E"; Log.ASSERT -> "A"
                else -> priority.toString()
            }
            val logMessage = "$logTime $priorityStr/$realTag: $message"
            fs.appendingSink(logFile).buffer().use { sink ->
                sink.writeUtf8("$logMessage\n")
            }
        } catch (e: Exception) {
            Log.e("FileLoggingTree", "写日志失败", e)
        }
    }

    fun getLatestLogPath(): Path = logFile
}