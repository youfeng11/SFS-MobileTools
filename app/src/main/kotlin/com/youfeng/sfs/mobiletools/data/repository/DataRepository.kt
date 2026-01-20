package com.youfeng.sfs.mobiletools.data.repository

import android.content.Context
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.data.SfsFileConfig
import javax.inject.Inject
import javax.inject.Singleton
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path

interface DataRepository {
    fun getCustomTranslationsList(): List<AssetInfo>
}

@Singleton
class DataRepositoryImpl @Inject constructor() : DataRepository {
    val fileSystem = FileSystem.SYSTEM

    override fun getCustomTranslationsList(): List<AssetInfo> {
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
            .map { path ->
                AssetInfo(
                    name = path.name.removeSuffix(".txt"),
                    type = AssetType.CustomTranslation
                )
            }
            .toList()
    }
}