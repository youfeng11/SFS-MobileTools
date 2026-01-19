package com.youfeng.sfs.mobiletools.data.repository

import android.content.Context
import com.youfeng.sfs.mobiletools.data.SfsFileConfig
import javax.inject.Inject
import javax.inject.Singleton
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path

interface DataRepository {
    fun getCustomTranslationsList(): List<String>
}

@Singleton
class DataRepositoryImpl @Inject constructor() : DataRepository {
    val fileSystem = FileSystem.SYSTEM

    override fun getCustomTranslationsList(): List<String> {
        val exampleFile = "Example.txt"
        val directory = SfsFileConfig.assetsDirectoryPath / SfsFileConfig.CUSTOM_TRANSLATIONS
        val filesSequence = fileSystem.list(directory)
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                !metadata.isDirectory &&
                path.name.endsWith(".txt") &&
                path.name != exampleFile
            }
            .map { path -> path.name.removeSuffix(".txt") }
            .toList()
    }
}